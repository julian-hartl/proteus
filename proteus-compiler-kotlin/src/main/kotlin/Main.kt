import lang.proteus.grammar.ProteusLexer
import lang.proteus.grammar.ProteusParser
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream


class Main {


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
//            Functions;
//            val compiler = ProteusCompiler()
//            compiler.run(args)
            val lexer = ProteusLexer(
                CharStreams.fromString("fn test();")
            )
            val tokens = CommonTokenStream(lexer)
            val parser = ProteusParser(tokens)
            parser.addErrorListener(BaseErrorListener())
            val tree = parser.compilationUnit().toStringTree()
            println(tree)

        }
    }
}
