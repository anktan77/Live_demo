package com.example.live_demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.Window;

import com.example.live_demo.utils.Global;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Field;

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
        loadFragment(new HomeFragment());
    }

    private void initPrivacy() {
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

    private void hideStatusBar(boolean b) {
    }


    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}