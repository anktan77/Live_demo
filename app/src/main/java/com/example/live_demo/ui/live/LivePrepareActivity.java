package com.example.live_demo.ui.live;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.capture.video.camera.CameraVideoChannel;
import com.example.capture.video.camera.VideoModule;
import com.example.framework.framework.modules.channels.ChannelManager;
import com.example.live_demo.R;
import com.example.live_demo.framework.PreprocessorFaceUnity;
import com.example.live_demo.protocol.model.response.LoginASPResponse;
import com.example.live_demo.ui.actionsheets.BeautySettingActionSheet;
import com.example.live_demo.ui.actionsheets.LiveRoomSettingActionSheet;
import com.example.live_demo.ui.components.CameraTextureView;
import com.example.live_demo.utils.Global;
import com.example.live_demo.utils.RandomUtil;
import com.example.live_demo.vlive.Config;

import org.jetbrains.annotations.NotNull;

public class LivePrepareActivity extends LiveBaseActivity implements View.OnClickListener, TextWatcher,
        BeautySettingActionSheet.BeautyActionSheetListener,
        LiveRoomSettingActionSheet.LiveRoomSettingActionSheetListener {

    public static final int RESULT_GO_LIVE = 2;

    private static final String TAG = LivePrepareActivity.class.getSimpleName();
    private static final int MAX_NAME_LENGTH = 25;

    private AppCompatTextView mStartBroadBtn;
    private AppCompatImageView mCloseBtn;
    private AppCompatImageView mSwitchBtn;
    private AppCompatImageView mRandomBtn;
    private RelativeLayout mEditLayout;
    private AppCompatTextView mEditHint;
    private AppCompatEditText mEditText;
    private AppCompatImageView mBeautyBtn;
    private AppCompatImageView mSettingBtn;
    private Dialog mExitDialog;

    private int roomType;
    private String mNameTooLongToastMsg;

    private FrameLayout mLocalPreviewLayout;
    private CameraVideoChannel mCameraChannel;
    private PreprocessorFaceUnity mPreprocessor;

    // N???u m??y ???nh v???n ti???p t???c, th?? m??y ???nh kh??ng ch???p
    // ???? d???ng v?? ch??ng t??i mu???n gi??? l???i qu?? tr??nh ch???p v?? chuy???n ti???p
    // t???i activity ti???p theo.
    // Camera Persist: m??y ???nh ???n ?????nh kh??ng
    private boolean mCameraPersist;

    private boolean mActivityFinished;
    // Permission Granted: cho ph??p hay ch??a
    private boolean mPermissionGranted;

    private int mVideoInitCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onGlobalLayoutCompleted() {

    }

    private void initUI() {
        // ???n thanh tr???ng th??i
        // "kh??ng c?? t??c d???ng g??"
        hideStatusBar(false);
        setContentView(R.layout.activity_live_prepare);

        View topLayout = findViewById(R.id.prepare_top_btn_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) topLayout.getLayoutParams();
        params.topMargin += systemBarHeight;
        topLayout.setLayoutParams(params);

        // random t??n room
        mEditText = findViewById(R.id.room_name_edit);
        mEditText.addTextChangedListener(this);
        setRandomRoomName();

        // gi???i h???n ????? d??i room name
        mNameTooLongToastMsg = String.format(getResources().getString(
                R.string.live_prepare_name_too_long_toast_format), MAX_NAME_LENGTH);

        mStartBroadBtn = findViewById(R.id.live_prepare_go_live_btn);
        mCloseBtn = findViewById(R.id.live_prepare_close);
        mSwitchBtn = findViewById(R.id.live_prepare_switch_camera);
        mRandomBtn = findViewById(R.id.random_btn);
        mEditLayout = findViewById(R.id.prepare_name_edit_layout);
        mEditHint = findViewById(R.id.room_name_edit_hint);
        mBeautyBtn = findViewById(R.id.live_prepare_beauty_btn);
        mSettingBtn = findViewById(R.id.live_prepare_setting_btn);

        mStartBroadBtn.setOnClickListener(this);
        mRandomBtn.setOnClickListener(this);
        mCloseBtn.setOnClickListener(this);
        mSwitchBtn.setOnClickListener(this);
        mBeautyBtn.setOnClickListener(this);
        mSettingBtn.setOnClickListener(this);

        // ChannelManager.ChannelID.CAMERA = 0;
        // l???y camera2
        // kh??ng quan tr???ng
        mCameraChannel = (CameraVideoChannel) VideoModule.instance().
                getVideoChannel(ChannelManager.ChannelID.CAMERA);
        mPreprocessor = (PreprocessorFaceUnity) VideoModule.instance().
                getPreprocessor(ChannelManager.ChannelID.CAMERA);

        mLocalPreviewLayout = findViewById(R.id.local_preview_layout);
        changeUIStyles();
        checkFUAuth();
    }

    private void changeUIStyles() {
        if (tabId == Config.LIVE_TYPE_VIRTUAL_HOST) {
            hideStatusBar(true);
            // It only accepts front camera frames for virtual images.
            mSwitchBtn.setVisibility(View.GONE);
            mCloseBtn.setImageResource(R.drawable.icon_back_black);
            mRandomBtn.setImageResource(R.drawable.random_button_black);
            mEditHint.setTextColor(getResources().getColor(R.color.gray_alpha2));
            mEditText.setTextColor(getResources().getColor(android.R.color.black));
            mEditLayout.setBackgroundResource(R.drawable.room_edit_layout_bg_gray);
            RelativeLayout layout = findViewById(R.id.activity_layout);
            layout.setBackgroundColor(Color.WHITE);
            mBeautyBtn.setVisibility(View.GONE);
            mSettingBtn.setVisibility(View.GONE);
            int virtualImage = getIntent().getIntExtra(
                    Global.Constants.KEY_VIRTUAL_IMAGE, -1);

            // The listener needs to be reset before the
            // virtual image is selected
            mPreprocessor.setOnBundleLoadedListener(this::tryInitializeVideo);

            mPreprocessor.setOnFirstFrameListener(this::tryInitializeVideo);
            config().setBeautyEnabled(true);
            startCameraCapture();
            mPreprocessor.onAnimojiSelected(virtualImage);
        } else {
            hideStatusBar(false);
            mCloseBtn.setImageResource(R.drawable.close_button_white);
            mSwitchBtn.setImageResource(R.drawable.switch_camera_white);
            mRandomBtn.setImageResource(R.drawable.random_button_white);
            mEditHint.setTextColor(getResources().getColor(R.color.gray_lightest));
            mEditText.setTextColor(getResources().getColor(android.R.color.white));
            mEditLayout.setBackgroundResource(R.drawable.room_edit_layout_bg_dark_gray);
            // startCameraCapture kh??ng quan tr???ng c?? th??? b???
            startCameraCapture();
            mPreprocessor.onAnimojiSelected(-1);
            // l???y camera set v??o
            mLocalPreviewLayout.addView(new CameraTextureView(this));
        }
    }

    private void checkFUAuth() {
        if (mPreprocessor != null &&
                !mPreprocessor.FUAuthenticated()) {
            showLongToast(getString(R.string.no_fu_auth_notice));
            if (tabId == Config.LIVE_TYPE_VIRTUAL_HOST) {
                mStartBroadBtn.setEnabled(false);
            }
        }
    }

    private void tryInitializeVideo() {
        if (mVideoInitCount >= 2) {
            return;
        }

        mVideoInitCount++;
        if (mVideoInitCount == 2) {
            runOnUiThread(() -> mLocalPreviewLayout.
                    addView(new CameraTextureView(this)));
        }
    }

    @Override
    protected void onPermissionGranted() {
        //nh???n int l?? host hay multi t??? roomfragment
        //l???y TAB_ID_MULTI = 0 l??m m???c ?????nh
        roomType = getIntent().getIntExtra(Global.Constants.TAB_KEY, Global.Constants.TAB_ID_MULTI);
        mPermissionGranted = true;
        initUI();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.live_prepare_close:
                onBackPressed();
                break;
            case R.id.live_prepare_switch_camera:
                switchCamera();
                break;
            case R.id.random_btn:
                setRandomRoomName();
                break;
            case R.id.live_prepare_go_live_btn:
                gotoBroadcastActivity();
                break;
            case R.id.live_prepare_beauty_btn:
                showActionSheetDialog(ACTION_SHEET_BEAUTY, tabIdToLiveType(tabId), true, true, this);
                break;
            case R.id.live_prepare_setting_btn:
                showActionSheetDialog(ACTION_SHEET_VIDEO, tabIdToLiveType(tabId), true, true, this);
                break;
        }
    }

    private void setRandomRoomName() {
        mEditText.setText(RandomUtil.randomLiveRoomName(this));
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(@NotNull Editable editable) {
        if (editable.length() > MAX_NAME_LENGTH) {
            showShortToast(mNameTooLongToastMsg);
            mEditText.setText(editable.subSequence(0, MAX_NAME_LENGTH));
            mEditText.setSelection(MAX_NAME_LENGTH);
        }
    }

    private boolean isRoomNameValid() {
        return mEditText.getText() != null && !TextUtils.isEmpty(mEditText.getText());
    }

    private void gotoBroadcastActivity() {
        if (!isRoomNameValid()) {
            showShortToast(getResources().getString(R.string.live_prepare_no_room_name));
            return;
        }

        mStartBroadBtn.setEnabled(false);

        Intent intent;
        switch (roomType) {
            case Config.LIVE_TYPE_SINGLE_HOST:
                intent = new Intent(this, SingleHostLiveActivity.class);
                break;
//            case Config.LIVE_TYPE_PK_HOST:
//                intent = new Intent(this, HostPKLiveActivity.class);
//                break;
            case Config.LIVE_TYPE_MULTI_HOST:
                intent = new Intent(this, MultiHostLiveActivity.class);
                break;
//            case Config.LIVE_TYPE_VIRTUAL_HOST:
//                intent = new Intent(this, VirtualHostLiveActivity.class);
//                break;
//            case Config.LIVE_TYPE_ECOMMERCE:
//                intent = new Intent(this, ECommerceLiveActivity.class);
//                break;
            default: return;
        }

        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }

        intent.putExtra(Global.Constants.KEY_ROOM_NAME, mEditText.getText().toString());
        startActivity(intent);
        // If we go live, we send a message to image select
        // activity that it does need to keep track in stack
        setResult(RESULT_GO_LIVE);
        mCameraPersist = true;
        finish();
    }

    @Override
    public void onActionSheetBeautyEnabled(boolean enabled) {
        findViewById(R.id.live_prepare_beauty_btn).setActivated(enabled);
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
    }

    @Override
    public void onActionSheetFrameRateSelected(int index) {
        config().setFrameRateIndex(index);
    }

    @Override
    public void onActionSheetBitrateSelected(int bitrate) {
        config().setVideoBitrate(bitrate);
    }

    @Override
    public void onActionSheetSettingBackPressed() {

    }

    @Override
    public void onBackPressed() {
        boolean fromVirtualImage = getIntent().getBooleanExtra(
                VirtualImageSelectActivity.
                        KEY_FROM_VIRTUAL_IMAGE, false);

        if (fromVirtualImage) {
            finish();
            return;
        }

        mExitDialog = showDialog(R.string.end_live_streaming_title_owner,
                R.string.end_live_streaming_message_owner, view -> {
                    dismissDialog();
                    finish();
                });
    }

    private void dismissDialog() {
        if (mExitDialog != null && mExitDialog.isShowing()) {
            mExitDialog.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPermissionGranted) {
            startCameraCapture();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!mCameraPersist && mCameraChannel != null && !mActivityFinished
                && mCameraChannel.hasCaptureStarted()) {
            mCameraChannel.stopCapture();
        }
    }

    @Override
    public void finish() {
        super.finish();
        dismissDialog();
        mActivityFinished = true;
        if (!mCameraPersist && mCameraChannel != null
                && mCameraChannel.hasCaptureStarted()) {
            mPreprocessor.onAnimojiSelected(-1);
            mCameraChannel.stopCapture();
        }
    }

    public void onPolicyClosed(View view) {
        if (view.getId() == R.id.live_prepare_policy_close) {
            findViewById(R.id.live_prepare_policy_caution_layout).setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoginASPRespone(LoginASPResponse response) {

    }
}
