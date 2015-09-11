package com.ssgames.com.omiplus.views;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ssgames.com.omiplus.R;
import com.ssgames.com.omiplus.bluetooth.BTDataPacket;
import com.ssgames.com.omiplus.model.OmiGameStat;
import com.ssgames.com.omiplus.model.OmiHand;
import com.ssgames.com.omiplus.model.OmiPlayer;
import com.ssgames.com.omiplus.model.OmiRound;
import com.ssgames.com.omiplus.util.Constants;

import org.json.JSONObject;

import java.util.Random;


public class OmiGameView extends LinearLayout {

    private static final String TAG = OmiGameView.class.getSimpleName();

    public static double cardRatio = 1.4;//height/width

    public interface OmiGameViewListener {

        public void playerDidDistributeCards(int[] player1Set, int[] player2Set, int[] player3Set, int[] player4Set);
        public void firstCardSetAppear();
        public void playerDidSelectTrumps(int suitNo, int option);
        public void secondCardSetAppear();
        public void playerDidPlayCard(int cardNo, int option);
        public void timeToNextHand();
        public void gameDidEndWithWinningTeam(int team);
    }

    private Context mContext = null;
    private OmiGameViewListener mOMOmiGameViewListener = null;
    private TextView playerName1 = null;
    private TextView playerName2 = null;
    private TextView playerName3 = null;
    private TextView playerName4 = null;

    private TextView txtWon1 = null;
    private TextView txtWon2 = null;
    private TextView txtWon3 = null;
    private TextView txtWon4 = null;

    private ImageButton btnSettings = null;
    private ImageButton btnLastHand = null;

    private LinearLayout trumpsSelectLayout = null;
    private ImageButton btnTrumps1 = null;
    private ImageButton btnTrumps2 = null;
    private ImageButton btnTrumps3 = null;
    private ImageButton btnTrumps4 = null;

    private LinearLayout cardPlayedLayout = null;
    private ImageView imgCard1 = null;
    private ImageView imgCard2 = null;
    private ImageView imgCard3 = null;
    private ImageView imgCard4 = null;

    private ImageButton btnSortHand = null;
    private ImageButton btnShowChat = null;

    private RelativeLayout cardsLayout = null;
    private LinearLayout chatLayout = null;
    private LinearLayout shuffleLayout = null;

    private ImageButton btnShuffle1 = null;
    private ImageButton btnShuffle2 = null;
    private ImageButton btnShuffle3 = null;

    private AlertDialog mAlertDialog = null;

    public String myName = null;
    public int myPlayerNo = 0;
    public int myTeam = 0;

    public OmiGameStat mOmiGameStat = null;
    public OmiHand mOmiHand = null;
    public OmiRound mOmiRound = null;

    private boolean iDidTheAction = false;

    private int[] mPack = null;
    private int[] myCards;
    private int calcCardH;

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
        if (mPack == null) initPack();

        shuffleLayout.setVisibility(View.VISIBLE);
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

