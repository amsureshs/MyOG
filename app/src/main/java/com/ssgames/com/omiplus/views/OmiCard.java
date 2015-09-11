package com.ssgames.com.omiplus.views;

import android.content.Context;
import android.util.AttributeSet;
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
        public void cardSelected(OmiCard card);
    }

    private OmiCardListener omiCardListener = null;
    private Context mContext = null;
    private ImageButton btnCard = null;
    private ImageView imgCardSuit = null;
    private TextView txtName = null;

    private int cardNo;

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
        btnCard.setEnabled(enable);
    }

    public int getCardNo() {
        return cardNo;
    }

    public void setCardNo(int cardNo) {
        this.cardNo = cardNo;

        String cardName = getCardName(cardNo);
        int suitNo = getSuitOfCard(cardNo);

        txtName.setText(cardName);
        switch (suitNo) {
            case 1 :
                txtName.setTextColor(mContext.getResources().getColor(R.color.black));
                imgCardSuit.setImageResource(R.mipmap.spaids);
                break;
            case 2 :
                txtName.setTextColor(mContext.getResources().getColor(R.color.red));
                imgCardSuit.setImageResource(R.mipmap.hearts);
                break;
            case 3 :
                txtName.setTextColor(mContext.getResources().getColor(R.color.black));
                imgCardSuit.setImageResource(R.mipmap.clubs);
                break;
            case 4 :
                txtName.setTextColor(mContext.getResources().getColor(R.color.red));
                imgCardSuit.setImageResource(R.mipmap.diamonds);
                break;
            default :
                break;
        }
    }

    public void setCardSize(int width, int height) {
        LayoutParams params = (LayoutParams) this.getLayoutParams();
        params.width = width;
        params.height = height;
        this.setLayoutParams(params);
    }

    private void init(Context context, int width, int height) {
        mContext = context;

        View inflater = LayoutInflater.from(context).inflate(R.layout.view_omi_card, this, true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        inflater.setLayoutParams(lp);

        btnCard = (ImageButton)inflater.findViewById(R.id.btnCard);
        btnCard.setEnabled(false);
        btnCard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cardSelected();
            }
        });

        imgCardSuit = (ImageView)inflater.findViewById(R.id.imgCardSuit);
        txtName = (TextView)inflater.findViewById(R.id.txtName);
    }

    private void cardSelected() {
        if(omiCardListener != null) omiCardListener.cardSelected(this);
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