package com.example.live_demo.ui.live;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatEditText;

import com.elvishew.xlog.XLog;
import com.example.live_demo.R;
import com.example.live_demo.protocol.interfaces.LoginService;
import com.example.live_demo.protocol.model.ClientProxy;
import com.example.live_demo.protocol.model.body.CommitGiftBody;
import com.example.live_demo.protocol.model.model.UserProfile;
import com.example.live_demo.protocol.model.request.CreateRoomRequest;
import com.example.live_demo.protocol.model.request.Request;
import com.example.live_demo.protocol.model.request.RoomRequest;
import com.example.live_demo.protocol.model.request.SendGiftRequest;
import com.example.live_demo.protocol.model.response.AudienceListResponse;
import com.example.live_demo.protocol.model.response.CreateRoomResponse;
import com.example.live_demo.protocol.model.response.EnterRoomResponse;
import com.example.live_demo.protocol.model.response.LoginASPResponse;
import com.example.live_demo.protocol.model.response.PostResponse;
import com.example.live_demo.protocol.model.response.Response;
import com.example.live_demo.ui.actionsheets.BackgroundMusicActionSheet;
import com.example.live_demo.ui.actionsheets.BeautySettingActionSheet;
import com.example.live_demo.ui.actionsheets.GiftActionSheet;
import com.example.live_demo.ui.actionsheets.LiveRoomSettingActionSheet;
import com.example.live_demo.ui.actionsheets.LiveRoomUserListActionSheet;
import com.example.live_demo.ui.actionsheets.VoiceActionSheet;
import com.example.live_demo.ui.actionsheets.toolactionsheet.LiveRoomToolActionSheet;
import com.example.live_demo.ui.components.GiftAnimWindow;
import com.example.live_demo.ui.components.LiveMessageEditLayout;
import com.example.live_demo.ui.components.LiveRoomMessageList;
import com.example.live_demo.ui.components.LiveRoomUserLayout;
import com.example.live_demo.ui.components.RtcStatsView;
import com.example.live_demo.ui.components.bottomLayout.LiveBottomButtonLayout;
import com.example.live_demo.utils.GiftUtil;
import com.example.live_demo.utils.Global;
import com.example.live_demo.vlive.Config;
import com.example.live_demo.vlive.shark.rtm.model.GiftRankMessage;
import com.example.live_demo.vlive.shark.rtm.model.NotificationMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public abstract class LiveRoomActivity extends LiveBaseActivity implements
        BeautySettingActionSheet.BeautyActionSheetListener,
        LiveRoomSettingActionSheet.LiveRoomSettingActionSheetListener,
        BackgroundMusicActionSheet.BackgroundMusicActionSheetListener,
        GiftActionSheet.GiftActionSheetListener,
        LiveRoomToolActionSheet.LiveRoomToolActionSheetListener,
        VoiceActionSheet.VoiceActionSheetListener,
        LiveBottomButtonLayout.LiveBottomButtonListener,
        TextView.OnEditorActionListener,
        LiveRoomUserLayout.UserLayoutListener,
        LiveRoomUserListActionSheet.OnUserSelectedListener {

    private static final String TAG = LiveRoomActivity.class.getSimpleName();
    private static final int IDEAL_MIN_KEYBOARD_HEIGHT = 200;
    private static final int MIN_ONLINE_MUSIC_INTERVAL = 100;

    private Rect mDecorViewRect;
    private int mInputMethodHeight;

    // UI components of a live room
    protected LiveRoomUserLayout participants;
    protected LiveRoomMessageList messageList;
    protected LiveBottomButtonLayout bottomButtons;
    // layout nh???p mess
    protected LiveMessageEditLayout messageEditLayout;
    protected AppCompatEditText messageEditText;
    protected RtcStatsView rtcStatsView;
    protected Dialog curDialog;

    protected InputMethodManager inputMethodManager;

    private LiveRoomUserListActionSheet mRoomUserActionSheet;


    private volatile long mLastMusicPlayedTimeStamp;

    private boolean mActivityFinished;
    protected boolean inEarMonitorEnabled;
    private boolean mHeadsetWithMicrophonePlugged;

    private BroadcastReceiver mHeadPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AudioManager.ACTION_HEADSET_PLUG.equals(action)) {
                boolean plugged = intent.getIntExtra("state", -1) == 1;
                boolean hasMic = intent.getIntExtra("microphone", -1) == 1;
                mHeadsetWithMicrophonePlugged = plugged && hasMic;
                XLog.d("Wired headset is plugged???" + mHeadsetWithMicrophonePlugged);
            }
        }
    };

    private NetworkReceiver mNetworkReceiver = new NetworkReceiver();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // khi click v??o edittext
        // l??m cho thanh nh???p editext ??? tr??n b??n ph??m
        getWindow().getDecorView().getViewTreeObserver()
                .addOnGlobalLayoutListener(this::detectKeyboardLayout);

        // s??? d???ng ????? t???i b??n ph??m
        // qu???n l?? b??n ph??m, ???n b??n ph??m
        inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        IntentFilter headPhoneFilter = new IntentFilter();
        headPhoneFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadPhoneReceiver, headPhoneFilter);


    }

    @Override
    protected void onPermissionGranted() {
        if (getIntent().getBooleanExtra(Global.Constants.KEY_CREATE_ROOM, false)) {
            createRoom();
        } else {
            enterRoom(roomId);
        }
    }


    // l???y ch??u cao c???a keyboard
    private void detectKeyboardLayout() {
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

        if (mDecorViewRect == null) {
            mDecorViewRect = rect;
        }

        int diff = mDecorViewRect.height() - rect.height();

        // Tr??nh nghe b??? c???c to??n c???c c?? th??? ???????c g???i m???t s???
        // khi ho???t ?????ng ???????c kh???i ch???y, ch??ng ta c???n quan t??m
        // v??? gi?? tr??? c???a chi???u cao ph????ng th???c nh???p ???????c ph??t hi???n th??nh
        // l???c ra nh???ng tr?????ng h???p kh??ng mong mu???n.
        if (diff == mInputMethodHeight) {
            // Ph????ng th???c nh???p v???n ???????c hi???n th???
            return;
        }

        if (diff > IDEAL_MIN_KEYBOARD_HEIGHT && mInputMethodHeight == 0) {
            mInputMethodHeight = diff;
            onInputMethodToggle(true, diff);
        } else if (mInputMethodHeight > 0) {
            onInputMethodToggle(false, mInputMethodHeight);
            mInputMethodHeight = 0;
        }
    }

    protected void onInputMethodToggle(boolean shown, int height) {
        // init edit layout
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) messageEditLayout.getLayoutParams();
        int change = shown ? height : -height;
        params.bottomMargin += change;
        messageEditLayout.setLayoutParams(params);

        if (shown) {
            messageEditText.requestFocus();
            // nh???p v??n b???n
            // x??? l?? nh???n n??t th???c hi???n g???i mess
            messageEditText.setOnEditorActionListener(this);
        } else {
            messageEditLayout.setVisibility(View.GONE);
        }
    }

    private void createRoom() {
        CreateRoomRequest request = new CreateRoomRequest();
        request.token = config().getUserProfile().getToken();
        request.type = getChannelTypeByTabId();
        request.roomName = roomName;
        int imageId = getIntent().getIntExtra(Global.Constants.KEY_VIRTUAL_IMAGE, -1);
        if (config().getUserProfile().getImageUrl() == ""){
            request.avatar = virtualImageIdToName(imageId);
        }
        else {
            request.avatar = config().getUserProfile().getImageUrl();
        }
        sendRequest(Request.CREATE_ROOM, request);
    }

    protected String virtualImageIdToName(int id) {
        switch (id) {
            case 0: return "dog";
            case 1: return "girl";
            default: return null;
        }
    }

    private int getChannelTypeByTabId() {
        switch (tabId) {
            case Config.LIVE_TYPE_MULTI_HOST:
                return ClientProxy.ROOM_TYPE_HOST_IN;
            case Config.LIVE_TYPE_PK_HOST:
                return ClientProxy.ROOM_TYPE_PK;
            case Config.LIVE_TYPE_SINGLE_HOST:
                return ClientProxy.ROOM_TYPE_SINGLE;
            case Config.LIVE_TYPE_VIRTUAL_HOST:
                return ClientProxy.ROOM_TYPE_VIRTUAL_HOST;
            case Config.LIVE_TYPE_ECOMMERCE:
                return ClientProxy.ROOM_TYPE_ECOMMERCE;
        }
        return -1;
    }

    // t???o room  l???y id room
    @Override
    public void onCreateRoomResponse(CreateRoomResponse response) {
        roomId = response.data;
        enterRoom(roomId);
    }

    protected void enterRoom(String roomId) {
        RoomRequest request = new RoomRequest(config().getUserProfile().getToken(), roomId);
        sendRequest(Request.ENTER_ROOM, request);
    }

    // v??o room l???y rtc
    @Override
    public void onEnterRoomResponse(EnterRoomResponse response) {
        if (response.code == Response.SUCCESS) {
            Config.UserProfile profile = config().getUserProfile();
            profile.setRtcToken(response.data.user.rtcToken);
            profile.setAgoraUid(response.data.user.uid);

            rtcChannelName = response.data.room.channelName;
            roomId = response.data.room.roomId;
            roomName = response.data.room.roomName;

            joinRtcChannel();
            joinRtmChannel();

            initUserCount(response.data.room.currentUsers,
                    response.data.room.rankUsers);
        }
    }

    private void initUserCount(final int total, final List<EnterRoomResponse.RankInfo> rankUsers) {
        runOnUiThread(() -> participants.reset(total, rankUsers));
    }

    @Override
    public void onActionSheetBeautyEnabled(boolean enabled) {
        if (bottomButtons != null) bottomButtons.setBeautyEnabled(enabled);
        enablePreProcess(enabled);
    }

    @Override
    public void onActionSheetBlurSelected(float blur) {
        setBlurValue(blur);
    }

    @Override
    public void onActionSheetWhitenSelected(float whiten) {
        setWhitenValue(whiten);
    }

    @Override
    public void onActionSheetCheekSelected(float cheek) {
        setCheekValue(cheek);
    }

    @Override
    public void onActionSheetEyeEnlargeSelected(float eye) {
        setEyeValue(eye);
    }

    @Override
    public void onActionSheetResolutionSelected(int index) {
        config().setResolutionIndex(index);
        setVideoConfiguration();
    }

    @Override
    public void onActionSheetFrameRateSelected(int index) {
        config().setFrameRateIndex(index);
        setVideoConfiguration();
    }

    @Override
    public void onActionSheetBitrateSelected(int bitrate) {
        config().setVideoBitrate(bitrate);
        setVideoConfiguration();
    }

    @Override
    public void onActionSheetSettingBackPressed() {
        dismissActionSheetDialog();
    }

    @Override
    public void onActionSheetMusicSelected(int index, String name, String url) {
        long now = System.currentTimeMillis();
        if (now - mLastMusicPlayedTimeStamp > MIN_ONLINE_MUSIC_INTERVAL) {
            rtcEngine().startAudioMixing(url, false, false, -1);
            if (bottomButtons != null) bottomButtons.setMusicPlaying(true);
            mLastMusicPlayedTimeStamp = now;
        }
    }

    @Override
    public void onActionSheetMusicStopped() {
        rtcEngine().stopAudioMixing();
        if (bottomButtons != null) bottomButtons.setMusicPlaying(false);
    }

    // g???i qu??, intdex l?? m?? gift
    // g???i qu?? kh??c v???i g???i tin nh???n v?? n?? s??? send tr???c ti???p ?????n server
    // sau ???? server tr??? v??? sdk rtm r???i x??? l??
    @Override
    public void onActionSheetGiftSend(String name, int index, int value) {
        // ???n h???p tho???i
        dismissActionSheetDialog();
        SendGiftRequest request = new SendGiftRequest(config().
                getUserProfile().getToken(), roomId, index);
        sendRequest(Request.SEND_GIFT, request);
    }

