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
import java.nio.file.FileAlreadyExistsException;
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
        System.out.println("sending offer");
        sendOffer(userID);
    }
    public void handleNewAccept(String sdp, String type, String id)
    {
        RTCSessionDescription rtcSessionDescription = new RTCSessionDescription(stringRTCSdpTypeHashMap.get(type), sdp);
        System.out.println(rtcPeerConnection.getConnectionState());
        rtcPeerConnection.setRemoteDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
            
            @Override
            public void onSuccess() {
                System.out.println("client A set Remote desc");
                webRtcClient.OnHandledAccept(id);
            }

            @Override
            public void onFailure(String s) {

                System.out.println("could not set remote desc");
                System.out.println(s);
                System.out.println("sdp " + sdp);
            }
        });
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
                        System.out.println("successfully set local desc");
                        webRtcClient.OnSuccessfullyCreatedOffer(rtcSessionDescription.sdp,rtcSessionDescription.sdpType.name(), userId );
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
        for (int i = 0; i < RTCSdpType.values().length; i++) {
            RTCSdpType x = RTCSdpType.values()[i];
            stringRTCSdpTypeHashMap.put(x.name(),x);
        }
        this.webRtcClient = webRtcClient;
        this.peerConnectionFactory = new PeerConnectionFactory();
        this.rtcConfiguration = getRtcConfiguration();
        this.rtcPeerConnection = getRtcPeerConnection();
        setUpDataToTransport(false,true, String.valueOf(webRtcClient.getID()));
        
        
        System.out.println("WebRtcWrapper initiated");
       
    }
    public void setUpDataChannel(String id)
    {
        if (webRtcClient.getID() != 5) return;

        System.out.println("setting up datachannel");
        RTCDataChannel dataChannel = rtcPeerConnection.createDataChannel("sendDataChannel", new RTCDataChannelInit());
        webRtcClient.OnNewDataChannel(dataChannel); 
    } 
    
    
    
    public void setUpDataToTransport(boolean video, boolean audio,String id)
    {
        if (webRtcClient.getID() != 5) return;
        setUpDataChannel(id);
        if (audio) {
            System.out.println("requested audio");
            ownAudio = getOwnAudio();
            rtcPeerConnection.addTrack(ownAudio.audioTrack, ownAudio.list);
            rtcRtpTransceiver = rtcPeerConnection.addTransceiver(ownAudio.audioTrack, new RTCRtpTransceiverInit());
            System.out.println("set audio track");
        }
        if (video) {
            System.out.println("reuqested video");
        }
   
       
    }
    
    
    public void handleNewReceivedOffer(String sdp, String type, String userID) {
        RTCSessionDescription rtcSessionDescription = new RTCSessionDescription(stringRTCSdpTypeHashMap.get(type), sdp);
        rtcPeerConnection.setRemoteDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
            @Override
            public void onSuccess() {
                System.out.println("client B set remote desc succ");
                rtcPeerConnection.createAnswer(new RTCAnswerOptions(), new CreateSessionDescriptionObserver() {
                    @Override
                    public void onSuccess(RTCSessionDescription rtcSessionDescription) {
                        System.out.println("anxwered");

                        rtcPeerConnection.setLocalDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
                            @Override
                            public void onSuccess() {
                                System.out.println("Client B set local desc");
                                System.out.println("sending message back to " + userID);
                                webRtcClient.OnSendAnswer(rtcSessionDescription.sdp, String.valueOf(rtcSessionDescription.sdpType), userID);
                            }

                            @Override
                            public void onFailure(String s) {
                                System.out.println("cliebt b not set local desc");
                            }
                        });
                    }

                    @Override
                    public void onFailure(String s) {
                        System.out.println("client b not set remote desc succ");
                    }
                });
            }

            @Override
            public void onFailure(String s) {
                System.out.println("unable to set remote desc");
                Logger.LogError(s);
            }


        });


    }
    
    public void handleNewIceCandidateForeign(RTCIceCandidate rtcIceCandidate)
    {
        System.out.println("handleNewIceCandidateForeign");
        rtcPeerConnection.addIceCandidate(rtcIceCandidate);
    }
    
}
