package org.example;

import dev.onvoid.webrtc.media.audio.AudioTrack;

import java.util.ArrayList;

public class OwnAudio {
    public AudioTrack audioTrack;
    public ArrayList<String> list;
    public OwnAudio(AudioTrack audioTrack, ArrayList<String> list)
    {
        this.audioTrack = audioTrack;
        this.list = list;
    }
}
