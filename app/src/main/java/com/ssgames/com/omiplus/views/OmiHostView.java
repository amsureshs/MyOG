package com.ssgames.com.omiplus.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ssgames.com.omiplus.R;

import java.util.ArrayList;


public class OmiHostView extends LinearLayout {

    private static final String TAG = OmiHostView.class.getSimpleName();

    public interface OmiHostViewListener {
        public void visibleButtonTapped();
        public void partnerSelected(String partnerName);
    }

    private OmiHostViewListener mOmiHostViewListener = null;
    private Button btnVisible = null;
    private boolean isVisibleOn = false;
    private boolean atPartnerSelect = false;
    private TextView headerView = null;
    private TextView playerOne = null;
    private TextView playerTwo = null;
    private TextView playerThree = null;

    private ArrayList<String> playersList = null;

    public OmiHostView(Context context, OmiHostViewListener omiHostViewListener) {
        super(context);
        init(context, omiHostViewListener);
    }

    public OmiHostView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OmiHostView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void showPartnerSelection() {
        atPartnerSelect = true;
        headerView.setText(R.string.host_select_partner);
    }

    public void visibilityFinished() {
        if (btnVisible != null) {
            btnVisible.setText(R.string.host_btn_go_visible);
            isVisibleOn = false;
        }
    }

    public void visibilityStarted() {
        if (btnVisible != null) {
            btnVisible.setText(R.string.host_btn_visible);
            isVisibleOn = true;
        }
    }

    public void addPartners(String[] gameNames) {
        if (playersList == null) {
            playersList = new ArrayList<>();
        }

        playersList.clear();

        for (String gameName : gameNames) {
            if (!playersList.contains(gameName)) {
                playersList.add(gameName);
            }
        }

        showGames();
    }

    private void showGames() {
        playerOne.setVisibility(View.INVISIBLE);
        playerTwo.setVisibility(View.INVISIBLE);
        playerThree.setVisibility(View.INVISIBLE);

        for (int i = 0; i < (playersList.size() > 3 ? 3 : playersList.size()); i++) {
            String gameName = playersList.get(i);
            switch (i) {
                case 0:
                    playerOne.setText(gameName);
                    playerOne.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    playerTwo.setText(gameName);
                    playerTwo.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    playerThree.setText(gameName);
                    playerThree.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    }

    private void init(Context context, OmiHostViewListener omiHostViewListener) {

        mOmiHostViewListener = omiHostViewListener;

        View inflater = LayoutInflater.from(context).inflate(R.layout.view_host, this, true);

        headerView  = (TextView)inflater.findViewById(R.id.txtHeader);

        btnVisible = (Button)inflater.findViewById(R.id.btnSearch);
        btnVisible.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isVisibleOn) {
                    if (mOmiHostViewListener != null) mOmiHostViewListener.visibleButtonTapped();
                }
            }
        });

        playerOne = (TextView)inflater.findViewById(R.id.playerOne);
        playerTwo = (TextView)inflater.findViewById(R.id.playerTwo);
        playerThree = (TextView)inflater.findViewById(R.id.playerThree);

        OnClickListener cellClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (atPartnerSelect && v.getClass().equals(TextView.class)) {
                    TextView textViewCell = (TextView)v;
                    if (mOmiHostViewListener != null) mOmiHostViewListener.partnerSelected(textViewCell.getText().toString());
                }
            }
        };
        playerOne.setOnClickListener(cellClickListener);
        playerTwo.setOnClickListener(cellClickListener);
        playerThree.setOnClickListener(cellClickListener);
    }
}
