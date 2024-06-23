package org.example;

import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaDevices;
import dev.onvoid.webrtc.media.MediaStream;
import dev.onvoid.webrtc.media.audio.AudioDevice;
import dev.onvoid.webrtc.media.audio.AudioOptions;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.audio.AudioTrackSource;
import dev.onvoid.webrtc.media.video.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WebRtcWrapper {
    public WebRtcClient webRtcClient;
    public PeerConnectionFactory peerConnectionFactory;
    public RTCConfiguration rtcConfiguration;
    public RTCPeerConnection rtcPeerConnection;
    public OwnAudio ownAudio;
   public HashMap<String, RTCSdpType> stringRTCSdpTypeHashMap = new HashMap<>(); 
   
   public RTCRtpTransceiver rtcRtpTransceiver;
   public boolean sentInitializeMethod = false;
   public String userResponsibleFor = null;
    private VideoDeviceSource videoSource;
    private VideoTrack videoTrack;
    
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
        return peerConnectionFactory.createPeerConnection(rtcConfiguration, new WebRtcPeerConnectionHandler(webRtcClient, userResponsibleFor));
    }
    public void startOfferSending(String userID)
    {
        Logger.LogMessage("sending offer");
        sendOffer(userID);
    }
    public void handleNewAccept(String sdp, String type, String id)
    {
        Logger.LogMessage("handle new accept");
        RTCSessionDescription rtcSessionDescription = new RTCSessionDescription(stringRTCSdpTypeHashMap.get(type), sdp);
        System.out.println(rtcPeerConnection.getConnectionState());
        rtcPeerConnection.setRemoteDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
            
            @Override
            public void onSuccess() {
                Logger.LogMessage("client A set Remote desc");
                webRtcClient.OnHandledAccept(id);
            }

            @Override
            public void onFailure(String s) {
                Logger.LogError("could not set remote desc " + s + "sdp: " + sdp);
            }
        });
    }
    
    private void sendOffer(String userId) {
        RTCOfferOptions rtcOfferOptions = new RTCOfferOptions();
        rtcPeerConnection.createOffer(rtcOfferOptions, new CreateSessionDescriptionObserver() {
            @Override
            public void onSuccess(RTCSessionDescription rtcSessionDescription) {
                rtcPeerConnection.setLocalDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                        Logger.LogMessage("successfully set local desc");
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
    public WebRtcWrapper(WebRtcClient webRtcClient, String userResponsibleFor, boolean shouldCreateDataChannel)throws IOException {
        this.userResponsibleFor = userResponsibleFor;
        for (int i = 0; i < RTCSdpType.values().length; i++) {
            RTCSdpType x = RTCSdpType.values()[i];
            stringRTCSdpTypeHashMap.put(x.name(),x);
        }
        this.webRtcClient = webRtcClient;
        this.peerConnectionFactory = new PeerConnectionFactory();
        this.rtcConfiguration = getRtcConfiguration();
        this.rtcPeerConnection = getRtcPeerConnection();

        setUpDataChannel("videoandtranscription");
        
        setUpDataToTransport();

        System.out.println("WebRtcWrapper initiated");
       
    }
    public void setUpDataChannel(String channelName)
    {
        System.out.println("setting up datachannel");
        RTCDataChannel dataChannel = rtcPeerConnection.createDataChannel(channelName, new RTCDataChannelInit());
        webRtcClient.OnNewDataChannel(dataChannel, userResponsibleFor); 
    } 
    
    
    
    public void setUpDataToTransport()
    {
        ownAudio = getOwnAudio();
        rtcPeerConnection.addTrack(ownAudio.audioTrack, ownAudio.list);
        rtcRtpTransceiver = rtcPeerConnection.addTransceiver(ownAudio.audioTrack, new RTCRtpTransceiverInit());
        Logger.LogMessage("set audio track");
        
        videoSource = new VideoDeviceSource();
        VideoDevice device = MediaDevices.getVideoCaptureDevices().get(0);
        videoSource.setVideoCaptureDevice(device);
        videoSource.setVideoCaptureCapability(MediaDevices.getVideoCaptureCapabilities(device).get(0)); //I believe index 0 is auto-resolution, 17 is 1280x720 @ 10fps
        videoSource.start();
        videoTrack = peerConnectionFactory.createVideoTrack("CAM", videoSource);
        rtcPeerConnection.addTrack(videoTrack, List.of("stream")); 
    }
    
    
    public void handleNewReceivedOffer(String sdp, String type, String userID) {
        RTCSessionDescription rtcSessionDescription = new RTCSessionDescription(stringRTCSdpTypeHashMap.get(type), sdp);
        rtcPeerConnection.setRemoteDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
            @Override
            public void onSuccess() {
                Logger.LogMessage("client B set remote desc succ");
                rtcPeerConnection.createAnswer(new RTCAnswerOptions(), new CreateSessionDescriptionObserver() {
                    @Override
                    public void onSuccess(RTCSessionDescription rtcSessionDescription) {

                        rtcPeerConnection.setLocalDescription(rtcSessionDescription, new SetSessionDescriptionObserver() {
                            @Override
                            public void onSuccess() {
                                Logger.LogMessage("Client B set local desc");
                                webRtcClient.OnSendAnswer(rtcSessionDescription.sdp, String.valueOf(rtcSessionDescription.sdpType), userID);
                            }

                            @Override
                            public void onFailure(String s) {
                                Logger.LogError("cliebt b not set local desc");
                            }
                        });
                    }

                    @Override
                    public void onFailure(String s) {
                        Logger.LogError("client b not set remote desc succ");
                    }
                });
            }

            @Override
            public void onFailure(String s) {
                Logger.LogError("unable to set remote desc" + s);
            }
        });


    }
    
    public void handleNewIceCandidateForeign(RTCIceCandidate rtcIceCandidate)
    {
        Logger.LogMessage("handleNewIceCandidateForeign");
        rtcPeerConnection.addIceCandidate(rtcIceCandidate);
    }
    
}
