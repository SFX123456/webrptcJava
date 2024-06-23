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
    private JFrame frame;
    private JLabel label;
    private BufferedImage bufferedImage; 
    public VideoViewer() {
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
        if (bufferedImage != null) {
            // Draw the image
            g.drawImage(bufferedImage, 0, 0, this);
        }
    }

    protected void OnNewVideoFrame2(BufferedImage image)
    {
        bufferedImage = image;
        repaint();
    }
}
