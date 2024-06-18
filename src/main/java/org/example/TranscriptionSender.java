package org.example;

import com.github.sarxos.webcam.Webcam;
import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCDataChannelBuffer;
import io.github.givimad.whisperjni.WhisperContext;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperJNI;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TranscriptionSender {
    private RTCDataChannel rtcDataChannel;
    private TranscriptionViewer transcriptionViewer;
    private Object lock;
   private String totalRecorded;
    public TranscriptionSender(RTCDataChannel rtcDataChannel, Object lock) throws IOException {
        this.rtcDataChannel = rtcDataChannel;
        this.transcriptionViewer = new TranscriptionViewer();
        this.lock = lock;
        totalRecorded = "";
    }

    public void sendMessages() {
        /*
        Thread t = new Thread(() -> {
            try 
            {
                runLoop();
            } catch (Exception e) 
            {
                Logger.LogMessage("Transcription Error: " + e.getMessage());
            }
        });
        t.start();
        
         */
       
         
    }

    private void runLoop() throws Exception {
        Logger.LogMessage("sartingp rocess");
        ProcessBuilder processBuilder = new ProcessBuilder();
        String currentDirectory = System.getProperty("user.dir");

        // Print the current working directory
        System.out.println("Current working directory: " + currentDirectory);

        // Example command: list directory contents (ls on Unix, dir on Windows)
        processBuilder.command("./src/main/java/org/example/Whisper/stream", "-m", "./src/main/java/org/example/Whisper/models/ggml-tiny.en.bin");

        // Start the process
        Process process = processBuilder.start();

        // Read the output from the command
        readErrorStream(new InputStreamReader(process.getErrorStream()));
        readTransText(new InputStreamReader(process.getInputStream()));
    }
    
    private void readErrorStream(InputStreamReader inputStreamReader)
    {
        Thread c = new Thread(() -> {
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            while (true) {
                try {
                    if (!((line = reader.readLine()) != null)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Logger.LogError("Transcription error: " + line);
            } 
        });
        
        c.start();
    }

    private void readTransText(InputStreamReader inputStreamReader)
    {
        Thread c = new Thread(() -> {
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            while (true) {
                try {
                    if (!((line = reader.readLine()) != null)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Logger.LogMessage("new transcriped text " +line);
                totalRecorded += line;
                try {
                    synchronized (lock) {
                        sendMessage(totalRecorded.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        c.start();
    }
    
    public void sendMessage(byte[] bytes) throws Exception {
        ByteBuffer sendBuffer = ByteBuffer.allocate(bytes.length);
        sendBuffer.put(bytes);
        rtcDataChannel.send(new RTCDataChannelBuffer(sendBuffer,false));
    }

 
    
}
