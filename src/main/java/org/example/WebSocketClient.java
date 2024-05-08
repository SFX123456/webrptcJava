package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


import dev.onvoid.webrtc.RTCIceCandidate;
import org.example.bean.RoomInfo;
import org.example.bean.UserBean;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;


import java.io.IOException;
import java.lang.invoke.StringConcatFactory;
import java.net.URI;


import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
    private WebRtcClient webRtcClient;
    public WebSocketClient(URI serverUri,int id, WebRtcClient webRtcClientMessageSender) {
        
        super(serverUri);
        myId = String.valueOf(id);
        webRtcClient = webRtcClientMessageSender;
    }

    private static Gson gson = new Gson();
    public String myId;
    

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        
    }

    @Override
    public void onMessage(String s) {
        System.out.println("got message");
        System.out.println(s);
        try {
            
            handleMessage(s);
        }catch (Exception e) {

        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("onclose");
    }

    @Override
    public void onError(Exception e) {
        System.out.println("onerror");
    }
    
    private void handleMessage(String message) throws IOException {
        EventData data;
        try {
            data = gson.fromJson(message, EventData.class);
        } catch (JsonSyntaxException e) {
            System.out.println("json解析错误：" + message);
            return;
        }
        switch (data.getEventName()) {
            case "__login_success":
                webRtcClient.OnConnectedToServer();
                break;      
            case "__icecandidate":
                handleNewIceClient(message,data);
                break;
            case "__suc_joined":
                handleJoinedRoom(message,data);
                break;
            case "__offer":
                handleNewOffer(message,data);
                break;

            case "__answer":
                handleNewAnswer(message,data);
                break;
            case "__new_joined":
                handleNewPersonJoined(message,data);
                break;
            default:
                System.out.println("got new method " + data.getEventName());
                break;
        }

    }

    private void handleNewAnswer(String message, EventData data) {
        Map map = data.getData();
        String sdp = (String)map.get("sdp");
        String type = (String)map.get("type");
        String userID = (String)map.get("userID");
        webRtcClient.OnGotAnswer(sdp,type,userID);
    }

    private void handleNewPersonJoined(String message, EventData data)
    {
        System.out.println("handlenewpersonjoined");
        Map map = data.getData();
        String userId = (String) map.get("userID");
        
        webRtcClient.OnSomeoneNewJoined(new UserBean(userId,"dfs"));
    }

    private void handleJoinedRoom(String message, EventData data) {
        System.out.println("handlejoinroom,");
        Map map = data.getData();
         int size = (int) Double.parseDouble(String.valueOf(map.get("roomSize")));
        String connections = (String)map.get("connections");
        String[] users = connections.split(",");
        RoomInfo roomInfo = new RoomInfo();
        roomInfo.setMaxSize(size);
        CopyOnWriteArrayList<UserBean> userBeans = new CopyOnWriteArrayList<>();
        if (!connections.equals("")) {
            for (String user : users) {
                userBeans.add(new UserBean(user,"sdf"));
            }
        }
        roomInfo.setUserBeans(userBeans);
        webRtcClient.OnConnectedToRoom(roomInfo);
    }

    private void handleNewOffer(String message, EventData data) {
        System.out.println("got new offer");
        Map map = data.getData();
        String type = (String) map.get("type");
        String sdp = (String) map.get("sdp");
        String id = (String) map.get("userID"); 
        String sendbackto = (String) map.get("sendbackto");
        webRtcClient.OnGotOffer(sdp,type, sendbackto);
        
    }

    private void handleNewIceClient(String message, EventData data) {
        Map hashMap = data.getData();
        String sdpMid = (String) hashMap.get("sdpMid");
        String sdp = (String) hashMap.get("sdp");
        int sdpMLineIndex = (int) hashMap.get("sdpMLineIndex");
        String sender = (String) hashMap.get("sender");
        webRtcClient.OnNewForeignIceCandidate(new RTCIceCandidate(sdpMid,sdpMLineIndex,sdp),sender);
    }
    
}
