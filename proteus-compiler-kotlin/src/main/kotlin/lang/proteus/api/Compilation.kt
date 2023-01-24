package lang.proteus.api

import lang.proteus.api.performance.ComputationTimeStopper
import lang.proteus.binding.Binder
import lang.proteus.binding.BoundBlockStatement
import lang.proteus.binding.BoundGlobalScope
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.evaluator.Evaluator
import lang.proteus.generation.CodeGenerator
import lang.proteus.generation.Lowerer
import lang.proteus.generation.Optimizer
import lang.proteus.generation.TreeShaker
import lang.proteus.syntax.parser.SyntaxTree

internal class Compilation(val syntaxTree: SyntaxTree) {

    private var _globalScope: BoundGlobalScope? = null


    val globalScope: BoundGlobalScope
        get() {
            if (_globalScope == null) {
                synchronized(this) {
                    if (_globalScope == null) {
                        _globalScope = Binder.bindGlobalScope(null, syntaxTree.root)
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
        val syntaxDiagnostics = syntaxTree.diagnostics
        globalScope.diagnostics.concat(syntaxDiagnostics)
        val diagnostics = globalScope.diagnostics
        val parseTime = computationTimeStopper.stop()
        if (diagnostics.hasErrors()) {
            return EvaluationResult(diagnostics, null, parseTime)
        }
        val program = Binder.bindProgram(globalScope, isMainFile = true)

        _globalScope = program.globalScope
        computationTimeStopper.start()
        val statement = getStatement()
        val codeGenerationTime = computationTimeStopper.stop()
        val functions = _globalScope!!.functions.toSet()
        val functionBodies = program.functionBodies.filter { functions.contains(it.key) }
        if (generateCode) {
            val generatedCode =
                CodeGenerator.generate(statement, functionBodies, functions)
            CodeGenerator.emitGeneratedCode(generatedCode)
        }
        if (program.diagnostics.hasErrors()) {
            return EvaluationResult(program.diagnostics, null, parseTime)
        }
        onWarning(program.diagnostics)
        diagnostics.concat(program.diagnostics)

        computationTimeStopper.start()
        val evaluator = Evaluator(variables, functionBodies, mainFunction = program.mainFunction!!)
        val value = evaluator.evaluate()
        val evaluationTime = computationTimeStopper.stop()
        return EvaluationResult(diagnostics, value, parseTime, evaluationTime, codeGenerationTime)
    }

    private fun getStatement(): BoundBlockStatement {
//        val shaken = TreeShaker.shake(globalScope)
//        _globalScope = shaken
        val lowered = Lowerer.lower(globalScope.statement)
        return Optimizer.optimize(lowered)
    }


}