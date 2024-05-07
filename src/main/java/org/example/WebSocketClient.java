package org.example;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.nio.channels.NonWritableChannelException;
import java.util.HashMap;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
    public WebSocketClient(URI serverUri,int id) {
        
        super(serverUri);
        myId = String.valueOf(id);
    }
    
    private String myId;
    

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("connection opened");
    }

    @Override
    public void onMessage(String s) {
        System.out.println("got message");
        System.out.println(s);
        JSONObject obj = new JSONObject(s);
        if (obj.getString("eventName").equals("__login_success")) {
            System.out.println("got login success");
            createNewRoom("helloworld",5);
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
    
    private void sendEventData(EventData data) {
        JSONObject object = new JSONObject();
        object.put("eventName", data.getEventName());
        object.put("data", data.getData());
       System.out.println(object);
        send(object.toString());
    }
    
    public void createNewRoom(String room,int roomSize) {
        EventData eventData = new EventData();
        eventData.setEventName("__create");
        HashMap hashMap = new HashMap<String, String>();
        hashMap.put("room", room);
        hashMap.put("userID",myId);
        hashMap.put("roomSize",5);

        eventData.setData(hashMap);
        sendEventData(eventData);
    }
    public void joinRoom(String room) {
        EventData eventData = new EventData();
        eventData.setEventName("__join");
        HashMap hashMap = new HashMap<String, String>();
        hashMap.put("room", room);
        hashMap.put("userID",myId);
        eventData.setData(hashMap);
        
        sendEventData(eventData); 
    }
}
