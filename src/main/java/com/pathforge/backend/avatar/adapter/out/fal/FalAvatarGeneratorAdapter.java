package com.pathforge.backend.avatar.adapter.out.fal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.pathforge.backend.avatar.adapter.out.fal.dto.FalGenerateRequest;
import com.pathforge.backend.avatar.adapter.out.fal.dto.FalGenerateResponse;
import com.pathforge.backend.avatar.application.AvatarGenerator;
import com.pathforge.backend.avatar.application.ImageData;
import com.pathforge.backend.avatar.config.FalProperties;
import com.pathforge.backend.avatar.domain.exception.AvatarGenerationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FalAvatarGeneratorAdapter implements AvatarGenerator {

    private static final String AUTH_HEADER_PREFIX = "Key ";

    private final WebClient falWebClient;
    private final WebClient downloadWebClient;
    private final FalProperties falProperties;

    public FalAvatarGeneratorAdapter(
            @Qualifier("falWebClient") WebClient falWebClient,
            @Qualifier("downloadWebClient") WebClient downloadWebClient,
            FalProperties falProperties
    ) {
        this.falWebClient = falWebClient;
        this.downloadWebClient = downloadWebClient;
        this.falProperties = falProperties;
    }

    @Override
    public ImageData generate(String sourceImageUrl, String prompt) {
        log.debug("Calling Fal.ai model={}", falProperties.model());

        FalGenerateResponse falResponse = falWebClient.post()
                .uri("/" + falProperties.model())
                .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER_PREFIX + falProperties.apiKey())
                .bodyValue(FalGenerateRequest.of(prompt, sourceImageUrl))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, res ->
                        res.bodyToMono(String.class).map(body -> new AvatarGenerationException(
                                "Fal.ai rejected the request (status=" + res.statusCode() + "): " + body)))
                .onStatus(HttpStatusCode::is5xxServerError, res ->
                        res.bodyToMono(String.class).map(body -> new AvatarGenerationException(
                                "Fal.ai server error (status=" + res.statusCode() + ")")))
                .bodyToMono(FalGenerateResponse.class)
                .blockOptional()
                .orElseThrow(() -> new AvatarGenerationException("Fal.ai returned an empty response"));

        FalGenerateResponse.FalImage falImage = falResponse.firstImage();
        log.debug("Fal.ai generated image URL: {}", falImage.url());

        byte[] imageBytes = downloadImageBytes(falImage.url());
        String contentType = (falImage.contentType() != null && !falImage.contentType().isBlank())
                ? falImage.contentType()
                : "image/jpeg";

        return new ImageData(imageBytes, contentType);
    }

    private byte[] downloadImageBytes(String imageUrl) {
        try {
            return downloadWebClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, res -> {
                        throw new AvatarGenerationException(
                                "Failed to download generated image from Fal.ai CDN: status=" + res.statusCode());
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
}
