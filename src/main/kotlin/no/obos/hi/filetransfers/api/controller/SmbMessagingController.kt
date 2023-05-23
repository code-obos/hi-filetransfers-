package no.obos.hi.filetransfers.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import no.obos.hi.filetransfers.model.FileDto
import no.obos.hi.filetransfers.service.SmbMessagingProcessor
import no.obos.springboot.tokenservice.api.controller.TokenServiceController
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(
    name = "Filetransfer controller"
)

@RestController
@RequestMapping("transfer")
class SmbMessagingController(
    val smbMessagingProcessor: SmbMessagingProcessor,
    val appLogger: Logger
) : TokenServiceController {
    @GetMapping("files")
    @Operation(description = "Export files from AS400")
    fun process(): ResponseEntity<*> {
        return try {
            appLogger.info("Starting exporting files from As400")
            val files = smbMessagingProcessor.getFiles()
            ResponseEntity.status(HttpStatus.OK).body(files)
        } catch (e: Exception) {
            appLogger.error("Failed to transfer files")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        } finally {
            appLogger.info("Successfully transfered files")
            smbMessagingProcessor.deleteTmpFilesFromAs400()
        }
    }

    @PostMapping("files")
    @Operation(description = "Save files in to As400")
    fun process(@RequestBody @Valid fileDtos: List<FileDto>): ResponseEntity<*> {
        return try {
            appLogger.info("Starting file storage to As400")
            smbMessagingProcessor.saveFiles(fileDtos)
            ResponseEntity.status(HttpStatus.OK).body(null)
        } catch (e: Exception) {
            appLogger.error("Failed to transfer files")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        } finally {
            appLogger.info("Successfully transfered files")
            smbMessagingProcessor.deleteTmpFilesToAs400()
        }
    }
}