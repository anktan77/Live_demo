package com.example.live_demo.ui.live;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.elvishew.xlog.XLog;
import com.example.capture.video.camera.CameraManager;
import com.example.capture.video.camera.VideoModule;
import com.example.framework.framework.RtcVideoConsumer;
import com.example.live_demo.BaseActivity;
import com.example.live_demo.R;
import com.example.live_demo.framework.PreprocessorFaceUnity;
import com.example.live_demo.utils.Global;
import com.example.live_demo.vlive.Config;
import com.example.live_demo.vlive.shark.rtc.RtcEventHandler;
import com.example.live_demo.vlive.shark.rtm.RtmMessageListener;
import com.example.live_demo.vlive.shark.rtm.RtmMessageManager;
import com.example.live_demo.vlive.shark.rtm.model.GiftRankMessage;
import com.example.live_demo.vlive.shark.rtm.model.NotificationMessage;
import com.example.live_demo.vlive.shark.rtm.model.PKStateMessage;
import com.example.live_demo.vlive.shark.rtm.model.SeatStateMessage;

import java.util.List;
import java.util.Map;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannelAttribute;
import io.agora.rtm.RtmChannelMember;


/// **
//        * Các công năng chung của phòng khách. Chẳng hạn như, chụp ảnh bằng máy ảnh ，
//        *, agora rtc, nhắn tin, kiểm tra quyền, giao tiếp với
//        * máy chủ back-end, v.v.
//        * /
public abstract class LiveBaseActivity extends BaseActivity
        implements RtcEventHandler, RtmMessageListener {
    protected static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSION_REQ = 1;

    // values of a live room
    protected String roomName;
    protected String roomId;
    protected boolean isOwner;
    protected String ownerId;
    protected boolean isHost;
    protected int myRtcRole;
    protected int ownerRtcUid;
    protected int tabId;

    // Current rtc channel generated by server
    // and obtained when entering the room.
    protected String rtcChannelName;

    private RtmMessageManager mMessageManager;
    private CameraManager mCameraVideoManager;
    private PreprocessorFaceUnity mFUPreprocessor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keepScreenOn(getWindow());
        checkPermissions();
    }

    protected void checkPermissions() {
        if (!permissionArrayGranted()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQ);
        } else {
            performInit();
        }
    }

    private boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean permissionArrayGranted() {
        boolean granted = true;
        for (String per : PERMISSIONS) {
            if (!permissionGranted(per)) {
                granted = false;
                break;
            }
        }
        return granted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ) {
            if (permissionArrayGranted()) {
                performInit();
            } else {
                Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void performInit() {
        initRoom();
        onPermissionGranted();
    }

    // owner id == user id
    private void initRoom() {
        Intent intent = getIntent();
        roomName = intent.getStringExtra(Global.Constants.KEY_ROOM_NAME);
        roomId = intent.getStringExtra(Global.Constants.KEY_ROOM_ID);
        isOwner = intent.getBooleanExtra(Global.Constants.KEY_IS_ROOM_OWNER, false);
        ownerId = intent.getStringExtra(Global.Constants.KEY_ROOM_OWNER_ID);
        isHost = isOwner;
        myRtcRole = isOwner ? Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE;

        // lấy int ví dụ (TAB_ID_MULTI = 0
        //          TAB_ID_SINGLE = 1)
        tabId = intent.getIntExtra(Global.Constants.TAB_KEY, 0);

        mMessageManager = RtmMessageManager.instance();
        mMessageManager.init(rtmClient());
        mMessageManager.registerMessageHandler(this);
        mMessageManager.setCallbackThread(new Handler(getMainLooper()));

        initCameraIfNeeded();
    }

    private void initCameraIfNeeded() {
        if (mCameraVideoManager == null) {
            mCameraVideoManager = cameraVideoManager();
        }

        if (mCameraVideoManager != null) {
            mCameraVideoManager.enablePreprocessor(
                config().isBeautyEnabled());
        }

        if (mFUPreprocessor == null &&
                mCameraVideoManager != null) {
            mFUPreprocessor = (PreprocessorFaceUnity)
                mCameraVideoManager.getPreprocessor();
        }
    }

    protected abstract void onPermissionGranted();

    @Override
    public void onStart() {
        super.onStart();
        registerRtcHandler(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        removeRtcHandler(this);
    }

    protected RtmMessageManager getMessageManager() {
        return mMessageManager;
    }

    protected void joinRtcChannel() {
        rtcEngine().setClientRole(myRtcRole);
        // lấy camera 2
        rtcEngine().setVideoSource(new RtcVideoConsumer(VideoModule.instance()));
        // cấu hình thông số camera
        setVideoConfiguration();
        // publish, rtcChannelName lấy từ việc onEnterRoomResponse
        rtcEngine().joinChannel(config().getUserProfile().getRtcToken(),
                rtcChannelName, null, (int) config().getUserProfile().getAgoraUid());
    }

    protected SurfaceView setupRemoteVideo(int uid) {
        SurfaceView surfaceView = RtcEngine.CreateRendererView(this);
        rtcEngine().setupRemoteVideo(new VideoCanvas(
                surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        return surfaceView;
    }

    protected void removeRemoteVideo(int uid) {
        rtcEngine().setupRemoteVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    protected void setVideoConfiguration() {
        rtcEngine().setVideoEncoderConfiguration(config().createVideoEncoderConfig(tabIdToLiveType(tabId)));
    }

    protected int tabIdToLiveType(int tabId) {
        if (tabId == Config.LIVE_TYPE_MULTI_HOST ||
            tabId == Config.LIVE_TYPE_SINGLE_HOST ||
            tabId == Config.LIVE_TYPE_PK_HOST ||
            tabId == Config.LIVE_TYPE_VIRTUAL_HOST ||
            tabId == Config.LIVE_TYPE_ECOMMERCE) {
            return tabId;
        } else return 0;
    }

    protected void startCameraCapture() {
        initCameraIfNeeded();
        if (mCameraVideoManager != null) {
            enablePreProcess(config().isBeautyEnabled());
            mCameraVideoManager.startCapture();
        }
    }

    protected void setLocalPreview(SurfaceView surfaceView) {
        initCameraIfNeeded();
        if (mCameraVideoManager != null) {
            mCameraVideoManager.setLocalPreview(surfaceView);
        }
    }

    protected void setLocalPreview(TextureView textureView) {
        initCameraIfNeeded();
        if (mCameraVideoManager != null) {
            mCameraVideoManager.setLocalPreview(textureView);
        }
    }

    protected void switchCamera() {
        initCameraIfNeeded();
        if (mCameraVideoManager != null) {
            mCameraVideoManager.switchCamera();
        }
    }

    protected void stopCameraCapture() {
        initCameraIfNeeded();
        if (mCameraVideoManager != null) {
            mCameraVideoManager.stopCapture();
        }
    }

    protected void enablePreProcess(boolean enabled) {
        initCameraIfNeeded();
        if (mCameraVideoManager != null) {
            mCameraVideoManager.enablePreprocessor(enabled);
        }
    }

    protected void setBlurValue(float blur) {
        initCameraIfNeeded();
        if (mFUPreprocessor != null) {
            mFUPreprocessor.setBlurValue(blur);
        }
    }

    protected void setWhitenValue(float whiten) {
        initCameraIfNeeded();
        if (mFUPreprocessor != null) {
            mFUPreprocessor.setWhitenValue(whiten);
        }
    }

    protected void setCheekValue(float cheek) {
        initCameraIfNeeded();
        if (mFUPreprocessor != null) {
            mFUPreprocessor.setCheekValue(cheek);
        }
    }

    protected void setEyeValue(float eye) {
        initCameraIfNeeded();
        if (mFUPreprocessor != null) {
            mFUPreprocessor.setEyeValue(eye);
        }
    }

    protected void joinRtmChannel() {
        mMessageManager.joinChannel(rtcChannelName, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                XLog.d("on rtm join channel success " + rtcChannelName);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                XLog.e("rtm join channel failed " + rtcChannelName + " msg:" + errorInfo.toString());
            }
        });
    }

    protected void leaveRtmChannel(ResultCallback<Void> callback) {
        mMessageManager.leaveChannel(callback);
    }

    @Override
    public void onRtmConnectionStateChanged(int state, int reason) {

    }

    @Override
    public void onRtmTokenExpired() {

    }

    @Override
    public void onRtmPeersOnlineStatusChanged(Map<String, Integer> map) {

    }

    @Override
    public void onRtmMemberCountUpdated(int memberCount) {

    }

    @Override
    public void onRtmAttributesUpdated(List<RtmChannelAttribute> attributeList) {

    }

    @Override
    public void onRtmMemberJoined(RtmChannelMember rtmChannelMember) {

    }

    @Override
    public void onRtmMemberLeft(RtmChannelMember rtmChannelMember) {

    }

    @Override
    public void onRtmSeatInvited(String userId, String userName, int index) {

    }

    @Override
    public void onRtmSeatApplied(String userId, String userName, int index) {

    }

    @Override
    public void onRtmInvitationAccepted(long processId, String userId, String userName, int index) {

    }

    @Override
    public void onRtmApplicationAccepted(long processId, String userId, String userName, int index) {

    }

    @Override
    public void onRtmInvitationRejected(long processId, String userId, String userName, int index) {

    }

    @Override
    public void onRtmApplicationRejected(long processId, String userId, String userName, int index) {

    }

    @Override
    public void onRtmOwnerForceLeaveSeat(String userId, String userName, int index) {

    }

    @Override
    public void onRtmHostLeaveSeat(String userId, String userName, int index) {

    }

    @Override
    public void onRtmPkReceivedFromAnotherHost(String userId, String userName, String pkRoomId) {

    }

    @Override
    public void onRtmPkAcceptedByTargetHost(String userId, String userName, String pkRoomId) {

    }

    @Override
    public void onRtmPkRejectedByTargetHost(String userId, String userName, String pkRoomId) {

    }

    @Override
    public void onRtmChannelMessageReceived(String peerId, String nickname, String content) {

    }

    @Override
    public void onRtmRoomGiftRankChanged(int total, List<GiftRankMessage.GiftRankItem> list) {

    }

    @Override
    public void onRtmOwnerStateChanged(String userId, String userName, int uid, int enableAudio, int enableVideo) {

    }

    @Override
    public void onRtmSeatStateChanged(List<SeatStateMessage.SeatStateMessageDataItem> data) {

    }

    @Override
    public void onRtmReceivePKEvent(PKStateMessage.PKStateMessageBody messageData) {

    }

    @Override
    public void onRtmGiftMessage(String fromUserId, String fromUserName, String toUserId, String toUserName, int giftId) {

    }

    @Override
    public void onRtmChannelNotification(int total, List<NotificationMessage.NotificationItem> list) {

    }

    @Override
    public void onRtmProductPurchased(String productId, int count) {

    }

    @Override
    public void onRtmProductStateChanged(String productId, int state) {

    }

    @Override
    public void onRtmLeaveMessage() {

    }

    @Override
    public void onRtcJoinChannelSuccess(String channel, int uid, int elapsed) {

    }

    @Override
    public void onRtcRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {

    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {

    }

    @Override
    public void onRtcChannelMediaRelayStateChanged(int state, int code) {

    }

    @Override
    public void onRtcChannelMediaRelayEvent(int code) {

    }

    @Override
    public void onRtcAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume) {

    }

    @Override
    public void onRtcAudioRouteChanged(int routing) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void finish() {
        super.finish();
        rtcEngine().leaveChannel();

        if (mMessageManager != null) {
            mMessageManager.removeMessageHandler(this);
            mMessageManager.leaveChannel(new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }

                @Override
                public void onFailure(ErrorInfo errorInfo) {

                }
            });
        }
    }
}
