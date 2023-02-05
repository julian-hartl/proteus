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

internal class Compilation private constructor(
    private val entryPointModule: lang.proteus.binding.Module,
    private val isInterpreting: Boolean,
) {

    private var _globalScope: BoundGlobalScope? = null

    companion object {

        fun compile(
            entryModule: lang.proteus.binding.Module,

            ): Compilation {
            return Compilation(entryModule, isInterpreting = false)
        }

        fun interpret(
            entryPointTree: lang.proteus.binding.Module,
        ): Compilation {
            return Compilation(entryPointTree, isInterpreting = true)
        }
    }


    val globalScope: BoundGlobalScope
        get() {
            if (_globalScope == null) {
                synchronized(this) {
                    if (_globalScope == null) {
                        _globalScope = Binder.bindGlobalScope(null, entryPointModule)
//                        _globalScope!!.diagnostics.concat(entryPointModule.)
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
    ): Diagnostics {
        val diagnostics = globalScope.diagnostics
        if (diagnostics.hasErrors()) {
            return diagnostics
        }
        val program = getProgram()
        if(program.diagnostics.hasErrors()) {
            return program.diagnostics
        }
        val generatedCode =
            CodeGenerator.generate(
                program
            )
        CodeGenerator.emitGeneratedCode(generatedCode)
        JVMEmitter.emit(program, outputPath)
        return program.diagnostics
    }

    private fun getProgram(): BoundProgram {
        val program = Binder.bindProgram(globalScope, module = entryPointModule, optimize = !isInterpreting)
        _globalScope = program.globalScope
        return program
    }


}