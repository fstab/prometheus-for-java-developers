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
