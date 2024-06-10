package org.example;

import com.github.sarxos.webcam.Webcam;
import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCDataChannelBuffer;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TranscriptionSender {
    private RTCDataChannel rtcDataChannel;
    private TranscriptionViewer transcriptionViewer;
    private Object lock;

    public TranscriptionSender(RTCDataChannel rtcDataChannel, Object lock) throws IOException {
        this.rtcDataChannel = rtcDataChannel;
        this.transcriptionViewer = new TranscriptionViewer();
        this.lock = lock;
    }

    public void sendMessages() {
        Thread t = new Thread(() -> {
            try {
                runLoop();
            } catch (Exception e) {

                Logger.LogMessage("VideoSender Error: " + e.getMessage());
            }
        });
        t.start();
    }

    private void runLoop() throws Exception {
        while (true) {
            synchronized (lock) {
                Logger.LogMessage("send message hallo");
                sendMessage("Hallo".getBytes(StandardCharsets.UTF_8));
            }
            transcriptionViewer.OnNewText("hallo");
            Thread.sleep(100);
        }
    }

    public void sendMessage(byte[] bytes) throws Exception {
        ByteBuffer sendBuffer = ByteBuffer.allocate(bytes.length);
        sendBuffer.put(bytes);
        rtcDataChannel.send(new RTCDataChannelBuffer(sendBuffer,false));
    }
}
