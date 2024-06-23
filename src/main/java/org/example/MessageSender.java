package org.example;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class MessageSender {
    public WebSocketClient webSocketClient;
    public static Object object = new Object();
    public MessageSender(WebSocketClient webSocketClient)
    {
        this.webSocketClient = webSocketClient;
    }
    
    public void sendNewIceCandidateMessage(String sdpMid, String sdp, String sdpMLineIndex, String idMessageReceiver, String sender)
    {
        Logger.LogMessage("send new ice candidare message");
        EventData eventData = new EventData();
        eventData.setEventName("__ice_candidate");
        HashMap map = new HashMap<String,String>();
        map.put("sdpMid", sdpMid);
        map.put("sdp",sdp);
        map.put("sdpMLineIndex", sdpMLineIndex);
        map.put("userID",idMessageReceiver);
        map.put("sender",sender);
        eventData.setData(map);
        sendEventData(eventData);
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

    private void sendEventData(EventData data) {
        JSONObject object = new JSONObject();
        object.put("eventName", data.getEventName());
        object.put("data", data.getData());
        System.out.println(object);
        synchronized (object) {
            webSocketClient.send(object.toString());
        }
    }
    
    public void sendAnswer(String sdp, String type, String id)
    {
        EventData eventData = new EventData();
        eventData.setEventName("__answer");

        HashMap map = new HashMap<String, String>();
        map.put("userID",id);
        map.put("sender",String.valueOf(webSocketClient.webRtcClient.getID()));
        int x = sdp.length();
        String data1 = sdp.substring(0,Math.round(x/3));
        String data2 = sdp.substring(Math.round(x/3));
        map.put("data", data1);
        map.put("type",type);
        map.put("sendbackto", webSocketClient.myId);
        map.put("part", "1");
        eventData.setData(map);
        sendEventData(eventData);
        map.remove("data");
        map.remove("type");
        map.put("data", data2);
        map.remove("part");
        map.put("part", "2");
        sendEventData(eventData);
    }
    
    public void sendNewOffer(String sdp, String type, String i) {
        EventData eventData = new EventData();
        eventData.setEventName("__offer");
        Map map = new HashMap<String, String>();
        map.put("userID",i);
        int x = sdp.length();
        String data1 = sdp.substring(0,Math.round(x/3));
        String data2 = sdp.substring(Math.round(x/3));
        map.put("data", data1);
        map.put("type",type);
        map.put("sendbackto", webSocketClient.myId);
        map.put("part", "1");
        eventData.setData(map);
       sendEventData(eventData);
        map.remove("data");
        map.remove("type");
        map.put("data", data2);
        map.remove("part");
        map.put("part", "2");
        sendEventData(eventData);
    }
}
