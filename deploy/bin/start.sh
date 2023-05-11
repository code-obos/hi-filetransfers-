#!/usr/bin/env sh

# Start service
MEMORY_CONF=""
SPRING_CONF="--spring.config.location=file:/opt/conf/"

java ${MEMORY_CONF} \
  -cp opt/app:opt/app/lib/* \
  no.obos.hi.filetransfers.ApplicationKt \
  ${SPRING_CONF}
