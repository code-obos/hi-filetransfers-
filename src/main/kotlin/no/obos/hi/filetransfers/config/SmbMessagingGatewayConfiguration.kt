package no.obos.hi.filetransfers.config

import jcifs.DialectVersion
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.file.support.FileExistsMode
import org.springframework.integration.smb.dsl.Smb
import org.springframework.integration.smb.session.SmbSessionFactory


@EnableIntegration
@ConfigurationProperties(prefix = "file-server.as400")
@Configuration
class SmbMessagingGatewayConfiguration {
    lateinit var host: String
    lateinit var username: String
    lateinit var password: String
    lateinit var shareAndDir: String

    var toDirectory: String = "til"
    var fromDirectory: String = "fra"
    var smbChannel = "toSmbChannel";

    @Bean
    fun smbSessionFactory(): SmbSessionFactory? {
        val smbSession = SmbSessionFactory()
        smbSession.host = host
        smbSession.username = username
        smbSession.password = password
        smbSession.port = 445
        smbSession.shareAndDir = shareAndDir
        smbSession.smbMinVersion = DialectVersion.SMB210
        smbSession.smbMaxVersion = DialectVersion.SMB311
        return smbSession
    }

    @Bean
    fun smbOutboundFlow(): IntegrationFlow? {
        return IntegrationFlow.from(smbChannel)
            .handle(
                Smb.outboundAdapter(smbSessionFactory(), FileExistsMode.REPLACE)
                    .useTemporaryFileName(false)
                    .remoteDirectory(toDirectory)
            ).get()
    }

}