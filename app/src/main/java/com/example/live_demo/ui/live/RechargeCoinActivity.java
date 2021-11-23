package com.example.live_demo.ui.live;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.live_demo.BaseActivity;
import com.example.live_demo.R;
import com.example.live_demo.protocol.interfaces.LoginService;
import com.example.live_demo.protocol.model.response.LoginASPResponse;
import com.example.live_demo.vlive.Config;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RechargeCoinActivity extends BaseActivity implements View.OnClickListener {

    private AppCompatTextView mConfirm, mCancel;
    private AppCompatEditText mCoinEditText;
    int setCoins;
    private static Retrofit retrofit;
    private static final String Base_Url = "http://10.0.2.2:8097";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge_coin);
        Init();

    }

    private void Init() {
        mConfirm = (AppCompatTextView) findViewById(R.id.recharge_coin_confirm_btn);
        mConfirm.setOnClickListener(this);
        mCancel = (AppCompatTextView) findViewById(R.id.recharge_coin_cancel_btn);
        mCancel.setOnClickListener(this);
        mCoinEditText = (AppCompatEditText) findViewById(R.id.recharge_coin_edit_text);
    }

    @Override
    public void onLoginASPRespone(LoginASPResponse response) {
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.recharge_coin_cancel_btn:
                finish();
                break;
            case R.id.recharge_coin_confirm_btn:
                RechargeCoin();
                break;
        }
    }

    private void RechargeCoin() {
        setCoins =Integer.parseInt(mCoinEditText.getText().toString()) ;
        Config.UserProfile profile = config().getUserProfile();
        String email = profile.getEmail();
        if (setCoins < 0){
            Toast.makeText(RechargeCoinActivity.this, "Coin phải lớn hơn 0", Toast.LENGTH_SHORT).show();
        }
        else {
            LoginService loginService =getRetrofit().create(LoginService.class);
            loginService.requestRechargeCoin(email,setCoins).enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.body() == 0){
                        Toast.makeText(RechargeCoinActivity.this, "Nạp thất bại", Toast.LENGTH_SHORT).show();
                    }
                    if (response.body() == 1){
                        Intent intent = new Intent(RechargeCoinActivity.this,ProfileUserActivity.class);
                        Toast.makeText(RechargeCoinActivity.this, "Nạp thành công", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(intent);
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {

                }
            });
        }

    }
}