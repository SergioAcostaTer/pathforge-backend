package com.pathforge.backend.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * CONFIG LAYER — AWS SDK v2 S3Client configured for Cloudflare R2.
 *
 * Cloudflare R2 is S3-compatible but requires:
 *   1. A custom endpoint URI (account-specific R2 endpoint)
 *   2. Static credentials (R2 Access Key + Secret Key)
 *   3. Path-style access (R2 does not support virtual-hosted style)
 *   4. A placeholder region — R2 ignores this, but the SDK requires it
 *
 * Architecture boundary: only R2AvatarStorageAdapter may depend on the S3Client bean.
 */
@Configuration
public class AwsConfig {

    @Bean
    public S3Client r2Client(R2Properties r2Properties) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                r2Properties.accessKey(),
                r2Properties.secretKey()
        );

        return S3Client.builder()
                // Cloudflare R2 custom endpoint — account-specific
                .endpointOverride(URI.create(r2Properties.endpoint()))
                // R2 is globally distributed; use "auto" or any valid AWS region as placeholder
                .region(Region.of("auto"))
                // Static credentials — R2 does not use IAM roles
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                // R2 requires path-style access: endpoint/{bucket}/key
                // rather than virtual-hosted: {bucket}.endpoint/key
                .forcePathStyle(true)
                .build();
    }
}
