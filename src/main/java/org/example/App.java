package org.example;










import org.bytedeco.javacv.*;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.util.HashMap;

public class App 
{
    public static void main( String[] args ) throws IOException, URISyntaxException, InterruptedException {
     
        new VideoSender(null);
        
        /*
        int myId = Integer.parseInt(args[0]);
        new WebRtcController(myId);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String line = bufferedReader.readLine();
        System.out.println(line);
       
         */
    }

  
}
