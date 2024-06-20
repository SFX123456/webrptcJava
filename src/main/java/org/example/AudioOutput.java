package org.example;

import javax.sound.sampled.*;
import java.util.ArrayList;

public class AudioOutput {
    private int bitsPerSample;
    private int sampleRate;
    private int channels;
    private SourceDataLine line;
    private static ArrayList<AudioOutput> outputs = new ArrayList<>();

    public static AudioOutput GetAudioOutput(int bitsPerSample, int sampleRate, int channels) throws LineUnavailableException {
        var res = outputs.stream().filter(x -> (x.bitsPerSample == bitsPerSample && x.sampleRate == sampleRate && x.channels == channels ) );
        AudioOutput output;
        if (res.count() == 0)  {
           
            output = new AudioOutput(bitsPerSample, sampleRate, channels);
            outputs.add(output);
        }
        else {
            output = res.toList().getFirst();
        }
        
        return output;
    }

    private AudioOutput(int bitsPerSample, int sampleRate, int channels) throws LineUnavailableException {
        this.bitsPerSample = bitsPerSample;
        this.sampleRate = sampleRate;
        this.channels = channels;

        AudioFormat format = new AudioFormat(sampleRate, bitsPerSample, channels, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start(); 
        
    }
    
    public void OnNewAudioData(byte[] audioData)
    {
        Logger.LogMessage("giving out data");
        line.write(audioData, 0, audioData.length);
    }
}
