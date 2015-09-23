package com.ssgames.com.omiplus.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ssgames.com.omiplus.R;
import com.ssgames.com.omiplus.util.Constants;

/**
 * Created by sura on 9/10/15.
 */
public class OmiCard extends LinearLayout{

    private static final String TAG = OmiCard.class.getSimpleName();

    public interface OmiCardListener {
        public void cardSelected(OmiCard card, int option);
    }

    private OmiCardListener omiCardListener = null;
    private Context mContext = null;
    private ImageButton btnCard = null;
    private ImageView imgCardSuit = null;
    private TextView txtName = null;

    private int mWidth = 0;
    private int mHeight = 0;

    private int cardNo;
    private boolean enable = false;

    public OmiCard(Context context, int width, int height) {
        super(context);
        init(context, width, height);
    }

    public OmiCard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OmiCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OmiCardListener getOmiCardListener() {
        return omiCardListener;
    }

    public void setOmiCardListener(OmiCardListener omiCardListener) {
        this.omiCardListener = omiCardListener;
    }

    public void enableCard(boolean enable) {
        this.enable = enable;
        if (enable) {
            this.setAlpha(1.0f);
        }else {
            this.setAlpha(0.6f);
        }
    }

    public int getSuit() {
        return getSuitOfCard(cardNo);
    }

    public int getCardNo() {
        return cardNo;
    }

    public void setCardNo(int cardNo) {
        this.cardNo = cardNo;

//        String cardName = getCardName(cardNo);
//        int suitNo = getSuitOfCard(cardNo);
//
//        txtName.setText(cardName);
//        switch (suitNo) {
//            case 1 :
//                txtName.setTextColor(mContext.getResources().getColor(R.color.black));
//                imgCardSuit.setImageResource(R.mipmap.spaids);
//                break;
//            case 2 :
//                txtName.setTextColor(mContext.getResources().getColor(R.color.red));
//                imgCardSuit.setImageResource(R.mipmap.hearts);
//                break;
//            case 3 :
//                txtName.setTextColor(mContext.getResources().getColor(R.color.black));
//                imgCardSuit.setImageResource(R.mipmap.clubs);
//                break;
//            case 4 :
//                txtName.setTextColor(mContext.getResources().getColor(R.color.red));
//                imgCardSuit.setImageResource(R.mipmap.diamonds);
//                break;
//            default :
//                break;
//        }

        int rId = mContext.getResources().getIdentifier("@mipmap/card_" + cardNo, null, mContext.getPackageName());

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), rId);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, mWidth, mHeight, false);
        btnCard.setImageBitmap(scaledBitmap);
    }

    private void init(Context context, int width, int height) {
        mContext = context;
        mWidth = width;
        mHeight = height;

        View inflater = LayoutInflater.from(context).inflate(R.layout.view_omi_card, this, true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        inflater.setLayoutParams(lp);

        btnCard = (ImageButton)inflater.findViewById(R.id.btnCard);
        btnCard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enable) {
                    cardSelected(0);
                }
                enable = false;
            }
        });
        btnCard.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (enable) {
                    cardSelected(1);
                }
                enable = false;
                return true;
            }
        });

        imgCardSuit = (ImageView)inflater.findViewById(R.id.imgCardSuit);
        txtName = (TextView)inflater.findViewById(R.id.txtName);
    }

    private void cardSelected(int option) {
        if(omiCardListener != null) omiCardListener.cardSelected(this, option);
    }

    private String getCardName(int cardNo) {

        int accNo = 0;

        if (cardNo < 200) {
            accNo = cardNo - 100;
        }else if (cardNo < 300) {
            accNo = cardNo - 200;
        }else if (cardNo < 400) {
            accNo = cardNo - 300;
        }else if (cardNo < 500) {
            accNo = cardNo - 400;
        }else {

        }

        if (accNo < 11) {
            return "" + accNo;
        }

        switch (accNo) {
            case 11 :
                return "J";
            case 12 :
                return "Q";
            case 13 :
                return "K";
            case 14 :
                return "A";
            default :
                break;
        }

        return "";
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
}