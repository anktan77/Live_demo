package com.example.live_demo.ui.live;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.live_demo.BaseActivity;
import com.example.live_demo.R;
import com.example.live_demo.protocol.interfaces.LoginService;
import com.example.live_demo.protocol.model.request.Request;
import com.example.live_demo.protocol.model.request.UserRequest;
import com.example.live_demo.protocol.model.response.EditUserResponse;
import com.example.live_demo.protocol.model.response.LoginASPResponse;
import com.example.live_demo.utils.Global;
import com.example.live_demo.vlive.Config;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ModifyUserNameActivity extends BaseActivity
        implements View.OnClickListener, TextWatcher {
    private static final int MAX_NAME_LENGTH = 15;

    private AppCompatTextView mDoneBtn;
    private AppCompatEditText mNameEditText;
    private String mNewName;
    private String mOldName;
    private static Retrofit retrofit;
    private static final String Base_Url = "http://192.168.1.5:8097";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(true);
        setContentView(R.layout.activity_modify_user_name);
        init();
    }

    private void init() {
        findViewById(R.id.modify_user_name_cancel_btn).setOnClickListener(this);
        mDoneBtn = findViewById(R.id.modify_user_name_confirm_btn);
        mNameEditText = findViewById(R.id.modify_user_name_edit_text);
        mDoneBtn.setOnClickListener(this);
        mNameEditText.addTextChangedListener(this);

        mOldName = getIntent().getStringExtra(Global.Constants.KEY_USER_NAME);
        mNameEditText.setText(mOldName);
        mDoneBtn.setEnabled(mNameEditText.length() > 0);
    }

    public static Retrofit getRetrofit(){

        if (retrofit == null){
            retrofit = new Retrofit.Builder().baseUrl(Base_Url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    @Override
    protected void onGlobalLayoutCompleted() {
        View topLayout = findViewById(R.id.modify_user_name_title_layout);
        if (topLayout != null) {
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) topLayout.getLayoutParams();
            params.topMargin += systemBarHeight;
            topLayout.setLayoutParams(params);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.modify_user_name_confirm_btn) {
            if (TextUtils.isEmpty(mNameEditText.getText())) {
                showShortToast(getResources().getString(R.string.modify_user_name_empty));
                return;
            }

            mNewName = mNameEditText.getText().toString();
            if (!mNewName.equals(mOldName)) {
                postEditName();
            } else {
                finish();
            }
        } else if (v.getId() == R.id.modify_user_name_cancel_btn) {
            setResult(Global.Constants.EDIT_USER_NAME_RESULT_CANCEL);
            finish();
        }
    }

    private void postEditName() {
        Config.UserProfile profile = config().getUserProfile();
        String email = profile.getEmail();
        LoginService loginService =getRetrofit().create(LoginService.class);
        loginService.requestEditName(email,mNewName).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                int code = response.body();
                if (code == 0){
                    Toast.makeText(ModifyUserNameActivity.this, "Lỗi", Toast.LENGTH_SHORT).show();
                }
                if (code == 1){
                    UserRequest request = new UserRequest(config().getUserProfile().getToken(),
                            config().getUserProfile().getUserId(), mNewName);
                    sendRequest(Request.EDIT_USER, request);
                    profile.setUserName(mNewName);
                }
                if (code == 2){
                    Toast.makeText(ModifyUserNameActivity.this, "User name đã tồn tại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {

            }
        });
    }

    @Override
    public void onLoginASPRespone(LoginASPResponse response) {

    }

    @Override
    public void onEditUserResponse(EditUserResponse response) {
        if (response.data) {
            Intent data = new Intent();
            data.putExtra(Global.Constants.KEY_USER_NAME, mNewName);
            setResult(Global.Constants.EDIT_USER_NAME_RESULT_DONE, data);
            config().getUserProfile().setUserName(mNewName);
            preferences().edit().putString(Global.Constants.KEY_USER_NAME, mNewName).apply();
            finish();
        } else {
            showShortToast(getResources().getString(R.string.modify_user_name_error));
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(@NonNull Editable s) {
        if (s.length() <= 0) {
            mDoneBtn.setEnabled(false);
        } else if (s.length() > MAX_NAME_LENGTH) {
            mNameEditText.setText(s.subSequence(0, MAX_NAME_LENGTH));
            mNameEditText.setSelection(MAX_NAME_LENGTH);
            showShortToast(getResources().getString(R.string.modify_user_name_too_long));
        } else {
            mDoneBtn.setEnabled(true);
        }
    }
}