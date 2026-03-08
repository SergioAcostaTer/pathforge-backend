package com.pathforge.backend.adapters.out.fal;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.pathforge.backend.adapters.out.fal.dto.FalGenerateRequest;
import com.pathforge.backend.adapters.out.fal.dto.FalGenerateResponse;
import com.pathforge.backend.config.FalProperties;
import com.pathforge.backend.domain.avatar.AvatarGenerator;
import com.pathforge.backend.domain.avatar.GeneratedImage;
import com.pathforge.backend.domain.avatar.exception.AvatarGenerationException;

import lombok.extern.slf4j.Slf4j;

/**
 * ADAPTER LAYER (out/fal) — Implements the AvatarGenerator domain port using Fal.ai.
 *
 * This class is the ONLY place in the application that knows about Fal.ai.
 * It handles:
 *   - Authentication header injection
 *   - JSON serialisation of the request
 *   - HTTP error mapping to domain exceptions
 *   - Downloading the generated image bytes from the temporary Fal.ai CDN URL
 *
 * Architecture boundary:
 *   - Implements domain port: AvatarGenerator
 *   - Must NOT be referenced by any class outside the adapters/out/fal package
 *     (Spring DI wires it via the AvatarGenerator interface)
 *   - No business logic allowed here
 */
@Slf4j
@Component
public class FalAvatarGeneratorAdapter implements AvatarGenerator {

    private static final String AUTH_HEADER_PREFIX = "Key ";

    private final WebClient falWebClient;
    private final WebClient downloadWebClient;
    private final FalProperties falProperties;

    /**
     * Constructor injection — falWebClient is the pre-configured Fal.ai client
     * from FalConfig; downloadWebClient is a vanilla client used to fetch
     * the generated image bytes from the temporary Fal.ai CDN URL.
     */
    public FalAvatarGeneratorAdapter(
            WebClient falWebClient,
            WebClient downloadWebClient,
            FalProperties falProperties
    ) {
        this.falWebClient = falWebClient;
        this.downloadWebClient = downloadWebClient;
        this.falProperties = falProperties;
    }

    /**
     * {@inheritDoc}
     *
     * Implementation detail (hidden from domain):
     *   1. POST to Fal.ai model endpoint with prompt + source image URL
     *   2. Parse the response to extract the generated image URL
     *   3. Download the image bytes from the temporary CDN URL
     *   4. Return as a GeneratedImage value object
     */
    @Override
    public GeneratedImage generate(String sourceImageUrl, String prompt) {
        log.debug("Calling Fal.ai model={} with sourceImageUrl={}", falProperties.model(), sourceImageUrl);

        // Step 1: Submit generation request to Fal.ai
        FalGenerateRequest requestBody = FalGenerateRequest.of(prompt, sourceImageUrl);

        FalGenerateResponse falResponse = falWebClient.post()
                .uri("/" + falProperties.model())
                .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER_PREFIX + falProperties.apiKey())
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class).map(body -> {
                            log.error("Fal.ai 4xx error: status={} body={}", clientResponse.statusCode(), body);
                            return new AvatarGenerationException(
                                    "Fal.ai rejected the request (status=" + clientResponse.statusCode() + "): " + body);
                        })
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(String.class).map(body -> {
                            log.error("Fal.ai 5xx error: status={} body={}", clientResponse.statusCode(), body);
                            return new AvatarGenerationException(
                                    "Fal.ai server error (status=" + clientResponse.statusCode() + ")");
                        })
                )
                .bodyToMono(FalGenerateResponse.class)
                .blockOptional()
                .orElseThrow(() -> new AvatarGenerationException("Fal.ai returned an empty response"));

        FalGenerateResponse.FalImage generatedImage = falResponse.firstImage();
        log.debug("Fal.ai generated image URL: {}", generatedImage.url());

        // Step 2: Download the generated image bytes from the temporary CDN URL
        byte[] imageBytes = downloadImageBytes(generatedImage.url());

        String contentType = resolveContentType(generatedImage.contentType());
        return new GeneratedImage(imageBytes, contentType);
    }

    private byte[] downloadImageBytes(String imageUrl) {
        try {
            return downloadWebClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        throw new AvatarGenerationException(
                                "Failed to download generated image from Fal.ai CDN: status=" + clientResponse.statusCode());
                    })
                    .bodyToMono(byte[].class)
                    .blockOptional()
                    .orElseThrow(() -> new AvatarGenerationException("No image data received from Fal.ai CDN"));
        } catch (AvatarGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new AvatarGenerationException("Unexpected error downloading generated image", e);
        }
    }

    private String resolveContentType(String falContentType) {
        if (falContentType != null && !falContentType.isBlank()) {
            return falContentType;
        }
        return "image/jpeg"; // safe default for Fal.ai responses
    }
}
