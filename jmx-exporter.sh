#!/bin/bash

# mkdir tmp
# cd tmp
# git clone https://github.com/prometheus/jmx_exporter
# cd jmx_exporter
# mvn clean install
# cd ../..
# rm -rf tmp

export JMX_EXPORTER=$HOME/.m2/repository/io/prometheus/jmx/jmx_prometheus_httpserver/0.7-SNAPSHOT/jmx_prometheus_httpserver-0.7-SNAPSHOT-jar-with-dependencies.jar

java -jar $JMX_EXPORTER 9200 config-remote.yml
