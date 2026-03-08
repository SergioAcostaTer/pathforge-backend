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
    S3Client r2Client(R2Properties r2Properties) {
        URI endpoint = resolveR2Endpoint(r2Properties);
        return S3Client.builder()
                .endpointOverride(endpoint)
                .region(Region.of("auto"))
                .credentialsProvider(credentials(r2Properties))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    S3Presigner r2Presigner(R2Properties r2Properties) {
        URI endpoint = resolveR2Endpoint(r2Properties);
        return S3Presigner.builder()
                .endpointOverride(endpoint)
                .region(Region.of("auto"))
                .credentialsProvider(credentials(r2Properties))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    private URI resolveR2Endpoint(R2Properties r2Properties) {
        String rawEndpoint = Objects.requireNonNullElse(r2Properties.endpoint(), "").trim();
        if (rawEndpoint.isEmpty()) {
            throw new IllegalStateException(
                    "Missing storage.r2.endpoint (R2_ENDPOINT). Set it to your Cloudflare R2 endpoint, "
                            + "for example: https://<accountid>.r2.cloudflarestorage.com");
        }

        String endpoint = rawEndpoint.contains("://") ? rawEndpoint : "https://" + rawEndpoint;
        URI uri = URI.create(endpoint);
        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new IllegalStateException(
                    "Invalid storage.r2.endpoint (R2_ENDPOINT): " + rawEndpoint
                            + ". Expected a valid URL like https://<accountid>.r2.cloudflarestorage.com");
        }

        return uri;
    }

    private StaticCredentialsProvider credentials(R2Properties r2Properties) {
        String accessKey = requireNonBlank("storage.r2.access-key", r2Properties.accessKey(), "R2_ACCESS_KEY");
        String secretKey = requireNonBlank("storage.r2.secret-key", r2Properties.secretKey(), "R2_SECRET_KEY");
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));
    }

    private String requireNonBlank(String propertyName, String value, String envVariable) {
        String normalized = Objects.requireNonNullElse(value, "").trim();
        if (normalized.isEmpty()) {
            throw new IllegalStateException("Missing " + propertyName + " (" + envVariable + ").");
        }
        return normalized;
    }
}
