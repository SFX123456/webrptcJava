package org.example;

import com.github.sarxos.webcam.Webcam;
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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class VideoSender {
    public RTCDataChannel rtcDataChannel;
    public VideoViewer videoViewer;
    private Object lock;
    public VideoSender(RTCDataChannel rtcDataChannel, Object lock) throws IOException {
        this.rtcDataChannel = rtcDataChannel;
        this.videoViewer = new VideoViewer();
        this.lock = lock;
    }
     public void sendMessages()
     {
         
         Thread t = new Thread(() ->  {
            try{
                 runLoop();
             } catch (Exception e) {

                 Logger.LogMessage("VideoSender Error: " + e.getMessage());
             }
         });
         t.start();
     }
    
    private void runLoop() throws Exception {
        List<Webcam> webcams = Webcam.getWebcams();

        if (webcams.isEmpty()) {
            System.out.println("No webcams found.");
        } else {
            for (int i = 0; i < webcams.size(); i++) {
                System.out.println("Webcam " + i + ": " + webcams.get(i).getName());
            }
        }
        Webcam webcam = webcams.get(0);
        webcam.open();


        while (true) {
            BufferedImage image = webcam.getImage();
            // Convert BufferedImage to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            synchronized (lock) {
                sendMessage(imageBytes);
            }
            videoViewer.OnNewVideoFrame(imageBytes);

            try {
                Thread.sleep(33);
            } catch (InterruptedException e) {
                Logger.LogMessage("error with thread: " + e.getMessage());
            }
        }
    }
    
    public void sendMessage(byte[] bytes) throws Exception {
        ByteBuffer sendBuffer = ByteBuffer.allocate(bytes.length);
        sendBuffer.put(bytes);
        rtcDataChannel.send(new RTCDataChannelBuffer(sendBuffer,false));
    }

 
    
}
