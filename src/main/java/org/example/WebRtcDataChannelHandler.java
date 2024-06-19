package org.example;

import dev.onvoid.webrtc.*;
import org.bytedeco.opencv.presets.opencv_core;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class WebRtcDataChannelHandler {
    public RTCDataChannel rtcDataChannel;
    private WebRtcClient webRtcClient;
    private String foreignID;
    private VideoViewer videoViewer;
    private final Object lock = new Object();
    private BufferedImage bufferedImage;

    public WebRtcDataChannelHandler(RTCDataChannel rtcDataChannel, WebRtcClient webRtcClient, String foreignID) {
        this.foreignID = foreignID;
        this.rtcDataChannel = rtcDataChannel;
        this.webRtcClient = webRtcClient;

        registerListener();
    }

    protected void registerListener() {
        rtcDataChannel.registerObserver(new RTCDataChannelObserver() {
            @Override
            public void onBufferedAmountChange(long l) {
                System.out.println("Buffered Amount changed");
            }

            @Override
            public void onStateChange() {

                System.out.println("DataChannel state changed");
                System.out.println(rtcDataChannel.getState());
                if (!rtcDataChannel.getState().equals(RTCDataChannelState.OPEN)) return;
                if (webRtcClient.DidSendInitializeMethod(foreignID)) return;
                //webRtcClient.OnNewBroadcastMessageRequested("Hallo welt");
                Logger.LogMessage("should send video messages niow");

                webRtcClient.OnDataChannelForVideoReady(rtcDataChannel, lock);
                
                webRtcClient.OnDataChannelForTextReady(rtcDataChannel, lock);

                webRtcClient.SentInitializeMessage(foreignID);
            }

            @Override
            public void onMessage(RTCDataChannelBuffer rtcDataChannelBuffer) {
                System.out.println("onMessage new messafe received");
                ByteBuffer buffer = rtcDataChannelBuffer.data;

                System.out.println("New message received");
                System.out.println("Position: " + buffer.position());
                System.out.println("Limit: " + buffer.limit());
                System.out.println("Capacity: " + buffer.capacity());
                Logger.LogMessage("remaining : " + buffer.remaining());

                if (!buffer.hasRemaining()) return;
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);

                if (isJPEG(bytes)) {
                    //videoViewer.OnNewImage(bytes);
                  
                    Logger.LogMessage("it is an jpeg");
                    buffer.clear();
                    return;
                }
                Logger.LogMessage("is is not an jpeg");
                //handle simple message
                Logger.LogMessage(rtcDataChannel.getLabel());
                String receivedMessage = new String(bytes, StandardCharsets.UTF_8);
                System.out.println("Received message: " + receivedMessage);


            }
        });
    }

    public static boolean isJPEG(byte[] bytes) {
        if (bytes.length < 4) {
            return false;
        }

        // Check the JPEG signature
        return (bytes[0] & 0xFF) == 0xFF &&
                (bytes[1] & 0xFF) == 0xD8 &&
                (bytes[2] & 0xFF) == 0xFF &&
                ((bytes[3] & 0xFF) == 0xE0 || (bytes[3] & 0xFF) == 0xE1);
    }
}
