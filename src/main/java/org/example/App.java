package org.example;






import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamStreamer;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCSdpType;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class App 
{
    public static void main( String[] args ) throws IOException, URISyntaxException {
        int myId = Integer.parseInt(args[0]);
        new WebRtcController(myId);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String line = bufferedReader.readLine();
        System.out.println(line);
    }
}
