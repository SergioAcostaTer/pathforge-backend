package com.pathforge.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.pathforge.backend.config.FalProperties;
import com.pathforge.backend.config.R2Properties;

@SpringBootApplication
@EnableConfigurationProperties({ FalProperties.class, R2Properties.class })
public class PathForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PathForgeApplication.class, args);
    }
}
