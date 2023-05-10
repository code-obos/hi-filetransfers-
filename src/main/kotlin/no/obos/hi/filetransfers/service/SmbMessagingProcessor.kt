package no.obos.hi.filetransfers.service


import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.io.File

@Service
class SmbMessagingProcessor(
    val smbMessagingGateway : SmbMessagingGateway,
    val appLogger: Logger
) {

    fun process() {
        appLogger.info("Starting transfer")
        val file = FileUtil.constructFile("./tmp")
        appLogger.info("local file: ${FileUtil.absolutePath(file)}")
        FileUtil.writeBytes(file, "test".toByteArray())
        appLogger.info("writing file to smb messaging gateway")
        smbMessagingGateway.sendToSmb(file)
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