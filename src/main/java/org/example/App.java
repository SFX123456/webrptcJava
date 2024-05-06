package org.example;



import dev.onvoid.webrtc.*;


import dev.onvoid.webrtc.media.MediaDevices;
import dev.onvoid.webrtc.media.MediaStream;
import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.audio.*;
import dev.onvoid.webrtc.media.video.*;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.lang.model.element.VariableElement;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * Hello world!
 *
 */
public class App 
{
    public static SourceDataLine line = null;
    public static void main( String[] args ) throws IOException, LineUnavailableException {
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        HashMap<String, RTCSdpType> stringRTCSdpTypeHashMap = new HashMap<>();
        
        for (int i = 0; i < RTCSdpType.values().length; i++) {
           RTCSdpType x = RTCSdpType.values()[i]; 
           stringRTCSdpTypeHashMap.put(x.name(),x);
        }


        List<VideoDevice> list = MediaDevices.getVideoCaptureDevices();
        
        AudioOptions audioOptions = new AudioOptions();
        AudioTrackSource audioTrackSource = new PeerConnectionFactory().createAudioSource(audioOptions);
        AudioTrack audioTrack = new PeerConnectionFactory().createAudioTrack("audio",audioTrackSource);
     
        /*
        System.out.println("start deg");
        System.out.println(list.size());
        System.out.println(list.get(0));
        VideoDevice camera = list.get(0);
        VideoDeviceSource videoDeviceSource = new VideoDeviceSource();
        videoDeviceSource.setVideoCaptureDevice(camera);
        
        VideoTrack videoTrack = new PeerConnectionFactory().createVideoTrack("hello", videoDeviceSource );
        
         */
        //System.out.println(videoTrack.getId());
     
        
        RTCConfiguration f = new RTCConfiguration();
        RTCIceServer rtcIceServer = new RTCIceServer();
        rtcIceServer.urls.add("stun:stun.stunprotocol.org:3478");
        rtcIceServer.urls.add("stun:stun.l.google.com:19302");
        ArrayList<String> candidates = new ArrayList<>();
        UUID clientAUUID = UUID.randomUUID();
        UUID clientBUUID = UUID.randomUUID();
        System.out.println(clientAUUID);
        System.out.println(clientBUUID);
        // Step 4: Create a PeerConnection
        
        RTCPeerConnection connection = new PeerConnectionFactory().createPeerConnection(f, new PeerConnectionObserver() {
            @Override
            public void onIceCandidate(RTCIceCandidate rtcIceCandidate) {
                System.out.println("got new ice candidate");
                if (rtcIceCandidate == null) return;
                JSONObject candidateJson = new JSONObject();
                candidateJson.put("candidate", rtcIceCandidate.sdp);
                candidateJson.put("sdpMid", rtcIceCandidate.sdpMid);
                candidateJson.put("sdpMLineIndex", rtcIceCandidate.sdpMLineIndex);

                String candidateString = candidateJson.toString();
                System.out.println(candidateString);
                candidates.add(candidateString);
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
            
             @Override
            public void onAddStream(MediaStream stream)
            {
                System.out.println("client 1: add stream");
            }
            @Override
            public void onTrack(RTCRtpTransceiver transceiver)
            {
                System.out.println("client 1: on Track");
            }
            @Override 
            public void onAddTrack(RTCRtpReceiver receiver, MediaStream[] mediaStreams)
            {
                System.out.println("client 1: add track");
            }
            
        });
      
        RTCOfferOptions rtcOfferOptions = new RTCOfferOptions();
        RTCDataChannel rtcDataChannel = connection.createDataChannel("sendDataChannel", new RTCDataChannelInit());
        ArrayList<String> v = new ArrayList<>();
        v.add("videochannel");
       //connection.addTrack(videoTrack, v);
        
        ArrayList<String> g = new ArrayList<>();
        g.add("audio");
        connection.addTrack(audioTrack,g);
        
        /*
        **
         */
        RTCRtpTransceiver transceiver = connection.addTransceiver(audioTrack,new RTCRtpTransceiverInit());
        
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

        
        
        final String[] rtcSessionDescription2 = {null};
        
        connection.createOffer(rtcOfferOptions, new CreateSessionDescriptionObserver() {
            @Override
            public void onSuccess(RTCSessionDescription rtcSessionDescription) {
                System.out.println("was suiccs");
                connection.setLocalDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                        System.out.println("set suc");
                        
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("sdp", rtcSessionDescription.sdp);
                        jsonObject.put("type", rtcSessionDescription.sdpType.name());
                        System.out.println("search");
                        System.out.println(jsonObject);
                        rtcSessionDescription2[0] = jsonObject.toString();
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
            public void onAddStream(MediaStream stream)
            {
                System.out.println("never gets called");
                System.out.println("client 2: add stream");
                VideoTrack[] x = stream.getVideoTracks();
                System.out.println(x.length);
                x[0].addSink(new VideoTrackSink() {
                    @Override
                    public void onVideoFrame(VideoFrame videoFrame) {
                        System.out.println("got new frame");
                        System.out.println(videoFrame.buffer.getHeight());
                        System.out.println(videoFrame.buffer.getWidth());
                    }
                });
                
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
                            playAudio(bytes);
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
            System.out.println("nnnnnnnnnnnnnnnnnnnnnnn");
            System.out.println(p);
            JSONObject candidateJson = new JSONObject(p);
            int sdpMLineIndex = candidateJson.getInt("sdpMLineIndex");
            String candidate = candidateJson.getString("candidate");
            System.out.println("can" + candidate);
            String sdpMid = candidateJson.getString("sdpMid");
            System.out.println(sdpMid);
            System.out.println(sdpMLineIndex);
            RTCIceCandidate rtcIceCandidate = new RTCIceCandidate( sdpMid, sdpMLineIndex, candidate);

            connectiona.addIceCandidate(rtcIceCandidate);
        });
        System.out.println("search2");
        System.out.println(rtcSessionDescription2[0]);
        JSONObject jsonObject = new JSONObject(rtcSessionDescription2[0]);
        String sdp = jsonObject.getString("sdp");
        String sdptype = jsonObject.getString("type");
        RTCSessionDescription rtcSessionDescription = new RTCSessionDescription(stringRTCSdpTypeHashMap.get(sdptype), sdp);
        connectiona.setRemoteDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
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

    public static void playAudio(byte[] audioData) {
    
            // Get default audio output device
            

            // Write audio data to the output line
            line.write(audioData, 0, audioData.length);

            // Close the line when finished
           
       
    }
}
