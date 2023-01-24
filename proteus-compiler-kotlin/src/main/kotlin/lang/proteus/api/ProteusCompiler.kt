package lang.proteus.api

import lang.proteus.api.performance.CompilationPerformance
import lang.proteus.api.performance.ComputationTime
import lang.proteus.api.performance.ComputationTimeStopper
import lang.proteus.api.performance.PerformancePrinter
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.DiagnosticsPrinter
import lang.proteus.printing.PrinterColor
import lang.proteus.symbols.TypeSymbol
import lang.proteus.syntax.parser.SyntaxTree
import lang.proteus.text.SourceText
import java.lang.management.MemoryUsage

internal class ProteusCompiler() {


    private val variables = mutableMapOf<String, Any>()

    private val computationTimeStopper = ComputationTimeStopper()
    fun compile(fileName: String): CompilationResult {
        val tree = SyntaxTree.load(fileName)
        return compileTree(tree, tree.sourceText, generateCode = true)
    }

    fun compileText(text: String, generateCode: Boolean = false): CompilationResult {
        val sourceText = SourceText.from(text)
        computationTimeStopper.start()
        val tree = SyntaxTree.parse(text)
        return compileTree(tree, sourceText, generateCode)
    }

    private fun compileTree(
        tree: SyntaxTree,
        sourceText: SourceText,
        generateCode: Boolean,
    ): CompilationResult {
        if (tree.hasErrors()) {
            DiagnosticsPrinter.printDiagnostics(tree.diagnostics)
            return CompilationResult(
                tree,
                null,
                null
            )
        }
        val lexerTime = computationTimeStopper.stop()


        val compilation = Compilation(tree)
        val compilationResult = compilation.evaluate(variables, generateCode = generateCode) {
            DiagnosticsPrinter.printDiagnostics(it)
        }
        val memoryUsage = getMemoryUsage()
        if (compilationResult.diagnostics.hasErrors()) {
            DiagnosticsPrinter.printDiagnostics(compilationResult.diagnostics)
        } else {
            printResult(compilationResult)
        }
        val performance = CompilationPerformance(
            lexerTime,
            compilationResult.parseTime ?: ComputationTime(0),
            compilationResult.evaluationTime ?: ComputationTime(0),
            compilationResult.codeGenerationTime ?: ComputationTime(0),
            memoryUsage
        )
        val performancePrinter = PerformancePrinter()
        performancePrinter.print(performance)
        return CompilationResult(tree, compilationResult, performance)
    }

    private fun getMemoryUsage(): Int {
        val memoryUsage: MemoryUsage = java.lang.management.ManagementFactory.getMemoryMXBean().heapMemoryUsage
        return (memoryUsage.used).toInt()
    }

    private fun printResult(compilationResult: EvaluationResult<*>) {
        val consolePrinter = ConsolePrinter()
        val value = compilationResult.value
        if (value != null) {
            consolePrinter.println()
            val color = TypeSymbol.fromValueOrAny(value).getOutputColor()
            consolePrinter.setColor(color)
            consolePrinter.println(value.toString())
        }
        consolePrinter.println()
    }


}

private fun TypeSymbol.getOutputColor(): PrinterColor {
    return when (this) {
        TypeSymbol.Int -> PrinterColor.BLUE
        TypeSymbol.Boolean -> PrinterColor.MAGENTA
        TypeSymbol.Type -> PrinterColor.RED
        TypeSymbol.String -> PrinterColor.GREEN
        TypeSymbol.Any -> PrinterColor.YELLOW
        else -> {
            PrinterColor.WHITE
        }
    }
}