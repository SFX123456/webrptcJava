package org.example;

import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCDataChannelBuffer;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javax.imageio.ImageIO;
import javax.xml.stream.FactoryConfigurationError;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class VideoSender {
    public RTCDataChannel rtcDataChannel;
    public VideoSender(RTCDataChannel rtcDataChannel) throws IOException {
        this.rtcDataChannel = rtcDataChannel;
        Thread thread = new Thread((Runnable) () -> {
            try {
                runLoop();
            } catch (IOException e) {
                Logger.LogMessage("VideoSender Error: " + e.getMessage());
            }
        });
        thread.start();
    }
    
    private void runLoop() throws IOException {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start();

        // Capture one frame
        CanvasFrame canvas = new CanvasFrame("Webcam");
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        while (true) {

            org.bytedeco.javacv.Frame frame = grabber.grab();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage bufferedImage = converter.convert(frame);

            // Convert BufferedImage to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            BufferedImage imageFromBytes = byteArrayToBufferedImage(imageBytes);

            // Create a frame to display the image
            // Display the BufferedImage on the canvas
            canvas.showImage(converter.convert(imageFromBytes));
     
            try {
                Thread.sleep(33);
            } catch (InterruptedException e) {
                Logger.LogMessage("error with thread: " + e.getMessage());
            }
        }

    }

    private static void byteArrayToImage(byte[] imageBytes, String filePath) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        BufferedImage image = ImageIO.read(bais);
        ImageIO.write(image, "jpg", new File(filePath));
    }
    
    
    public void sendMessage(byte[] bytes) throws Exception {
        ByteBuffer sendBuffer = ByteBuffer.allocate(bytes.length);
        sendBuffer.put(bytes);
        rtcDataChannel.send(new RTCDataChannelBuffer(sendBuffer,false));
    }

    private static BufferedImage byteArrayToBufferedImage(byte[] imageBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bais);
    }
    
}
