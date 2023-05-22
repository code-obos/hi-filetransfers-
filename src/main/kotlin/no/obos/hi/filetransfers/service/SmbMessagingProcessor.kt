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
    fun processFilesToAs400(fileDtos: List<FileDto>) {
        appLogger.info("Starting file transfer to as400")
        appLogger.info("Files to transfer ${fileDtos.size}")

        fileDtos.forEach {
            processFiletransfer(it)
        }
    }

    fun processFiletransfer(fileDto: FileDto) {
        val decodedFileContent = Base64.getDecoder().decode(fileDto.content)
        val file = FileUtil.constructFile("${localDirectoryConfig.toAs400}/${fileDto.filename}")
        FileUtil.writeBytes(file, decodedFileContent)
        appLogger.info("writing local file to ${FileUtil.absolutePath(file)}")
        appLogger.info("Sending file to smb messaging gateway")
        smbMessagingGateway.toAs400Channel(file)
    }

    fun getFilesfromAs400(): List<FileDto>? {
        appLogger.info("Starting file transfer from as400")
        val files = getLocalFiles(localDirectoryConfig.fromAs400)

        if (files.isNullOrEmpty()) {
            return listOf()
        }

        appLogger.info("Found ${files.size}")
        appLogger.info("Storing files in backup folder")
        storeFilesInBackupFolder(files)

        val fileDtos = arrayListOf<FileDto>()

        files.forEach {
            val encodedContent = Base64.getEncoder().encodeToString(it.readBytes())
            fileDtos.add(FileDto(filename = it.name, content = encodedContent))
        }

        return fileDtos
    }

    fun storeFilesInBackupFolder(files: Array<out File>?) {
        appLogger.info("Storing files in backup folder")
        files?.forEach {
            smbMessagingGateway.toBackupInAs400Channel(it)
        }
    }

    fun getLocalFiles(pathName: String): Array<out File>? {
        val folder = File(pathName)
        return folder.listFiles()
    }

    fun deleteTmpFilesFromAs400() {
        appLogger.info("Deleting tmp files from As400")
        getLocalFiles(localDirectoryConfig.fromAs400)?.forEach {
            it.delete()
            appLogger.info("Deleting ${it.name} in folder ${it.absolutePath}")
        }
    }

    fun deleteTmpFilesToAs400() {
        appLogger.info("Deleting tmp files to As400")
        getLocalFiles(localDirectoryConfig.toAs400)?.forEach {
            it.delete()
            appLogger.info("Deleting ${it.name} in folder ${it.absolutePath}")
        }
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