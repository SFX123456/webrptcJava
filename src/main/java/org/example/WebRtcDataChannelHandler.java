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
    private TranscriptionViewer transcriptionViewer;
    private final Object lock = new Object();
    
    public WebRtcDataChannelHandler(RTCDataChannel rtcDataChannel, WebRtcClient webRtcClient, String foreignID) {
        this.foreignID = foreignID;
        this.rtcDataChannel = rtcDataChannel;
        this.webRtcClient = webRtcClient;
        this.transcriptionViewer = new TranscriptionViewer();
        registerListener();
    }

    protected void registerListener() {
        rtcDataChannel.registerObserver(new RTCDataChannelObserver() {
            @Override
            public void onBufferedAmountChange(long l) {
               Logger.LogMessage("Buffered Amount changed");
            }

            @Override
            public void onStateChange() {

               Logger.LogMessage("DataChannel state changed: " + rtcDataChannel.getState());
                if (!rtcDataChannel.getState().equals(RTCDataChannelState.OPEN)) return;
                if (webRtcClient.DidSendInitializeMethod(foreignID)) return;
                //webRtcClient.OnNewBroadcastMessageRequested("Hallo welt");
                Logger.LogMessage("should send video messages niow");

                //webRtcClient.OnDataChannelForVideoReady(rtcDataChannel, lock);
                
                webRtcClient.OnDataChannelForTextReady(rtcDataChannel, lock);

                webRtcClient.SentInitializeMessage(foreignID);
            }

            @Override
            public void onMessage(RTCDataChannelBuffer rtcDataChannelBuffer) {
                ByteBuffer buffer = rtcDataChannelBuffer.data;
             
                if (!buffer.hasRemaining()) return;
                
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                
                String receivedMessage = new String(bytes, StandardCharsets.UTF_8);
               transcriptionViewer.OnNewText(receivedMessage);
            }
        });
    }
}
