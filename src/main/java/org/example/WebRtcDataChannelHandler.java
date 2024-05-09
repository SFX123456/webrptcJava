package org.example;

import dev.onvoid.webrtc.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class WebRtcDataChannelHandler {
    public RTCDataChannel rtcDataChannel;
    private WebRtcClient webRtcClient;
    private String foreignID;
    public WebRtcDataChannelHandler(RTCDataChannel rtcDataChannel, WebRtcClient webRtcClient, String foreignID)
    {
        this.foreignID = foreignID;
        this.rtcDataChannel = rtcDataChannel;
        this.webRtcClient = webRtcClient;
       registerListener(); 
    }
    
    protected void registerListener()
    {
       rtcDataChannel.registerObserver(new RTCDataChannelObserver() {
            @Override
            public void onBufferedAmountChange(long l) {
                System.out.println("Buffered Amount changed");
            }

            @Override
            public void onStateChange() {
              
                System.out.println("DataChannel state changed");
                System.out.println(rtcDataChannel.getState());
                if (!rtcDataChannel.getState().equals(RTCDataChannelState.OPEN))return;
                if (webRtcClient.DidSendInitializeMethod(foreignID))return;
                webRtcClient.OnNewBroadcastMessageRequested("Hallo welt");
                
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

               if (buffer.hasRemaining()) {
                   byte[] bytes = new byte[buffer.remaining()];
                   buffer.get(bytes);
                   String receivedMessage = new String(bytes, StandardCharsets.UTF_8);
                   System.out.println("Received message: " + receivedMessage);
               } else {
                   System.out.println("No data to read");
               }

               if (webRtcClient.getID() != 5)return;
               if (!webRtcClient.DidSendInitializeMethod(foreignID)) return;

                Thread thread = new Thread(() -> {
                    while (true) {
                        System.out.println("type in new message to send");
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                        try {
                            String line = bufferedReader.readLine();
                            webRtcClient.OnNewBroadcastMessageRequested(line);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                thread.start();
           }
        });
    }
}
