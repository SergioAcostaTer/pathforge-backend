package com.pathforge.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan  // auto-discovers all @ConfigurationProperties in this package
public class PathForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PathForgeApplication.class, args);
    }
}
