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
