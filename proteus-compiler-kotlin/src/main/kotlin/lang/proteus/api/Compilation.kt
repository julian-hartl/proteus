package lang.proteus.api

import lang.proteus.api.performance.ComputationTimeStopper
import lang.proteus.binding.Binder
import lang.proteus.binding.BoundGlobalScope
import lang.proteus.binding.BoundProgram
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.emit.JVMEmitter
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.evaluator.Evaluator
import lang.proteus.generation.CodeGenerator
import lang.proteus.syntax.parser.SyntaxTree

internal class Compilation private constructor(
    private val entryPointTree: SyntaxTree,
) {

    private var _globalScope: BoundGlobalScope? = null

    companion object {

        fun compile(
            entryPointTree: SyntaxTree,

            ): Compilation {
            return Compilation(entryPointTree)
        }

        fun interpret(
            entryPointTree: SyntaxTree,
        ): Compilation {
            return Compilation(entryPointTree)
        }
    }


    val globalScope: BoundGlobalScope
        get() {
            if (_globalScope == null) {
                synchronized(this) {
                    if (_globalScope == null) {
                        _globalScope = Binder.bindGlobalScope(null, entryPointTree.root)
                        _globalScope!!.diagnostics.concat(entryPointTree.diagnostics)
                    }
                }
            }
            return _globalScope!!
        }


    fun evaluate(
        variables: MutableMap<String, Any>,
        generateCode: Boolean = false,
        onWarning: (Diagnostics) -> Unit,
    ): EvaluationResult<*> {
        val computationTimeStopper = ComputationTimeStopper()
        computationTimeStopper.start()
        val diagnostics = globalScope.diagnostics
        val parseTime = computationTimeStopper.stop()
        if (diagnostics.hasErrors()) {
            return EvaluationResult(diagnostics, null, parseTime)
        }
        computationTimeStopper.start()
        val program = getProgram()

        val functionBodies = program.functionBodies
        val variableDeclarations = program.variableInitializers
        if (generateCode) {
            val generatedCode =
                CodeGenerator.generate(
                    program
                )
            CodeGenerator.emitGeneratedCode(generatedCode)
        }
        if (program.diagnostics.hasErrors()) {
            return EvaluationResult(program.diagnostics, null, parseTime)
        }
        val codeGenerationTime = computationTimeStopper.stop()
        onWarning(program.diagnostics)
        diagnostics.concat(program.diagnostics)

        computationTimeStopper.start()
        val evaluator = Evaluator(functionBodies, mainFunction = program.mainFunction!!, variableDeclarations)
        val value = evaluator.evaluate()
        val evaluationTime = computationTimeStopper.stop()
        return EvaluationResult(diagnostics, value, parseTime, evaluationTime, codeGenerationTime)
    }

    fun emit(
        outputPath: String,
    ) {
        val program = getProgram()
        val generatedCode =
            CodeGenerator.generate(
                program
            )
        CodeGenerator.emitGeneratedCode(generatedCode)
        JVMEmitter.emit(program, outputPath)
    }

    private fun getProgram(): BoundProgram {
        val program = Binder.bindProgram(globalScope, mainTree = entryPointTree)
        _globalScope = program.globalScope
        return program
    }


}