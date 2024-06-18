package org.example;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class VideoViewer extends JPanel {
  //private CanvasFrame canvas;
    private Java2DFrameConverter converter;
    private JFrame frame;
    private JLabel label;
    private BufferedImage image;
    public VideoViewer() {
    //    canvas = new CanvasFrame("Webcam");
      //  canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame = new JFrame("BufferedImage Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Create an instance of BufferedImageDisplay
        frame.add(this);

        // Display the frame
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            // Draw the image
            g.drawImage(image, 0, 0, this);
        }
    }


    public void OnNewVideoFrame(byte[] bytes) {
        /*
        try {
    
            image = byteArrayToBufferedImage(bytes);
            Logger.LogMessage("here 2");
        //    System.out.println(canvas.isActive());
          //  System.out.println(canvas.isEnabled());
            for (int i = 0; i < 10; i++) {
                System.out.println(bytes[i]);
            } 
            //repaint();
         
            
            
             
            //converter = new Java2DFrameConverter();
            //var farme = converter.convert(imageFromBytes);  
            //System.out.println(farme);
            //System.out.println(farme.imageHeight);
            //canvas.showImage(converter.convert(imageFromBytes));
            Logger.LogMessage("here 1");
        } catch (Exception e) {
            Logger.LogError(e.getMessage());
        }
        
         */
    }

    private static BufferedImage byteArrayToBufferedImage(byte[] imageBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bais);
    }
}
