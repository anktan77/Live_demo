package com.example.framework.framework.modules.producers;

import com.example.capture.video.camera.VideoCaptureFrame;

public interface IVideoProducer {
    void connectChannel(int channelId);
    void pushVideoFrame(VideoCaptureFrame frame);
    void disconnect();
}
