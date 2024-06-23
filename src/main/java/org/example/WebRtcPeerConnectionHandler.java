package org.example;

import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.FourCC;
import dev.onvoid.webrtc.media.MediaStream;
import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.MediaStreamTrackEndedListener;
import dev.onvoid.webrtc.media.audio.AudioPlayer;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.audio.AudioTrackSink;
import dev.onvoid.webrtc.media.video.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import org.java_websocket.handshake.ClientHandshake;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.isNull;

public class WebRtcPeerConnectionHandler implements PeerConnectionObserver {

    public WebRtcClient webRtcClient;
    private String foreignID = null;
    private VideoViewer videoViewer;
   
    private ArrayList<VideoFrameBuffer> videoFrames;

    public WebRtcPeerConnectionHandler(WebRtcClient webRtcClient, String foreignID) {
        this.foreignID = foreignID;
        this.webRtcClient = webRtcClient;
        videoFrames = new ArrayList<>();
    }
    private ByteBuffer byteBuffer;
 
    private Integer lastHeight = null;
    private Integer lastWidth = null;
    private int[] argPixel;
    private BufferedImage bufferedImage;
    private int[] pixels;
    private DataBufferInt bufferInt;

    @Override
    public void onIceCandidate(RTCIceCandidate rtcIceCandidate) {
        System.out.println("got new ice candidate");
        if (rtcIceCandidate == null) return;
        webRtcClient.OnNewOwnIceCandidate(rtcIceCandidate.sdp, rtcIceCandidate.sdpMid, rtcIceCandidate.sdpMLineIndex);
    }

    @Override
    public void onTrack(RTCRtpTransceiver transceiver) {
        System.out.println("client 2: on Track");
        MediaStreamTrack typ = transceiver.getReceiver().getTrack();
        System.out.println(typ.getKind());
        if (typ.getKind().equals(MediaStreamTrack.VIDEO_TRACK_KIND)) {
            videoViewer = new VideoViewer();
            System.out.println("is videotrackl");
            if (webRtcClient.getID() == 5) return;
            VideoTrack videoTrack1 = (VideoTrack) typ;
            typ.addTrackEndedListener(new MediaStreamTrackEndedListener() {
                @Override
                public void onTrackEnd(MediaStreamTrack mediaStreamTrack) {
                    System.out.println("video ended");
                }
            });
            
            videoTrack1.addSink(new VideoTrackSink() {
                @Override
                public void onVideoFrame(VideoFrame videoFrame) {
                    VideoFrameBuffer buffer = videoFrame.buffer;
                    videoFrames.add(buffer);
                    dealWithFrame(buffer);
                }
            });
        } else {
            System.out.println("its an audiotrack");

            AudioTrack audioTrack = (AudioTrack) typ;

            audioTrack.addSink(new org.example.AudioPlayer());
            
        }
    }
    
    private CompletableFuture<Boolean> dealWithFrame(VideoFrameBuffer buffer) {
        int width = buffer.getWidth();
        int height = buffer.getHeight();
        if (byteBuffer == null || width != lastWidth || height != lastHeight) {
            lastWidth = width;
            lastHeight = height;
            byteBuffer = ByteBuffer.wrap(new byte[width * height * 4]);
            argPixel = new int[width * height];
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            bufferInt = (DataBufferInt) bufferedImage.getRaster().getDataBuffer();
            pixels = bufferInt.getData();
        }
        
        try {
            VideoBufferConverter.convertFromI420(buffer, byteBuffer, FourCC.ARGB);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        for (int i = 0; i < argPixel.length; i++) {
            int b = byteBuffer.get(i * 4) & 0xFF;
            int g = byteBuffer.get(i * 4 + 1) & 0xFF;
            int r = byteBuffer.get(i * 4 + 2) & 0xFF;
            int a = byteBuffer.get(i * 4 + 3) & 0xFF;
            argPixel[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        System.arraycopy(argPixel, 0, pixels, 0, argPixel.length);

        try {
            videoViewer.OnNewVideoFrame2(bufferedImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return CompletableFuture.supplyAsync(() -> true);
    }

    @Override
    public void onAddTrack(RTCRtpReceiver receiver, MediaStream[] mediaStreams) {
        System.out.println("client 2: onaddtrack");
        System.out.println(mediaStreams.length);

    }

    @Override
    public void onDataChannel(RTCDataChannel dataChannel) {
        System.out.println("client b got data channel");
        System.out.println("never gets called");
        webRtcClient.OnNewDataChannel(dataChannel, foreignID);

    }

    @Override
    public void onSignalingChange(RTCSignalingState state) {
        System.out.println("Signaling changed");
        System.out.println(state);
        System.out.println(state.name());

    }

    @Override
    public void onConnectionChange(RTCPeerConnectionState state) {
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
