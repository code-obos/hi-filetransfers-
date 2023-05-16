package no.obos.hi.filetransfers.service

import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.messaging.handler.annotation.Payload
import java.io.File

@MessagingGateway
interface SmbMessagingGateway {
    @Gateway(requestChannel = "toAs400Channel")
    fun toAs400Channel(file: File?);

    @Gateway(requestChannel = "toBackupInAs400Channel")
    fun toBackupInAs400Channel(file: File?)
}