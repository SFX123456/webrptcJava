package org.example;

import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCDataChannelBuffer;
import dev.onvoid.webrtc.RTCDataChannelObserver;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class WebRtcDataChannelHandler {
    private RTCDataChannel rtcDataChannel;
    public WebRtcDataChannelHandler(RTCDataChannel rtcDataChannel)
    {
        this.rtcDataChannel = rtcDataChannel;
       registerListener(); 
    }
    
    protected void registerListener()
    {
       rtcDataChannel.registerObserver(new RTCDataChannelObserver() {
            @Override
            public void onBufferedAmountChange(long l) {
                System.out.println("lllllllllllllllllllllll");
            }

            @Override
            public void onStateChange() {

                System.out.println("lllllllllllllllllllllll");
                ByteBuffer sendBuffer = ByteBuffer.allocate(1024);

// Put some data into the buffer. For example, a simple string.
                String message = "Hello, WebRTC!";
                sendBuffer.put(message.getBytes(StandardCharsets.UTF_8));

// Prepare the buffer for reading or sending by flipping it
                sendBuffer.flip();
                System.out.println("Sending buffer");
                System.out.println("Position: " + sendBuffer.position());
                System.out.println("Limit: " + sendBuffer.limit());
                System.out.println("Capacity: " + sendBuffer.capacity());
                try {
                    rtcDataChannel.send(new RTCDataChannelBuffer(sendBuffer,false));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
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
           }
        });
    }
}
