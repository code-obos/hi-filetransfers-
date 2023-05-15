package no.obos.hi.filetransfers.service

import no.obos.hi.filetransfers.config.LocalDirectoryConfig
import no.obos.hi.filetransfers.model.FileDto
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

@Service
class SmbMessagingProcessor(
    val smbMessagingGateway : SmbMessagingGateway,
    val localDirectoryConfig: LocalDirectoryConfig,
    val appLogger: Logger
) {
    fun processFilesToAs400(fileDtos: List<FileDto>) {
            appLogger.info("Files to transfer ${fileDtos.size}")
            fileDtos.forEach {
                processFiletransfer(it)
            }
    }

    fun processFiletransfer(fileDto: FileDto) {
        appLogger.info("Starting file transfer to as400")
        val string = String(Base64.getDecoder().decode(fileDto.content))
        val file = FileUtil.constructFile("${localDirectoryConfig.toPath}/${fileDto.filename}")
        FileUtil.writeBytes(file, string.toByteArray())
        appLogger.info("local file: ${FileUtil.absolutePath(file)}")
        appLogger.info("writing file to smb messaging gateway")
        smbMessagingGateway.toAs400Channel(file)
    }

    fun getFilesfromAs400() : List<FileDto>? {
        appLogger.info("Starting file transfer from as400")
        val files = getLocalFilesFromAs400()
        appLogger.info("Found ${files?.size}")
        appLogger.info("Storing in backup folder")
        storeFilesInBackupFolder(files)
        val fileDtos = arrayListOf<FileDto>()

        files?.forEach {
            val encodedContent = Base64.getEncoder().encodeToString(it.readBytes())
            fileDtos.add(FileDto(filename = it.name, content = encodedContent ))
        }

        return fileDtos
    }

    fun storeFilesInBackupFolder(files: Array<out File>?) {
        appLogger.info("Storing files in backup folder")
        files?.forEach {
            smbMessagingGateway.toBackupInAs400Channel(it)
        }
    }

    fun deleteFilesFromAs400() {
        appLogger.info("Deleting tmp files from AS400")
        getLocalFilesFromAs400()?.forEach {
            it.delete()
        }
    }

    fun deleteFilesToAs400() {
        appLogger.info("Deleting tmp files from AS400")
        getLocalFilesToAs400()?.forEach {
            it.delete()
        }
    }

    /**
     * Files transferred from folder hi/onprop/til
     */
    fun getLocalFilesFromAs400() : Array<out File>? {
        val folder = File(localDirectoryConfig.toPath)
        return folder.listFiles()
    }

    /**
     * Files transferred from folder hi/onprop/fra
     */
    fun getLocalFilesToAs400() : Array<out File>? {
        val folder = File(localDirectoryConfig.fromPath)
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