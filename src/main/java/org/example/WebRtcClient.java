package org.example;

import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCIceCandidate;
import org.example.bean.EventData;
import org.example.bean.RoomInfo;
import org.example.bean.UserBean;

import java.io.IOException;


public interface WebRtcClient {
    public int getID();
    public void OnNewForeignIceCandidate(RTCIceCandidate rtcIceCandidate, String sender);

    public void OnConnectedToServer() throws IOException;

    public void OnGotOffer(String sdp, String type , String userID);

    public void OnNewBroadcastMessageRequested(String message);
    
    public void OnGotAnswer(String sdp, String type , String userID, String sender);
    public void OnHandledAccept(String userID);
    
    public void OnNewAudio(byte[] audioData);

    public void OnSendAnswer(String sdp, String type, String id);

    public void OnNewOwnIceCandidate(String sdp, String sdpMid, int sdpMLineIndex);

    public void OnSuccessfullyCreatedOffer(String sdp, String type, String userID);

    public void OnNewDataChannel(RTCDataChannel rtcDataChannel, String foreignID);
    public void OnConnectedToRoom(RoomInfo roomInfo);
    public void OnSomeoneNewJoined(UserBean userBean);
    public void SentInitializeMessage(String foreignID);
    public boolean DidSendInitializeMethod(String foreignID);
    
}