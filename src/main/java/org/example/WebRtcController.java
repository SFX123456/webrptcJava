package org.example;

import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCIceCandidate;
import org.example.bean.EventData;
import org.example.bean.RoomInfo;
import org.example.bean.UserBean;

import javax.lang.model.element.VariableElement;
import javax.sound.sampled.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebRtcController implements WebRtcClient {
    public static SourceDataLine line = null; 
    public MessageSender messageSender;
    public WebSocketClient webSocketClient;
    private boolean connected = false;
    private RoomInfo CurrentRoom = null;
    private WebRtcWrapper webRtcWrapper;
    public int myId;
    public ArrayList<WebRtcDataChannelHandler> webRtcDataChannelHandlers = new ArrayList<>();
    final public int MAXROOMSIZE = 5;
    final public String ROOMNAME = "helloworld";
    public HashMap<String,Boolean> userSentOffer = new HashMap<String, Boolean>();
    public WebRtcController(int id) throws URISyntaxException, IOException {
        myId = id;
        webSocketClient = connectToWebSocketServer();
        messageSender = new MessageSender(webSocketClient);
        
        try {
            setUpAudio();
        }
        catch (Exception e) {
            System.out.println("Unable to identify audio output");
        }
    }
    
    
    public WebSocketClient connectToWebSocketServer() throws URISyntaxException {
        int deviceType = 1;
        String websocketUrl = "ws://127.0.0.1:5000/ws/";
        URI uri = new URI(websocketUrl + myId + "/" + deviceType);
        WebSocketClient webSocketClient = new WebSocketClient(uri,myId, this);
        webSocketClient.connect();
        return webSocketClient;
    }
    
   @Override
   public void OnSomeoneNewJoined(UserBean userBean)
   {
       
       CurrentRoom.getUserBeans().add(userBean);
       webRtcWrapper.startOfferSending(userBean.getUserId());
   }
    
    @Override
    public void OnConnectedToServer() throws IOException {
        connected = true;
        System.out.println("Successfully connected to Websocketserver creating lobby");
        webRtcWrapper = new WebRtcWrapper(this);
        if (myId == 5) {
            System.out.println("my id " + myId);
            messageSender.createNewRoom(ROOMNAME,MAXROOMSIZE);
        }
        else 
            messageSender.joinRoom(ROOMNAME);
    }
    
    
    @Override
    public void OnConnectedToRoom(RoomInfo roomInfo)
    {
       CurrentRoom = roomInfo;
       System.out.println("connected to room");
      
    }

    @Override
    public void OnGotOffer(String sdp, String type, String userID ) {
        System.out.println("received offer");
        webRtcWrapper.handleNewReceivedOffer(sdp,type, userID);
    }

    @Override
    public void OnGotAnswer(String sdp, String type, String userID) {
        webRtcWrapper.handleNewAccept(sdp,type,userID);
    }

    @Override
    public void OnHandledAccept(String userID) {
       webRtcWrapper.setUpDataToTransport(false,true,userID); 
    }

    @Override
    public void OnSendAnswer(String sdp, String type, String id)
    {
       messageSender.sendAnswer(sdp,type,id);
    }

    @Override
    public int getID() {
        return myId;
    }

    @Override
    public void OnNewForeignIceCandidate(RTCIceCandidate rtcIceCandidate, String sender) {
       webRtcWrapper.handleNewIceCandidateForeign(rtcIceCandidate);
    }
    
    
    private void setUpAudio() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
    }

    public void OnNewAudio(byte[] audioData) {
        line.write(audioData, 0, audioData.length);
    }
    
    public void OnNewOwnIceCandidate(String sdp, String sdpMid, int sdpMLineIndex )
    {
        System.out.println("broadcast message");
         CopyOnWriteArrayList<UserBean> users = CurrentRoom.getUserBeans();   
         users.forEach(userBean -> {
             System.out.println("broadcast new ice candidate message to " + userBean.getUserId());
             messageSender.sendNewIceCandidateMessage(sdpMid,sdp,sdpMLineIndex,Integer.parseInt(userBean.getUserId()), String.valueOf(myId));
         });
    }

    @Override
    public void OnSuccessfullyCreatedOffer(String sdp, String type, String id) {
        System.out.println("succes created offer");
        if (CurrentRoom == null) return;
        
        messageSender.sendNewOffer(sdp,type,id);
        
    }
    
    @Override
    public void OnNewDataChannel(RTCDataChannel rtcDataChannel)
    {
        webRtcDataChannelHandlers.add( new WebRtcDataChannelHandler(rtcDataChannel));
    }

    
}
