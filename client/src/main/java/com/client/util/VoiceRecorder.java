package com.client.util;

import com.client.chatwindow.Listener;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class VoiceRecorder extends VoiceUtil {

    public static void captureAudio() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            Runnable runner = new Runnable() {
                final int bufferSize = (int)format.getSampleRate() * format.getFrameSize();
                final byte[] buffer = new byte[bufferSize];

                public void run() {
                    out = new ByteArrayOutputStream();
                    isRecording = true;
                    try {
                        while (isRecording) {
                            int count = line.read(buffer, 0, buffer.length);
                            if (count > 0) {
                                out.write(buffer, 0, count);
                            }
                        }
                    } finally {
                        try {
                            out.close();
                            out.flush();
                            line.close();
                            line.flush();
                            Listener.sendVoiceMessage(out.toByteArray());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            Thread captureThread = new Thread(runner);
            captureThread.start();
        } catch (LineUnavailableException e) {
            System.err.println("Line unavailable: " );
            e.printStackTrace();
        }
    }
}
