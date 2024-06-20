package org.example;

import dev.onvoid.webrtc.media.audio.AudioSource;
import dev.onvoid.webrtc.media.audio.AudioTrackSource;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class AudioSourceWrapper implements AudioSource {
    public static SourceDataLine line = null;
    
    @Override
    public int onPlaybackData(byte[] bytes, int i, int i1, int i2, int i3) {
          
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try {

            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return bytes.length;
    }
}
