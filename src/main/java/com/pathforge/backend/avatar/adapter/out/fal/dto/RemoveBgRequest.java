package com.pathforge.backend.avatar.adapter.out.fal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RemoveBgRequest(@JsonProperty("image_url") String imageUrl) {}
