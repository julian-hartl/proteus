package lang.proteus.text

import java.io.File

class SourceFileReader {

    companion object {
        const val PROTEUS_FILE_EXTENSION = "psl"
    }

    fun getSourceFile(path: String): File {

        return java.io.File(path)
    }

    fun validateSourceFile(sourceFile: File) {
        val path = sourceFile.absolutePath
        if (!sourceFile.isFile) {
            throw IllegalArgumentException("File $path does not exist")
        }
        if (!sourceFile.canRead()) {
            throw IllegalArgumentException("File $path cannot be read")
        }
        val fileName = sourceFile.name
        val fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1)
        if (fileExtension != PROTEUS_FILE_EXTENSION) {
            throw IllegalArgumentException("File $path is not a Proteus file")
        }
    }

    fun readAndValidateSourceFile(sourcePath: String): String {
        val file = getSourceFile(sourcePath)
        validateSourceFile(file)
        return readSourceFile(file)
    }

    fun readSourceFile(sourceFile: File): String {
        return sourceFile.readText()
    }

}