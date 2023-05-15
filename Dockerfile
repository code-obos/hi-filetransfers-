FROM registry.obos.no/3rd/eclipse-temurin:17.0.3_7-jdk-alpine AS build

COPY target/*.jar .
RUN mkdir -p build && \
    cd build && \
    jar -xf ../*.jar

FROM registry.obos.no/3rd/eclipse-temurin:17.0.3_7-jre-alpine

ARG PORT
# Setup
RUN addgroup -S spring && adduser -S spring -G spring
RUN chown -R spring:spring /opt


# Application, configuration and log directories
ARG APP_DIR=/opt/app
RUN mkdir -p ${APP_DIR} && mkdir -p opt/conf && mkdir -p /opt/log && mkdir -p /opt/log_old

# Application, configuration and log directories
ARG APP_DIR=/opt/app
RUN mkdir -p ${APP_DIR}  \
    && mkdir -p opt/conf  \
    && mkdir -p /opt/log  \
    && mkdir -p /opt/log_old  \
    && mkdir -p /opt/app/tmp/from  \
    && mkdir -p /opt/app/tmp/to \
    && mkdir -p /opt/app/tmp/from \

# Dependency layers for the application
COPY --from=build build/BOOT-INF/lib ${APP_DIR}/lib
COPY --from=build build/META-INF ${APP_DIR}/META-INF
COPY --from=build build/BOOT-INF/classes ${APP_DIR}

# Starting binary
ADD deploy/bin/start.sh /
RUN chmod 755 /start.sh

USER spring
EXPOSE $PORT
CMD ["sh", "/start.sh"]