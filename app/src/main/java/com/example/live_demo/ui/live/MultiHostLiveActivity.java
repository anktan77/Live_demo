package com.example.live_demo.ui.live;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.elvishew.xlog.XLog;
import com.example.live_demo.R;
import com.example.live_demo.protocol.manager.SeatServiceManager;
import com.example.live_demo.protocol.model.model.SeatInfo;
import com.example.live_demo.protocol.model.request.AudienceListRequest;
import com.example.live_demo.protocol.model.request.ModifySeatStateRequest;
import com.example.live_demo.protocol.model.request.ModifyUserStateRequest;
import com.example.live_demo.protocol.model.request.Request;
import com.example.live_demo.protocol.model.request.SeatInteractionRequest;
import com.example.live_demo.protocol.model.response.AudienceListResponse;
import com.example.live_demo.protocol.model.response.EnterRoomResponse;
import com.example.live_demo.protocol.model.response.LoginASPResponse;
import com.example.live_demo.protocol.model.response.Response;
import com.example.live_demo.protocol.model.response.SeatStateResponse;
import com.example.live_demo.protocol.model.types.SeatInteraction;
import com.example.live_demo.ui.actionsheets.InviteUserActionSheet;
import com.example.live_demo.ui.actionsheets.toolactionsheet.LiveRoomToolActionSheet;
import com.example.live_demo.ui.components.CameraSurfaceView;
import com.example.live_demo.ui.components.CameraTextureView;
import com.example.live_demo.ui.components.LiveMessageEditLayout;
import com.example.live_demo.ui.components.LiveMultiHostSeatLayout;
import com.example.live_demo.ui.components.SeatItemDialog;
import com.example.live_demo.ui.components.VoiceIndicateGifView;
import com.example.live_demo.ui.components.bottomLayout.LiveBottomButtonLayout;
import com.example.live_demo.utils.Global;
import com.example.live_demo.utils.UserUtil;
import com.example.live_demo.vlive.shark.rtm.model.SeatStateMessage;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;

