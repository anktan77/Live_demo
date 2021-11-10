package com.example.framework.framework.modules.channels;

import android.content.Context;

import com.example.capture.video.camera.CameraVideoChannel;
import com.example.framework.framework.modules.consumers.IVideoConsumer;
import com.example.framework.framework.modules.processors.IPreprocessor;
import com.example.framework.framework.modules.producers.IVideoProducer;

public class ChannelManager {
    public static final String TAG = ChannelManager.class.getSimpleName();
    private static final int CHANNEL_COUNT = 3;

    public static class ChannelID {
        public static final int CAMERA = 0;
        public static final int SCREEN_SHARE = 1;
        public static final int CUSTOM = 2;

        public static String toString(int id) {
            switch (id) {
                case CAMERA: return "camera_channel";
                case SCREEN_SHARE: return "ScreenShare_channel";
                case CUSTOM: return "custom_channel";
                default: return "undefined_channel";
            }
        }
    }

    public ChannelManager(Context context) {
        // The context should have no relation
        // to any Activity or a Service instance.
        mContext = context.getApplicationContext();
    }

    private Context mContext;
    // CHANNEL_COUNT = 3
    private VideoChannel[] mChannels = new VideoChannel[CHANNEL_COUNT];

    public VideoChannel connectProducer(IVideoProducer producer, int id) {
        // đảm bảo kênh chạy
        ensureChannelRunning(id);
        mChannels[id].connectProducer(producer);
        return mChannels[id];
    }

    public void disconnectProducer(int id) {
        ensureChannelRunning(id);
        mChannels[id].disconnectProducer();
    }

    public VideoChannel connectConsumer(IVideoConsumer consumer, int id, int type) {
        // tạo array lớp con
        ensureChannelRunning(id);
        // lấy lớp con mchannel[0] = Camera Video Channel
        mChannels[id].connectConsumer(consumer, type);
        // trả về lớp con
        return mChannels[id];
    }

    public void disconnectConsumer(IVideoConsumer consumer, int id) {
        ensureChannelRunning(id);
        mChannels[id].disconnectConsumer(consumer);
    }

    // đảm bảo kênh chạy
    public void ensureChannelRunning(int channelId) {
        checkChannelId(channelId);
        // khởi tạo kênh
        // *
        if (mChannels[channelId] == null) {
            mChannels[channelId] = createVideoChannel(channelId);
        }

        if (!mChannels[channelId].isRunning()) {
            mChannels[channelId].startChannel();
        }
    }

    public void stopChannel(int channelId) {
        checkChannelId(channelId);

        if (mChannels[channelId] != null &&
                mChannels[channelId].isRunning()) {
            mChannels[channelId].stopChannel();
            mChannels[channelId] = null;
        }
    }

    public void enableOffscreenMode(int channelId, boolean enable) {
        ensureChannelRunning(channelId);
        mChannels[channelId].enableOffscreenMode(enable);
    }

    public void setPreprocessor(int channelId, IPreprocessor preprocessor) {
        checkChannelId(channelId);
        if (mChannels[channelId] == null) {
            mChannels[channelId] = createVideoChannel(channelId);
        }

        mChannels[channelId].setPreprocessor(preprocessor);
    }

    public IPreprocessor getPreprocessor(int channelId) {
        checkChannelId(channelId);
        return mChannels[channelId] == null ?
                null : mChannels[channelId].getPreprocessor();
    }

    // tạo lớp con array channel
    private VideoChannel createVideoChannel(int id) {
        return id == ChannelID.CAMERA ?
                new CameraVideoChannel(mContext, id) :
                new VideoChannel(mContext, id);
    }

    private void checkChannelId(int channelId) {
        if (channelId < ChannelID.CAMERA || channelId > ChannelID.CUSTOM) {
            throw new IllegalArgumentException(
                    "[ChannelManager] : Id channel không xác định");
        }
    }

    public VideoChannel getVideoChannel(int channelId) {
        checkChannelId(channelId);
        return mChannels[channelId];
    }
}
