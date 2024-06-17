package org.example;

public class WebRtcCallController {
    private static WebRtcController webRtcController = null;
    public static void call(int id, String roomName ) throws Exception
    {
        if (isInActiveCall())
        {
            throw new Exception("Already active call");
        }
        webRtcController= new WebRtcController(id, roomName);
    }

    public static boolean isInActiveCall()
    {
        return webRtcController != null;
    }
}
