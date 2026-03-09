package com.pathforge.backend.avatar.application.port.out;

import com.pathforge.backend.avatar.application.ImageData;

public interface BackgroundRemover {

    ImageData removeBackground(ImageData image);
}
