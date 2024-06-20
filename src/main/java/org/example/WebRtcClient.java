package org.example;

import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.media.video.VideoFrame;
import org.example.bean.EventData;
import org.example.bean.RoomInfo;
import org.example.bean.UserBean;

import java.io.IOException;


public interface WebRtcClient {
    int getID();
    void OnNewForeignIceCandidate(RTCIceCandidate rtcIceCandidate, String sender);

    void OnConnectedToServer() throws IOException;

    void OnGotOffer(String sdp, String type , String userID);

    void OnNewBroadcastMessageRequested(String message);
    
    void OnGotAnswer(String sdp, String type , String userID, String sender);
    void OnHandledAccept(String userID);
    
    void OnNewAudio(byte[] audioData, int bitsPerSample, int sampleRate, int channels);
    void OnDataChannelForTextReady(RTCDataChannel rtcDataChannel, Object lock);
    void OnSendAnswer(String sdp, String type, String id);

    void OnNewOwnIceCandidate(String sdp, String sdpMid, int sdpMLineIndex);

    void OnSuccessfullyCreatedOffer(String sdp, String type, String userID);

    void OnNewDataChannel(RTCDataChannel rtcDataChannel, String foreignID);
    void OnConnectedToRoom(RoomInfo roomInfo);
    void OnSomeoneNewJoined(UserBean userBean);
    void SentInitializeMessage(String foreignID);
    boolean DidSendInitializeMethod(String foreignID);
    
}