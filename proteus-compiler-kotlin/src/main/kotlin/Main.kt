import lang.proteus.api.ProteusCompiler
import lang.proteus.external.Functions

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
