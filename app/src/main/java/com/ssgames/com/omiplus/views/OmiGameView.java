package com.ssgames.com.omiplus.views;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
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

import java.util.ArrayList;
import java.util.Arrays;
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
    private ImageButton btnPlayedCard1 = null;
    private ImageButton btnPlayedCard2 = null;
    private ImageButton btnPlayedCard3 = null;
    private ImageButton btnPlayedCard4 = null;

    private ImageButton btnSortHand = null;
    private ImageButton btnShowChat = null;

    private RelativeLayout cardsLayout = null;
    private LinearLayout chatLayout = null;
    private LinearLayout shuffleLayout = null;
    private LinearLayout nextCardTrumps = null;

    private ImageButton btnShuffle1 = null;
    private ImageButton btnShuffle2 = null;
    private ImageButton btnShuffle3 = null;

    private LinearLayout trumpsShowLayout = null;
    private ImageView imgTrumpsView = null;

    private RelativeLayout animationLayout = null;

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

    private ArrayList<OmiCard> omiCards = null;

    private boolean lastEnable = false;
    private int lastEnaSuit = 0;

    private int lastPlayNo = 0;

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

        final float xp = shuffleLayout.getX();
        final float ysp = shuffleLayout.getY();
        final float yep = ysp + shuffleLayout.getHeight();

        TranslateAnimation tAnimation = new TranslateAnimation(xp,xp,ysp,yep);
        tAnimation.setDuration(600);
        tAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                shuffleLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        shuffleLayout.startAnimation(tAnimation);


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

        for (int i = 0; i < 32; i++) {
            Log.v(TAG, "Card " + mPack[i]);
        }

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

        ArrayList<View> views = new ArrayList<>();

        for (int i = 0; i < cardsLayout.getChildCount(); i++) {
            View v = cardsLayout.getChildAt(i);
            v.setVisibility(View.GONE);
            views.add(v);
        }

        for (View v: views) {
            cardsLayout.removeView(v);
        }
        views.clear();

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

            final float xp = cardW * i;

            final boolean animEnd = (i == 3);

            TranslateAnimation tAnimation = new TranslateAnimation(0,xp,yp,yp);
            tAnimation.setDuration(1000);
            tAnimation.setFillBefore(false);
            tAnimation.setFillEnabled(true);
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

        //TODO TEST
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

        CharSequence text = "Select trumps.";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(mContext, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        trumpsSelectLayout.post(new Runnable() {
            @Override
            public void run() {
                showTrumpsSelectView();
            }
        });
    }

    private void showTrumpsSelectView() {
        int layerW = btnTrumps1.getWidth();
        int layerH = btnTrumps1.getHeight();

        int cardH = getHeightOfACard(layerH, layerW);
        int cardW = (int)(cardH / cardRatio);

        Bitmap spaidsMap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.spaids);
        Bitmap reSpaidsMap = Bitmap.createScaledBitmap(spaidsMap, cardW, cardH, false);
        btnTrumps1.setImageBitmap(reSpaidsMap);

        Bitmap heartsMap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.hearts);
        Bitmap reHeartsMap = Bitmap.createScaledBitmap(heartsMap, cardW, cardH, false);
        btnTrumps2.setImageBitmap(reHeartsMap);

        Bitmap clubsMap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.clubs);
        Bitmap reClubsMap = Bitmap.createScaledBitmap(clubsMap, cardW, cardH, false);
        btnTrumps4.setImageBitmap(reClubsMap);

        Bitmap diamMap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.diamonds);
        Bitmap reDiamMap = Bitmap.createScaledBitmap(diamMap, cardW, cardH, false);
        btnTrumps3.setImageBitmap(reDiamMap);

        showFourSuitTrumpsSelectMessage();
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
        trumpsSelectLayout.setVisibility(View.GONE);
        nextCardTrumps.setVisibility(View.VISIBLE);

        nextCardTrumps.post(new Runnable() {
            @Override
            public void run() {

                int cardW = nextCardTrumps.getWidth();
                int cardH = nextCardTrumps.getHeight();

                cardH = getHeightOfACard(cardW, cardH);
                cardW = (int) (cardH / cardRatio);

                final OmiCard omiCard = new OmiCard(mContext, cardW, cardH);
                omiCard.setCardNo(myCards[4]);
                nextCardTrumps.addView(omiCard);

                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.3f);
                alphaAnimation.setDuration(600);
                alphaAnimation.setFillBefore(false);
                alphaAnimation.setFillEnabled(true);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        nextCardTrumps.removeView(omiCard);
                        nextCardTrumps.setVisibility(View.GONE);
                        playerSelectedTrumps(getSuitOfCard(myCards[4]), 1);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                omiCard.startAnimation(alphaAnimation);
            }
        });
    }

    public void playerSelectingTrumps(OmiPlayer omiPlayer) {
        CharSequence text = getPlayerNameOf(omiPlayer.getPlayerNo()) + " is selecting trumps.";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(mContext, text, duration);
        toast.show();
    }

    public void playerSelectedTrumps(int suitNo, int option) {

        //TODO TEST
        //mOmiHand.setTrumps(suitNo);

        if (iDidTheAction) {
            if (mOMOmiGameViewListener != null) mOMOmiGameViewListener.playerDidSelectTrumps(suitNo, option);
        }else {
            if (option == 1) {
                showTrumpsSelectedFromSecondHand();
            }
        }

        animationLayout.setVisibility(View.VISIBLE);

        final int suit = suitNo;
        trumpsShowLayout.setVisibility(View.VISIBLE);
        trumpsShowLayout.post(new Runnable() {
            @Override
            public void run() {
                animateTrumpsSelection(suit);
            }
        });
    }

    private void animateTrumpsSelection(int suitNo) {
        int lW = trumpsShowLayout.getWidth();
        int lH = trumpsShowLayout.getHeight();

        int w = lW/2;
        if (lW > lH) {
            w = lH/2;
        }

        int rId = R.mipmap.spaids;
        if (suitNo == 1) {
            rId = R.mipmap.spaids;
        }else if (suitNo == 2) {
            rId = R.mipmap.hearts;
        }else if (suitNo == 3) {
            rId = R.mipmap.clubs;
        }else if (suitNo == 4) {
            rId = R.mipmap.diamonds;
        }

        imgTrumpsView.setVisibility(View.INVISIBLE);
        Bitmap trumpsMap = BitmapFactory.decodeResource(mContext.getResources(), rId);
        Bitmap reTrumpsMap = Bitmap.createScaledBitmap(trumpsMap, w, w, false);
        imgTrumpsView.setImageBitmap(reTrumpsMap);

        float animLW = animationLayout.getWidth();
        float animLH = animationLayout.getHeight();

        final ImageView imgView = new ImageView(mContext);
        imgView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imgView.setImageBitmap(reTrumpsMap);
        imgView.setX(animLW/2.0f - w/2.0f);
        imgView.setY(animLH/2.0f - w/2.0f);
        animationLayout.addView(imgView);

        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 3.5f, 1, 3.5f, Animation.ABSOLUTE, animLW/2.0f, Animation.ABSOLUTE, animLH/2.0f);
        scaleAnimation.setDuration(800);
        scaleAnimation.setFillBefore(false);
        scaleAnimation.setFillEnabled(true);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.6f);
        alphaAnimation.setDuration(800);
        alphaAnimation.setFillBefore(false);
        alphaAnimation.setFillEnabled(true);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animationLayout.removeView(imgView);
                animationLayout.setVisibility(View.GONE);
                imgTrumpsView.setVisibility(View.VISIBLE);
                trumpsSelectAnimationEnd();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imgView.startAnimation(animationSet);
    }

    public void showTrumpsSelectedFromSecondHand() {
        CharSequence text = "Trumps was selected from second hand.";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(mContext, text, duration);
        toast.show();
    }

    private void trumpsSelectAnimationEnd() {
        trumpsSelectLayout.setVisibility(View.GONE);
        cardPlayedLayout.setVisibility(View.VISIBLE);
        showReceivedCardsSecondSet();
    }

    private void showReceivedCardsSecondSet() {

        if (omiCards == null) {
            omiCards = new ArrayList<>();
        }

        for (OmiCard omiCard: omiCards) {
            omiCard.setOmiCardListener(null);
        }

        if (omiCards != null && omiCards.size() > 0) {
            omiCards.clear();
        }

        omiCards = new ArrayList<>();

        ArrayList<View> views = new ArrayList<>();

        for (int i = 0; i < cardsLayout.getChildCount(); i++) {
            View v = cardsLayout.getChildAt(i);
            v.setVisibility(View.GONE);
            views.add(v);
        }

        for (View v: views) {
            cardsLayout.removeView(v);
        }
        views.clear();

        int layerW = cardsLayout.getWidth();
        int layerH = cardsLayout.getHeight();

        int cardW = layerW/4;
        int cardH = layerH;

        cardH = getHeightOfACard(cardW, cardH);
        cardW = (int)(cardH / cardRatio);

        float cardSp = (layerW - cardW)/7.0f;

        calcCardH = cardH;
        for (int i = 0; i < 8; i++) {
            final OmiCard omiCard = new OmiCard(mContext, cardW, cardH);
            omiCard.setOmiCardListener(getOmiCardListener());
            omiCard.setCardNo(myCards[i]);
            cardsLayout.addView(omiCard);

            omiCards.add(omiCard);

            final float xp = cardSp * i;

            final boolean animEnd = (i == 7);

            TranslateAnimation tAnimation = new TranslateAnimation(0,xp,0,0);
            tAnimation.setDuration(800);
            tAnimation.setFillBefore(false);
            tAnimation.setFillEnabled(true);
            tAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    omiCard.setX(xp);

                    if (animEnd) {
                        secondSetShowAnimationEnd();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            omiCard.startAnimation(tAnimation);
        }
    }

    private OmiCard.OmiCardListener getOmiCardListener() {
        return new OmiCard.OmiCardListener() {
            @Override
            public void cardSelected(OmiCard card, int option) {
                if (lastPlayNo == 4) lastPlayNo = 0;
                lastPlayNo++;
                playerPlayedWithOption(lastPlayNo, card.getCardNo(), option);
            }
        };
    }

    private void secondSetShowAnimationEnd() {
        btnSortHand.setVisibility(View.VISIBLE);

        if (iDidTheAction) {
            enablePlayCards(true, 0);
            if (mOMOmiGameViewListener != null) mOMOmiGameViewListener.secondCardSetAppear();
        }else {
            enablePlayCards(false, 0);
        }
    }

    private void sortButtonTapped() {
        btnSortHand.setVisibility(View.INVISIBLE);
        Arrays.sort(myCards);
        rearrangeCards();
    }

    private void rearrangeCards() {
        for (OmiCard omiCard: omiCards) {
            omiCard.setOmiCardListener(null);
            cardsLayout.removeView(omiCard);
        }

        if (omiCards != null && omiCards.size() > 0) {
            omiCards.clear();
        }

        if (myCards.length == 0) {
            return;
        }

        omiCards = new ArrayList<>();

        int layerW = cardsLayout.getWidth();
        int layerH = cardsLayout.getHeight();

        int cardW = layerW/4;
        int cardH = layerH;

        cardH = getHeightOfACard(cardW, cardH);
        cardW = (int)(cardH / cardRatio);

        int totalCards = myCards.length;

        if (totalCards > 4) {
            float cardSp = (layerW - cardW)/(totalCards - 1);
            calcCardH = cardH;
            for (int i = 0; i < totalCards; i++) {
                final OmiCard omiCard = new OmiCard(mContext, cardW, cardH);
                omiCard.setOmiCardListener(getOmiCardListener());
                omiCard.setCardNo(myCards[i]);
                cardsLayout.addView(omiCard);

                omiCards.add(omiCard);

                final float xp = cardSp * i;

                TranslateAnimation tAnimation = new TranslateAnimation(0,xp,0,0);
                tAnimation.setDuration(800);
                tAnimation.setFillBefore(false);
                tAnimation.setFillEnabled(true);
                tAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        omiCard.setX(xp);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                omiCard.startAnimation(tAnimation);
            }
        }else {
            float startX = (layerW/2.0f) - ((cardW/2) * totalCards);

            calcCardH = cardH;
            for (int i = 0; i < totalCards; i++) {
                final OmiCard omiCard = new OmiCard(mContext, cardW, cardH);
                omiCard.setOmiCardListener(getOmiCardListener());
                omiCard.setCardNo(myCards[i]);
                cardsLayout.addView(omiCard);

                omiCards.add(omiCard);

                final float xp = startX + (cardW * i);

                TranslateAnimation tAnimation = new TranslateAnimation(0,xp,0,0);
                tAnimation.setDuration(800);
                tAnimation.setFillBefore(false);
                tAnimation.setFillEnabled(true);
                tAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        omiCard.setX(xp);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                omiCard.startAnimation(tAnimation);
            }
        }

        enablePlayCards(lastEnable, lastEnaSuit);
    }

    private void updateHandOnCardChanged(int cardNo) {
        OmiCard remCard = null;
        for (OmiCard omiCard: omiCards) {
            if (omiCard.getCardNo() == cardNo) {
                omiCard.setOmiCardListener(null);
                cardsLayout.removeView(omiCard);
                remCard = omiCard;
                break;
            }
        }

        omiCards.remove(remCard);

        if (myCards.length == 0) {
            return;
        }

        int layerW = cardsLayout.getWidth();
        int layerH = cardsLayout.getHeight();

        int cardW = layerW/4;
        int cardH = layerH;

        cardH = getHeightOfACard(cardW, cardH);
        cardW = (int)(cardH / cardRatio);

        int totalCards = myCards.length;

        if (totalCards > 4) {
            float cardSp = (layerW - cardW)/(totalCards - 1);
            calcCardH = cardH;
            for (int i = 0; i < totalCards; i++) {
                final OmiCard omiCard = omiCards.get(i);
                float currentX = omiCard.getX();
                final float newX = cardSp * i;
                float gap = newX - currentX;

                TranslateAnimation tAnimation = new TranslateAnimation(0,gap,0,0);
                tAnimation.setDuration(800);
                tAnimation.setFillBefore(false);
                tAnimation.setFillEnabled(true);
                tAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        omiCard.setX(newX);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                omiCard.startAnimation(tAnimation);
            }
        }else {
            float startX = (layerW/2.0f) - ((cardW/2) * totalCards);

            calcCardH = cardH;
            for (int i = 0; i < totalCards; i++) {

                final OmiCard omiCard = omiCards.get(i);
                float currentX = omiCard.getX();
                final float newX = startX + (cardW * i);
                float gap = newX - currentX;

                TranslateAnimation tAnimation = new TranslateAnimation(0,gap,0,0);
                tAnimation.setDuration(800);
                tAnimation.setFillBefore(false);
                tAnimation.setFillEnabled(true);
                tAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        omiCard.setX(newX);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                omiCard.startAnimation(tAnimation);
            }
        }

        enablePlayCards(lastEnable, lastEnaSuit);
    }

    private void enablePlayCards(boolean enable, int suitNo) {

        lastEnable = enable;
        lastEnaSuit = suitNo;

        if (enable) {
            iDidTheAction = true;
        }

        if (suitNo == 0) {
            for (OmiCard omiCard: omiCards) {
                omiCard.enableCard(enable);
            }
        }else {
            for (OmiCard omiCard: omiCards) {
                if (omiCard.getSuit() == suitNo) {
                    omiCard.enableCard(enable);
                }else {
                    omiCard.enableCard(false);
                }
            }
        }
    }

    //this is call after current player played or on command
    private void playerPlayedWithOption(final int playerNo, final int cardNo, final int option) {

        //TODO TEST

//        if (mOmiRound == null) {
//            mOmiRound = new OmiRound();
//            mOmiRound.setTrumps(mOmiHand.getTrumps());
//            mOmiRound.setSuit(getSuitOfCard(cardNo));
//
//            OmiRound lastRound = mOmiHand.getCurrentRound();
//
//            if (lastRound == null) {
//                mOmiRound.setRoundNo(1);
//                mOmiHand.setCurrentRound(mOmiRound);
//                mOmiHand.setLastRound(mOmiRound);
//            }else {
//                mOmiRound.setRoundNo(lastRound.getRoundNo() + 1);
//                mOmiHand.setCurrentRound(mOmiRound);
//                mOmiHand.setLastRound(lastRound);
//            }
//        }
//
//        if (mOmiRound.getStartedPlayerNo() == 0) {
//            mOmiRound.setStartedPlayerNo(myPlayerNo);
//        }
//
//        mOmiRound.setPlayerCard(myPlayerNo, cardNo);

        if (iDidTheAction) {
            if (mOMOmiGameViewListener != null) mOMOmiGameViewListener.playerDidPlayCard(cardNo, option);
        }

        if (iDidTheAction) {
            int len = myCards.length;
            int[] newCards = new int[len - 1];
            int i = 0;
            for(int card: myCards) {
                if (card != cardNo) {
                    newCards[i] = card;
                    i++;
                }
            }
            myCards = newCards;
            updateHandOnCardChanged(cardNo);
        }

        animationLayout.setVisibility(View.VISIBLE);
        animationLayout.post(new Runnable() {
            @Override
            public void run() {
                showCardSelectedAnimation(playerNo, cardNo, option);
            }
        });
    }

    private void showCardSelectedAnimation(final int playerNo, int cardNo, final int option) {

        float lW = animationLayout.getWidth();
        float lH = animationLayout.getHeight();

        float imgCardW = btnPlayedCard1.getWidth();
        float imgCardH = btnPlayedCard1.getHeight();

        int cardH = getHeightOfACard((int) imgCardW, (int) imgCardH);
        int cardW = (int)(cardH / cardRatio);

        Bitmap diamMap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.card_102);
        Bitmap reDiamMap = Bitmap.createScaledBitmap(diamMap, cardW, cardH, false);

        if (playerNo == 1) {
            btnPlayedCard1.setImageBitmap(reDiamMap);
        }else if (playerNo == 2) {
            btnPlayedCard2.setImageBitmap(reDiamMap);
        }else if (playerNo == 3) {
            btnPlayedCard3.setImageBitmap(reDiamMap);
        }else {
            btnPlayedCard4.setImageBitmap(reDiamMap);
        }

        float startX = 0;
        float startY = 0;

        float endX = 0;
        float endY = 0;

        float xDelta = 0;
        float yDelta = 0;

        if (playerNo == 1) {
            startX = lW/2.0f - cardW/2.0f;
            startY = lH - 2*cardH;

            endX = lW/2.0f - cardW/2.0f;
            endY = lH/2.0f + dpToPx(1);

            xDelta = endX-startX;
            yDelta = endY-startY;
        }else if (playerNo == 3) {
            startX = lW/2.0f - cardW/2.0f;
            startY = cardH - dpToPx(1);

            endX = lW/2.0f - cardW/2.0f;
            endY = lH/2.0f - btnPlayedCard3.getHeight();

            xDelta = endX-startX;
            yDelta = endY-startY;
        }else if (playerNo == 2) {
            startX = lW - 2*cardW;
            startY = lH/2.0f - btnPlayedCard2.getHeight()/2.0f;

            endX = lW/2.0f + btnPlayedCard2.getWidth()/2.0f + dpToPx(4);
            endY = lH/2.0f - btnPlayedCard2.getHeight()/2.0f;

            xDelta = endX-startX;
            yDelta = endY-startY;
        }else if (playerNo == 4) {
            startX = cardW;
            startY = lH/2.0f - btnPlayedCard2.getHeight()/2.0f;

            endX = lW/2.0f - btnPlayedCard2.getWidth()/2.0f - btnPlayedCard2.getWidth() - dpToPx(4);
            endY = lH/2.0f - btnPlayedCard2.getHeight()/2.0f;

            xDelta = endX-startX;
            yDelta = endY-startY;
        }

        final OmiCard omiCard = new OmiCard(mContext, cardW, cardH);
        omiCard.setCardNo(cardNo);
        animationLayout.addView(omiCard);
        omiCard.setX(startX);
        omiCard.setY(startY);

        final float endCX = endX;
        final float endCY = endY;

        int duration = 500;
        if (option <= 0) {
            duration = 1000;
        }

        TranslateAnimation tAnimation = new TranslateAnimation(0, xDelta, 0, yDelta);
        tAnimation.setDuration(duration);
        tAnimation.setFillBefore(false);
        tAnimation.setFillEnabled(true);
        tAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                int w = omiCard.getWidth();
                int h = omiCard.getHeight();
                int no = omiCard.getCardNo();
                omiCard.setVisibility(View.INVISIBLE);
                animationLayout.removeView(omiCard);

                final OmiCard card = new OmiCard(mContext, w, h);
                card.setCardNo(no);
                animationLayout.addView(card);
                card.setX(endCX);
                card.setY(endCY);

                showCardSelectedOptionAnimation(card, playerNo, option);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        omiCard.startAnimation(tAnimation);
    }

    private void showCardSelectedOptionAnimation(final OmiCard omiCard, final int playerNo, int option) {
        if (option <= 0) {

            omiCard.setVisibility(View.GONE);
            animationLayout.setVisibility(View.GONE);

            if (playerNo == 1) {
                btnPlayedCard1.setVisibility(VISIBLE);
            }else if (playerNo == 2) {
                btnPlayedCard2.setVisibility(VISIBLE);
            }else if (playerNo == 3) {
                btnPlayedCard3.setVisibility(VISIBLE);
            }else {
                btnPlayedCard4.setVisibility(VISIBLE);
            }

            test(playerNo);

            return;
        }

        float xDelta;
        float yDelta;

        if (playerNo == 1) {
            xDelta = animationLayout.getWidth()/2.0f;
            yDelta = animationLayout.getHeight()/2.0f + dpToPx(1) + btnPlayedCard1.getHeight()/2.0f;
        }else if (playerNo == 2) {
            xDelta = animationLayout.getWidth()/2.0f + btnPlayedCard2.getWidth() + dpToPx(4);
            yDelta = animationLayout.getHeight()/2.0f;
        }else if (playerNo == 3) {
            xDelta = animationLayout.getWidth()/2.0f;
            yDelta = animationLayout.getHeight()/2.0f - dpToPx(1) - btnPlayedCard3.getHeight()/2.0f;
        }else {
            xDelta = animationLayout.getWidth()/2.0f - btnPlayedCard2.getWidth() - dpToPx(4);
            yDelta = animationLayout.getHeight()/2.0f;
        }

        RotateAnimation rotateAnimation = new RotateAnimation(0.0f, 360.0f, Animation.ABSOLUTE, xDelta, Animation.ABSOLUTE, yDelta);
        rotateAnimation.setFillBefore(false);
        rotateAnimation.setFillEnabled(true);
        rotateAnimation.setDuration(500);
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                omiCard.setVisibility(View.GONE);
                animationLayout.setVisibility(View.GONE);

                if (playerNo == 1) {
                    btnPlayedCard1.setVisibility(VISIBLE);
                } else if (playerNo == 2) {
                    btnPlayedCard2.setVisibility(VISIBLE);
                } else if (playerNo == 3) {
                    btnPlayedCard3.setVisibility(VISIBLE);
                } else {
                    btnPlayedCard4.setVisibility(VISIBLE);
                }

                //playCardAnimationEnd();
                test(playerNo);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        omiCard.startAnimation(rotateAnimation);

    }

    private void test(int playerNo) {
        if (playerNo != 4) {
            return;
        }

        showRoundWinAnimation(4);

//        animationLayout.setVisibility(View.VISIBLE);
//        animationLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < animationLayout.getChildCount(); i++) {
//                    View v = animationLayout.getChildAt(i);
//                    v.setVisibility(View.VISIBLE);
//                }
//            }
//        });

    }

    public void playerPlayedCard(OmiPlayer omiPlayer, BTDataPacket btDataPacket) {

        JSONObject bodyJson = btDataPacket.getBodyAsJson();

        int playerNo = omiPlayer.getPlayerNo();
        int cardNo = bodyJson.optInt(Constants.OmiJsonKey.CARD_NUMBER_KEY);
        int option = bodyJson.optInt(Constants.OmiJsonKey.OPTION_KEY);

        iDidTheAction = false;
        playerPlayedWithOption(playerNo, cardNo, option);
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

            showRoundWinAnimation(winner);
            //judge is continuing after the animation

        } else {
            int nextPlayer = mOmiRound.getNextPlayer();
            if (nextPlayer == myPlayerNo) {
                enablePlayCards(true, mOmiRound.getSuit());
            }else {
                enablePlayCards(false, 0);
            }
        }
    }

    private void showRoundWinAnimation(final int winner) {

        animationLayout.setVisibility(View.VISIBLE);
        animationLayout.post(new Runnable() {
            @Override
            public void run() {

                float lW = animationLayout.getWidth();
                float lH = animationLayout.getHeight();

                float vW = btnPlayedCard1.getWidth();
                float vH = btnPlayedCard1.getHeight();

                btnPlayedCard1.setVisibility(View.INVISIBLE);
                btnPlayedCard2.setVisibility(View.INVISIBLE);
                btnPlayedCard3.setVisibility(View.INVISIBLE);
                btnPlayedCard4.setVisibility(View.INVISIBLE);

                float endX;
                float endY;

                if (winner == 1) {
                    endX = lW/2.0f - vW/2.0f;
                    endY = lH - 2*vH;
                } else if (winner == 2) {
                    endX = lW - vW;
                    endY = lH/2.0f - vH/2.0f;
                } else if (winner == 3) {
                    endX = lW/2.0f - vW/2.0f;
                    endY = vH;
                } else {
                    endX = vW;
                    endY = lH/2.0f - vH/2.0f;
                }

                for (int i = 0; i < animationLayout.getChildCount(); i++) {
                    final View v = animationLayout.getChildAt(i);
                    v.setVisibility(View.VISIBLE);

                    float startX = v.getX();
                    float startY = v.getY();

                    float xDelta = endX - startX;
                    float yDelta = endY - startY;

                    final boolean endAnim = (i == 3);

                    TranslateAnimation tAnimation = new TranslateAnimation(0, xDelta, 0, yDelta);
                    tAnimation.setDuration(800);
                    tAnimation.setFillBefore(false);
                    tAnimation.setFillEnabled(true);
                    tAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                            v.setVisibility(View.GONE);
                            animationLayout.removeView(v);

                            if (endAnim) {
                                animationLayout.setVisibility(View.GONE);
                                //showingRoundWinAnimationEnd(winner);
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    v.startAnimation(tAnimation);

                }
            }
        });
    }

    private void showingRoundWinAnimationEnd(int winner) {
        if (mOmiHand.isHandOver()) {
            updateWinHands();
            int winningTeam = mOmiHand.getWinningTeam();
            if (winningTeam == 1) {
                mOmiGameStat.addWinToTeam(1);
            }else if (winningTeam == 2) {
                mOmiGameStat.addWinToTeam(2);
            }else {
                mOmiGameStat.addDraws();
            }

            int endReach = mOmiGameStat.getTeamIfWin();
            if (endReach == 0) {//continue game
                if (mOMOmiGameViewListener != null) mOMOmiGameViewListener.timeToNextHand();
            }else if (endReach == 1) {//team 1 win
                showWinningScreen(1);
            }else if (endReach == 2) {//team 2 win
                showWinningScreen(2);
            }
        }else {
            updateWinHands();
            if (winner == myPlayerNo) {
                mOmiRound = null;
                enablePlayCards(true, 0);
            }else {
                enablePlayCards(false, 0);
            }
        }
    }

    private void updateWinHands() {
        txtWon1.setText("Won " + mOmiHand.getPlayer1Wins());
        txtWon2.setText("Won " + mOmiHand.getPlayer2Wins());
        txtWon3.setText("Won " + mOmiHand.getPlayer3Wins());
        txtWon4.setText("Won " + mOmiHand.getPlayer4Wins());
    }

    private void showWinningScreen(int team) {

    }

    private void winningScreenAnimationEnd() {
        if (mOMOmiGameViewListener != null) mOMOmiGameViewListener.gameDidEndWithWinningTeam(mOmiGameStat.getTeamIfWin());
    }

    private void init(Context context, OmiGameViewListener omiGameViewListener) {
        mContext = context;
        mOMOmiGameViewListener = omiGameViewListener;
        //TODO TEST Game
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

        trumpsShowLayout = (LinearLayout)inflater.findViewById(R.id.trumpsShowLayout);
        imgTrumpsView = (ImageView)inflater.findViewById(R.id.imgTrumpsView);

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

        animationLayout = (RelativeLayout)inflater.findViewById(R.id.animationLayout);
        animationLayout.setEnabled(true);
        animationLayout.setVisibility(View.GONE);

        nextCardTrumps = (LinearLayout)inflater.findViewById(R.id.nextCardTrumps);
        nextCardTrumps.setVisibility(View.GONE);

        btnTrumps1 = (ImageButton)inflater.findViewById(R.id.btnTrumps1);
        btnTrumps1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playerSelectedTrumps(1, 0);
            }
        });

        btnTrumps2 = (ImageButton)inflater.findViewById(R.id.btnTrumps2);
        btnTrumps2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playerSelectedTrumps(2, 0);
            }
        });

        btnTrumps3 = (ImageButton)inflater.findViewById(R.id.btnTrumps3);
        btnTrumps3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playerSelectedTrumps(4, 0);
            }
        });

        btnTrumps4 = (ImageButton)inflater.findViewById(R.id.btnTrumps4);
        btnTrumps4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playerSelectedTrumps(3, 0);
            }
        });

        cardPlayedLayout = (LinearLayout)inflater.findViewById(R.id.cardPlayedLayout);
        cardPlayedLayout.setVisibility(View.GONE);

        btnPlayedCard1 = (ImageButton)inflater.findViewById(R.id.btnPlayedCard1);
        btnPlayedCard2 = (ImageButton)inflater.findViewById(R.id.btnPlayedCard2);
        btnPlayedCard3 = (ImageButton)inflater.findViewById(R.id.btnPlayedCard3);
        btnPlayedCard4 = (ImageButton)inflater.findViewById(R.id.btnPlayedCard4);

        btnPlayedCard1.setVisibility(INVISIBLE);
        btnPlayedCard2.setVisibility(INVISIBLE);
        btnPlayedCard3.setVisibility(INVISIBLE);
        btnPlayedCard4.setVisibility(INVISIBLE);

        btnSortHand = (ImageButton)inflater.findViewById(R.id.btnSortHand);
        btnSortHand.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sortButtonTapped();
            }
        });
        btnSortHand.setVisibility(View.INVISIBLE);

        btnShowChat = (ImageButton)inflater.findViewById(R.id.btnShowChat);
        btnShowChat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO TEST
                cmdShuffleThePack();
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

        mPack = getShuffledPack();
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

        Random r = new Random();
        for (int i = 0; i < shuffleCount; i++) {

            int[] newPack = new int[32];

            int rand = r.nextInt(3) + 6;

            int randFirst = r.nextInt(3) + 12;

            int range1 = randFirst;
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

            mPack = newPack;
        }

        return mPack;
    }

    private int[] getShuffledPack() {

        int[] newPack = new int[32];

        for (int i = 0; i < 8; i++) {
            newPack[i] = mPack[i*4];
            newPack[i+8] = mPack[i*4 + 1];
            newPack[i+16] = mPack[i*4 + 2];
            newPack[i+24] = mPack[i*4 + 3];
        }

        mPack = newPack;
        return mPack;
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

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}
