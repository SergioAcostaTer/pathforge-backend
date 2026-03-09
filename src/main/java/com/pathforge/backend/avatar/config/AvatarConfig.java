package com.pathforge.backend.avatar.config;

import java.net.URI;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
class AvatarConfig {

    @Bean
    @Qualifier("falRestClient")
    RestClient falRestClient(FalProperties falProperties) {
        return RestClient.builder()
                .baseUrl(falProperties.baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    @Qualifier("downloadRestClient")
    RestClient downloadRestClient() {
        return RestClient.builder().build();
    }

    @Bean
    S3Client r2Client(URI r2Endpoint, StaticCredentialsProvider r2Credentials) {
        return S3Client.builder()
                .endpointOverride(r2Endpoint)
                .region(Region.of("auto"))
                .credentialsProvider(r2Credentials)
                .forcePathStyle(true)
                .build();
    }

    @Bean
    S3Presigner r2Presigner(URI r2Endpoint, StaticCredentialsProvider r2Credentials) {
        return S3Presigner.builder()
                .endpointOverride(r2Endpoint)
                .region(Region.of("auto"))
                .credentialsProvider(r2Credentials)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    @Bean
    URI r2Endpoint(R2Properties r2) {
        String raw = Objects.requireNonNullElse(r2.endpoint(), "").trim();
        if (raw.isEmpty()) {
            throw new IllegalStateException(
                    "Missing storage.r2.endpoint (R2_ENDPOINT). Set it to your Cloudflare R2 endpoint, "
                            + "for example: https://<accountid>.r2.cloudflarestorage.com");
        }
        String withScheme = raw.contains("://") ? raw : "https://" + raw;
        URI uri = URI.create(withScheme);
        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new IllegalStateException(
                    "Invalid storage.r2.endpoint (R2_ENDPOINT): " + raw
                            + ". Expected a valid URL like https://<accountid>.r2.cloudflarestorage.com");
        }
        return uri;
    }

    @Bean
    StaticCredentialsProvider r2Credentials(R2Properties r2) {
        String accessKey = requireNonBlank("storage.r2.access-key", r2.accessKey(), "R2_ACCESS_KEY");
        String secretKey = requireNonBlank("storage.r2.secret-key", r2.secretKey(), "R2_SECRET_KEY");
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    private String requireNonBlank(String propertyName, String value, String envVariable) {
        String normalized = Objects.requireNonNullElse(value, "").trim();
        if (normalized.isEmpty()) {
            throw new IllegalStateException("Missing " + propertyName + " (" + envVariable + ").");
        }
        return normalized;
    }
}
