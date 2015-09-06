package com.ssgames.com.omiplus.model;

import com.ssgames.com.omiplus.util.Constants;

/**
 * Created by sura on 9/5/15.
 */
public class OmiPlayer {
    private String uniqueName = null;
    private String nickName = null;
    private int playerNo = 0;
    private int wonCount = 0;

    //functions
    public void selectSuit(Constants.OmiSuit suit) {

    }

    public void putCard(int cardNo, int playOption) {

    }

    public void collectCards() {

    }

    //attributes

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getPlayerNo() {
        return playerNo;
    }

    public void setPlayerNo(int playerNo) {
        this.playerNo = playerNo;
    }

    public int getWonCount() {
        return wonCount;
    }

    public void setWonCount(int wonCount) {
        this.wonCount = wonCount;
    }
}
