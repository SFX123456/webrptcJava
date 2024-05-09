package org.example;

import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaStream;
import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.audio.AudioPlayer;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.audio.AudioTrackSink;
import dev.onvoid.webrtc.media.video.VideoFrame;
import dev.onvoid.webrtc.media.video.VideoTrack;
import dev.onvoid.webrtc.media.video.VideoTrackSink;
import org.java_websocket.handshake.ClientHandshake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class WebRtcPeerConnectionHandler implements PeerConnectionObserver {

    public WebRtcClient webRtcClient;
    private String foreignID = null;
    public WebRtcPeerConnectionHandler(WebRtcClient webRtcClient, String foreignID)
    {
        this.foreignID = foreignID;
        this.webRtcClient = webRtcClient;
    }
    private AudioPlayer audioPlayer;
    private boolean sendInitialMessage = false;
    @Override
    public void onIceCandidate(RTCIceCandidate rtcIceCandidate) {
        System.out.println("got new ice candidate");
        if (rtcIceCandidate == null) return;
        webRtcClient.OnNewOwnIceCandidate(rtcIceCandidate.sdp, rtcIceCandidate.sdpMid, rtcIceCandidate.sdpMLineIndex);
        
    }


    @Override
    public void onTrack(RTCRtpTransceiver transceiver)
    {
        System.out.println("client 2: on Track");
        MediaStreamTrack typ =  transceiver.getReceiver().getTrack();
        System.out.println(typ.getKind());
        if (typ.getKind().equals(MediaStreamTrack.VIDEO_TRACK_KIND)) {
            System.out.println("is videotrackl");
            VideoTrack videoTrack1 = (VideoTrack) typ;
            videoTrack1.addSink(new VideoTrackSink() {
                @Override
                public void onVideoFrame(VideoFrame videoFrame) {
                    System.out.println("new frame received");
                }
            });
        }
        else {
            System.out.println("its an audiotrack");
            
            AudioTrack audioTrack1 = (AudioTrack) typ;
           
            audioTrack1.addSink(new AudioTrackSink() {
                @Override
                public void onData(byte[] bytes, int i, int i1, int i2, int i3) {
                    //System.out.println("got audio data");
                    //System.out.println(bytes.length);
                    webRtcClient.OnNewAudio(bytes);
                }
            });
            
             

        }
    }
    @Override
    public void onAddTrack(RTCRtpReceiver receiver, MediaStream[] mediaStreams)
    {
        System.out.println("client 2: onaddtrack");
        System.out.println(mediaStreams.length);

    }
    @Override
    public void onDataChannel(RTCDataChannel dataChannel) {
        System.out.println("client b got data channel");
        System.out.println("never gets called");
        webRtcClient.OnNewDataChannel(dataChannel,foreignID);
        
    }

    @Override
    public void onSignalingChange(RTCSignalingState state)
    {
        System.out.println("Signaling changed");
        System.out.println(state);
        System.out.println(state.name());
        
    }

    @Override
    public void onConnectionChange(RTCPeerConnectionState state)
    {
      System.out.println("con changed"); 

    }
    @Override
    public void onIceConnectionChange(RTCIceConnectionState state) {
        Logger.LogMessage("on ice connection change" + state);
        
    }
    @Override
    public void onStandardizedIceConnectionChange(RTCIceConnectionState state) {
        Logger.LogMessage("onstandardizediceconnec" + state);
    }
    @Override
    public void onIceConnectionReceivingChange(boolean receiving) {
        Logger.LogMessage("iceconnectzionreceivincgha" + receiving);
    }

    @Override
    public void onIceGatheringChange(RTCIceGatheringState state) {
        Logger.LogMessage("icegatheringchnage" + state.name());
    }

    public void onIceCandidateError(RTCPeerConnectionIceErrorEvent event) {
        System.out.println("ice candidate erro");
        Logger.LogError(event.getErrorText());
    }
    
}
