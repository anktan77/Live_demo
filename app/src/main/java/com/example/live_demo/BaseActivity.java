package com.example.live_demo;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity implements ClientProxyListener {
    private static final String TAG = BaseActivity.class.getSimpleName();

    protected static final int ACTION_SHEET_VIDEO = 0;
    protected static final int ACTION_SHEET_BEAUTY = 1;
    protected static final int ACTION_SHEET_BG_MUSIC = 2;
    protected static final int ACTION_SHEET_GIFT = 3;
    protected static final int ACTION_SHEET_TOOL = 4;
    protected static final int ACTION_SHEET_VOICE = 5;
    protected static final int ACTION_SHEET_INVITE_AUDIENCE = 6;
    protected static final int ACTION_SHEET_ROOM_USER = 7;
    protected static final int ACTION_SHEET_PK_ROOM_LIST = 8;
    protected static final int ACTION_SHEET_PRODUCT_LIST = 9;
    protected static final int ACTION_SHEET_PRODUCT_INVITE_ONLINE_SHOP = 10;

    private static final int ACTION_SHEET_DIALOG_STYLE_RES = R.style.live_room_dialog;
    private static final int TOAST_SHORT_INTERVAL = 2000;

    protected int systemBarHeight;
    protected int displayHeight;
    protected int displayWidth;
}
