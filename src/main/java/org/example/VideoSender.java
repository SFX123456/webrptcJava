package org.example;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamUtils;
import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCDataChannelBuffer;
import dev.onvoid.webrtc.media.video.VideoCapture;
import dev.onvoid.webrtc.media.video.VideoTrack;
import dev.onvoid.webrtc.media.video.VideoTrackSource;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.presets.opencv_core;

import javax.imageio.ImageIO;
import javax.xml.stream.FactoryConfigurationError;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class VideoSender {
    public RTCDataChannel rtcDataChannel;
    public VideoViewer videoViewer;
    private Object lock;
    private static final String FORMAT = "jpg";

    private static boolean run = true;
    private BufferedImage bufferedImage = null;
    public VideoSender(RTCDataChannel rtcDataChannel, Object lock) throws IOException {
        this.rtcDataChannel = rtcDataChannel;

       // this.videoViewer = new VideoViewer();
        this.lock = lock;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook triggered. Cleaning up resources...");
            run = false;

        }));
    }
     public void sendMessages()
     {
         /*
         Thread thread = new Thread(() ->{
         try{
             runLoop();
         } catch (Exception e) {

             Logger.LogMessage("VideoSender Error: " + e.getMessage());
         } 
         });
         thread.start();
        
        
          */
         
     }
    
    private void runLoop() throws Exception {
        Logger.LogMessage("start webcams");
        List<Webcam> webcams = Webcam.getWebcams();
        for (Webcam webcam : webcams) {
           System.out.println(webcam); 
        }
        Webcam webcam = webcams.get(0);
        webcam.open();
        var loc = webcam.getLock();
        

        ByteArrayOutputStream baos;
        int i = 0;
        while (run) {
            if (bufferedImage != null) {
                bufferedImage.flush();
            }
            bufferedImage = webcam.getImage();
            // Convert BufferedImage to byte array
            baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            System.out.println(base64Image);
            baos.flush();
            try {
                loc.unlock();
                Thread.sleep(33);
                loc.lock();
            } catch (InterruptedException e) {

            }
            
        }
        webcam.close();

        System.exit(0);
    }
    
    public void sendMessage(byte[] bytes) throws Exception {
        ByteBuffer sendBuffer = ByteBuffer.allocate(bytes.length);
        sendBuffer.put(bytes);
        rtcDataChannel.send(new RTCDataChannelBuffer(sendBuffer,false));
    }

 
    
}
