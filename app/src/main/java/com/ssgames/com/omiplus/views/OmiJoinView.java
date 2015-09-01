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

public class OmiJoinView extends LinearLayout {

    private static final String TAG = OmiJoinView.class.getSimpleName();

    public interface OmiJoinViewListener {
        public void searchButtonTapped();
        public void gameSelected(String gameName);
    }

    private OmiJoinViewListener mOmiJoinViewListener = null;
    private Button btnSearch = null;
    private boolean isSearching = false;
    private TextView gameOne = null;
    private TextView gameTwo = null;
    private TextView gameThree = null;

    private ArrayList<String> gameList = null;

    public OmiJoinView(Context context, OmiJoinViewListener omiJoinViewListener) {
        super(context);
        init(context, omiJoinViewListener);
    }

    public OmiJoinView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OmiJoinView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void searchFinished() {
        if (btnSearch != null) {
            btnSearch.setText(R.string.join_btn_search);
            isSearching = false;
        }
    }

    public void searchStarted() {
        if (btnSearch != null) {
            btnSearch.setText(R.string.join_btn_searching);
            isSearching = true;
        }
    }

    public void addGames(String[] gameNames) {
        if (gameList == null) {
            gameList = new ArrayList<>();
        }

        for (String gameName : gameNames) {
            if (!gameList.contains(gameName)) {
                gameList.add(gameName);
            }
        }

        showGames();
    }

    private void showGames() {
        gameOne.setVisibility(View.INVISIBLE);
        gameTwo.setVisibility(View.INVISIBLE);
        gameThree.setVisibility(View.INVISIBLE);

        for (int i = 0; i < (gameList.size() > 3 ? 3 : gameList.size()); i++) {
            String gameName = gameList.get(i);
            switch (i) {
                case 0:
                    gameOne.setText(gameName);
                    gameOne.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    gameTwo.setText(gameName);
                    gameTwo.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    gameThree.setText(gameName);
                    gameThree.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    }

    private void init(Context context, OmiJoinViewListener omiJoinViewListener) {

        mOmiJoinViewListener = omiJoinViewListener;

        View inflater = LayoutInflater.from(context).inflate(R.layout.view_join, this, true);

        btnSearch = (Button)inflater.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSearching) {
                    if (mOmiJoinViewListener != null) mOmiJoinViewListener.searchButtonTapped();
                }
            }
        });

        gameOne = (TextView)inflater.findViewById(R.id.gameOneCell);
        gameTwo = (TextView)inflater.findViewById(R.id.gameTwoCell);
        gameThree = (TextView)inflater.findViewById(R.id.gameThreeCell);

        OnClickListener cellClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getClass().equals(TextView.class)) {
                    TextView textViewCell = (TextView)v;
                    if (mOmiJoinViewListener != null) mOmiJoinViewListener.gameSelected(textViewCell.getText().toString());
                }
            }
        };
        gameOne.setOnClickListener(cellClickListener);
        gameTwo.setOnClickListener(cellClickListener);
        gameThree.setOnClickListener(cellClickListener);
    }
}
