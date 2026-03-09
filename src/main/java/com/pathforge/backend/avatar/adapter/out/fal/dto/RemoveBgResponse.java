package com.pathforge.backend.avatar.adapter.out.fal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RemoveBgResponse(BgImage image) {

    public record BgImage(
            String url,
            @JsonProperty("content_type") String contentType
    ) {}
}
