package no.obos.hi.filetransfers.service

import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.MessagingGateway
import java.io.File

@MessagingGateway
interface SmbMessagingGateway {

    @Gateway(requestChannel = "toSmbChannel")
    fun sendToSmb(file: File?)

    /*@Gateway(requestChannel = "fromSmbChannel")
    fun getFromSmb(file: File?)*/
}