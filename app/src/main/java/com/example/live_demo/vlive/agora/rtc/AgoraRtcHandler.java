package com.example.live_demo.vlive.agora.rtc;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.IRtcEngineEventHandler;

public class AgoraRtcHandler extends IRtcEngineEventHandler {
    private List<RtcEventHandler> mHandlers;

    public AgoraRtcHandler() {
        mHandlers = new ArrayList<>();
    }

    public void registerEventHandler(RtcEventHandler handler) {
        //đăng ký trình xử lý sự kiện
        if (!mHandlers.contains(handler)) {
            mHandlers.add(handler);
        }
    }

    public void removeEventHandler(RtcEventHandler handler) {
        //loại bỏ Trình xử lý sự kiện
        mHandlers.remove(handler);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) { //elapsed:trôi qua
        for (RtcEventHandler handler : mHandlers) {
            handler.onRtcJoinChannelSuccess(channel, uid, elapsed);
        }
    }

    @Override
    public void onRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
        //thay đổi trạng thái video từ xa
        for (RtcEventHandler handler : mHandlers) {
            handler.onRtcRemoteVideoStateChanged(uid, state, reason, elapsed);
        }
    }

    @Override
    public void onRtcStats(RtcStats stats) {
        for (RtcEventHandler handler : mHandlers) {
            handler.onRtcStats(stats);
        }
    }

    @Override
    public void onChannelMediaRelayStateChanged(int state, int code) {
        for (RtcEventHandler handler : mHandlers) {
            handler.onRtcChannelMediaRelayStateChanged(state, code);
        }
    }

    @Override
    public void onChannelMediaRelayEvent(int code) {
        for (RtcEventHandler handler : mHandlers) {
            handler.onRtcChannelMediaRelayEvent(code);
        }
    }

    @Override
    public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
        for (RtcEventHandler handler : mHandlers) {
            handler.onRtcAudioVolumeIndication(speakers, totalVolume);
        }
    }

    @Override
    public void onAudioRouteChanged(int routing) {
        for (RtcEventHandler handler : mHandlers) {
            handler.onRtcAudioRouteChanged(routing);
        }
    }
}
