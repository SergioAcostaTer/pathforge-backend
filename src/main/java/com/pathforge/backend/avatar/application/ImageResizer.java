package com.pathforge.backend.avatar.application;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Component;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

@Component
public class ImageResizer {

    private static final int SIZE = 512;

    public ImageData resize(ImageData image) {
        try {
            var out = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(image.data()))
                    .size(SIZE, SIZE)
                    .crop(Positions.CENTER)
                    .outputFormat("jpeg")
                    .toOutputStream(out);
            return new ImageData(out.toByteArray(), "image/jpeg");
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not process image: " + e.getMessage(), e);
        }
    }
}
