package com.pathforge.backend.avatar.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
class AvatarConfig {

    private static final int MAX_IN_MEMORY_SIZE = 10 * 1024 * 1024;

    @Bean
    @Qualifier("falWebClient")
    WebClient falWebClient(FalProperties falProperties) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .build();

        return WebClient.builder()
                .baseUrl(falProperties.baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean
    @Qualifier("downloadWebClient")
    WebClient downloadWebClient() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .build();
        return WebClient.builder().exchangeStrategies(strategies).build();
    }

    @Bean
    S3Client r2Client(R2Properties r2Properties) {
        return S3Client.builder()
                .endpointOverride(URI.create(r2Properties.endpoint()))
                .region(Region.of("auto"))
                .credentialsProvider(credentials(r2Properties))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    S3Presigner r2Presigner(R2Properties r2Properties) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(r2Properties.endpoint()))
                .region(Region.of("auto"))
                .credentialsProvider(credentials(r2Properties))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    private StaticCredentialsProvider credentials(R2Properties r2Properties) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(r2Properties.accessKey(), r2Properties.secretKey()));
    }
}
