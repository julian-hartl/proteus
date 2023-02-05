package lang.proteus.api

import lang.proteus.api.performance.CompilationPerformance
import lang.proteus.api.performance.ComputationTime
import lang.proteus.api.performance.ComputationTimeStopper
import lang.proteus.api.performance.PerformancePrinter
import lang.proteus.binding.Module
import lang.proteus.diagnostics.Diagnostics
import lang.proteus.diagnostics.MutableDiagnostics
import lang.proteus.evaluator.EvaluationResult
import lang.proteus.metatdata.Metadata
import lang.proteus.metatdata.Metadata.PROTEUS_FILE_OUTPUT_EXTENSION_WITH_DOT
import lang.proteus.parser.CompilationUnit
import lang.proteus.parser.Parser
import lang.proteus.printing.ConsolePrinter
import lang.proteus.printing.DiagnosticsPrinter
import lang.proteus.printing.PrinterColor
import lang.proteus.symbols.ModuleReferenceSymbol
import lang.proteus.symbols.TypeSymbol
import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import java.lang.management.MemoryUsage

internal class ProteusCompiler(
    private val outputGeneratedCode: Boolean = true,
) {

    @Option(name = "-o", usage = "The path of the output java byte code file")
    private var byteCodeOutPath: String? = null

    @Argument(metaVar = "source", usage = "The path of the source file", required = true)
    private var sourcePath = ""

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
            if (byteCodeOutPath?.endsWith(PROTEUS_FILE_OUTPUT_EXTENSION_WITH_DOT) == false) {
                println("The output file must be a $PROTEUS_FILE_OUTPUT_EXTENSION_WITH_DOT file")
                return
            }
            try {
                compileFile(sourcePath)
            } catch (e: Exception) {
                e.printStackTrace()
                println()
            }
        } catch (e: CmdLineException) {
            System.err.println(e.message)
            parser.printUsage(System.err)
        }

    }

    fun compileFile(fileName: String) {
        computationTimeStopper.start()
        val tree = Parser.load(fileName)
        compileTree(tree, MutableDiagnostics(), fileName)
    }

    fun interpretFile(fileName: String): InterpretationResult {
        computationTimeStopper.start()
        val tree = Parser.load(fileName)
        return interpretTree(tree, MutableDiagnostics())
    }

    fun interpretText(text: String): InterpretationResult {
        computationTimeStopper.start()
        val tree = Parser.parse(text)
        return interpretTree(tree, MutableDiagnostics())
    }

    private fun compileTree(
        compilationUnit: CompilationUnit,
        diagnostics: Diagnostics,
        fileName: String,
    ) {
        if (diagnostics.hasErrors()) {
            DiagnosticsPrinter.printDiagnostics(diagnostics)
            return
        }
        val moduleReferenceSymbol = ModuleReferenceSymbol(
            listOf(
                fileName
            ),
        )
        val module = Module(moduleReferenceSymbol, compilationUnit)
        val outputPath =
            byteCodeOutPath ?: moduleReferenceSymbol.name.replace(
                Metadata.PROTEUS_FILE_EXTENSION_WITH_DOT,
                PROTEUS_FILE_OUTPUT_EXTENSION_WITH_DOT
            )
        val compilation = Compilation.compile(compilationUnit, module)
        val diagnostics = compilation.emit(
            outputPath
        )
        if (diagnostics.hasErrors()) {
            DiagnosticsPrinter.printDiagnostics(diagnostics)
            return
        }
    }

    private fun interpretTree(
        tree: CompilationUnit,
        diagnostics: Diagnostics,
    ): InterpretationResult {
        if (diagnostics.hasErrors()) {
            DiagnosticsPrinter.printDiagnostics(diagnostics)
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
            val color = PrinterColor.WHITE
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