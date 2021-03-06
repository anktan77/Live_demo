package com.example.framework.framework;

import android.opengl.GLES20;

import com.elvishew.xlog.XLog;
import com.example.capture.video.camera.VideoCaptureFrame;
import com.example.capture.video.camera.VideoModule;
import com.example.framework.framework.modules.channels.ChannelManager;
import com.example.framework.framework.modules.channels.VideoChannel;
import com.example.framework.framework.modules.consumers.IVideoConsumer;

import io.agora.rtc.mediaio.IVideoFrameConsumer;
import io.agora.rtc.mediaio.IVideoSource;
import io.agora.rtc.mediaio.MediaIO;
import io.agora.rtc.video.AgoraVideoFrame;

public class RtcVideoConsumer implements IVideoConsumer, IVideoSource {
    private static final String TAG = RtcVideoConsumer.class.getSimpleName();
//    Từ khóa volatile được sử dụng để đánh dấu
//    một biến Java là "đã được lưu trữ trong bộ nhớ chính".
    private volatile IVideoFrameConsumer mRtcConsumer;
    private volatile boolean mValidInRtc;

    private volatile VideoModule mVideoModule;
    private int mChannelId;

    public RtcVideoConsumer(VideoModule videoModule) {
        this(videoModule, ChannelManager.ChannelID.CAMERA);
    }

    private RtcVideoConsumer(VideoModule videoModule, int channelId) {
        mVideoModule = videoModule;
        mChannelId = channelId;
    }

    @Override
    public void onConsumeFrame(VideoCaptureFrame frame, VideoChannel.ChannelContext context) {
        if (mValidInRtc) {
            int format = frame.format.getTexFormat() == GLES20.GL_TEXTURE_2D
                    ? AgoraVideoFrame.FORMAT_TEXTURE_2D
                    : AgoraVideoFrame.FORMAT_TEXTURE_OES;
            if (mRtcConsumer != null) {
                mRtcConsumer.consumeTextureFrame(frame.textureId, format,
                        frame.format.getWidth(), frame.format.getHeight(),
                        frame.rotation, frame.timestamp, frame.textureTransform);
            }
        }
    }

    @Override
    public void connectChannel(int channelId) {
        // lấy camera2
        VideoChannel channel = mVideoModule.connectConsumer(
                this, channelId, IVideoConsumer.TYPE_OFF_SCREEN);
    }

    @Override
    public void disconnectChannel(int channelId) {
        mVideoModule.disconnectConsumer(this, channelId);
    }

    @Override
    public Object onGetDrawingTarget() {
        // Rtc engine does not draw the frames
        // on any target window surface
        return null;
    }

    @Override
    public int onMeasuredWidth() {
        return 0;
    }

    @Override
    public int onMeasuredHeight() {
        return 0;
    }

    @Override
    public boolean onInitialize(IVideoFrameConsumer consumer) {
        XLog.i("onInitialize");
        mRtcConsumer = consumer;
        return true;
    }

    @Override
    public boolean onStart() {
        XLog.i("onStart");
        connectChannel(mChannelId);
        mValidInRtc = true;
        return true;
    }

    @Override
    public void onStop() {
        XLog.i("onStop");
        mValidInRtc = false;
        mRtcConsumer = null;
    }

    @Override
    public void onDispose() {
        XLog.i("onDispose");
        mValidInRtc = false;
        mRtcConsumer = null;
        disconnectChannel(mChannelId);
    }

    @Override
    public int getBufferType() {
        return MediaIO.BufferType.TEXTURE.intValue();
    }

    @Override
    public int getCaptureType() {
        return 0;
    }

    @Override
    public int getContentHint() {
        return 0;
    }
}
