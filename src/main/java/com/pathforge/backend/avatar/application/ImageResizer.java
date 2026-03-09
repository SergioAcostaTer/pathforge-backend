package com.pathforge.backend.avatar.application;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Component;

import net.coobird.thumbnailator.Thumbnails;

@Component
public class ImageResizer {

    private static final int MAX_SIZE = 512;

    public ImageData resize(ImageData image) {
        try {
            var out = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(image.data()))
                    .size(MAX_SIZE, MAX_SIZE)
                    .keepAspectRatio(true)
                    .outputFormat("jpeg")
                    .toOutputStream(out);
            return new ImageData(out.toByteArray(), "image/jpeg");
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not process image: " + e.getMessage(), e);
        }
    }
}
