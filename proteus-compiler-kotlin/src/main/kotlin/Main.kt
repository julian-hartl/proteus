import lang.proteus.api.ProteusCompiler
import lang.proteus.external.Functions
import lang.proteus.metatdata.Metadata
import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option


class Main {


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Functions;
            val compiler = ProteusCompiler()
            compiler.run(args)
        }
    }
}
