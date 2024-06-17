package org.example;

import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.video.VideoTrack;

import java.util.ArrayList;

public class OwnVideo {
    public VideoTrack videoTrack;
    public ArrayList<String> list;
    public OwnVideo(VideoTrack videoTrack, ArrayList<String> list)
    {
        this.videoTrack = videoTrack;
        this.list = list;
    }
}
