package no.obos.hi.filetransfers.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.obos.hi.filetransfers.service.SmbMessagingProcessor
import no.obos.springboot.tokenservice.api.controller.TokenServiceController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "transfer controller controller"
)


@RestController
@RequestMapping("transfer")
class SmbMessagingController(val smbMessagingProcessor: SmbMessagingProcessor): TokenServiceController {

    @GetMapping("run")
    @Operation(description = "does the transfer")
    fun process(): ResponseEntity<*> {
        smbMessagingProcessor.process()
        return ResponseEntity.status(HttpStatus.OK).body(null)
    }

}