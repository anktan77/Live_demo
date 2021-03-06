package com.example.live_demo.protocol.manager;

import android.text.TextUtils;

import com.elvishew.xlog.XLog;
import com.example.live_demo.protocol.model.request.PKRequest;
import com.example.live_demo.protocol.model.request.Request;
import com.example.live_demo.protocol.model.types.PKConstant;
import com.example.live_demo.utils.SharkLiveApplication;


public class PKServiceManager {
    private SharkLiveApplication mApplication;

    public PKServiceManager(SharkLiveApplication application) {
        this.mApplication = application;
    }

    private void sendPKBehaviorRequest(String token, String myRoomId, String targetRoomId, int type) {
        mApplication.proxy().sendRequest(Request.PK_BEHAVIOR, new PKRequest(token, myRoomId,  targetRoomId, type));
    }

    private void sendPKEndRequest(String token, String myRoomId) {
        mApplication.proxy().sendRequest(Request.PK_END, new PKRequest(token, myRoomId, null, 0));
    }

    private String getValidToken(String errorMessage) {
        String token = mApplication.config().getUserProfile().getToken();
        if (TextUtils.isEmpty(token)) {
            XLog.e(errorMessage);
            token = null;
        }

        return token;
    }

    public void invitePK(String myRoomId, String targetRoomId) {
        String token = getValidToken("PKManager invite pk token invalid");
        if (token != null) {
            sendPKBehaviorRequest(token, myRoomId, targetRoomId, PKConstant.PK_BEHAVIOR_INVITE);
        }
    }

    public void acceptPKInvitation(String myRoomId, String targetRoomId) {
        String token = getValidToken("PKManager accept pk invitation token invalid");
        if (token != null) {
            sendPKBehaviorRequest(token, myRoomId, targetRoomId, PKConstant.PK_BEHAVIOR_ACCEPT);
        }
    }

    public void rejectPKInvitation(String myRoomId, String targetRoomId) {
        String token = getValidToken("PKManager reject pk invitation token invalid");
        if (token != null) {
            sendPKBehaviorRequest(token, myRoomId, targetRoomId, PKConstant.PK_BEHAVIOR_REJECT);
        }
    }

    public void endPkSession(String myRoomId) {
        String token = getValidToken("PKManager end pk session token invalid");
        if (token != null) {
            sendPKEndRequest(token, myRoomId);
        }
    }
}