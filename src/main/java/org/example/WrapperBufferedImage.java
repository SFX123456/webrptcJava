package org.example;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public class WrapperBufferedImage implements Serializable {
    public BufferedImage image;
    public WrapperBufferedImage(BufferedImage image) {
        this.image = image;
    }
}
