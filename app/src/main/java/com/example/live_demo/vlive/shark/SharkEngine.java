package com.example.live_demo.vlive.shark;

import androidx.annotation.NonNull;

import com.example.live_demo.utils.SharkLiveApplication;
import com.example.live_demo.utils.UserUtil;
import com.example.live_demo.vlive.shark.rtc.SharkRtcHandler;
import com.example.live_demo.vlive.shark.rtc.RtcEventHandler;
import com.example.live_demo.vlive.shark.rtm.RtmMessageManager;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtm.RtmClient;

public class SharkEngine {
    private static final String TAG = SharkEngine.class.getSimpleName();
    private RtcEngine mRtcEngine;
    private SharkRtcHandler mRtcEventHandler = new SharkRtcHandler();

    private RtmClient mRtmClient;

    public SharkEngine(@NonNull SharkLiveApplication application, String appId) {
        try {
            mRtcEngine = RtcEngine.create(application, appId, mRtcEventHandler);
            mRtcEngine.enableVideo();
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            mRtcEngine.enableDualStreamMode(false);
            mRtcEngine.setLogFile(UserUtil.rtcLogFilePath(application));

            mRtmClient = RtmClient.createInstance(application, appId, RtmMessageManager.instance());
            mRtmClient.setLogFile(UserUtil.rtmLogFilePath(application));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public RtcEngine rtcEngine() {
        return mRtcEngine;
    }

    public RtmClient rtmClient() {
        return mRtmClient;
    }

    public void registerRtcHandler(RtcEventHandler handler) {
        if (mRtcEventHandler != null) mRtcEventHandler.registerEventHandler(handler);
    }

    public void removeRtcHandler(RtcEventHandler handler) {
        if (mRtcEventHandler != null) mRtcEventHandler.removeEventHandler(handler);
    }

    public void release() {
        if (mRtcEngine != null) RtcEngine.destroy();
        if (mRtmClient != null) {
            mRtmClient.logout(null);
            mRtmClient.release();
        }
    }
}
