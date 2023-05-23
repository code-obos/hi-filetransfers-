package no.obos.hi.filetransfers.service

import no.obos.hi.filetransfers.config.LocalDirectoryConfig
import no.obos.hi.filetransfers.model.FileDto
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

@Service
class SmbMessagingProcessor(
    val smbMessagingGateway: SmbMessagingGateway,
    val localDirectoryConfig: LocalDirectoryConfig,
    val appLogger: Logger
) {
    fun saveFiles(fileDtos: List<FileDto>) {
        appLogger.info("Files to save ${fileDtos.size}")

        if (fileDtos.isEmpty()) {
            appLogger.info("No files to transfer")
            return;
        }

        fileDtos.forEach {
            saveFile(it)
        }
    }

    fun saveFile(fileDto: FileDto) {
        try {
            val decodedFileContent = Base64.getDecoder().decode(fileDto.content)
            val file = FileUtil.constructFile("${localDirectoryConfig.toAs400}/${fileDto.filename}")
            FileUtil.writeBytes(file, decodedFileContent)
            appLogger.info("writing local file to ${FileUtil.absolutePath(file)}")
            appLogger.info("Sending file to smb messaging gateway")
            smbMessagingGateway.toAs400Channel(file)
        } catch (e: Exception) {
            appLogger.error("Failed to store files to As400 with message: ${e.message}")
        }
    }

    fun getFiles(): List<FileDto>? {
        val fileDtos = arrayListOf<FileDto>()
        try {
            val files = getLocalFiles(localDirectoryConfig.fromAs400)

            if (files.isNullOrEmpty()) {
                appLogger.info("No files found")
                return listOf()
            }

            appLogger.info("Found ${files.size}")
            appLogger.info("Storing ${files.size} files in backup folder")
            saveFilesInBackupFolder(files)

            files.forEach {
                val encodedContent = Base64.getEncoder().encodeToString(it.readBytes())
                fileDtos.add(FileDto(filename = it.name, content = encodedContent))
            }

            return fileDtos
        } catch (e : Exception) {
            appLogger.error("Failed to export files from As400 with message: ${e.message}")
        }
        return fileDtos;
    }

    fun saveFilesInBackupFolder(files: Array<out File>?) {
        files?.forEach {
            smbMessagingGateway.toBackupInAs400Channel(it)
            appLogger.info("Saved ${it.name} in backup folder")
        }
    }

    fun deleteTmpFilesFromAs400() {
        appLogger.info("Deleting tmp files from As400")
        getLocalFiles(localDirectoryConfig.fromAs400)?.forEach {
            it.delete()
            appLogger.info("Deleted ${it.name} in folder ${it.absolutePath}")
        }
    }

    fun deleteTmpFilesToAs400() {
        appLogger.info("Deleting tmp files to As400")
        getLocalFiles(localDirectoryConfig.toAs400)?.forEach {
            it.delete()
            appLogger.info("Deleted ${it.name} in folder ${it.absolutePath}")
        }
    }

    fun getLocalFiles(pathName: String): Array<out File>? {
        val folder = File(pathName)
        return folder.listFiles()
    }

    @Suppress("RedundantNullableReturnType")
    object FileUtil { // necessary to be able to mock for unit tests
        fun constructFile(pathName: String): File? {
            return File(pathName)
        }

        fun absolutePath(file: File?): String = file!!.absolutePath

        fun writeBytes(file: File?, bytes: ByteArray) = file!!.writeBytes(bytes)
    }
}