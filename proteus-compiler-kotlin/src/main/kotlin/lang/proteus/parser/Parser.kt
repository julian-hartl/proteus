package lang.proteus.parser

import lang.proteus.grammar.ProteusLexer
import lang.proteus.grammar.ProteusParser
import lang.proteus.grammar.ProteusParser.CompilationUnitContext
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import java.io.File


internal class Parser {
    companion object {
        public fun load(path: String): CompilationUnit {
            val file = File(path)
            val fileContent = file.readText()
            val input = CharStreams.fromString(fileContent)
            val lexer = ProteusLexer(input)
            val tokenStream = CommonTokenStream(lexer)
            val parser = ProteusParser(tokenStream)
            // todo: error handling
            return parser.compilationUnit()
        }
    }
}