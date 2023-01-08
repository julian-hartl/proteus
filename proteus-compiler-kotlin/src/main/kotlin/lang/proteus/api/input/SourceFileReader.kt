package lang.proteus.api.input

import java.io.File

class SourceFileReader(private val path: String) : ProteusSourceTextInputReader() {

    private var hasRead = false

    override fun read(): String? {
        if (hasRead) {
            return null
        }
        val file = File(path)
        validateSourceFile(file)
        hasRead = true
        return file.readText()
    }

    private fun validateSourceFile(sourceFile: File) {
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

    companion object {
        private const val PROTEUS_FILE_EXTENSION: String = "psl"
    }

}