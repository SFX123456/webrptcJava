package org.example;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MessageSender {
    public WebSocketClient webSocketClient;
    public MessageSender(WebSocketClient webSocketClient)
    {
        this.webSocketClient = webSocketClient;
    }
    
    public void sendNewIceCandidateMessage(String sdpMid, String sdp, int sdpMLineIndex, int idMessageReceiver)
    {
        EventData eventData = new EventData();
        eventData.setEventName("__ice_candidate");
        Map map = eventData.getData();
        map.put("sdpMid", sdpMid);
        map.put("sdp",sdp);
        map.put("sdpMLineIndex", sdpMLineIndex);
        map.put("userID",idMessageReceiver);
        webSocketClient.send(eventData.toString());
    }

    public void joinRoom(String room) {
        EventData eventData = new EventData();
        eventData.setEventName("__join");
        HashMap hashMap = new HashMap<String, String>();
        hashMap.put("room", room);
        hashMap.put("userID",webSocketClient.myId);
        eventData.setData(hashMap);

       sendEventData(eventData);
    }



    public void createNewRoom(String room,int roomSize) {
        System.out.println("shoud creazte new room");
        EventData eventData = new EventData();
        eventData.setEventName("__create");
        HashMap hashMap = new HashMap<String, String>();
        hashMap.put("room", room);
        hashMap.put("userID",webSocketClient.myId);
        hashMap.put("roomSize",roomSize);

        eventData.setData(hashMap);
        sendEventData(eventData);
    }

    private void sendEventData(EventData data) {
        JSONObject object = new JSONObject();
        object.put("eventName", data.getEventName());
        object.put("data", data.getData());
        System.out.println(object);
        webSocketClient.send(object.toString());
    }
    
    public void sendAnswer(String sdp, String type, String id)
    {
        EventData eventData = new EventData();
        eventData.setEventName("__answer");
        HashMap hashMap = new HashMap<String, String>();
        hashMap.put("userID",id);
        hashMap.put("sdp",sdp);
        hashMap.put("type",type);
        eventData.setData(hashMap);
        sendEventData(eventData);
    }

    public void sendNewOffer(String sdp, String type, String i) {
        EventData eventData = new EventData();
        eventData.setEventName("__offer");
        Map map = new HashMap<String, String>();
        map.put("userID",i);
        map.put("sdp",sdp );
        map.put("type",type);
        eventData.setData(map);
       sendEventData(eventData);
    }
}
