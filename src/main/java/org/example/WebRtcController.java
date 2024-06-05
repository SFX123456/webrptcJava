package org.example;

import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCDataChannelBuffer;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.media.video.VideoFrame;
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebRtcController implements WebRtcClient {
    public static SourceDataLine line = null; 
    public MessageSender messageSender;
    public WebSocketClient webSocketClient;
    private RoomInfo CurrentRoom = null;
    public int myId;
    public ArrayList<WebRtcDataChannelHandler> webRtcDataChannelHandlers = new ArrayList<>();
    final public int MAXROOMSIZE = 5;
    final public String ROOMNAME = "helloworld";
    private static Object object = new Object();
    public HashMap<String,WebRtcWrapper> UserIdToPeerConnection = new HashMap<>();
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
       System.out.println("on someone new joined");
       CurrentRoom.getUserBeans().add(userBean);
       try {
           WebRtcWrapper webRtcWrapper1 = new WebRtcWrapper(this, userBean.getUserId(),true);
           UserIdToPeerConnection.put(userBean.getUserId(),webRtcWrapper1);
           System.out.println("sending offer to " + userBean.getUserId());
           webRtcWrapper1.startOfferSending(userBean.getUserId());
       }
       catch (Exception e) {
            Logger.LogError(e.getMessage());
       }
       Logger.LogMessage("on someone new joined end");
    
   }

    @Override
    public void SentInitializeMessage(String foreignID) {
        UserIdToPeerConnection.get(foreignID).sentInitializeMethod = true;
    }

    @Override
    public boolean DidSendInitializeMethod(String foreignID) {
        return UserIdToPeerConnection.get(foreignID).sentInitializeMethod;
    }

    @Override
    public void OnConnectedToServer() throws IOException {
        System.out.println("Successfully connected to Websocketserver creating lobby");
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
        System.out.println("received offer from " + userID);
        try {
            WebRtcWrapper webRtcWrapper1 = new WebRtcWrapper(this, userID,false);
            UserIdToPeerConnection.put(userID,webRtcWrapper1);
            webRtcWrapper1.handleNewReceivedOffer(sdp,type, userID);
        }
        catch (Exception e) {
            Logger.LogError(e.getMessage());
        }
    }

    @Override
    public void OnGotAnswer(String sdp, String type, String userID, String sender) {
        System.out.println("got answer from " + sender);
        UserIdToPeerConnection.get(sender).handleNewAccept(sdp,type,userID);
    }

    @Override
    public void OnHandledAccept(String userID) {
       //webRtcWrapper.setUpDataToTransport(false,true,userID); 
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
       UserIdToPeerConnection.get(sender).handleNewIceCandidateForeign(rtcIceCandidate);
    }
    
    
    private void setUpAudio() throws LineUnavailableException {
        
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
       
         
    }

    public void OnNewAudio(byte[] audioData) {
        //System.out.println("new audio");
        if (myId != 5) return;
       
        line.write(audioData, 0, audioData.length);
    }

    @Override
    public void OnNewVideoFrame(VideoFrame videoFrame) {
        System.out.println("received new video frame");
    }

    public void OnNewOwnIceCandidate(String sdp, String sdpMid, int sdpMLineIndex )
    {
        System.out.println("broadcast message");
         CopyOnWriteArrayList<UserBean> users = CurrentRoom.getUserBeans();   
         users.forEach(userBean -> {
             System.out.println("broadcast new ice candidate message to " + userBean.getUserId());
             messageSender.sendNewIceCandidateMessage(sdpMid,sdp, String.valueOf(sdpMLineIndex),userBean.getUserId(), String.valueOf(myId));
         });
    }

    @Override
    public void OnSuccessfullyCreatedOffer(String sdp, String type, String id) {
        System.out.println("succes created offer");
        if (CurrentRoom == null) return;
      
        messageSender.sendNewOffer(sdp,type,id);
    }
    
    @Override
    public void OnNewDataChannel(RTCDataChannel rtcDataChannel, String foreignID)
    {
        System.out.println("new data channel");
        webRtcDataChannelHandlers.add( new WebRtcDataChannelHandler(rtcDataChannel,this,foreignID));
    }
    
    
    @Override
    public void OnNewBroadcastMessageRequested(String message)
    {
        System.out.println("new broadcast message req");
        ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
        sendBuffer.put(message.getBytes(StandardCharsets.UTF_8));
        System.out.println("broadcasting message" + webRtcDataChannelHandlers.size());
        sendBuffer.flip();
        webRtcDataChannelHandlers.forEach(handler -> {
            try {
                System.out.println("sending on one webrtc data handler " + message);
                synchronized (object) {
                    handler.rtcDataChannel.send(new RTCDataChannelBuffer(sendBuffer,false));
                    System.out.println("sent");
                }
            } catch (Exception e) {
                System.out.println("unable to send message");
            }
        });
    }

    
}
