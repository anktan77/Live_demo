package com.example.live_demo.ui.main.fragments;


import com.example.live_demo.protocol.model.ClientProxy;
import com.example.live_demo.protocol.model.response.LoginASPResponse;
import com.example.live_demo.ui.live.MultiHostLiveActivity;

public class HostInFragment extends AbsPageFragment {
    @Override
    protected int onGetRoomListType() {
        return ClientProxy.ROOM_TYPE_HOST_IN;
    }

    @Override
    protected Class<?> getLiveActivityClass() {
        return MultiHostLiveActivity.class;
    }

    @Override
    public void onLoginASPRespone(LoginASPResponse response) {

    }
}
