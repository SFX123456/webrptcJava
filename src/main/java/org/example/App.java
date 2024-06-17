package org.example;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import javax.imageio.ImageIO;
import java.io.*;
import java.net.URISyntaxException;

public class App 
{
    public static void main( String[] args ) throws IOException, URISyntaxException, InterruptedException {
        
        int myId = Integer.parseInt(args[0]);
        new WebRtcController(myId, "hello world");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String line = bufferedReader.readLine();
        System.out.println(line);
       
    }
}