public class MultiHostLiveActivity extends LiveRoomActivity implements View.OnClickListener,
        LiveMultiHostSeatLayout.LiveHostInSeatOnClickedListener,
        InviteUserActionSheet.InviteUserActionSheetListener,
        SeatItemDialog.OnSeatDialogItemClickedListener {

    private class OwnerUIManager {
        String userId;
        int profileIconRes;
        int rtcUid;


        FrameLayout userLayout;
        AppCompatImageView profileImage;
        AppCompatImageView audioMuteIcon;
        VoiceIndicateGifView mIndicateView;
        AppCompatTextView ownerNameText;


        CameraTextureView localPreview;


        SurfaceView remotePreview;

        boolean iAmOwner;
        boolean videoMuted;
        boolean audioMuted;

        OwnerUIManager(@NonNull RelativeLayout layout, String userId, boolean iAmOwner, int rtcUid) {
            userLayout = layout.findViewById(R.id.room_owner_video_layout);
            this.userId = userId;
            this.profileIconRes = UserUtil.getUserProfileIcon(userId);
            audioMuteIcon = layout.findViewById(R.id.live_host_in_owner_mute_icon);
            mIndicateView = layout.findViewById(R.id.live_host_in_owner_voice_indicate);
            ownerNameText = layout.findViewById(R.id.live_host_in_owner_name);

            this.rtcUid = rtcUid;
            setOwner(iAmOwner);
            showUserProfile();
        }

        private void setOwner(boolean isOwner) {
            iAmOwner = isOwner;
            videoMuted = !iAmOwner;
            audioMuted = !iAmOwner;
            audioMuteIcon.setVisibility(audioMuted ? View.VISIBLE : View.GONE);
            mIndicateView.setVisibility(audioMuted ? View.GONE : View.VISIBLE);
        }

        private void setOwnerName(String name) {
            ownerNameText.setText(name);
        }


        void asOwner(boolean audioMuted, boolean videoMuted) {
            iAmOwner = true;
            if (!videoMuted) showVideoUI();
            audioMuteIcon.setVisibility(audioMuted ? View.VISIBLE : View.GONE);
            mIndicateView.setVisibility(audioMuted ? View.GONE : View.VISIBLE);
            rtcEngine().muteLocalAudioStream(audioMuted);
            this.videoMuted = videoMuted;
            this.audioMuted = audioMuted;
        }

        private void showUserProfile() {
            profileImage = new AppCompatImageView(userLayout.getContext());
            profileImage.setImageResource(profileIconRes);
            profileImage.setClipToOutline(true);
            profileImage.setOutlineProvider(new RoomOwnerOutline());
            profileImage.setScaleType(ImageView.ScaleType.FIT_XY);
            userLayout.removeAllViews();
            userLayout.addView(profileImage);
        }

        void showVideoUI() {
            userLayout.removeAllViews();
            profileImage = null;
            if (iAmOwner) {
                localPreview = new CameraTextureView(userLayout.getContext());
                localPreview.setClipToOutline(true);
                localPreview.setOutlineProvider(new RoomOwnerOutline());
                userLayout.addView(localPreview);
            } else {
                remotePreview = setupRemoteVideo(rtcUid);
                remotePreview.setZOrderMediaOverlay(true);
                remotePreview.setClipToOutline(true);
                remotePreview.setOutlineProvider(new RoomOwnerOutline());
                userLayout.addView(remotePreview);
            }
        }

        void removeVideoUI(boolean showProfile) {
            if (iAmOwner && localPreview != null &&
                    localPreview.getParent() == userLayout) {
                userLayout.removeAllViews();
                localPreview = null;
            } else if (!iAmOwner && remotePreview != null &&
                    remotePreview.getParent() == userLayout) {
                userLayout.removeAllViews();
                remotePreview = null;
            }

            if (showProfile) showUserProfile();
        }

        void setAudioMuted(boolean muted) {
            if (muted == audioMuted && audioMuteIcon.getVisibility() == View.VISIBLE) return;
            audioMuted = muted;
            audioMuteIcon.setVisibility(audioMuted ? View.VISIBLE : View.GONE);
            mIndicateView.setVisibility(audioMuted ? View.GONE : View.VISIBLE);
        }

        void setVideoMuted(boolean muted) {
            if (muted == videoMuted) {
                if (muted && profileImage == null) {

                    return;
                } else if (!muted && (localPreview != null || remotePreview != null)) {

                    return;
                }
            }

            videoMuted = muted;
            if (videoMuted) {
                removeVideoUI(true);
            } else {
                showVideoUI();
            }
        }

        void startVoiceIndicateAnim() {
            if (mIndicateView != null &&
                    mIndicateView.getVisibility() == View.VISIBLE) {
                mIndicateView.start(Global.Constants.VOICE_INDICATE_INTERVAL);
            }
        }
    }

    private class RoomOwnerOutline extends ViewOutlineProvider {
        private int mRadius = getResources().getDimensionPixelSize(R.dimen.live_host_in_owner_video_radius);

        @Override
        public void getOutline(View view, Outline outline) {
            Rect rect = new Rect();
            view.getGlobalVisibleRect(rect);
            int leftMargin = 0;
            int topMargin = 0;
            Rect selfRect = new Rect(leftMargin, topMargin,
                    rect.right - rect.left - leftMargin,
                    rect.bottom - rect.top - topMargin);
            outline.setRoundRect(selfRect, mRadius);
        }
    }

    private static final int ROOM_NAME_HINT_COLOR = Color.rgb(101, 101, 101);
    private static final int ROOM_NAME_COLOR = Color.rgb(235, 235, 235);

    private OwnerUIManager mOwnerUIManager;
    private InviteUserActionSheet mInviteUserListActionSheet;
    private LiveMultiHostSeatLayout mSeatLayout;

    // Generated by back end server according to room id
    private List<SeatInfo> mSeatInfoList;

    private SeatServiceManager mSeatManager;

    private boolean mTopLayoutCalculated;

    @Override
    protected void onPermissionGranted() {
        mSeatManager = new SeatServiceManager(application());
        initUI();
        super.onPermissionGranted();
    }

    private void initUI() {
        hideStatusBar(false);
        setContentView(R.layout.activity_multi_host_live);
        setRoomNameText();

        participants = findViewById(R.id.host_in_participant);
        participants.init();
        participants.setUserLayoutListener(this);

        bottomButtons = findViewById(R.id.host_in_bottom_layout);
        bottomButtons.init();
        bottomButtons.setLiveBottomButtonListener(this);
        bottomButtons.setRole(isOwner ? LiveBottomButtonLayout.ROLE_OWNER :
                isHost ? LiveBottomButtonLayout.ROLE_HOST :
                        LiveBottomButtonLayout.ROLE_AUDIENCE);
        if (isOwner || isHost) {
            bottomButtons.setBeautyEnabled(config().isBeautyEnabled());
        }

        findViewById(R.id.live_bottom_btn_close).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_more).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun1).setOnClickListener(this);
        findViewById(R.id.live_bottom_btn_fun2).setOnClickListener(this);

        messageList = findViewById(R.id.message_list);
        messageList.init();
        messageEditLayout = findViewById(R.id.message_edit_layout);
        messageEditText = messageEditLayout.findViewById(LiveMessageEditLayout.EDIT_TEXT_ID);

        mOwnerUIManager = new OwnerUIManager(findViewById(
                R.id.room_owner_layout), ownerId, isOwner, ownerRtcUid);
        if (isOwner) {
            startCameraCapture();
            mOwnerUIManager.showVideoUI();
            mOwnerUIManager.setOwnerName(config().getUserProfile().getUserName());
        }

        mSeatLayout = findViewById(R.id.live_host_in_seat_layout);
        mSeatLayout.setOwner(isOwner);
        mSeatLayout.setHost(isHost);
        mSeatLayout.setMyUserId(config().getUserProfile().getUserId());
        mSeatLayout.setSeatListener(this);


        rtcEngine().enableAudioVolumeIndication(
                Global.Constants.VOICE_INDICATE_INTERVAL,
                Global.Constants.VOICE_INDICATE_SMOOTH,  false);

        rtcStatsView = findViewById(R.id.multi_host_rtc_stats);
        rtcStatsView.setCloseListener(view -> rtcStatsView.setVisibility(View.GONE));
    }

    private void setRoomNameText() {
        String nameHint = getResources().getString(R.string.live_host_in_room_name_hint);
        SpannableString name = new SpannableString(nameHint + roomName);
        name.setSpan(new ForegroundColorSpan(ROOM_NAME_HINT_COLOR),
                0, nameHint.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        name.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_size_medium)),
                0, nameHint.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        name.setSpan(new ForegroundColorSpan(ROOM_NAME_COLOR),
                nameHint.length(), name.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        name.setSpan(new AbsoluteSizeSpan(getResources().getDimensionPixelSize(R.dimen.text_size_normal)),
                nameHint.length(), name.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        ((AppCompatTextView) findViewById(R.id.host_in_room_name)).setText(name);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.host_in_top_participant_layout);
        if (topLayout != null && !mTopLayoutCalculated) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
            params.topMargin += systemBarHeight;
            topLayout.setLayoutParams(params);
            mTopLayoutCalculated = true;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.live_bottom_btn_close:
                onBackPressed();
                break;
            case R.id.live_bottom_btn_more:
                LiveRoomToolActionSheet toolSheet = (LiveRoomToolActionSheet)
                        showActionSheetDialog(ACTION_SHEET_TOOL,
                                tabIdToLiveType(tabId), isHost, true, this);
                toolSheet.setEnableInEarMonitoring(inEarMonitorEnabled);
                break;
            case R.id.live_bottom_btn_fun1:
                if (isOwner) {
                    showActionSheetDialog(ACTION_SHEET_BG_MUSIC, tabIdToLiveType(tabId), true, true, this);
                } else {
                    showActionSheetDialog(ACTION_SHEET_GIFT, tabIdToLiveType(tabId), false, true, this);
                }
                break;
            case R.id.live_bottom_btn_fun2:
                if (isOwner || isHost) {
                    // this button is hidden when current user is not host.
                    showActionSheetDialog(ACTION_SHEET_BEAUTY, tabIdToLiveType(tabId), true, true, this);
                }
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (isHost) {
            // The owner will reasonably be a host
            stopCameraCapture();
            mOwnerUIManager.removeVideoUI(false);
        }

        bottomButtons.clearStates(application());
    }

    @Override
    public void onRtcJoinChannelSuccess(String channel, int uid, int elapsed) {
        XLog.i("onRtcJoinChannelSuccess:" + channel + " uid:" + (uid & 0xFFFFFFFFL));
    }

    @Override
    public void onLoginASPRespone(LoginASPResponse response) {

    }

    @Override
    public void onEnterRoomResponse(EnterRoomResponse response) {
        super.onEnterRoomResponse(response);
        if (response.code == Response.SUCCESS) {
            ownerId = response.data.room.owner.userId;
            ownerRtcUid = response.data.room.owner.uid;
            mOwnerUIManager.rtcUid = response.data.room.owner.uid;


            String myId = config().getUserProfile().getUserId();
            boolean hostAudioMuted = false;
            boolean hostVideoMuted = false;
            if (!isOwner && myId.equals(response.data.room.owner.userId)) {
                isOwner = true;
                isHost = true;
                myRtcRole = Constants.CLIENT_ROLE_BROADCASTER;
                rtcEngine().setClientRole(myRtcRole);
            } else if (!isHost) {
                for (SeatInfo info: response.data.room.coVideoSeats) {
                    if (info.seat.state == SeatInfo.TAKEN && myId.equals(info.user.userId)) {
                        isHost = true;
                        myRtcRole = Constants.CLIENT_ROLE_BROADCASTER;
                        rtcEngine().setClientRole(myRtcRole);
                        hostAudioMuted = info.user.enableAudio !=
                                SeatInfo.User.USER_AUDIO_ENABLE;
                        hostVideoMuted = info.user.enableVideo !=
                                SeatInfo.User.USER_VIDEO_ENABLE;
                        break;
                    }
                }
            }

            final boolean audioMutedAsHost = hostAudioMuted;
            final boolean videoMutedAsHost = hostVideoMuted;

            runOnUiThread(() -> {
                mOwnerUIManager.setOwnerName(response.data.room.owner.userName);

                // Update seat UI
                mSeatInfoList = response.data.room.coVideoSeats;
                if (mSeatInfoList != null && !mSeatInfoList.isEmpty()) {
                    updateSeatStates(mSeatInfoList);
                }

                if (isOwner) {
                    becomesOwner(response.data.room.owner.enableAudio !=
                                    SeatInfo.User.USER_AUDIO_ENABLE,
                            response.data.room.owner.enableVideo !=
                                    SeatInfo.User.USER_VIDEO_ENABLE);
                } else if (isHost) {
                    becomeBroadcaster(audioMutedAsHost, videoMutedAsHost);
                } else {
                    // Explicitly emphasizes that I am not the owner
                    mOwnerUIManager.setOwner(false);
                    mOwnerUIManager.setAudioMuted(response.data.room.owner.enableAudio
                            != SeatInfo.User.USER_AUDIO_ENABLE);
                    mOwnerUIManager.setVideoMuted(response.data.room.owner.enableVideo
                            != SeatInfo.User.USER_VIDEO_ENABLE);
                }
            });
        }
    }

    private void becomesOwner(boolean audioMuted, boolean videoMuted) {
        mOwnerUIManager.asOwner(audioMuted, videoMuted);
        if (!videoMuted) startCameraCapture();
        mSeatLayout.setOwner(isOwner);
        bottomButtons.setRole(LiveBottomButtonLayout.ROLE_OWNER);
        bottomButtons.setBeautyEnabled(config().isBeautyEnabled());
        config().setAudioMuted(audioMuted);
        config().setVideoMuted(videoMuted);
    }

    private void becomeBroadcaster(boolean audioMuted, boolean videoMuted) {
        isHost = true;
        mSeatLayout.setHost(true);
        mSeatLayout.setMyUserId(config().getUserProfile().getUserId());
        bottomButtons.setRole(LiveBottomButtonLayout.ROLE_HOST);
        bottomButtons.setBeautyEnabled(config().isBeautyEnabled());
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        config().setAudioMuted(audioMuted);
        config().setVideoMuted(videoMuted);

        if (!videoMuted) {
            startCameraCapture();
        } else {
            stopCameraCapture();
        }
    }

    private void becomeAudience() {
        isHost = false;
        stopCameraCapture();
        mSeatLayout.setHost(false);
        bottomButtons.setRole(LiveBottomButtonLayout.ROLE_AUDIENCE);
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        config().setAudioMuted(true);
        config().setVideoMuted(true);
    }

    private void updateSeatStates(List<SeatInfo> list) {
        List<SeatStateMessage.SeatStateMessageDataItem> itemList = new ArrayList<>();
        for (SeatInfo info : list) {
            SeatStateMessage.SeatStateMessageDataItem item =
                    new SeatStateMessage.SeatStateMessageDataItem();
            SeatStateMessage.SeatState seat = new SeatStateMessage.SeatState();
            seat.no = info.seat.no;
            seat.state = info.seat.state;
            item.seat = seat;

            SeatStateMessage.UserState user = new SeatStateMessage.UserState();
            user.userId = info.user.userId;
            user.userName = info.user.userName;
            user.uid = info.user.uid;
            user.enableAudio = info.user.enableAudio;
            user.enableVideo = info.user.enableVideo;
            item.user = user;

            itemList.add(item);
        }

        mSeatLayout.updateStates(itemList);
    }

    @Override
    public void onRequestSeatStateResponse(SeatStateResponse response) {
        super.onRequestSeatStateResponse(response);
    }

    @Override
    public void onSeatAdapterHostInviteClicked(int position, View view) {
        mInviteUserListActionSheet = (InviteUserActionSheet) showActionSheetDialog(
                ACTION_SHEET_INVITE_AUDIENCE, tabIdToLiveType(tabId), isHost, true, this);
        mInviteUserListActionSheet.setSeatNo(position + 1);
        requestAudienceList();
    }

    private void requestAudienceList() {
        sendRequest(Request.AUDIENCE_LIST, new AudienceListRequest(
                config().getUserProfile().getToken(),
                roomId, null, AudienceListRequest.TYPE_AUDIENCE));
    }

    @Override
    public void onAudienceListResponse(AudienceListResponse response) {
        super.onAudienceListResponse(response);

        if (mInviteUserListActionSheet != null &&
                mInviteUserListActionSheet.isShown()) {
            runOnUiThread(() -> mInviteUserListActionSheet.append(response.data.list));
        }
    }

    @Override
    public void onSeatAdapterAudienceApplyClicked(int position, View view) {
        curDialog = showDialog(R.string.live_room_host_in_audience_apply_title,
                R.string.live_room_host_in_audience_apply_message,
                v -> {
                    audienceApplyForSeat(position);
                    closeDialog();
                });
    }

    private void audienceApplyForSeat(int position) {
        mSeatManager.apply(roomId, ownerId, position + 1);
    }

    @Override
    public void onRtmSeatApplied(String userId, String userName, int seatId) {

        if (!isOwner) return;
        String title = getResources().getString(R.string.live_room_host_in_audience_apply_title);
        String message = getResources().getString(R.string.live_room_host_in_audience_apply_owner_message);
        message = String.format(message, userName, seatId);
        curDialog = showDialog(title, message,
                R.string.dialog_positive_button_accept, R.string.dialog_negative_button_refuse,
                view -> {
                    mSeatManager.ownerAccept(roomId, userId, seatId);
                    curDialog.dismiss();
                },
                view -> {
                    mSeatManager.ownerReject(roomId, userId, seatId);
                    curDialog.dismiss();
                });
    }

    @Override
    public void onRtmApplicationAccepted(long processId, String userId, String userName, int index) {
        showShortToast(getResources().getString(R.string.apply_seat_success));
    }

    @Override
    public void onRtmInvitationAccepted(long processId, String userId, String userName, int index) {
        showShortToast(getResources().getString(R.string.invite_success));
    }

    @Override
    public void onRtmApplicationRejected(long processId, String userId, String nickname, int index) {
        String title = getResources().getString(R.string.live_room_host_in_apply_rejected);
        String message = getResources().getString(R.string.live_room_host_in_apply_rejected_message);
        message = String.format(message, nickname);
        curDialog = showSingleButtonConfirmDialog(title, message, view -> curDialog.dismiss());
    }

    @Override
    public void onRtmInvitationRejected(long processId, String userId, String nickname, int index) {
        String title = getResources().getString(R.string.live_room_host_in_invite_rejected);
        String message = getResources().getString(R.string.live_room_host_in_invite_rejected_message);
        message = String.format(message, nickname);
        curDialog = showSingleButtonConfirmDialog(title, message, view -> curDialog.dismiss());
    }

    @Override
    public void onRtmOwnerStateChanged(String userId, String userName, int uid, int enableAudio, int enableVideo) {
        // Server thông báo qua tin nhắn rtm rằng chủ phòng đã thay đổi trạng thái của mình
        runOnUiThread(() -> {
            boolean audioMuted = enableAudio != SeatInfo.User.USER_AUDIO_ENABLE;
            boolean videoMuted = enableVideo != SeatInfo.User.USER_VIDEO_ENABLE;
            mOwnerUIManager.setAudioMuted(audioMuted);
            mOwnerUIManager.setVideoMuted(videoMuted);
            config().setAudioMuted(audioMuted);
            config().setVideoMuted(videoMuted);
        });
    }

    @Override
    public void onRtmSeatStateChanged(List<SeatStateMessage.SeatStateMessageDataItem> list) {
        runOnUiThread(() -> mSeatLayout.updateStates(list));
    }

    @Override
    public void onRtcAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume) {
        if (totalVolume <= 0) return;

        runOnUiThread(() -> {
            for (IRtcEngineEventHandler.AudioVolumeInfo info : speakers) {
                if (isOwner && info.uid == 0 || info.uid == ownerRtcUid) {
                    mOwnerUIManager.startVoiceIndicateAnim();
                }

                mSeatLayout.audioIndicate(speakers, (int) config().getUserProfile().getAgoraUid());
            }

        });
    }

    @Override
    public void onSeatAdapterMoreClicked(int position, View view, int seatState, int audioMuteState) {
        if (isOwner || isHost) {
            int mode = isOwner ? SeatItemDialog.MODE_OWNER : SeatItemDialog.MODE_HOST;
            SeatItemDialog dialog = new SeatItemDialog(this, seatState,
                    audioMuteState, mode, view, position, this);
            dialog.show();
        }
    }


    @Override
    public SurfaceView onSeatAdapterItemVideoShowed(int position, int uid, boolean mine, boolean audioMuted, boolean videoMuted) {
        SurfaceView surfaceView;
        if (mine) {
            surfaceView = new CameraSurfaceView(this);
            becomeBroadcaster(audioMuted, videoMuted);
        } else {
            surfaceView = setupRemoteVideo(uid);
        }

        return surfaceView;
    }


    @Override
    public void onSeatAdapterItemVideoRemoved(int position, int uid, SurfaceView view, boolean mine, boolean remainsHost) {
        if (!mine) {
            removeRemoteVideo(uid);
        } else {
            if (!remainsHost) {
                becomeAudience();
            } else {

                isHost = true;
                mSeatLayout.setHost(true);
                mSeatLayout.setMyUserId(config().getUserProfile().getUserId());
                bottomButtons.setRole(LiveBottomButtonLayout.ROLE_HOST);
                bottomButtons.setBeautyEnabled(config().isBeautyEnabled());
            }
        }
    }

    @Override
    public void onSeatAdapterItemMyAudioMuted(int position, boolean muted) {
        rtcEngine().muteLocalAudioStream(muted);
        config().setAudioMuted(muted);
    }

    @Override
    public void onSeatDialogItemClicked(int position, SeatItemDialog.Operation operation) {
        final LiveMultiHostSeatLayout.SeatItem item = mSeatLayout.getSeatItem(position);

        String title = null;
        String message = null;
        Request request = null;
        int type = 0;

        switch (operation) {
            case mute:
                title = getResources().getString(R.string.dialog_multi_host_mute_title);
                message = getResources().getString(R.string.dialog_multi_host_mute_message);
                message = String.format(message, item.userName);
                type = Request.MODIFY_USER_STATE;
                request = new ModifyUserStateRequest(
                        config().getUserProfile().getToken(), roomId, item.userId,
                        0,   // Notify that the seat has disabled audio
                        // keep video state unchanged
                        item.videoMuteState == SeatInfo.User.USER_VIDEO_ENABLE ? 1 : 0,
                        1     // Always enable chat
                );
                break;
            case unmute:
                title = getResources().getString(R.string.dialog_multi_host_unmute_title);
                message = getResources().getString(R.string.dialog_multi_host_unmute_message);
                message = String.format(message, item.userName);
                type = Request.MODIFY_USER_STATE;
                request = new ModifyUserStateRequest(
                        config().getUserProfile().getToken(), roomId, item.userId,
                        1,   // Notify that the seat has enabled audio
                        // keep video state unchanged
                        item.videoMuteState == SeatInfo.User.USER_VIDEO_ENABLE ? 1 : 0,
                        1     // Always enable chat
                );
                break;
            case leave:
                title = getResources().getString(R.string.dialog_multi_host_leave_title);
                String myUserId = config().getUserProfile().getUserId();
                int interaction;
                if (myUserId.equals(item.userId)) {
                    message = getResources().getString(R.string.dialog_multi_host_leave_message_host);
                    interaction = SeatInteraction.HOST_LEAVE;
                } else {
                    message = getResources().getString(R.string.dialog_multi_host_leave_message_owner);
                    message = String.format(message, TextUtils.isEmpty(item.userName) ? item.userId : item.userName);
                    interaction = SeatInteraction.OWNER_FORCE_LEAVE;
                }
                type = Request.SEAT_INTERACTION;
                request = new SeatInteractionRequest(
                        config().getUserProfile().getToken(),
                        roomId, item.userId, position + 1, interaction);
                break;
            case open:
                title = getResources().getString(R.string.dialog_multi_host_open_seat_title);
                message = getResources().getString(R.string.dialog_multi_host_open_seat_message);
                type = Request.MODIFY_SEAT_STATE;
                request = new ModifySeatStateRequest(
                        config().getUserProfile().getToken(), roomId,
                        item.userId, position + 1, SeatInfo.OPEN);
                break;
            case close:
                title = getResources().getString(R.string.dialog_multi_host_block_seat_title);
                message = getResources().getString(R.string.dialog_multi_host_block_seat_message);
                type = Request.MODIFY_SEAT_STATE;
                request = new ModifySeatStateRequest(
                        config().getUserProfile().getToken(), roomId,
                        null, position + 1, SeatInfo.CLOSE);
                break;
        }

        final int requestType = type;
        final Request seatRequest = request;
        curDialog = showDialog(title, message,
                R.string.dialog_positive_button,
                R.string.dialog_negative_button, view -> {
                    sendRequest(requestType, seatRequest);
                    curDialog.dismiss();
                }, view -> curDialog.dismiss());

    }

    @Override
    public void onSeatAdapterPositionClosed(int position, View view) {

    }

    @Override
    public void onActionSheetAudienceInvited(int seatId, String userId, String userName) {

        if (mInviteUserListActionSheet != null && mInviteUserListActionSheet.isShown()) {
            dismissActionSheetDialog();
        }

        mSeatManager.invite(roomId, userId, seatId);
    }

    // index là số ghế
    // userID là id chủ room
    // username là name chủ room
    @Override
    public void onRtmSeatInvited(String userId, String userName, int index) {
        if (isOwner) return;
        String title = getResources().getString(R.string.live_room_host_in_invite_user_list_action_sheet_title);
        String message = getResources().getString(R.string.live_room_host_in_invited_by_owner);
        message = String.format(message, userName, index);
        curDialog = showDialog(title, message,
                R.string.dialog_positive_button_accept, R.string.dialog_negative_button_refuse,
                view -> {
                    mSeatManager.audienceAccept(roomId, userId, index);
                    curDialog.dismiss();
                },
                view -> {
                    mSeatManager.audienceReject(roomId, userId, index);
                    curDialog.dismiss();
                });
    }

    @Override
    public void onActionSheetVideoClicked(boolean muted) {
        super.onActionSheetVideoClicked(muted);
        if (!isOwner && !isHost) return;
        if (muted) {
            stopCameraCapture();
        } else {
            startCameraCapture();
        }

        String myUserId = config().getUserProfile().getUserId();
        if (isOwner) {
            mOwnerUIManager.setVideoMuted(muted);
            modifyUserState(myUserId, !mOwnerUIManager.audioMuted, !muted);
        } else if (isHost) {
            LiveMultiHostSeatLayout.SeatItem item = mSeatLayout.getSeatItem(myUserId);
            if (item != null) {
                modifyUserState(myUserId, item.audioMuteState ==
                        SeatInfo.User.USER_AUDIO_ENABLE, !muted);
            }
        }
    }

    @Override
    public void onActionSheetSpeakerClicked(boolean muted) {
        super.onActionSheetSpeakerClicked(muted);
        String myUserId = config().getUserProfile().getUserId();
        if (isOwner) {
            modifyUserState(myUserId, !muted, !mOwnerUIManager.videoMuted);
        } else if (isHost) {
            LiveMultiHostSeatLayout.SeatItem item = mSeatLayout.getSeatItem(myUserId);
            if (item != null) {
                modifyUserState(myUserId, !muted, item.videoMuteState == SeatInfo.User.USER_VIDEO_ENABLE);
            }
        }
    }

    private void modifyUserState(String userId, boolean enableAudio,
                                 boolean enableVideo) {

        ModifyUserStateRequest request = new ModifyUserStateRequest(
                config().getUserProfile().getToken(), roomId, userId,
                enableAudio ? 1 : 0, enableVideo ? 1 : 0, 1);
        sendRequest(Request.MODIFY_USER_STATE, request);
    }
}
