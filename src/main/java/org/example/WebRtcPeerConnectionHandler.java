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

import static java.util.Objects.isNull;

public class WebRtcPeerConnectionHandler implements PeerConnectionObserver {

    public WebRtcClient webRtcClient;
    private String foreignID = null;
    private VideoViewer videoViewer;
    private boolean first = true;
    private ImageView imageView;

    public WebRtcPeerConnectionHandler(WebRtcClient webRtcClient, String foreignID) {
        this.foreignID = foreignID;
        this.webRtcClient = webRtcClient;
    }

    private AudioPlayer audioPlayer;
    private boolean sendInitialMessage = false;

    private PixelBuffer<ByteBuffer> pixelBuffer;

    private ByteBuffer byteBuffer;
    private Integer integ = 0;

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
                    System.out.println("new videoframe");
                    if (!first) return;
                    integ++;
                    //first = false;
                    VideoFrameBuffer buffer = videoFrame.buffer;
                    int width = buffer.getWidth();
                    int height = buffer.getHeight();

                    
                    var bytes = new byte[width * height * 4];
                    byteBuffer = ByteBuffer.wrap(bytes);
                        
                    System.out.println("help 1");

                    try {
                        VideoBufferConverter.convertFromI420(buffer, byteBuffer, FourCC.ARGB);
                        System.out.println("help 2");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    int[] argbPixels = new int[width * height];
                    for (int i = 0; i < argbPixels.length; i++) {
                        int b = bytes[i * 4] & 0xFF;
                        int g = bytes[i * 4 + 1] & 0xFF;
                        int r = bytes[i * 4 + 2] & 0xFF;
                        int a = bytes[i * 4 + 3] & 0xFF;
                        argbPixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
                    }
                    System.out.println("help 3");

                    try {
                        
                        // Create BufferedImage and set pixel data
                        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        DataBufferInt bufferInt = (DataBufferInt) image.getRaster().getDataBuffer();
                        int[] pixels = bufferInt.getData();
                        System.arraycopy(argbPixels, 0, pixels, 0, argbPixels.length);

                        // Write BufferedImage to PNG file
                        File outputFile = new File("E:\\untitled1\\" + integ + "output.png");
                        
                        System.out.println("help 5");
                        ImageIO.write(image, "PNG", outputFile);
                        
                        // Convert BufferedImage to byte array
                        var baos = new ByteArrayOutputStream();
                        //ImageIO.write(image, "png", baos);
                        //byte[] imageBytes = baos.toByteArray();
                        //videoViewer.OnNewVideoFrame(imageBytes);
                        baos.close();
                        System.out.println("PNG image saved successfully.");
                        image.flush();
                        buffer.release();

                    } catch (IOException e) {
                        System.out.println("Error: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }

                    System.out.println("help 6");
                   
                    
                }
            });
        } else {
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
    private static int[] byteArrayToIntArray(byte[] bytes) {
        int[] result = new int[bytes.length / 4];
        for (int i = 0; i < result.length; i++) {
            int b0 = bytes[i * 4] & 0xFF;
            int b1 = bytes[i * 4 + 1] & 0xFF;
            int b2 = bytes[i * 4 + 2] & 0xFF;
            int b3 = bytes[i * 4 + 3] & 0xFF;
            result[i] = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
        }
        return result;
    }
    private static int clamp(int value) {
        return Math.min(Math.max(value, 0), 255);
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
