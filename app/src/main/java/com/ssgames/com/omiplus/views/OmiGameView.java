package com.ssgames.com.omiplus.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ssgames.com.omiplus.R;
import com.ssgames.com.omiplus.bluetooth.BTDataPacket;
import com.ssgames.com.omiplus.model.OmiHand;
import com.ssgames.com.omiplus.model.OmiPlayer;
import com.ssgames.com.omiplus.model.OmiRound;
import com.ssgames.com.omiplus.util.Constants;

import org.json.JSONObject;


public class OmiGameView extends LinearLayout {

    private static final String TAG = OmiGameView.class.getSimpleName();

    public interface OmiGameViewListener {

        public void playerDidDistributeCards(int[] player1Set, int[] player2Set, int[] player3Set, int[] player4Set);
        public void firstCardSetAppear();
        public void playerDidSelectTrumps(int suitNo, int option);
        public void secondCardSetAppear();
    }

    private OmiGameViewListener mOMOmiGameViewListener = null;
    private TextView playerName1 = null;
    private TextView playerName2 = null;
    private TextView playerName3 = null;
    private TextView playerName4 = null;

    public String myName = null;
    public int myPlayerNo = 0;
    public int myTeam = 0;

    public OmiHand mOmiHand = null;
    public OmiRound mOmiRound = null;

    private boolean iDidTheAction = false;

    private int[] myCards;

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

        String player1 = playerNames.optString(Constants.OmiJsonKey.PLAYER_NAME_1_KEY, "");
        String player2 = playerNames.optString(Constants.OmiJsonKey.PLAYER_NAME_2_KEY, "");
        String player3 = playerNames.optString(Constants.OmiJsonKey.PLAYER_NAME_3_KEY, "");
        String player4 = playerNames.optString(Constants.OmiJsonKey.PLAYER_NAME_4_KEY, "");

        if (player1.equalsIgnoreCase(myName)) {
            myPlayerNo = 1;
            myTeam = 1;
        }else if (player2.equalsIgnoreCase(myName)) {
            myPlayerNo = 2;
            myTeam = 2;
        }else if (player3.equalsIgnoreCase(myName)) {
            myPlayerNo = 3;
            myTeam = 1;
        }else if (player4.equalsIgnoreCase(myName)) {
            myPlayerNo = 4;
            myTeam = 2;
        }

        switch (myPlayerNo) {
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

    public void cmdShuffleThePack() {
        iDidTheAction = true;
    }

    public void playerShufflingPack(OmiPlayer omiPlayer, BTDataPacket btDataPacket) {
        String playerName = "";
        if (omiPlayer != null) {
            playerName = omiPlayer.getNickName();
        }else if (btDataPacket != null) {
            JSONObject bodyObj = btDataPacket.getBodyAsJson();
            if (bodyObj != null) {
                int playerNo = bodyObj.optInt(Constants.OmiJsonKey.PLAYER_NUMBER_KEY);
                playerName = getPlayerNameOf(playerNo);
            }
        }

        //TODO show shuffling
    }

    private void shuffleThePack(int option) {
        //shuffling algorithm is going here

        int[] player1Set = new int[8];
        int[] player2Set = new int[8];
        int[] player3Set = new int[8];
        int[] player4Set = new int[8];

        if (mOMOmiGameViewListener != null) mOMOmiGameViewListener.playerDidDistributeCards(player1Set, player2Set, player3Set, player4Set);

        switch (myPlayerNo) {
            case 1:
                showReceivedCards(player1Set);
                break;
            case 2:
                showReceivedCards(player2Set);
                break;
            case 3:
                showReceivedCards(player3Set);
                break;
            case 4:
                showReceivedCards(player4Set);
                break;
            default:
                break;
        }
    }

    public void showReceivedCards(int[] cards) {
        myCards = cards;
        //TODO show first set
    }

    private void firstSetShowAnimationEnd() {
        if (iDidTheAction) {
            iDidTheAction = false;
            if (mOMOmiGameViewListener != null) mOMOmiGameViewListener.firstCardSetAppear();
        }
    }

    public void cmdSelectTrumps() {
        iDidTheAction = true;
        //TODO show select trumps screen
    }

    public void playerSelectingTrumps(OmiPlayer omiPlayer) {
        //TODO show selecting
    }

    public void playerSelectedTrumps(int suitNo, int option) {
        //TODO show animation
        if (iDidTheAction) {
            if (mOMOmiGameViewListener != null) mOMOmiGameViewListener.playerDidSelectTrumps(suitNo, option);
        }else {
            if (option == 1) {
                showTrumpsSelectedFromSecondHand();
            }
        }
    }

    public void showTrumpsSelectedFromSecondHand() {
        //TODO
    }

    private void trumpsSelectAnimationEnd() {
        showReceivedCardsSecondSet();
    }

    private void showReceivedCardsSecondSet() {
        //TODO show first set
        //TODO show sort button
    }

    private void secondSetShowAnimationEnd() {

        showSortButton(true);

        if (iDidTheAction) {
            enablePlayCards(true, 0);
            iDidTheAction = false;
            if (mOMOmiGameViewListener != null) mOMOmiGameViewListener.secondCardSetAppear();
        }
    }

    private void showSortButton(boolean show) {
        //TODO
    }

    private void enablePlayCards(boolean enable, int suitNo) {
        if (enable) {
            iDidTheAction = true;
        }
        //TODO
    }

    private void playerPlayedWithOption() {

    }

    public void playerPlayedCard(OmiPlayer omiPlayer, BTDataPacket btDataPacket) {
        //TODO omiPlayer may have only playerNo
        //TODO
    }

    private void init(Context context, OmiGameViewListener omiGameViewListener) {
        mOMOmiGameViewListener = omiGameViewListener;

        View inflater = LayoutInflater.from(context).inflate(R.layout.view_game, this, true);

        playerName1  = (TextView)inflater.findViewById(R.id.txtPlayerName1);
        playerName2 = (TextView)inflater.findViewById(R.id.txtPlayerName2);
        playerName3 = (TextView)inflater.findViewById(R.id.txtPlayerName3);
        playerName4 = (TextView)inflater.findViewById(R.id.txtPlayerName4);
    }

    private String getPlayerNameOf(int playerNo) {
        String name = "";
        switch (playerNo) {
            case 1:
                name = playerName1.getText().toString();
                break;
            case 2:
                name = playerName2.getText().toString();
                break;
            case 3:
                name = playerName3.getText().toString();
                break;
            case 4:
                name = playerName4.getText().toString();
                break;
            default:
                break;
        }

        return name;
    }
}
