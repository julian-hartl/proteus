package lang.proteus.api

import lang.proteus.api.performance.ComputationTimeStopper
import lang.proteus.binding.Binder
import lang.proteus.binding.BoundBlockStatement
import lang.proteus.binding.BoundGlobalScope
import lang.proteus.binding.ControlFlowGraph
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.evaluator.Evaluator
import lang.proteus.generation.CodeGenerator
import lang.proteus.generation.Lowerer
import lang.proteus.generation.Optimizer
import lang.proteus.syntax.parser.SyntaxTree

internal class Compilation internal constructor(val previous: Compilation?, val syntaxTree: SyntaxTree) {

    private var _globalScope: BoundGlobalScope? = null

    constructor(syntaxTree: SyntaxTree) : this(null, syntaxTree) {

    }

    val globalScope: BoundGlobalScope
        get() {
            if (_globalScope == null) {
                synchronized(this) {
                    if (_globalScope == null) {
                        _globalScope = Binder.bindGlobalScope(previous?.globalScope, syntaxTree.root)
                    }
                }
            }
            return _globalScope!!
        }

    fun continueWith(syntaxTree: SyntaxTree): Compilation {
        return Compilation(this, syntaxTree)
    }

    fun evaluate(variables: MutableMap<String, Any>, generateCode: Boolean = false): EvaluationResult<*> {

        val computationTimeStopper = ComputationTimeStopper()
        computationTimeStopper.start()
        val syntaxDiagnostics = syntaxTree.diagnostics
        globalScope.diagnostics.concat(syntaxDiagnostics)
        val diagnostics = globalScope.diagnostics
        val parseTime = computationTimeStopper.stop()
        if (diagnostics.hasErrors()) {
            return EvaluationResult(diagnostics, null, parseTime)
        }
        val program = Binder.bindProgram(globalScope)

        if (program.diagnostics.hasErrors()) {
            return EvaluationResult(program.diagnostics, null, parseTime)
        }
        diagnostics.concat(program.diagnostics)
        computationTimeStopper.start()
        val statement = getStatement()
        val codeGenerationTime = computationTimeStopper.stop()
        if (generateCode) {
            val generatedCode = CodeGenerator.generate(statement, program.functionBodies)
            CodeGenerator.emitGeneratedCode(generatedCode)
        }
        computationTimeStopper.start()
        val evaluator = Evaluator(statement, variables, program.functionBodies)
        val value = evaluator.evaluate()
        val evaluationTime = computationTimeStopper.stop()
        return EvaluationResult(diagnostics, value, parseTime, evaluationTime, codeGenerationTime)
    }

    private fun getStatement(): BoundBlockStatement {
        val lowered = Lowerer.lower(globalScope.statement)
        val optimized = Optimizer.optimize(lowered)
        return optimized
    }


}