package com.pathforge.backend.avatar.adapter.out.fal;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.pathforge.backend.avatar.adapter.out.fal.dto.RemoveBgRequest;
import com.pathforge.backend.avatar.adapter.out.fal.dto.RemoveBgResponse;
import com.pathforge.backend.avatar.application.ImageData;
import com.pathforge.backend.avatar.application.port.out.BackgroundRemover;
import com.pathforge.backend.avatar.config.FalProperties;
import com.pathforge.backend.avatar.domain.exception.AvatarGenerationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FalBackgroundRemoverAdapter implements BackgroundRemover {

    private static final String MODEL = "fal-ai/imageutils/rembg";
    private static final String AUTH_HEADER_PREFIX = "Key ";

    private final RestClient falRestClient;
    private final RestClient downloadRestClient;
    private final FalProperties falProperties;

    public FalBackgroundRemoverAdapter(
            @Qualifier("falRestClient") RestClient falRestClient,
            @Qualifier("downloadRestClient") RestClient downloadRestClient,
            FalProperties falProperties) {
        this.falRestClient = falRestClient;
        this.downloadRestClient = downloadRestClient;
        this.falProperties = falProperties;
    }

    @Override
    public ImageData removeBackground(ImageData image) {
        String dataUri = "data:%s;base64,%s".formatted(
                image.contentType(),
                Base64.getEncoder().encodeToString(image.data()));

        log.info("Removing background via {}", MODEL);

        RemoveBgResponse response = falRestClient.post()
                .uri("/" + MODEL)
                .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER_PREFIX + falProperties.apiKey())
                .body(new RemoveBgRequest(dataUri))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new AvatarGenerationException(
                            "Background removal failed (status=%s)".formatted(res.getStatusCode()));
                })
                .body(RemoveBgResponse.class);

        if (response == null || response.image() == null) {
            throw new AvatarGenerationException("Background remover returned an empty response");
        }

        log.info("Background removed → {}", response.image().url());

        byte[] bytes = downloadRestClient.get()
                .uri(response.image().url())
                .retrieve()
                .body(byte[].class);

        if (bytes == null || bytes.length == 0) {
            throw new AvatarGenerationException("No image data received from background remover");
        }

        var ct = response.image().contentType();
        return new ImageData(bytes, ct != null && !ct.isBlank() ? ct : "image/png");
    }
}
