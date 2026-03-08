package com.pathforge.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * CONFIG LAYER — WebClient beans for Fal.ai communication.
 *
 * Two beans are produced:
 *
 *   falWebClient      — pre-configured with the Fal.ai base URL, JSON content type,
 *                       and a custom ObjectMapper using snake_case naming (Fal.ai API contract).
 *
 *   downloadWebClient — a generic WebClient for downloading binary content (image bytes)
 *                       from arbitrary URLs (Fal.ai CDN). Configured with a generous
 *                       memory buffer to handle large images.
 *
 * Architecture boundary: only the FalAvatarGeneratorAdapter may depend on these beans.
 */
@Configuration
public class FalConfig {

    /**
     * Maximum in-memory buffer size for WebClient responses: 10 MB.
     * Increase if you expect larger generated images.
     */
    private static final int MAX_IN_MEMORY_SIZE = 10 * 1024 * 1024;

    @Bean
    public WebClient falWebClient(FalProperties falProperties, ObjectMapper globalObjectMapper) {
        // Fal.ai uses snake_case — create a dedicated mapper to avoid affecting other beans.
        ObjectMapper falMapper = globalObjectMapper.copy()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE);
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(falMapper, MediaType.APPLICATION_JSON));
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(falMapper, MediaType.APPLICATION_JSON));
                })
                .build();

        return WebClient.builder()
                .baseUrl(falProperties.baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean
    public WebClient downloadWebClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .build();

        return WebClient.builder()
                .exchangeStrategies(strategies)
                .build();
    }
}
