package org.example;

import dev.onvoid.webrtc.media.audio.AudioTrackSink;

import javax.sound.sampled.*;

public class AudioPlayer implements AudioTrackSink {

    private SourceDataLine audioLine;

    
    @Override
    public void onData(byte[] data, int bitsPerSample, int sampleRate, int channels, int frames) {
        AudioFormat format = new AudioFormat(sampleRate, bitsPerSample, channels, true, false);
        if (audioLine == null || !audioLine.isOpen() || !audioLine.getFormat().matches(format)) {
            if (audioLine != null && audioLine.isOpen()) {
                audioLine.close();
            }
            try {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                audioLine = (SourceDataLine) AudioSystem.getLine(info);
                audioLine.open(format);
                audioLine.start();
            } catch (LineUnavailableException e) {
                System.out.println("Audio line for playing back is unavailable.");
                e.printStackTrace();
                return;
            }
        }

        audioLine.write(data, 0, frames * channels * (bitsPerSample / 8));
        
    }

    public void close() {
        if (audioLine != null) {
            audioLine.drain();
            audioLine.close();
        }
    }
}