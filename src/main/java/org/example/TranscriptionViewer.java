package org.example;

import org.bytedeco.opencv.presets.opencv_core;

public class TranscriptionViewer {
    public void OnNewText(String text)
    {
        Logger.LogMessage(text);
    }
}
