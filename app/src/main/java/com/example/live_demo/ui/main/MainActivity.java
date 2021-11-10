package com.example.live_demo.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.os.Handler;

import com.elvishew.xlog.XLog;
import com.example.live_demo.BaseActivity;
import com.example.live_demo.R;
import com.example.live_demo.protocol.model.request.Request;
import com.example.live_demo.protocol.model.request.UserRequest;
import com.example.live_demo.protocol.model.response.AppVersionResponse;
import com.example.live_demo.protocol.model.response.CreateUserResponse;
import com.example.live_demo.protocol.model.response.GiftListResponse;
import com.example.live_demo.protocol.model.response.LoginASPResponse;
import com.example.live_demo.protocol.model.response.LoginResponse;
import com.example.live_demo.protocol.model.response.MusicListResponse;
import com.example.live_demo.protocol.model.response.Response;
import com.example.live_demo.ui.components.PrivacyTermsDialog;
import com.example.live_demo.utils.Global;
import com.example.live_demo.utils.RandomUtil;
import com.example.live_demo.vlive.Config;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Field;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;

// BaseActivity luôn thực hiện trước
public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int NETWORK_CHECK_INTERVAL = 10000;
    private static final int MAX_PERIODIC_APP_ID_TRY_COUNT = 5;

    private BottomNavigationView mNavView;
    private NavController mNavController;
    private int mAppIdTryCount;
    private PrivacyTermsDialog termsDialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(true);
        setContentView(R.layout.activity_main);
        initPrivacy();
        initUI();
        initAsync();