        CharSequence text = playerName + " is shuffling the pack.";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(mContext, text, duration);
        toast.show();
    }

    private void playerShuffledThePack(int option) {


        shuffleLayout.setVisibility(View.GONE);
        //TODO show animation

//        final float xp = shuffleLayout.getX();
//        final float ysp = shuffleLayout.getY();
//        final float yep = ysp + shuffleLayout.getHeight();
//
//        TranslateAnimation tAnimation = new TranslateAnimation(xp,xp,ysp,yep);
//        tAnimation.setDuration(600);
//        tAnimation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                shuffleLayout.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        shuffleLayout.startAnimation(tAnimation);


        int shuffleCount = 0;
        switch (option) {
            case 1:
                shuffleCount = 5;
                break;
            case 2:
                shuffleCount = 10;
                break;
            case 3:
                shuffleCount = 15;
                break;
            default:
                break;
        }

        mPack = getShuffledPack(shuffleCount);

        int[] player1Set = new int[8];
        int[] player2Set = new int[8];
        int[] player3Set = new int[8];
        int[] player4Set = new int[8];

        int[] set1 = new int[8];
        int[] set2 = new int[8];
        int[] set3 = new int[8];
        int[] set4 = new int[8];

        for (int i = 0; i < 4; i++) {
            set1[i] = mPack[i];
            set1[i+4] = mPack[i+16];

            set2[i] = mPack[i+4];
            set2[i+4] = mPack[i+16+4];

            set3[i] = mPack[i+4+4];
            set3[i+4] = mPack[i+16+4+4];

            set4[i] = mPack[i+4+4+4];
            set4[i+4] = mPack[i+16+4+4+4];
        }

        switch (myPlayerNo) {
            case 1:
                player1Set = set4;
                player2Set = set1;
                player3Set = set2;
                player4Set = set3;
                break;
            case 2:
                player1Set = set3;
                player2Set = set4;
                player3Set = set1;
                player4Set = set2;
                break;
            case 3:
                player1Set = set2;
                player2Set = set3;
                player3Set = set4;
                player4Set = set1;
                break;
            case 4:
                player1Set = set1;
                player2Set = set2;
                player3Set = set3;
                player4Set = set4;
                break;
            default:
                break;
        }

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
        cardsLayout.setVisibility(View.VISIBLE);
        cardsLayout.post(new Runnable() {
            @Override
            public void run() {
                showFirstHandInCardsLayout();
            }
        });
    }

    private void showFirstHandInCardsLayout() {

        for (int i = 0; i < cardsLayout.getChildCount(); i++) {
            View v = cardsLayout.getChildAt(i);
            cardsLayout.removeView(v);
        }

        int layerW = cardsLayout.getWidth();
        int layerH = cardsLayout.getHeight();

        int cardW = layerW/4;
        int cardH = layerH;

        cardH = getHeightOfACard(cardW, cardH);
        cardW = (int)(cardH / cardRatio);

        calcCardH = cardH;
        final float yp = (layerH - cardH)/2;
        for (int i = 0; i < 4; i++) {
            final OmiCard omiCard = new OmiCard(mContext, cardW, cardH);
            omiCard.setCardNo(myCards[i]);
            cardsLayout.addView(omiCard);

            final float xp = cardW * (3 - i);

            final boolean animEnd = (i == 3);

            TranslateAnimation tAnimation = new TranslateAnimation(0,xp,yp,yp);
            tAnimation.setDuration(1000);
            tAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    omiCard.setX(xp);
                    omiCard.setY(yp);

                    if (animEnd) {
                        firstSetShowAnimationEnd();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            omiCard.startAnimation(tAnimation);
        }


    }

    private void firstSetShowAnimationEnd() {
        if (iDidTheAction) {
            iDidTheAction = false;
            if (mOMOmiGameViewListener != null) mOMOmiGameViewListener.firstCardSetAppear();
        }

        cmdSelectTrumps();

//        int trumpSelPlayerNo = mOmiHand.getShuffledPlayerNo() + 1;
//        if (mOmiHand.getShuffledPlayerNo() == 4) {
//            trumpSelPlayerNo = 1;
//        }
//
//        if (trumpSelPlayerNo == myPlayerNo) {
//            cmdSelectTrumps();
//        }else {
//            OmiPlayer omiPlayer = new OmiPlayer();
//            omiPlayer.setPlayerNo(trumpSelPlayerNo);
//            playerSelectingTrumps(omiPlayer);
//        }
    }

    public void cmdSelectTrumps() {
        iDidTheAction = true;
        trumpsSelectLayout.setVisibility(View.VISIBLE);
        //TODO show select trumps screen
    }

    private void showFourSuitTrumpsSelectMessage() {
        int suitNo1 = getSuitOfCard(myCards[0]);
        int suitNo2 = getSuitOfCard(myCards[1]);
        int suitNo3 = getSuitOfCard(myCards[2]);
        int suitNo4 = getSuitOfCard(myCards[3]);

        if (suitNo1 != suitNo2 && suitNo1 != suitNo3 && suitNo1 != suitNo4 && suitNo2 != suitNo3 && suitNo2 != suitNo4 && suitNo3 != suitNo4) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Do you need select trumps from next card?");
            builder.setMessage("Since your cards have four different suits you can choose suit of the next card as trumps.");
            builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAlertDialog.dismiss();
                    showNextHandFirstCard();
                }
            });
            builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAlertDialog.dismiss();
                }
            });
            mAlertDialog = builder.create();
            mAlertDialog.setCanceledOnTouchOutside(false);
            mAlertDialog.setCancelable(false);
            mAlertDialog.show();
        }
    }

    private void showNextHandFirstCard() {
        //TODO
    }

    public void playerSelectingTrumps(OmiPlayer omiPlayer) {
        //TODO show selecting
    }

    public void playerSelectedTrumps(int suitNo, int option) {

        mOmiHand.setTrumps(suitNo);

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

    //this is call after current player played or on command
    private void playerPlayedWithOption(int playerNo, int cardNo, int option) {

        if (mOmiRound == null) {
            mOmiRound = new OmiRound();
            mOmiRound.setTrumps(mOmiHand.getTrumps());
            mOmiRound.setSuit(getSuitOfCard(cardNo));

            OmiRound lastRound = mOmiHand.getCurrentRound();

            if (lastRound == null) {
                mOmiRound.setRoundNo(1);
                mOmiHand.setCurrentRound(mOmiRound);
                mOmiHand.setLastRound(mOmiRound);
            }else {
                mOmiRound.setRoundNo(lastRound.getRoundNo() + 1);
                mOmiHand.setCurrentRound(mOmiRound);
                mOmiHand.setLastRound(lastRound);
            }
        }

        if (mOmiRound.getStartedPlayerNo() == 0) {
            mOmiRound.setStartedPlayerNo(myPlayerNo);
        }

        mOmiRound.setPlayerCard(myPlayerNo, cardNo);

        if (iDidTheAction) {
            if (mOMOmiGameViewListener != null) mOMOmiGameViewListener.playerDidPlayCard(cardNo, option);
        }
        //TODO show play card animation
    }

    public void playerPlayedCard(OmiPlayer omiPlayer, BTDataPacket btDataPacket) {

        JSONObject bodyJson = btDataPacket.getBodyAsJson();

        int playerNo = omiPlayer.getPlayerNo();
        int cardNo = bodyJson.optInt(Constants.OmiJsonKey.CARD_NUMBER_KEY);
        int option = bodyJson.optInt(Constants.OmiJsonKey.OPTION_KEY);

        playerPlayedWithOption(playerNo, cardNo, option);

        //TODO omiPlayer may have only playerNo
        //TODO
    }

    private void playCardAnimationEnd() {
        judgeGame();
    }

    private void judgeGame() {
        if (mOmiRound.didAllPlayersPlay()) {
            int winner = mOmiRound.getWinner();
            mOmiHand.addWinToPlayer(winner);
            mOmiHand.addLastRoundToPack();
            updatePack();
            mOmiGameStat.addWinToPlayer(winner);

            if (mOmiHand.isHandOver()) {

                int winningTeam = mOmiHand.getWinningTeam();
                if (winningTeam == 1) {
                    mOmiGameStat.addWinToTeam(1);
                }else if (winningTeam == 2) {
                    mOmiGameStat.addWinToTeam(2);
                }else {
                    mOmiGameStat.addDraws();
                }

                int endReach = mOmiGameStat.getTeamIfWin();
                if (endReach == 0) {
                    if (mOMOmiGameViewListener != null) mOMOmiGameViewListener.timeToNextHand();
                }else if (endReach == 1) {
                    showWinningScreen(1);
                }else if (endReach == 2) {
                    showWinningScreen(2);
                }
            }else {
                if (winner == myPlayerNo) {
                    mOmiRound = null;
                    enablePlayCards(true, 0);
                }
            }
        } else {
            int nextPlayer = mOmiRound.getNextPlayer();
            if (nextPlayer == myPlayerNo) {
                enablePlayCards(true, mOmiRound.getSuit());
            }
        }
    }

    private void showWinningScreen(int team) {

    }

    private void winningScreenAnimationEnd() {
        if (mOMOmiGameViewListener != null) mOMOmiGameViewListener.gameDidEndWithWinningTeam(mOmiGameStat.getTeamIfWin());
    }

    private void init(Context context, OmiGameViewListener omiGameViewListener) {
        mContext = context;
        mOMOmiGameViewListener = omiGameViewListener;
        //TODO
        mOMOmiGameViewListener = null;
        myPlayerNo = 1;

        mOmiGameStat = new OmiGameStat();
        View inflater = LayoutInflater.from(context).inflate(R.layout.view_game, this, true);

        playerName1  = (TextView)inflater.findViewById(R.id.txtPlayerName1);
        playerName2 = (TextView)inflater.findViewById(R.id.txtPlayerName2);
        playerName3 = (TextView)inflater.findViewById(R.id.txtPlayerName3);
        playerName4 = (TextView)inflater.findViewById(R.id.txtPlayerName4);

        txtWon1  = (TextView)inflater.findViewById(R.id.txtWon1);
        txtWon2  = (TextView)inflater.findViewById(R.id.txtWon2);
        txtWon3  = (TextView)inflater.findViewById(R.id.txtWon3);
        txtWon4  = (TextView)inflater.findViewById(R.id.txtWon4);

        btnSettings = (ImageButton)inflater.findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnLastHand = (ImageButton)inflater.findViewById(R.id.btnLastHand);
        btnLastHand.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        trumpsSelectLayout = (LinearLayout)inflater.findViewById(R.id.trumpsSelectLayout);
        trumpsSelectLayout.setVisibility(View.GONE);

        btnTrumps1 = (ImageButton)inflater.findViewById(R.id.btnTrumps1);
        btnTrumps1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnTrumps2 = (ImageButton)inflater.findViewById(R.id.btnTrumps2);
        btnTrumps2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnTrumps3 = (ImageButton)inflater.findViewById(R.id.btnTrumps3);
        btnTrumps3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnTrumps4 = (ImageButton)inflater.findViewById(R.id.btnTrumps4);
        btnTrumps4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        cardPlayedLayout = (LinearLayout)inflater.findViewById(R.id.cardPlayedLayout);
        cardPlayedLayout.setVisibility(View.GONE);

        imgCard1 = (ImageView)inflater.findViewById(R.id.imgCard1);
        imgCard2 = (ImageView)inflater.findViewById(R.id.imgCard2);
        imgCard3 = (ImageView)inflater.findViewById(R.id.imgCard3);
        imgCard4 = (ImageView)inflater.findViewById(R.id.imgCard4);

        btnSortHand = (ImageButton)inflater.findViewById(R.id.btnSortHand);
        btnSortHand.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cmdShuffleThePack();
            }
        });
        //btnSortHand.setVisibility(View.INVISIBLE);

        btnShowChat = (ImageButton)inflater.findViewById(R.id.btnShowChat);
        btnShowChat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        cardsLayout = (RelativeLayout)inflater.findViewById(R.id.cardsLayout);
        cardsLayout.setVisibility(View.VISIBLE);

        chatLayout = (LinearLayout)inflater.findViewById(R.id.chatLayout);
        chatLayout.setVisibility(View.GONE);

        shuffleLayout = (LinearLayout)inflater.findViewById(R.id.shuffleLayout);
        shuffleLayout.setVisibility(View.GONE);

        btnShuffle1 = (ImageButton)inflater.findViewById(R.id.btnShuffle1);
        btnShuffle1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playerShuffledThePack(1);
            }
        });

        btnShuffle2 = (ImageButton)inflater.findViewById(R.id.btnShuffle2);
        btnShuffle2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playerShuffledThePack(2);
            }
        });

        btnShuffle3 = (ImageButton)inflater.findViewById(R.id.btnShuffle3);
        btnShuffle3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playerShuffledThePack(3);
            }
        });
    }

    private void initPack() {
        mPack = new int[32];
        int sp = 107;
        int he = 207;
        int cl = 307;
        int di = 407;

        int j = 8;
        int k = 16;
        int l = 24;
        for (int i = 0; i < 8; i++) {
            mPack[i] = sp;
            mPack[j] = he;
            mPack[k] = cl;
            mPack[l] = di;

            j++; k++; l++;
            sp++; he++; cl++; di++;
        }

        mPack = getShuffledPack(3);
    }

    private void updatePack() {

        if (mPack == null) mPack = new int[32];

        int roundNo = mOmiRound.getRoundNo();
        int startIndex = (roundNo - 1) * 4;
        switch (mOmiRound.getStartedPlayerNo()) {
            case 1:
                mPack[startIndex] = mOmiRound.getPlayer1Card();
                mPack[startIndex+1] = mOmiRound.getPlayer2Card();
                mPack[startIndex+2] = mOmiRound.getPlayer3Card();
                mPack[startIndex+3] = mOmiRound.getPlayer4Card();
                break;
            case 2:
                mPack[startIndex] = mOmiRound.getPlayer2Card();
                mPack[startIndex+1] = mOmiRound.getPlayer3Card();
                mPack[startIndex+2] = mOmiRound.getPlayer4Card();
                mPack[startIndex+3] = mOmiRound.getPlayer1Card();
                break;
            case 3:
                mPack[startIndex] = mOmiRound.getPlayer3Card();
                mPack[startIndex+1] = mOmiRound.getPlayer4Card();
                mPack[startIndex+2] = mOmiRound.getPlayer1Card();
                mPack[startIndex+3] = mOmiRound.getPlayer2Card();
                break;
            case 4:
                mPack[startIndex] = mOmiRound.getPlayer4Card();
                mPack[startIndex+1] = mOmiRound.getPlayer1Card();
                mPack[startIndex+2] = mOmiRound.getPlayer2Card();
                mPack[startIndex+3] = mOmiRound.getPlayer3Card();
                break;
            default:
                break;
        }
    }

    private int[] getShuffledPack(int shuffleCount) {
        int[] newPack = new int[32];

        Random r = new Random();
        for (int i = 0; i < shuffleCount; i++) {
            int rand = r.nextInt(7) + 5;

            int range1 = (32/2) - (rand/2);
            int range2 = range1 + rand;

            int pi = 0;
            for (int j = range1; j < range2; j++) {
                newPack[pi] = mPack[j];
                pi++;
            }

            for (int j = range2; j < 32; j++) {
                newPack[pi] = mPack[j];
                pi++;
            }

            for (int j = 0; j < range1; j++) {
                newPack[pi] = mPack[j];
                pi++;
            }
        }

        return newPack;
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

    private int getSuitOfCard(int cardNo) {
        if (cardNo < 200) {
            return Constants.OmiSuit.SPADES;
        }else if (cardNo < 300) {
            return Constants.OmiSuit.HEARTS;
        }else if (cardNo < 400) {
            return Constants.OmiSuit.CLUBS;
        }else if (cardNo < 500) {
            return Constants.OmiSuit.DIAMONDS;
        }else {
            return Constants.OmiSuit.NONE;
        }
    }

    private int getHeightOfACard(int containerW, int containerH) {

        double hFromW = containerW * cardRatio;

        if (hFromW <= containerH) {
            return (int)hFromW;
        }else {
            return containerH;
        }
    }
}