//   / **
//           *
//           * @param gi??m s??t tr???ng th??i gi??m s??t l?? t?????ng ????? ???????c ki???m tra
//      * @ tr??? l???i true n???u tuy???n ??m thanh hi???n t???i l?? c?? d??y ho???c kh??ng c?? d??y
//      * tai nghe v???i micr??, tuy???n ??m thanh c?? th??? ???????c k??ch ho???t.
//      * Tr??? v??? true n???u tr???ng th??i ???????c ph??p thay ?????i.
//      * /
    @Override
    public boolean onActionSheetEarMonitoringClicked(boolean monitor) {
        if (monitor) {
            if (mHeadsetWithMicrophonePlugged) {
                rtcEngine().enableInEarMonitoring(true);
                inEarMonitorEnabled = true;
                return true;
            } else {
                showShortToast(getResources().getString(R.string.in_ear_monitoring_failed));
                // In ear monitor state does not change here.
                return false;
            }
        } else {
            rtcEngine().enableInEarMonitoring(false);
            // It is always allowed to disable the in-ear monitoring.
            inEarMonitorEnabled = false;
            return true;
        }
    }

    @Override
    public void onActionSheetRealDataClicked() {
        if (rtcStatsView != null) {
            runOnUiThread(() -> {
                int visibility = rtcStatsView.getVisibility();
                if (visibility == View.VISIBLE) {
                    rtcStatsView.setVisibility(View.GONE);
                } else if (visibility == View.GONE) {
                    rtcStatsView.setVisibility(View.VISIBLE);
                    rtcStatsView.setLocalStats(0, 0, 0, 0);
                }

                // Only clicking data button will dismiss
                // the action sheet dialog.
                dismissActionSheetDialog();
            });
        }
    }

    @Override
    public void onActionSheetSettingClicked() {
        showActionSheetDialog(ACTION_SHEET_VIDEO, tabIdToLiveType(tabId), isHost, false, this);
    }

    @Override
    public void onActionSheetRotateClicked() {
        switchCamera();
    }

    @Override
    public void onActionSheetVideoClicked(boolean muted) {
        if (isHost || isOwner) {
            rtcEngine().muteLocalVideoStream(muted);
            config().setVideoMuted(muted);
        }
    }

    @Override
    public void onActionSheetSpeakerClicked(boolean muted) {
        if (isHost || isOwner) {
            rtcEngine().muteLocalAudioStream(muted);
            config().setAudioMuted(muted);
        }
    }

    @Override
    public void onActionSheetAudioRouteSelected(int type) {

    }

    @Override
    public void onActionSheetAudioRouteEnabled(boolean enabled) {

    }

    @Override
    public void onActionSheetAudioBackPressed() {

        dismissActionSheetDialog();
    }

    @Override
    public void onLiveBottomLayoutShowMessageEditor() {
        if (messageEditLayout != null) {
            messageEditLayout.setVisibility(View.VISIBLE);
            messageEditText.requestFocus();
            // l??m keyboard xu???t hi???n khi click v??o editext
            inputMethodManager.showSoftInput(messageEditText, 0);
        }
    }

    // x??? l?? nh???n n??t th???c hi???n s??? g???i mess
    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            String editable = messageEditText.getText().toString();
            if (TextUtils.isEmpty(editable)) {
                showShortToast(getResources().getString(R.string.live_send_empty_message));
            } else {
                sendChatMessage(editable.toString());
                // sau khi nh???p xong mess tr??? v??? r???ng
                messageEditText.setText("");
            }

            // ???n b??n ph??m
            inputMethodManager.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    private void sendChatMessage(String content) {
        Config.UserProfile profile = config().getUserProfile();
        getMessageManager().sendChatMessage(profile.getUserId(),
                profile.getUserName(), content, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });
        messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, profile.getUserName(), content);
    }

    protected boolean isCurDialogShowing() {
        return curDialog != null && curDialog.isShowing();
    }

    protected void closeDialog() {
        if (isCurDialogShowing()) {
            curDialog.dismiss();
        }
    }

    // th??ng tin user ??? trong room
    @Override
    public void onUserLayoutShowUserList(View view) {
        // show dialog
        // tabId l???y t??? LiveBaseActivity xem th??? l?? ki???u ph??ng g??
        // isHost l???y t??? LiveBaseActivity xem th??? c?? ph???i ch??? ph??ng hay k;
        mRoomUserActionSheet = (LiveRoomUserListActionSheet)
                showActionSheetDialog(ACTION_SHEET_ROOM_USER, tabIdToLiveType(tabId), isHost, true, this);

        // truy???n d??? li???u qua LiveRoomUserListActionSheet
        mRoomUserActionSheet.setup(proxy(), this, roomId, config().getUserProfile().getToken());

        // l???y danh s??ch kh??n gi???
        mRoomUserActionSheet.requestMoreAudience();
    }

    // th???c hi???n sau khi tr??? v??? danh s??ch kh??n gi??? b??n ph??a client.class
    @Override
    public void onAudienceListResponse(AudienceListResponse response) {
        List<UserProfile> userList = new ArrayList<>();
        for (AudienceListResponse.AudienceInfo info : response.data.list) {
            UserProfile profile = new UserProfile();
            profile.setUserId(info.userId);
            profile.setUserName(info.userName);
            profile.setAvatar(info.avatar);
            userList.add(profile);
        }

        if (mRoomUserActionSheet != null && mRoomUserActionSheet.getVisibility() == View.VISIBLE) {
            runOnUiThread(() -> mRoomUserActionSheet.appendUsers(userList));
        }
    }

    @Override
    public void onActionSheetUserListItemSelected(String userId, String userName) {
        // Called when clicking an online user's name, and want to see the detail
    }

    // c??i n??y quan tr???ng ch???y ??a lu???ng, nh???n mess t??? Rtm
    @Override
    public void onRtmChannelMessageReceived(String peerId, String nickname, String content) {
        runOnUiThread(() -> messageList.addMessage(LiveRoomMessageList.MSG_TYPE_CHAT, nickname, content));
    }

    @Override
    public void onRtmRoomGiftRankChanged(int total, List<GiftRankMessage.GiftRankItem> list) {
        // The rank of user sending gifts has changed. The client
        // needs to update UI in this callback.
        if (list == null) return;

        List<EnterRoomResponse.RankInfo> rankList = new ArrayList<>();
        for (GiftRankMessage.GiftRankItem item : list) {
            EnterRoomResponse.RankInfo info = new EnterRoomResponse.RankInfo();
            info.userId = item.userId;
            info.userName = item.userName;
            info.avatar = item.avatar;
            rankList.add(info);
        }

        runOnUiThread(() -> participants.reset(rankList));
    }

    int mgift = 1;
    // t???ng gift
    @Override
    public void onRtmGiftMessage(String fromUserId, String fromUserName, String toUserId, String toUserName, int giftId) {
        runOnUiThread(() -> {

            String from = TextUtils.isEmpty(fromUserName) ? fromUserId : fromUserName;
            String to = TextUtils.isEmpty(toUserName) ? toUserId : toUserName;
            commitGift(fromUserName, toUserName, giftId);

        });
    }

    private static Retrofit retrofit;
    private static final String Base_Url = "http://192.168.1.5:8097";

    public static Retrofit getRetrofit(){

        if (retrofit == null){
            retrofit = new Retrofit.Builder().baseUrl(Base_Url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private void commitGift(String fromUserName, String toUserName, int giftId) {
        int coins =  application().config().getGiftList().get(giftId).getPoint();
        LoginService loginService =getRetrofit().create(LoginService.class);
        loginService.requestCommitGift(new CommitGiftBody(fromUserName,toUserName,coins)).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, retrofit2.Response<Integer> response) {
                int code = response.body();
                if (code == 0){
                    Toast.makeText(LiveRoomActivity.this, "s??? d?? kh??ng ?????", Toast.LENGTH_SHORT).show();
                }
                if (code == 1) {
                    messageList.addMessage(LiveRoomMessageList.MSG_TYPE_GIFT, fromUserName, toUserName, giftId);
                    GiftAnimWindow window = new GiftAnimWindow(LiveRoomActivity.this, R.style.gift_anim_window);
                    window.setAnimResource(GiftUtil.getGiftAnimRes(giftId));
                    window.show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {

            }
        });
    }


    // th??ng b??o ra kh???i ph??ng hay v??o ph??ng
    @Override
    public void onRtmChannelNotification(int total, List<NotificationMessage.NotificationItem> list) {
        // th??ng b??o v??o ph??ng hay ra ph??ng
        runOnUiThread(() -> {
            // update room user count
            participants.reset(total);
            for (NotificationMessage.NotificationItem item : list) {
                messageList.addMessage(LiveRoomMessageList.MSG_TYPE_SYSTEM, item.userName, "", item.state);
            }
        });
    }

    @Override
    public void onRtmLeaveMessage() {
        runOnUiThread(this::leaveRoom);
    }

    @Override
    public void onStart() {
        super.onStart();
        if ((isOwner || isHost) && !config().isVideoMuted()) {
            startCameraCapture();
        }
    }

    @Override
    public void onRtcJoinChannelSuccess(String channel, int uid, int elapsed) {
        XLog.d("onRtcJoinChannelSuccess:" + channel + " uid:" + (uid & 0xFFFFFFFFL));
    }

    @Override
    public void onRtcRemoteVideoStateChanged(int uid, int state, int reason, int elapsed) {
        XLog.d("onRtcRemoteVideoStateChanged: " + (uid & 0xFFFFFFFFL) +
                " state:" + state + " reason:" + reason);
    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        runOnUiThread(() -> {
            if (rtcStatsView != null && rtcStatsView.getVisibility() == View.VISIBLE) {
                rtcStatsView.setLocalStats(stats.rxKBitRate,
                        stats.rxPacketLossRate, stats.txKBitRate,
                        stats.txPacketLossRate);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if ((isHost || isOwner) && !config().isVideoMuted()
                && !mActivityFinished) {

            stopCameraCapture();
        }
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        int titleRes;
        int messageRes;
        if (isHost || isOwner) {
            titleRes = R.string.end_live_streaming_title_owner;
            messageRes = R.string.end_live_streaming_message_owner;
        } else {
            titleRes = R.string.finish_broadcast_title_audience;
            messageRes = R.string.finish_broadcast_message_audience;
        }
        curDialog = showDialog(titleRes, messageRes, view -> leaveRoom());
    }

    protected void leaveRoom() {
        leaveRoom(roomId);
        finish();
        closeDialog();
        dismissActionSheetDialog();
    }

    protected void leaveRoom(String roomId) {
        sendRequest(Request.LEAVE_ROOM, new RoomRequest(
                config().getUserProfile().getToken(), roomId));
    }

    @Override
    public void finish() {
        super.finish();
        mActivityFinished = true;
        stopCameraCapture();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mHeadPhoneReceiver);
    }

    @Override
    public void onResponseError(int requestType, int error, String message) {
        XLog.e("request:" + requestType + " error:" + error + " msg:" + message);
        runOnUiThread(() -> showLongToast("request type: "+
                Request.getRequestString(requestType) + " " + message));
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkReceiver);
    }

    protected static class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return;

            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info == null || !info.isAvailable() || !info.isConnected()) {
                Toast.makeText(context, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
            } else {
                int type = info.getType();
                if (ConnectivityManager.TYPE_WIFI == type) {
                    Toast.makeText(context, R.string.network_switch_to_wifi, Toast.LENGTH_SHORT).show();
                } else if (ConnectivityManager.TYPE_MOBILE == type) {
                    Toast.makeText(context, R.string.network_switch_to_mobile , Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
