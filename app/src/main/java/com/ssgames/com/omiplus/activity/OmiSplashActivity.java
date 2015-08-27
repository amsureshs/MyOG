package com.ssgames.com.omiplus.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ssgames.com.omiplus.R;
import com.ssgames.com.omiplus.util.OmiUtils;

public class OmiSplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_omi_splash);

        OmiUtils.isFreshStart = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(OmiSplashActivity.this, OmiMainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
