package no.obos.hi.filetransfers.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class LoggerConfig {
    @Bean
    @Scope("prototype")
    fun appLogger(ip: InjectionPoint): Logger? {
        return LoggerFactory.getLogger((ip.member.declaringClass))
    }
}