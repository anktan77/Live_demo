package com.example.live_demo.ui.live;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.live_demo.BaseActivity;
import com.example.live_demo.R;
import com.example.live_demo.protocol.model.response.LoginASPResponse;
import com.example.live_demo.ui.main.MainActivity;
import com.example.live_demo.vlive.Config;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ProfileUserActivity extends BaseActivity implements View.OnClickListener {

    private AppCompatTextView mSignOutBtn, mProfileEmail;
    private AppCompatImageView mCloseProfile;
    GoogleSignInClient mGoogleSignInClient;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(true);
        setContentView(R.layout.activity_profile_user);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Init();

    }

    private void Init() {
        Config.UserProfile profile = config().getUserProfile();
        mSignOutBtn = findViewById(R.id.profile_user_sign_out_btn);
        mSignOutBtn.setOnClickListener(this);
        mCloseProfile = findViewById(R.id.profile_user_activity_close);
        mCloseProfile.setOnClickListener(this);
        mProfileEmail = (AppCompatTextView) findViewById(R.id.profile_user_email_text);
        mProfileEmail.setText(profile.getEmail());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.profile_user_sign_out_btn:
                signOutApp();
                break;
            case R.id.profile_user_activity_close:
                closeProfileUser();
                break;
        }
    }

    private void closeProfileUser() {
        finish();
    }


    private void signOutApp() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(ProfileUserActivity.this, LoginActivity.class);
                        // kết thúc all activity and fragments
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        finishAffinity();
                        startActivity(intent);
                        // khởi động lại ứng dụng
                        System.exit(0);
//                        finish();

                    }
                });
    }

    @Override
    public void onLoginASPRespone(LoginASPResponse response) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}