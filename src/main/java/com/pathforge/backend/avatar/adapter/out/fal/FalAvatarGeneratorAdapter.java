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
    public ImageData generate(String sourceImageUrl, String prompt) {
        log.debug("Calling Fal.ai model={}", falProperties.model());

        FalGenerateResponse falResponse = falRestClient.post()
                .uri("/" + falProperties.model())
                .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER_PREFIX + falProperties.apiKey())
                .body(FalGenerateRequest.of(prompt, sourceImageUrl))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new AvatarGenerationException(
                            "Fal.ai rejected the request (status=" + response.getStatusCode() + "): "
                                    + responseBody(response));
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new AvatarGenerationException(
                            "Fal.ai server error (status=" + response.getStatusCode() + "): "
                                    + responseBody(response));
                })
                .body(FalGenerateResponse.class);

        if (falResponse == null) {
            throw new AvatarGenerationException("Fal.ai returned an empty response");
        }

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
            byte[] bytes = downloadRestClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new AvatarGenerationException(
                                "Failed to download generated image from Fal.ai CDN: status="
                                        + response.getStatusCode());
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
