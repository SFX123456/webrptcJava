package org.example;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class VideoViewer {
  private CanvasFrame canvas;
    private Java2DFrameConverter converter;
    public VideoViewer() {
        canvas = new CanvasFrame("Webcam");
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);


        converter = new Java2DFrameConverter();
    }

    public void OnNewVideoFrame(byte[] bytes) {
        try {
    
            BufferedImage imageFromBytes = byteArrayToBufferedImage(bytes);
            
            canvas.showImage(converter.convert(imageFromBytes));
        } catch (Exception e) {
            Logger.LogError(e.getMessage());
        }
    }

    private static BufferedImage byteArrayToBufferedImage(byte[] imageBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bais);
    }
}
