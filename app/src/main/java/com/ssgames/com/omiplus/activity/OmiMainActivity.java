package com.ssgames.com.omiplus.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ssgames.com.omiplus.R;
import com.ssgames.com.omiplus.util.Constants;

public class OmiMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_omi_main);

        Button btnHost = (Button)findViewById(R.id.btnHostGame);
        btnHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hostButtonTapped();
            }
        });

        Button btnJoin = (Button)findViewById(R.id.btnJoinGame);
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinButtonTapped();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void hostButtonTapped() {
        Intent gameIntent = new Intent(OmiMainActivity.this, OmiGameActivity.class);
        gameIntent.putExtra(Constants.ExtraKey.EXTRA_KEY_HOST_OR_JOIN, true);
        startActivity(gameIntent);
    }

    private void joinButtonTapped() {
        Intent gameIntent = new Intent(OmiMainActivity.this, OmiGameActivity.class);
        gameIntent.putExtra(Constants.ExtraKey.EXTRA_KEY_HOST_OR_JOIN, false);
        startActivity(gameIntent);
    }
}