//        loadFragment(new HomeFragment());
    }

    private void initPrivacy() {
        //kiểm tra có lưu KEY_SHOW_PRIVACY vào share preferences chưa
        if (!preferences().getBoolean(Global.Constants.KEY_SHOW_PRIVACY, false)) {
            termsDialog = new PrivacyTermsDialog(this);
            termsDialog.setPrivacyTermsDialogListener(new PrivacyTermsDialog.OnPrivacyTermsDialogListener() {
                @Override
                public void onPositiveClick() {
                    preferences().edit().putBoolean(Global.Constants.KEY_SHOW_PRIVACY, true).apply();
                }

                @Override
                public void onNegativeClick() {
                    finish();
                }
            });
            termsDialog.show();
        }
    }

    private void initUI() {
        initNavigation();
    }

    private void initNavigation() {
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);

        mNavView = findViewById(R.id.nav_view);
        mNavView.setItemIconTintList(null);
        changeItemHeight(mNavView);
        mNavView.setOnNavigationItemSelectedListener(item -> {
            int selectedId = item.getItemId();
            int currentId = mNavController.getCurrentDestination() == null ?
                    0 : mNavController.getCurrentDestination().getId();

            // Do not respond to this click event because
            // we do not want to refresh this fragment
            // by repeatedly selecting the same menu item.
            if (selectedId == currentId) return false;
            NavigationUI.onNavDestinationSelected(item, mNavController);
            hideStatusBar(getWindow(), true);
            return true;
        });
    }

    public int getSystemBarHeight() {
        return systemBarHeight;
    }

    public void setNavigationSelected(int resId, Bundle bundle) {
        mNavView.setSelectedItemId(resId);
        mNavController.navigate(resId, bundle);
    }

    private void changeItemHeight(@NonNull BottomNavigationView navView) {
        // Bottom navigation menu uses a hardcode menu item
        // height which cannot be changed by a layout attribute.
        // Change the item height using reflection for
        // a comfortable padding between icon and label.
        int itemHeight = getResources().getDimensionPixelSize(R.dimen.nav_bar_height);
        BottomNavigationMenuView menu =
                (BottomNavigationMenuView) navView.getChildAt(0);
        try {
            Field itemHeightField = BottomNavigationMenuView.class.getDeclaredField("itemHeight");
            itemHeightField.setAccessible(true);
            itemHeightField.set(menu, itemHeight);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // xử lý chạy đa luồng
    private void initAsync() {
        new Thread(() -> {
            checkUpdate();
            getGiftList();
            getMusicList();
        }).start();
    }

    // tiến hành request app version (chạy đa luồng)
    private void checkUpdate() {
        // kiểm tra appId == null thì thực hiện
        if (!config().hasCheckedVersion()) {
            sendRequest(Request.APP_VERSION, getAppVersion());
        }
    }

    @Override
    public void onLoginASPRespone(LoginASPResponse response) {

    }
    // quy trình: Override luôn thực hiện đầu tiên

    // sau khi thực hiện request appid ở dòng 141
    // thì sẽ response về onAppVersionResponse (lưu dữ liệu ở class ClientProxyListener)
    @Override
    public void onAppVersionResponse(AppVersionResponse response) {
        // tiến hành set vào config
        config().setVersionInfo(response.data);
        config().setAppId(response.data.config.appId);
        // set vào SharkLiveApplication
        application().initEngine(response.data.config.appId);
        mAppIdTryCount = 0;
        login();
    }

    private void login() {
        // khai báo
        Config.UserProfile profile = config().getUserProfile();
        // lấy dữ liệu trong share preferences set vào profile
        initUserFromStorage(profile);
        // nếu profile = null
        if (!profile.isValid()) {
            createUser();
        } else {
            loginToServer();
        }
    }

    //lưu user profile vào Share references
    private void initUserFromStorage(Config.UserProfile profile) {
        profile.setUserId(preferences().getString(Global.Constants.KEY_PROFILE_UID, null));
        profile.setUserName(preferences().getString(Global.Constants.KEY_USER_NAME, null));
        profile.setImageUrl(preferences().getString(Global.Constants.KEY_IMAGE_URL, null));
        profile.setToken(preferences().getString(Global.Constants.KEY_TOKEN, null));
    }

    private void createUser() {
        //random username trong class randomUtil
        String userName = RandomUtil.randomUserName(this);

        //truyền vào config().getUserProfile()
        config().getUserProfile().setUserName(userName);

        //lưu tên username vào lưu trữ tạm thời trên máy với mã lưu KEY_USER_NAME = "key-user-name"
        preferences().edit().putString(Global.Constants.KEY_USER_NAME, userName).apply();

        // request api để nhận id token mới,
        // CREATE_USER = 5
        sendRequest(Request.CREATE_USER, new UserRequest(userName));
    }

    // sau khi thực hiện request create user ở dòng 197
    // thì sẽ response về onCreateUserResponse (lưu dữ liệu ở class ClientProxyListener)
    // trả về userId
    @Override
    public void onCreateUserResponse(CreateUserResponse response) {
        createUserFromResponse(response);
        loginToServer();
    }


    private void createUserFromResponse(CreateUserResponse response) {
        Config.UserProfile profile = config().getUserProfile();

        //set userid sau khi gọi api tạo user vào profile
        profile.setUserId(response.data.userId);

        //lưu user id vào máy
        preferences().edit().putString(Global.Constants.KEY_PROFILE_UID, profile.getUserId()).apply();
    }

    // tiến hành login server khi đã tạo user token lưu vào máy
    // login bằng user id
    private void loginToServer() {
        // USER_LOGIN = 7
        sendRequest(Request.USER_LOGIN, config().getUserProfile().getUserId());
    }

    // sau khi thực hiện request login ở dòng 224
    // thì sẽ response về onLoginResponse (lưu dữ liệu ở class ClientProxyListener)
    //sau khi login và nhận data, set vào profile
    @Override
    public void onLoginResponse(LoginResponse response) {
        if (response != null && response.code == Response.SUCCESS) {
            Config.UserProfile profile = config().getUserProfile();
            profile.setToken(response.data.userToken);
            profile.setRtmToken(response.data.rtmToken);
            profile.setAgoraUid(response.data.uid);
            preferences().edit().putString(Global.Constants.KEY_TOKEN, response.data.userToken).apply();
            joinRtmServer();
        }
    }

    private void joinRtmServer() {
        Config.UserProfile profile = config().getUserProfile();
        // gửi rtm token, uid
        rtmClient().login(profile.getRtmToken(), String.valueOf(profile.getAgoraUid()), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                XLog.d("rtm client login success:" + config().getUserProfile().getRtmToken());
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });
    }

    private void getGiftList() {
        sendRequest(Request.GIFT_LIST, null);
    }

    @Override
    public void onGiftListResponse(GiftListResponse response) {
        config().initGiftList(this);
    }

    private void getMusicList() {
        sendRequest(Request.MUSIC_LIST, null);
    }

    @Override
    public void onMusicLisResponse(MusicListResponse response) {
        config().setMusicList(response.data);
    }

    @Override
    public void onResponseError(int requestType, int error, String message) {
        XLog.e("request:" + requestType + " error:" + error + " msg:" + message);

        switch (requestType) {
            case Request.APP_VERSION:
                if (mAppIdTryCount <= MAX_PERIODIC_APP_ID_TRY_COUNT) {
                    new Handler(getMainLooper()).postDelayed(
                            MainActivity.this::checkUpdate, NETWORK_CHECK_INTERVAL);
                    mAppIdTryCount++;
                }
                break;
            default: runOnUiThread(() -> showLongToast("Request type: "+
                    Request.getRequestString(requestType) + " " + message));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (termsDialog != null) {
            termsDialog.dismiss();
        }
    }


    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}