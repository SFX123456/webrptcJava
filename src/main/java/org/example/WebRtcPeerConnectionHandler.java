package org.example;

import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaStream;
import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.audio.AudioTrackSink;
import dev.onvoid.webrtc.media.video.VideoFrame;
import dev.onvoid.webrtc.media.video.VideoTrack;
import dev.onvoid.webrtc.media.video.VideoTrackSink;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class WebRtcPeerConnectionHandler implements PeerConnectionObserver {

    public WebRtcClient webRtcClient;
    public WebRtcPeerConnectionHandler(WebRtcClient webRtcClient)
    {
        this.webRtcClient = webRtcClient;
    }
    
    @Override
    public void onIceCandidate(RTCIceCandidate rtcIceCandidate) {
        System.out.println("got new ice candidate");
        if (rtcIceCandidate == null) return;
        webRtcClient.OnNewOwnIceCandidate(rtcIceCandidate.sdp, rtcIceCandidate.sdpMid, rtcIceCandidate.sdpMLineIndex);
                /*
                JSONObject candidateJson = new JSONObject();
                candidateJson.put("candidate", rtcIceCandidate.sdp);
                candidateJson.put("sdpMid", rtcIceCandidate.sdpMid);
                candidateJson.put("sdpMLineIndex", rtcIceCandidate.sdpMLineIndex);

                String candidateString = candidateJson.toString();
                System.out.println(candidateString);
                candidates.add(candidateString);
               
                 */
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
                    System.out.println("got audio data");
                    System.out.println(bytes.length);
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
        webRtcClient.OnNewDataChannel(dataChannel);
        
    }

    @Override
    public void onSignalingChange(RTCSignalingState state)
    {
        System.out.println("sdklfmsdfijmdsf");
        System.out.println(state);
        System.out.println(state.name());
    }

    @Override
    public void onConnectionChange(RTCPeerConnectionState state)
    {
        System.out.println("sdlfjnsufidn");
    }

    
}
