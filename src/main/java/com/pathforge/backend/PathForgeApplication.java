package com.pathforge.backend;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
@RequiredArgsConstructor
public class PathForgeApplication {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";

    private final Environment env;

    public static void main(String[] args) {
        SpringApplication.run(PathForgeApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        String port = env.getProperty("server.port", "8080");
        String host = "localhost";
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ignored) {
        }

        var profiles = env.getActiveProfiles().length > 0 ? String.join(", ", env.getActiveProfiles()) : "default";

        log.info(YELLOW + "──────────────────────────────────────────────" + RESET);
        log.info(GREEN + "  PathForge Backend started" + RESET);
        log.info(GREEN + "  Profile:   {}" + RESET, profiles);
        log.info(GREEN + "  Local:     http://localhost:{}" + RESET, port);
        log.info(GREEN + "  External:  http://{}:{}" + RESET, host, port);
        log.info(CYAN + "  API:       http://localhost:{}/index.html" + RESET, port);
        log.info(YELLOW + "──────────────────────────────────────────────" + RESET);
    }
}
