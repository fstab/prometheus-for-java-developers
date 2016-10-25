Prometheus for Java Developers
==============================

Java examples:

* Direct instrumentation with the Prometheus client library
* Prometheus bridges for the two big frameworks:
  * Spring: Spring Boot Actuator to Prometheus bridge example
  * Java EE: JMX to Prometheus bridge example
* Prometheus bridges for independent 3rd party frameworks:
  * Dropwizard to Prometheus bridge example

The goal is to show different alternatives. In the demo, all alternatives will be added to the same application. In practice, you would choose one of the alternatives and use only one of these alternatives in your application.

Hello, World!
-------------

[https://github.com/fstab/prometheus-demo-new/tree/01-hello-world](https://github.com/fstab/prometheus-demo-new/tree/01-hello-world)

pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>de.consol.labs.promdemo</groupId>
    <artifactId>hello-world</artifactId>
    <version>0.1-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.4.5.RELEASE</version>
    </parent>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>

        <!-- Spring -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

src/main/java/de/consol/labs/promdemo/HelloWorldController.java

```java
package de.consol.labs.promdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@SpringBootApplication
public class HelloWorldController {

    @RequestMapping(path = "/hello-world")
    public @ResponseBody String sayHello() {
        return "hello, world";
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldController.class, args);
    }
}
```

test

```console
> curl http://localhost:8080/hello-world
```

Direct Instrumentation w/ Prometheus client library
---------------------------------------------------

New dependency

```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>simpleclient_common</artifactId>
    <version>0.0.21</version>
</dependency>
```

src/main/java/de/consol/labs/promdemo/HelloWorldController.java

```java
package de.consol.labs.promdemo;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.common.TextFormat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.Writer;

@Controller
@SpringBootApplication
public class HelloWorldController {

    private final Counter promRequestsTotal = Counter.build()
            .name("requests_total")
            .help("Total number of requests.")
            .register();

    @RequestMapping(path = "/hello-world")
    public @ResponseBody String sayHello() {
        promRequestsTotal.inc();
        return "hello, world";
    }

    @RequestMapping(path = "/prometheus")
    public void metrics(Writer responseWriter) throws IOException {
        TextFormat.write004(responseWriter, CollectorRegistry.defaultRegistry.metricFamilySamples());
        responseWriter.close();
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldController.class, args);
    }
}
```

test

```console
> curl http://localhost:8080/hello-world
> curl http://localhost:8080/prometheus
```

Direct Instrumentation w/ Prometheus client library and Spring Boot Endpoint
----------------------------------------------------------------------------

New dependency

```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>simpleclient_spring_boot</artifactId>
    <version>0.0.21</version>
</dependency>
```

src/main/java/de/consol/labs/promdemo/HelloWorldController.java

```java
package de.consol.labs.promdemo;

import io.prometheus.client.Counter;
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@SpringBootApplication
@EnablePrometheusEndpoint
public class HelloWorldController {

    private final Counter promRequestsTotal = Counter.build()
            .name("requests_total")
            .help("Total number of requests.")
            .register();

    @RequestMapping(path = "/hello-world")
    public @ResponseBody String sayHello() {
        promRequestsTotal.inc();
        return "hello, world";
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldController.class, args);
    }
}
```

We removed the request handler for `/prometheus` endpoint (`metrics()` method) and annotate class with `@EnablePrometheusEndpoint`.
As a result, the `/prometheus` endpoint will be implicitly created, the application behaves like in the previous example.

Spring Boot Actuator
--------------------

### Enabling the Spring Boot Actuator

New dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

test

```console
> curl http://localhost:8080/metrics
```

### Adding a Custom Metric to Spring Boot Actuator

src/main/java/de/consol/labs/promdemo/HelloWorldController.java

```java
package de.consol.labs.promdemo;

import io.prometheus.client.Counter;
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@SpringBootApplication
@EnablePrometheusEndpoint
public class HelloWorldController {

    private final Counter promRequestsTotal = Counter.build()
            .name("requests_total")
            .help("Total number of requests.")
            .register();
    private final CounterService springRequestsTotal;

    public HelloWorldController(@Autowired CounterService sprintRequestsTotal) {
        this.springRequestsTotal = sprintRequestsTotal;
    }

    @RequestMapping(path = "/hello-world")
    public @ResponseBody String sayHello() {
        promRequestsTotal.inc();
        springRequestsTotal.increment("counter.calls.promdemo.hello_world");
        return "hello, world";
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldController.class, args);
    }
}
```

### Spring Boot Actuator to Prometheus Bridge

src/main/java/de/consol/labs/promdemo/HelloWorldController.java

```java
package de.consol.labs.promdemo;

import io.prometheus.client.Counter;
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.SpringBootMetricsCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

@Controller
@SpringBootApplication
@EnablePrometheusEndpoint
public class HelloWorldController {

    private final Counter promRequestsTotal = Counter.build()
            .name("requests_total")
            .help("Total number of requests.")
            .register();
    private final CounterService springRequestsTotal;

    public HelloWorldController(@Autowired Collection<PublicMetrics> publicMetrics, @Autowired CounterService sprintRequestsTotal) {
        this.springRequestsTotal = sprintRequestsTotal;
        new SpringBootMetricsCollector(publicMetrics).register();
    }

    @RequestMapping(path = "/hello-world")
    public @ResponseBody String sayHello() {
        promRequestsTotal.inc();
        springRequestsTotal.increment("counter.calls.promdemo.hello_world");
        return "hello, world";
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldController.class, args);
    }
}
```

test

```console
> curl http://localhost:8080/prometheus-metrics
```

Java Management Extensions (JMX)
--------------------------------

### Enable JMX

Run the demo application with JMX enabled:

```console
> mvn clean package
> java \
    -Djava.net.preferIPv4Stack=true \
    -Djava.rmi.server.hostname=localhost \
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.port=5555 \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    -jar target/hello-world-0.1-SNAPSHOT.jar
```

Run `jvisualvm`. When running for the first time
* Under 'tools -> plugins': Install VisualVM-MBeans
* Under 'file -> add jmx connecVon': localhost:5555, Do not require SSL

For the demo:
* Go to `java.lang Memory` (on the MBeans tab)
* Click on the `javax.management.openmbean.CompositeDataSupport` bean for `HeapMemoryUsage`. Documentation for `committed`, `init`, `max`, `used` can be found on [https://docs.oracle.com/javase/8/docs/api/java/lang/management/MemoryUsage.html](https://docs.oracle.com/javase/8/docs/api/java/lang/management/MemoryUsage.html)

By default Spring Boot will expose management endpoints as JMX MBeans under the `org.springframework.boot` domain.

### Custom JMX Metric (MBean)

src/main/java/de/consol/labs/promdemo/HelloWorldController.java

```java
package de.consol.labs.promdemo;

import io.prometheus.client.Counter;
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.SpringBootMetricsCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@SpringBootApplication
@ManagedResource(objectName="de.consol.labs:name=PromDemoBean")
@EnablePrometheusEndpoint
public class HelloWorldController {

    private final Counter promRequestsTotal = Counter.build()
            .name("requests_total")
            .help("Total number of requests.")
            .register();
    private final CounterService springRequestsTotal;
    private final AtomicInteger jmxRequestsTotal = new AtomicInteger();

    public HelloWorldController(@Autowired Collection<PublicMetrics> publicMetrics, @Autowired CounterService sprintRequestsTotal) {
        this.springRequestsTotal = sprintRequestsTotal;
        new SpringBootMetricsCollector(publicMetrics).register();
    }

    @RequestMapping(path = "/hello-world")
    public @ResponseBody String sayHello() {
        promRequestsTotal.inc();
        springRequestsTotal.increment("counter.calls.promdemo.hello_world");
        jmxRequestsTotal.incrementAndGet();
        return "hello, world";
    }

    @ManagedAttribute(description="jmx_requests_total")
    public int getJmxRequestsTotal() {
        return jmxRequestsTotal.get();
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldController.class, args);
    }
}
```

### JMX Prometheus Bridge

config-remote.yml

```yml
---
hostPort: 127.0.0.1:5555
rules:
- pattern: ".*"
```

jmx-exporter.sh

```bash
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
```

test

```console
> curl http://localhost:9200/metrics
```

Notes

* With each scrape, Prometheus loads all JMX beans, then applies matching rules. If this takes long (because MBeans calculate data on the fly), use the JMX exporter's blacklist / whitelist facility, which is applied before MBeans are loaded.

### JMX Prometheus Agent

config-agent.yml

```yml
---
rules:
- pattern: ".*"
```

jmx-agent.sh

```bash
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
```

test

```console
> curl http://localhost:9200/metrics
```

Dropwizard
----------

### Create a Custom Metric with Dropwizard

New dependency:

```xml
<dependency>
    <groupId>io.dropwizard.metrics</groupId>
    <artifactId>metrics-core</artifactId>
    <version>3.1.2</version>
</dependency>
```

src/main/java/de/consol/labs/promdemo/HelloWorldController.java

```java
package de.consol.labs.promdemo;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.SpringBootMetricsCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@SpringBootApplication
@ManagedResource(objectName="de.consol.labs:name=PromDemoBean")
@EnablePrometheusEndpoint
public class HelloWorldController {

    private final Counter promRequestsTotal = Counter.build()
            .name("requests_total")
            .help("Total number of requests.")
            .register();
    private final CounterService springRequestsTotal;
    private final AtomicInteger jmxRequestsTotal = new AtomicInteger();
    private final com.codahale.metrics.Counter dwRequestsTotal;

    public HelloWorldController(@Autowired Collection<PublicMetrics> publicMetrics, @Autowired CounterService sprintRequestsTotal) {
        this.springRequestsTotal = sprintRequestsTotal;
        new SpringBootMetricsCollector(publicMetrics).register();
        MetricRegistry dropwizardRegistry = new MetricRegistry();
        dwRequestsTotal = dropwizardRegistry.counter("dropwizard_requests_total");
        ConsoleReporter.forRegistry(dropwizardRegistry).build().start(5, TimeUnit.SECONDS);
    }

    @RequestMapping(path = "/hello-world")
    public @ResponseBody String sayHello() {
        promRequestsTotal.inc();
        springRequestsTotal.increment("counter.calls.promdemo.hello_world");
        jmxRequestsTotal.incrementAndGet();
        dwRequestsTotal.inc();
        return "hello, world";
    }

    @ManagedAttribute(description="jmx_requests_total")
    public int getJmxRequestsTotal() {
        return jmxRequestsTotal.get();
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldController.class, args);
    }
}
```

### Dropwizard Prometheus Bridge

New dependency

```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>simpleclient_dropwizard</artifactId>
    <version>0.0.21</version>
</dependency>
```

src/main/java/de/consol/labs/promdemo/HelloWorldController.java

```java
package de.consol.labs.promdemo;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.SpringBootMetricsCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@SpringBootApplication
@ManagedResource(objectName="de.consol.labs:name=PromDemoBean")
@EnablePrometheusEndpoint
public class HelloWorldController {

    private final Counter promRequestsTotal = Counter.build()
            .name("requests_total")
            .help("Total number of requests.")
            .register();
    private final CounterService springRequestsTotal;
    private final AtomicInteger jmxRequestsTotal = new AtomicInteger();
    private final com.codahale.metrics.Counter dwRequestsTotal;

    public HelloWorldController(@Autowired Collection<PublicMetrics> publicMetrics, @Autowired CounterService sprintRequestsTotal) {
        this.springRequestsTotal = sprintRequestsTotal;
        new SpringBootMetricsCollector(publicMetrics).register();
        MetricRegistry dropwizardRegistry = new MetricRegistry();
        dwRequestsTotal = dropwizardRegistry.counter("dropwizard_requests_total");
        ConsoleReporter.forRegistry(dropwizardRegistry).build().start(5, TimeUnit.SECONDS);
        new DropwizardExports(dropwizardRegistry).register();
    }

    @RequestMapping(path = "/hello-world")
    public @ResponseBody String sayHello() {
        promRequestsTotal.inc();
        springRequestsTotal.increment("counter.calls.promdemo.hello_world");
        jmxRequestsTotal.incrementAndGet();
        dwRequestsTotal.inc();
        return "hello, world";
    }

    @ManagedAttribute(description="jmx_requests_total")
    public int getJmxRequestsTotal() {
        return jmxRequestsTotal.get();
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldController.class, args);
    }
}
```
