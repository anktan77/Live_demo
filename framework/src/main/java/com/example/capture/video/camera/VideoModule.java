package com.example.capture.video.camera;

import android.content.Context;
import android.util.Log;

import com.example.framework.framework.modules.channels.ChannelManager;
import com.example.framework.framework.modules.channels.VideoChannel;
import com.example.framework.framework.modules.consumers.IVideoConsumer;
import com.example.framework.framework.modules.processors.IPreprocessor;
import com.example.framework.framework.modules.producers.IVideoProducer;

public class VideoModule {
    private static final String TAG = VideoModule.class.getSimpleName();

    // volatile đọc từ bộ nhớ chính của máy
    private volatile static VideoModule mSelf;
    private ChannelManager mChannelManager;
    private boolean mHasInitialized;

    public static VideoModule instance() {
        if (mSelf == null) {
            synchronized (VideoModule.class) {
                mSelf = new VideoModule();
            }
        }

        return mSelf;
    }

    private VideoModule() {

    }

    // khi khai báo VideoModule nó sẽ chạy cái init
    public void init(Context context) {
        mChannelManager = new ChannelManager(context);
        mHasInitialized = true;
    }

    public boolean hasInitialized() {
        return mHasInitialized;
    }

    public VideoChannel connectProducer(IVideoProducer producer, int id) {
        return mChannelManager.connectProducer(producer, id);
    }

    public VideoChannel connectConsumer(IVideoConsumer consumer, int id, int type) {
        return mChannelManager.connectConsumer(consumer, id, type);
    }

    public void disconnectProducer(IVideoProducer producer, int id) {
        mChannelManager.disconnectProducer(id);
    }

    public void disconnectConsumer(IVideoConsumer consumer, int id) {
        mChannelManager.disconnectConsumer(consumer, id);
    }

    public void startChannel(int id) {
        mChannelManager.ensureChannelRunning(id);
    }

    // không quan trọng
    public void stopChannel(int channelId) {
        mChannelManager.stopChannel(channelId);
    }

    public void stopAllChannels() {
        stopChannel(ChannelManager.ChannelID.CAMERA);
        stopChannel(ChannelManager.ChannelID.SCREEN_SHARE);
        stopChannel(ChannelManager.ChannelID.CUSTOM);
    }


    public void enableOffscreenMode(int channelId, boolean enabled) {
        mChannelManager.enableOffscreenMode(channelId, enabled);
    }

    public void setPreprocessor(int channelId, IPreprocessor preprocessor) {
        if (getPreprocessor(channelId) == null) {
            Log.i(TAG, "current preprocessor has not been set");
            mChannelManager.setPreprocessor(channelId, preprocessor);
        }
    }

    public void enablePreprocessor(int channelId, boolean enabled) {
        if (getPreprocessor(channelId) != null) {
            mChannelManager.getVideoChannel(channelId).enablePreProcess(enabled);
        }
    }

    public IPreprocessor getPreprocessor(int channelId) {
        return mChannelManager.getPreprocessor(channelId);
    }

    public VideoChannel getVideoChannel(int channelId) {
        return mChannelManager.getVideoChannel(channelId);
    }
}
