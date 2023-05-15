package no.obos.hi.filetransfers.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.obos.hi.filetransfers.model.FileDto

import no.obos.hi.filetransfers.service.SmbMessagingProcessor
import no.obos.springboot.tokenservice.api.controller.TokenServiceController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "transfer controller"
)

@RestController
@RequestMapping("transfer")
class SmbMessagingController(val smbMessagingProcessor: SmbMessagingProcessor): TokenServiceController {

    @PostMapping("file")
    @Operation(description = "Stores file in to AS400")
    fun process(fileDto: FileDto): ResponseEntity<*> {
        smbMessagingProcessor.processFileToAs400(fileDto)
        return ResponseEntity.status(HttpStatus.OK).body(null)
    }

    @PostMapping("files")
    @Operation(description = "Stores files in to AS400")
    fun process(fileDto: List<FileDto>): ResponseEntity<*> {
        smbMessagingProcessor.processFilesToAs400(fileDto)
        return ResponseEntity.status(HttpStatus.OK).body(null)
    }

}