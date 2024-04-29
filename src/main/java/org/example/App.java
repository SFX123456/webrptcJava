package org.example;

import dev.onvoid.webrtc.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Stack;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        RTCConfiguration f = new RTCConfiguration();
        RTCIceServer rtcIceServer = new RTCIceServer();
        rtcIceServer.urls.add("stun:stun.stunprotocol.org:3478");
        rtcIceServer.urls.add("stun:stun.l.google.com:19302");
        ArrayList<RTCIceCandidate> candidates = new ArrayList<>();
        // Step 4: Create a PeerConnection
        
        RTCPeerConnection connection = new PeerConnectionFactory().createPeerConnection(f, new PeerConnectionObserver() {
            @Override
            public void onIceCandidate(RTCIceCandidate rtcIceCandidate) {
                System.out.println("got new ice candidate");
                if (rtcIceCandidate == null) return;
                candidates.add(rtcIceCandidate);
            }
            
            @Override
            public void onDataChannel(RTCDataChannel dataChannel) {
                System.out.println("got data channelö");
            }
            @Override
            public void onSignalingChange(RTCSignalingState state)
            {
                System.out.println("fisstclient 1");
                System.out.println(state);
                System.out.println(state.name());
            }

            @Override
            public void onConnectionChange(RTCPeerConnectionState state)
            {
                System.out.println("firstclient 2");
            }
            
        });
      
        RTCOfferOptions rtcOfferOptions = new RTCOfferOptions();
        RTCDataChannel rtcDataChannel = connection.createDataChannel("sendDataChannel", new RTCDataChannelInit());
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

                System.out.println("new message client 1");
            }
        });


        final RTCSessionDescription[] rtcSessionDescription2 = {null};
        
        connection.createOffer(rtcOfferOptions, new CreateSessionDescriptionObserver() {
            @Override
            public void onSuccess(RTCSessionDescription rtcSessionDescription) {
                System.out.println("was suiccs");
                connection.setLocalDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                        System.out.println("set suc");
                        rtcSessionDescription2[0] = rtcSessionDescription;
                        
                    }

                    @Override
                    public void onFailure(String s) {
                        System.out.println("not suc");
                    }
                });
            }

            @Override
            public void onFailure(String s) {
                System.out.println("no suc");
            }
        });
        RTCConfiguration fa = new RTCConfiguration();
        RTCIceServer rtcIceServera = new RTCIceServer();
        rtcIceServera.urls.add("stun:stun.stunprotocol.org:3478");
        rtcIceServera.urls.add("stun:stun.l.google.com:19302");

        RTCPeerConnection connectiona = new PeerConnectionFactory().createPeerConnection(fa, new PeerConnectionObserver() {
            @Override
            public void onIceCandidate(RTCIceCandidate rtcIceCandidate) {
                System.out.println("client b got new ice candidate");
                connection.addIceCandidate(rtcIceCandidate);
            }

            @Override
            public void onDataChannel(RTCDataChannel dataChannel) {
                System.out.println("client b got data channel");
                dataChannel.registerObserver(new RTCDataChannelObserver() {
                    @Override
                    public void onBufferedAmountChange(long l) {
                        System.out.println("client b buffered amount change");
                    }

                    @Override
                    public void onStateChange() {

                        System.out.println("client b state chjamghed");
                    }

                    @Override
                    public void onMessage(RTCDataChannelBuffer rtcDataChannelBuffer) {
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

        });
        
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        String line = bf.readLine();
        System.out.println(line);

        
        // Step 4: Create a PeerConnection

       
        
        candidates.forEach(p -> {

            connectiona.addIceCandidate(p);
        });
        
        connectiona.setRemoteDescription(rtcSessionDescription2[0], new SetSessionDescriptionObserver() {
            @Override
            public void onSuccess() {
                System.out.println("client B set remote desc succ");
                
            }

            @Override
            public void onFailure(String s) {
                System.out.println("client b not set remote desc succ");
            }
        });
        connectiona.createAnswer(new RTCAnswerOptions(), new CreateSessionDescriptionObserver() {
            @Override
            public void onSuccess(RTCSessionDescription rtcSessionDescription) {
                System.out.println("anxwered");
                connectiona.setLocalDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                        System.out.println("Client B set local desc");
                    }

                    @Override
                    public void onFailure(String s) {
                        System.out.println("cliebt b not set local desc");
                    }
                });
                
                
                connection.setRemoteDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                            System.out.println("client A set Remote desc");
                    }

                    @Override
                    public void onFailure(String s) {

                        System.out.println("client A not set Remote desc");
                    }
                });
                
                
                
            }

            @Override
            public void onFailure(String s) {
                System.out.println("unable to answer");
            }
        });

      
        
        
        String jsujdf = bf.readLine();
        System.out.println(jsujdf);
        
    }
}
