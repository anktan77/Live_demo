package com.example.live_demo.ui.main.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.live_demo.R;
import com.example.live_demo.protocol.model.ClientProxy;
import com.example.live_demo.ui.live.SingleHostLiveActivity;


public class SingleHostFragment extends AbsPageFragment {


    @Override
    protected int onGetRoomListType() {
        return ClientProxy.ROOM_TYPE_SINGLE;
    }

    @Override
    protected Class<?> getLiveActivityClass() {
        return SingleHostLiveActivity.class;
    }
}