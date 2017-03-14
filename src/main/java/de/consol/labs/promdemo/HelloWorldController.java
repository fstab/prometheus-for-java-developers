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
