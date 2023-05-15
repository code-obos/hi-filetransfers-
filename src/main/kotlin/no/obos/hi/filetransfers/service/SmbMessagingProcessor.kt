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

    fun processFileToAs400(fileDto: FileDto) {
        try {
            processFiletransfer(fileDto)
        } catch (e: Exception) {
            appLogger.info(e.message);
        }
    }

    fun processFilesToAs400(fileDto: List<FileDto>) {
        try {

            fileDto.forEach {
                processFileToAs400(it)
            }
        } catch (e: Exception) {
            appLogger.info(e.message);
        }
    }

    fun processFiletransfer(fileDto: FileDto) {
        appLogger.info("Starting file transfer")
        val string = decodeBase64String(fileDto)
        val file = FileUtil.constructFile("${localDirectoryConfig.toPath}/${fileDto.filename}.txt")
        FileUtil.writeBytes(file, string.toByteArray())
        appLogger.info("local file: ${FileUtil.absolutePath(file)}")
        appLogger.info("writing file to smb messaging gateway")
        smbMessagingGateway.toAs400Channel(file);
    }

    fun processFilesToAzureStorage() {

    }

    fun decodeBase64String(fileDto: FileDto): String {
        return String(Base64.getDecoder().decode(fileDto.content))
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