package org.example;

import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaStream;
import dev.onvoid.webrtc.media.audio.AudioOptions;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.audio.AudioTrackSource;
import dev.onvoid.webrtc.media.video.VideoDevice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class WebRtcWrapper {
    public WebRtcClient webRtcClient;
    public PeerConnectionFactory peerConnectionFactory;
    public RTCConfiguration rtcConfiguration;
    public RTCPeerConnection rtcPeerConnection;
    public OwnAudio ownAudio;
   public HashMap<String, RTCSdpType> stringRTCSdpTypeHashMap = new HashMap<>(); 
   
   public RTCRtpTransceiver rtcRtpTransceiver;
   public WebRtcDataChannelHandler webRtcDataChannelHandler;
    
    private OwnAudio getOwnAudio() {
        AudioOptions audioOptions = new AudioOptions();
        AudioTrackSource audioTrackSource = peerConnectionFactory.createAudioSource(audioOptions);
        
        AudioTrack audioTrack =  peerConnectionFactory.createAudioTrack("audio",audioTrackSource);
        ArrayList<String> g = new ArrayList<>();
        g.add("audio");
        return new OwnAudio(audioTrack, g);
    }
    private RTCConfiguration getRtcConfiguration() {
        RTCConfiguration f = new RTCConfiguration();
        RTCIceServer rtcIceServer = new RTCIceServer();
        rtcIceServer.urls.add("stun:stun.stunprotocol.org:3478");
        rtcIceServer.urls.add("stun:stun.l.google.com:19302");
        f.iceServers.add(rtcIceServer);
        return f;
    }
    
    private RTCPeerConnection getRtcPeerConnection() {
        return peerConnectionFactory.createPeerConnection(rtcConfiguration, new WebRtcPeerConnectionHandler(webRtcClient));
    }
    public void startOfferSending(String userID)
    {
        sendOffer(userID);
    }
    
    private void sendOffer(String userId) {
        RTCOfferOptions rtcOfferOptions = new RTCOfferOptions();
        rtcPeerConnection.createOffer(rtcOfferOptions, new CreateSessionDescriptionObserver() {
            @Override
            public void onSuccess(RTCSessionDescription rtcSessionDescription) {
                System.out.println("was suiccs");
                rtcPeerConnection.setLocalDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                        System.out.println("set suc");
                        webRtcClient.OnSuccessfullyCreatedOffer(rtcSessionDescription.sdp,rtcSessionDescription.sdpType.name(), userId );
                        /*
                        jsonObject.put("sdp", rtcSessionDescription.sdp);
                        jsonObject.put("type", rtcSessionDescription.sdpType.name());
                        System.out.println("search");
                        System.out.println(jsonObject);
                        
                         */
                        //rtcSessionDescription2[0] = jsonObject.toString();
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
    }
    public WebRtcWrapper(WebRtcClient webRtcClient) throws IOException {
        this.webRtcClient = webRtcClient;
        this.peerConnectionFactory = new PeerConnectionFactory();
        this.rtcConfiguration = getRtcConfiguration();
        this.rtcPeerConnection = getRtcPeerConnection();
        this.ownAudio = getOwnAudio();
        
        for (int i = 0; i < RTCSdpType.values().length; i++) {
            RTCSdpType x = RTCSdpType.values()[i];
            stringRTCSdpTypeHashMap.put(x.name(),x);
        }
        System.out.println("WebRtcWrapper initiated");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        if (!line.equals("Y")) return;
        System.out.println("adding track");
        //rtcPeerConnection.addTrack(ownAudio.audioTrack, ownAudio.list);
        RTCDataChannel dataChannel = rtcPeerConnection.createDataChannel("sendDataChannel", new RTCDataChannelInit());
        webRtcDataChannelHandler = new WebRtcDataChannelHandler(dataChannel);
        rtcRtpTransceiver = rtcPeerConnection.addTransceiver(ownAudio.audioTrack, new RTCRtpTransceiverInit());
       
    }
    
    
    public void handleNewReceivedOffer(String sdp, String type, String userID) {
        RTCSessionDescription rtcSessionDescription = new RTCSessionDescription(stringRTCSdpTypeHashMap.get(type), sdp);
        rtcPeerConnection.setRemoteDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
            @Override
            public void onSuccess() {
                System.out.println("client B set remote desc succ");

            }

            @Override
            public void onFailure(String s) {
                System.out.println("client b not set remote desc succ");
            }
        });
        rtcPeerConnection.createAnswer(new RTCAnswerOptions(), new CreateSessionDescriptionObserver() {
            @Override
            public void onSuccess(RTCSessionDescription rtcSessionDescription) {
                System.out.println("anxwered");
                
                rtcPeerConnection.setLocalDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                        System.out.println("Client B set local desc");
                        webRtcClient.OnSendAnswer(rtcSessionDescription.sdp,String.valueOf(rtcSessionDescription.sdpType),userID);
                    }

                    @Override
                    public void onFailure(String s) {
                        System.out.println("cliebt b not set local desc");
                    }
                });

/*
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
*/


            }

            @Override
            public void onFailure(String s) {
                System.out.println("unable to answer");
            }
        });


    }
    
    public void handleNewIceCandidateForeign(RTCIceCandidate rtcIceCandidate)
    {
        System.out.println("handleNewIceCandidateForeign");
        rtcPeerConnection.addIceCandidate(rtcIceCandidate);
    }
    
}
