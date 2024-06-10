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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TranscriptionSender {
    private RTCDataChannel rtcDataChannel;
    private TranscriptionViewer transcriptionViewer;
    private Object lock;
    private WhisperContext ctx ;
    private WhisperFullParams params ;
    private WhisperJNI whisperJNI;
    private DataLine.Info dataLineInfo;
    private final static int SAMPLESARRAYSIZE = 1000000;
    private final static int SIZEBYTESSAMPLE = 2;
    private final static int SIZESAMPLES = 16000 * 10;
    private final static long TIMECYCLEAUDIOPROCESSING = 30000;
    private AudioFormat audioFormat;
    public TranscriptionSender(RTCDataChannel rtcDataChannel, Object lock) throws IOException {
        this.rtcDataChannel = rtcDataChannel;
        this.transcriptionViewer = new TranscriptionViewer();
        this.lock = lock;
        
        audioFormat = getAudioFormat();
        
        WhisperJNI.loadLibrary();
        WhisperJNI.setLibraryLogger(null);
        whisperJNI = new WhisperJNI();

        Path path = Paths.get("C:\\Users\\woelflenico\\Downloads\\ggml-tiny.en.bin");
        ctx = whisperJNI.init(path);
        params = new WhisperFullParams();

        dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        if (!AudioSystem.isLineSupported(dataLineInfo)) {
            System.out.println("Line not supported");
            System.exit(0);
        }

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

        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        line.open(audioFormat);
        line.start();

        int audioBufferSize = (int) audioFormat.getSampleRate() * audioFormat.getChannels() * (int) TIMECYCLEAUDIOPROCESSING/1000;
        byte[] audioBytes = new byte[audioBufferSize];
        AudioInputStream isS = new AudioInputStream(line);
        float[] samples = new float[SAMPLESARRAYSIZE];
        int currentindex = 0;
        Boolean[] run = new Boolean[1];
        run[0] = true;
        
        while (true) {
            int nBytesRead = isS.read(audioBytes, 0, line.available());

            if (nBytesRead > 0) {
                int numSamples = nBytesRead / SIZEBYTESSAMPLE;

                for (int i = 0; i < numSamples; i++) {
                    int start = i * SIZEBYTESSAMPLE;
                    int sampleValue = 0;
                    for (int j = 0; j < SIZEBYTESSAMPLE; j++) {
                        sampleValue += (audioBytes[start + j] & 0xFF) << (8 * j);
                    }
                    samples[currentindex] = (float) sampleValue / (float) Math.pow(2, audioFormat.getSampleSizeInBits() - 1);
                    currentindex++;
                    if (currentindex % SIZESAMPLES == 0) {
                        float[] test = samples.clone();
                        Thread.ofVirtual().start(()-> {
                            analyzeAudio(test);

                        });
                        return;
                    }

                }

            }
            synchronized (lock) {
                Logger.LogMessage("send message hallo");
                sendMessage("Hallo".getBytes(StandardCharsets.UTF_8));
            }
            transcriptionViewer.OnNewText("hallo");
            Thread.sleep(100);
        }
    }

    private void analyzeAudio(float[] samples) {
        System.out.println("analyzing audio");

        int result = whisperJNI.full(ctx, params, samples, samples.length);
        if (result != 0) {
            throw new RuntimeException("Transcription failed with code " + result);
        }

        String text = whisperJNI.fullGetSegmentText(ctx, 0);
        System.out.println(text);
        try {
            sendMessage(text.getBytes(StandardCharsets.UTF_8));
            transcriptionViewer.OnNewText(text);
        }
        catch (Exception e) {
            Logger.LogError(e.getMessage());
        }
    }

    public void sendMessage(byte[] bytes) throws Exception {
        ByteBuffer sendBuffer = ByteBuffer.allocate(bytes.length);
        sendBuffer.put(bytes);
        rtcDataChannel.send(new RTCDataChannelBuffer(sendBuffer,false));
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
    }
    
    
}
