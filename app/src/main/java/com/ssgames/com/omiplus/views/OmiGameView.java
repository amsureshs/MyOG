package com.ssgames.com.omiplus.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ssgames.com.omiplus.R;
import com.ssgames.com.omiplus.util.Constants;

import org.json.JSONObject;


public class OmiGameView extends LinearLayout {

    private static final String TAG = OmiGameView.class.getSimpleName();

    public interface OmiGameViewListener {
        public void visibleButtonTapped();
        public void partnerSelected(String partnerName);
    }

    private OmiGameViewListener mOMOmiGameViewListener = null;
    private TextView playerName1 = null;
    private TextView playerName2 = null;
    private TextView playerName3 = null;
    private TextView playerName4 = null;

    public String myName = null;
    private int myPlace = 0;

    public OmiGameView(Context context, OmiGameViewListener omiGameViewListener) {
        super(context);
        init(context, omiGameViewListener);
    }

    public OmiGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OmiGameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPlayerNames(JSONObject playerNames) {
        if (playerNames == null) {
            Log.v(TAG, "Null player names");
            return;
        }

        Log.v(TAG, "My Name: " + myName);
        Log.v(TAG, "Player names: " + playerNames.toString());

        String player1 = playerNames.optString(Constants.MultiPlayerKey.PLAYER_NAME_1_KEY, "");
        String player2 = playerNames.optString(Constants.MultiPlayerKey.PLAYER_NAME_2_KEY, "");
        String player3 = playerNames.optString(Constants.MultiPlayerKey.PLAYER_NAME_3_KEY, "");
        String player4 = playerNames.optString(Constants.MultiPlayerKey.PLAYER_NAME_4_KEY, "");

        if (player1.equalsIgnoreCase(myName)) {
            myPlace = 1;
        }else if (player2.equalsIgnoreCase(myName)) {
            myPlace = 2;
        }else if (player3.equalsIgnoreCase(myName)) {
            myPlace = 3;
        }else if (player4.equalsIgnoreCase(myName)) {
            myPlace = 4;
        }

        switch (myPlace) {
            case 1:
                playerName1.setText(player1);
                playerName2.setText(player2);
                playerName3.setText(player3);
                playerName4.setText(player4);
                break;
            case 2:
                playerName1.setText(player2);
                playerName2.setText(player3);
                playerName3.setText(player4);
                playerName4.setText(player1);
                break;
            case 3:
                playerName1.setText(player3);
                playerName2.setText(player4);
                playerName3.setText(player1);
                playerName4.setText(player2);
                break;
            case 4:
                playerName1.setText(player4);
                playerName2.setText(player1);
                playerName3.setText(player2);
                playerName4.setText(player3);
                break;
            default:
                break;
        }

    }

    private void init(Context context, OmiGameViewListener omiGameViewListener) {
        mOMOmiGameViewListener = omiGameViewListener;

        View inflater = LayoutInflater.from(context).inflate(R.layout.view_game, this, true);

        playerName1  = (TextView)inflater.findViewById(R.id.txtPlayerName1);
        playerName2 = (TextView)inflater.findViewById(R.id.txtPlayerName2);
        playerName3 = (TextView)inflater.findViewById(R.id.txtPlayerName3);
        playerName4 = (TextView)inflater.findViewById(R.id.txtPlayerName4);
    }
}
