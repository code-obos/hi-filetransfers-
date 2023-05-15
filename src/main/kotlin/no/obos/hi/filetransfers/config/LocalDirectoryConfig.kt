package no.obos.hi.filetransfers.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "localdirpath")
@Configuration
class LocalDirectoryConfig {
    lateinit var toPath: String
    lateinit var fromPath: String
}