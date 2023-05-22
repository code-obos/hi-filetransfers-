package no.obos.hi.filetransfers.config

import jcifs.DialectVersion
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.Pollers
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec
import org.springframework.integration.file.support.FileExistsMode
import org.springframework.integration.smb.dsl.Smb
import org.springframework.integration.smb.session.SmbSessionFactory
import org.springframework.messaging.Message
import java.io.File


@EnableIntegration
@ConfigurationProperties(prefix = "file-server")
@Configuration
class SmbMessagingGatewayConfiguration {
    lateinit var host: String
    lateinit var username: String
    lateinit var password: String
    lateinit var remoteBaseDir: String
    lateinit var localDirPathFromAs400: String

    var toDirectory: String = "onprop/til"
    var fromDirectory: String = "onprop/fra"
    val backupDirectory: String = "onprop/til/backup"
    var toAs400Channel = "toAs400Channel"
    var toBackupInAs400Channel = "toBackupInAs400Channel"

    @Bean
    fun smbSessionFactory(): SmbSessionFactory? {
        val smbSession = SmbSessionFactory()
        smbSession.host = host
        smbSession.username = username
        smbSession.password = password
        smbSession.port = 445
        smbSession.shareAndDir = remoteBaseDir
        smbSession.smbMinVersion = DialectVersion.SMB210
        smbSession.smbMaxVersion = DialectVersion.SMB311
        return smbSession
    }

    @Bean
    fun smbOutboundFlow(): IntegrationFlow? {
        return IntegrationFlow.from(toAs400Channel)
            .handle(
                Smb.outboundAdapter(smbSessionFactory(), FileExistsMode.REPLACE)
                    .useTemporaryFileName(false)
                    .autoCreateDirectory(true)
                    .remoteDirectory(fromDirectory)
            ).get()
    }

    @Bean
    fun smbInboundFlow(): IntegrationFlow? {
        return IntegrationFlow
            .from(Smb.inboundAdapter(smbSessionFactory())
                .preserveTimestamp(true)
                .remoteDirectory(toDirectory)
                .autoCreateLocalDirectory(true)
                .localDirectory(File(localDirPathFromAs400))
                .deleteRemoteFiles(true)
            ) { e: SourcePollingChannelAdapterSpec ->
                e.id("smbInboundAdapter")
                    .autoStartup(true)
                    .poller(Pollers.fixedDelay(5000))
            }
            .handle { m: Message<*> ->
                println(
                    "Added file ${m.headers["file_name"]} to folder $localDirPathFromAs400"
                )
            }
            .get()
    }

    @Bean
    fun smbBackupFlow(): IntegrationFlow? {
        return IntegrationFlow.from(toBackupInAs400Channel)
            .handle(
                Smb.outboundAdapter(smbSessionFactory(), FileExistsMode.IGNORE)
                    .useTemporaryFileName(false)
                    .autoCreateDirectory(true)
                    .remoteDirectory(backupDirectory)
            ).get()
    }

}