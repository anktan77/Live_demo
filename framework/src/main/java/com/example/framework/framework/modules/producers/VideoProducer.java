package com.example.framework.framework.modules.producers;

import android.os.Handler;
import android.util.Log;

import com.example.capture.video.camera.VideoCaptureFrame;
import com.example.capture.video.camera.VideoModule;
import com.example.framework.framework.modules.channels.VideoChannel;

public abstract class VideoProducer implements IVideoProducer {
    private static final String TAG = VideoProducer.class.getSimpleName();

    private VideoChannel videoChannel;
    protected volatile Handler pChannelHandler;

    @Override
    public void connectChannel(int channelId) {
        videoChannel = VideoModule.instance().connectProducer(this, channelId);
        // quan trọng
        pChannelHandler = videoChannel.getHandler();
    }

    @Override
    public void pushVideoFrame(final VideoCaptureFrame frame) {
        if (pChannelHandler == null) {
            return;
        }

        pChannelHandler.post(() -> {
            try {
                // Chụp sử dụng môi trường OpenGL
                // ngữ cảnh để xem trước kết cấu, vì vậy việc chụp
                // luồng và luồng video sử dụng
                // ngữ cảnh OpenGL được chia sẻ.
                // Do đó updateTexImage () là hợp lệ ở đây.
                frame.surfaceTexture.updateTexImage();
                if (frame.textureTransform == null) frame.textureTransform = new float[16];
                frame.surfaceTexture.getTransformMatrix(frame.textureTransform);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if (videoChannel != null) {
                videoChannel.pushVideoFrame(frame);
            }
        });
    }

    @Override
    public void disconnect() {
        Log.i(TAG, "disconnect");

        if (videoChannel != null) {
            videoChannel.disconnectProducer();
            videoChannel = null;
        }
    }
}
