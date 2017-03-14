#!/bin/bash

mvn clean package

java \
    -Djava.net.preferIPv4Stack=true \
    -Djava.rmi.server.hostname=localhost \
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.port=5555 \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    -jar target/hello-world-0.1-SNAPSHOT.jar
