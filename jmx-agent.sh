#!/bin/bash

# mkdir tmp
# cd tmp
# git clone https://github.com/prometheus/jmx_exporter
# cd jmx_exporter
# mvn clean install
# cd ../..
# rm -rf tmp

mvn clean package

export AGENT_JAR=$HOME/.m2/repository/io/prometheus/jmx/jmx_prometheus_javaagent/0.7-SNAPSHOT/jmx_prometheus_javaagent-0.7-SNAPSHOT.jar

java -javaagent:$AGENT_JAR=9200:config-agent.yml -jar target/hello-world-0.1-SNAPSHOT.jar
