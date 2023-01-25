package lang.proteus.syntax.parser

import lang.proteus.syntax.lexer.SyntaxToken
import lang.proteus.syntax.lexer.token.Keyword
import lang.proteus.syntax.lexer.token.Token
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

internal class ImportStatementSyntax(
    val importToken: SyntaxToken<Keyword.Import>,
    val filePathToken: SyntaxToken<Token.String>,
    val semiColon: SyntaxToken<Token.SemiColon>,
    syntaxTree: SyntaxTree,
) : MemberSyntax(syntaxTree) {

    companion object {
        private val librariesPath =
            Paths.get(System.getenv("HOME") + File.separator + ".proteus" + File.separator + "libraries")

        // Library imports are of the form "libName/path/to/file"
        private val libraryImportRegex = Regex("^[a-zA-Z][^.][^/]*[/]?[^./]+\$")
    }

    override val token: Token
        get() = Token.ImportStatement

    val rawFilePath = filePathToken.literal

    val resolvedFilePath: String
        get() {
            val filePath = filePathToken.literal
            val filePathBuilder = StringBuilder()
            val importPath = Paths.get(filePath)
            if (isLibraryImport) {
                filePathBuilder.append(librariesPath.resolve(importPath))
            } else {
                val parentPath = Paths.get(syntaxTree.sourceText.absolutePath).parent
                filePathBuilder.append(parentPath.resolve(importPath))
            }
            if (!filePathBuilder.endsWith(".psl")) {
                filePathBuilder.append(".psl")
            }
            return filePathBuilder.toString()
        }

    val isLibraryImport: Boolean
        get() = libraryImportRegex.matches(rawFilePath)

    val isRelativeImport: Boolean
        get() = rawFilePath.startsWith("./") || rawFilePath.startsWith("../")

    val isValidImport: Boolean
        get() = isLibraryImport || isRelativeImport

    val path: Path
        get() = Paths.get(resolvedFilePath)

    val file: File
        get() = path.toFile()
}