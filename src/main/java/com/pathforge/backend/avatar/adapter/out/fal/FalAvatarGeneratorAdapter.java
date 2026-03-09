package com.pathforge.backend.avatar.adapter.out.fal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import com.pathforge.backend.avatar.adapter.out.fal.dto.FalGenerateRequest;
import com.pathforge.backend.avatar.adapter.out.fal.dto.FalGenerateResponse;
import com.pathforge.backend.avatar.application.ImageData;
import com.pathforge.backend.avatar.application.port.out.AvatarGenerator;
import com.pathforge.backend.avatar.config.FalProperties;
import com.pathforge.backend.avatar.domain.exception.AvatarGenerationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FalAvatarGeneratorAdapter implements AvatarGenerator {

    private static final String AUTH_HEADER_PREFIX = "Key ";

    private final RestClient falRestClient;
    private final RestClient downloadRestClient;
    private final FalProperties falProperties;

    public FalAvatarGeneratorAdapter(
            @Qualifier("falRestClient") RestClient falRestClient,
            @Qualifier("downloadRestClient") RestClient downloadRestClient,
            FalProperties falProperties) {
        this.falRestClient = falRestClient;
        this.downloadRestClient = downloadRestClient;
        this.falProperties = falProperties;
    }

    @Override
    public ImageData generate(String sourceImageUrl, String prompt, String negativePrompt) {
        var request = FalGenerateRequest.of(prompt, sourceImageUrl);
        log.info("""
                Fal.ai request →
                  model          : {}
                  imageUrls      : {}
                  aspectRatio    : {}
                  outputFormat   : {}
                  safetyTolerance: {}
                  prompt         : {}""",
                falProperties.model(), request.imageUrls(),
                request.aspectRatio(), request.outputFormat(), request.safetyTolerance(),
                prompt);

        FalGenerateResponse falResponse = falRestClient.post()
                .uri("/" + falProperties.model())
                .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER_PREFIX + falProperties.apiKey())
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, response) -> {
                    throw new AvatarGenerationException(
                            "Fal.ai rejected the request (status=%s): %s".formatted(response.getStatusCode(), responseBody(response)));
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, response) -> {
                    throw new AvatarGenerationException(
                            "Fal.ai server error (status=%s): %s".formatted(response.getStatusCode(), responseBody(response)));
                })
                .body(FalGenerateResponse.class);

        if (falResponse == null) {
            throw new AvatarGenerationException("Fal.ai returned an empty response");
        }

        var falImage = falResponse.firstImage();
        log.info("Fal.ai response → imageUrl={}, contentType={}, size={}x{}",
                falImage.url(), falImage.contentType(), falImage.width(), falImage.height());

        var ct = falImage.contentType();
        return new ImageData(downloadImageBytes(falImage.url()), ct != null && !ct.isBlank() ? ct : "image/jpeg");
    }

    private byte[] downloadImageBytes(String imageUrl) {
        try {
            byte[] bytes = downloadRestClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, response) -> {
                        throw new AvatarGenerationException(
                                "Failed to download generated image from Fal.ai CDN: status=%s".formatted(response.getStatusCode()));
                    })
                    .body(byte[].class);

            if (bytes == null || bytes.length == 0) {
                throw new AvatarGenerationException("No image data received from Fal.ai CDN");
            }
            return bytes;
        } catch (AvatarGenerationException e) {
            throw e;
        } catch (Exception e) {
            throw new AvatarGenerationException("Unexpected error downloading generated image", e);
        }
    }

    private String responseBody(org.springframework.http.client.ClientHttpResponse response) {
        try {
            return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "<unreadable error body>";
        }
    }
}
