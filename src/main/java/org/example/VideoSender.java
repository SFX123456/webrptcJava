package org.example;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
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
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class VideoSender {
    public RTCDataChannel rtcDataChannel;
    public VideoViewer videoViewer;
    private Object lock;
    private BufferedImage bufferedImage;
    public VideoSender(RTCDataChannel rtcDataChannel, Object lock) throws IOException {
        this.rtcDataChannel = rtcDataChannel;
        //this.videoViewer = new VideoViewer();
        this.lock = lock;
    }
     public void sendMessages()
     {
         
        Logger.LogMessage("sending images");
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
        try {
            // Start the JAR process
            ProcessBuilder builder = new ProcessBuilder("java", "-jar", "C:\\Users\\woelflenico\\IdeaProjects\\WebcamRecorder\\target\\WebcamRecorder-1.0-SNAPSHOT.jar");
            builder.redirectErrorStream(true); // Redirect stderr to stdout
            Process process = builder.start();

            // Read output from the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Counter for image file names
            int imageCount = 0;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                // Decode Base64 string to image bytes
                byte[] imageBytes = new byte[3];
                try {

                    imageBytes = Base64.getDecoder().decode(line);
                    synchronized (lock) {
                        sendMessage(imageBytes);
                    }
                }
                catch (Exception e) {
                    System.out.println("überspringe line " + line);
                }
                // Write image bytes to file
                /*
                String fileName = "image_" + imageCount + ".jpg";
                Files.write(Paths.get(fileName), imageBytes);
                System.out.println("Saved image: " + fileName);
*/
                // Increment image counter
                imageCount++;

                // You can add further logic or conditions for stopping the process
                // For example, process.destroy() or break; if certain conditions are met
            }

            // Close the reader and wait for the process to exit
            reader.close();
            process.waitFor();

            // Print exit value
            System.out.println("Process exited with code " + process.exitValue());

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendMessage(byte[] bytes) throws Exception {
        ByteBuffer sendBuffer = ByteBuffer.allocate(bytes.length);
        sendBuffer.put(bytes);
        rtcDataChannel.send(new RTCDataChannelBuffer(sendBuffer,false));
    }

 
    
}
