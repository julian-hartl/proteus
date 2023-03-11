package lang.proteus.api

import lang.proteus.api.performance.CompilationPerformance
import lang.proteus.api.performance.ComputationTime
import lang.proteus.api.performance.ComputationTimeStopper
import lang.proteus.api.performance.PerformancePrinter
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.metatdata.Metadata
import lang.proteus.metatdata.Metadata.PROTEUS_FILE_OUTPUT_EXTENSION_WITH_DOT
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.DiagnosticsPrinter
import lang.proteus.printing.PrinterColor
import lang.proteus.symbols.TypeSymbol
import lang.proteus.syntax.parser.SyntaxTree
import lang.proteus.text.SourceText
import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import java.lang.management.MemoryUsage

internal class ProteusCompiler(
    private val outputGeneratedCode: Boolean = true,
) {

    @Option(name = "-o", usage = "The path of the output file")
    private var byteCodeOutPath: String? = null

    @Argument(metaVar = "source", usage = "The path of the source file", required = true)
    private var sourcePath = ""

    @Option(name = "-e", usage = "The emitter to use", depends = ["-o"])
    private var emitterName: String? = null

    private val variables = mutableMapOf<String, Any>()

    private val computationTimeStopper = ComputationTimeStopper()

    fun run(args: Array<String>) {
        val parser = CmdLineParser(this)
        try {
            parser.parseArgument(*args)
            if (!sourcePath.endsWith(Metadata.PROTEUS_FILE_EXTENSION_WITH_DOT)) {
                println("The source file must be a .psl file")
                return
            }
            try {
                if(emitterName != null) {
                    compileFile(sourcePath, emitterName!!)
                }else {
                    interpretFile(sourcePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println()
            }
        } catch (e: CmdLineException) {
            System.err.println(e.message)
            parser.printUsage(System.err)
        }

    }

    private fun compileFile(fileName: String, emitterName: String) {
        computationTimeStopper.start()
        val tree = SyntaxTree.load(fileName)
        compileTree(tree, tree.sourceText, emitterName)
    }

    fun interpretFile(fileName: String): InterpretationResult {
        computationTimeStopper.start()
        val tree = SyntaxTree.load(fileName)
        return interpretTree(tree)
    }

    fun interpretText(text: String): InterpretationResult {
        computationTimeStopper.start()
        val tree = SyntaxTree.parse(text)
        return interpretTree(tree)
    }

    private fun compileTree(
        tree: SyntaxTree,
        sourceText: SourceText,
        emitterName: String
    ) {
        if (tree.hasErrors()) {
            DiagnosticsPrinter.printDiagnostics(tree.diagnostics)
            return
        }
        val outputPath =
            byteCodeOutPath ?: sourceText.absolutePath.replace(
                Metadata.PROTEUS_FILE_EXTENSION_WITH_DOT,
                PROTEUS_FILE_OUTPUT_EXTENSION_WITH_DOT
            )
        val compilation = Compilation.compile(tree, emitterName)
        val diagnostics = compilation.emit(
            outputPath
        )
        if(diagnostics.hasErrors()) {
            DiagnosticsPrinter.printDiagnostics(diagnostics)
            return
        }
    }

    private fun interpretTree(
        tree: SyntaxTree,
    ): InterpretationResult {
        if (tree.hasErrors()) {
            DiagnosticsPrinter.printDiagnostics(tree.diagnostics)
            return InterpretationResult(
                tree,
                null,
                null
            )
        }
        val lexerTime = computationTimeStopper.stop()


        computationTimeStopper.start()
        val compilation = Compilation.interpret(tree)
        val evaluationResult = compilation.evaluate(variables, generateCode = outputGeneratedCode) {
            DiagnosticsPrinter.printDiagnostics(it)
        }
        val memoryUsage = getMemoryUsage()
        if (evaluationResult.diagnostics.hasErrors()) {
            DiagnosticsPrinter.printDiagnostics(evaluationResult.diagnostics)
        } else {
            printResult(evaluationResult)
        }
        val performance = CompilationPerformance(
            lexerTime,
            evaluationResult.parseTime ?: ComputationTime(0),
            evaluationResult.evaluationTime ?: ComputationTime(0),
            evaluationResult.codeGenerationTime ?: ComputationTime(0),
            memoryUsage
        )
        val performancePrinter = PerformancePrinter()
        performancePrinter.print(performance)
        return InterpretationResult(tree, evaluationResult, performance)
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