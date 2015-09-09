package com.ssgames.com.omiplus.model;

import com.ssgames.com.omiplus.util.Constants;

/**
 * Created by Admin on 9/8/15.
 */
public class OmiRound {
    private Constants.OmiSuit suit = Constants.OmiSuit.NONE;
    private Constants.OmiSuit trumps = Constants.OmiSuit.NONE;
    private int startedPlayerNo = 0;
    private int player1Card = 0;
    private int player2Card = 0;
    private int player3Card = 0;
    private int player4Card = 0;
    private int winner = 0;

    public Constants.OmiSuit getSuit() {
        return suit;
    }

    public void setSuit(Constants.OmiSuit suit) {
        this.suit = suit;
    }

    public Constants.OmiSuit getTrumps() {
        return trumps;
    }

    public void setTrumps(Constants.OmiSuit trumps) {
        this.trumps = trumps;
    }

    public int getStartedPlayerNo() {
        return startedPlayerNo;
    }

    public void setStartedPlayerNo(int startedPlayerNo) {
        this.startedPlayerNo = startedPlayerNo;
    }

    public int getPlayer1Card() {
        return player1Card;
    }

    public void setPlayer1Card(int player1Card) {
        this.player1Card = player1Card;
    }

    public int getPlayer2Card() {
        return player2Card;
    }

    public void setPlayer2Card(int player2Card) {
        this.player2Card = player2Card;
    }

    public int getPlayer3Card() {
        return player3Card;
    }

    public void setPlayer3Card(int player3Card) {
        this.player3Card = player3Card;
    }

    public int getPlayer4Card() {
        return player4Card;
    }

    public void setPlayer4Card(int player4Card) {
        this.player4Card = player4Card;
    }

    public int getWinner() {

        int player1Points = getPlayerPoints(player1Card);
        int player2Points = getPlayerPoints(player2Card);
        int player3Points = getPlayerPoints(player3Card);
        int player4Points = getPlayerPoints(player4Card);

        int max = player1Points;
        winner = 1;
        if (max < player2Points) {
            max = player2Points;
            winner = 2;
        }
        if (max < player3Points) {
            max = player3Points;
            winner = 3;
        }
        if (max < player4Points) {
            winner = 4;
        }

        return winner;
    }

    private int getPlayerPoints(int playerCard) {

        Constants.OmiSuit playerSuit = Constants.OmiSuit.NONE;
        int cardPoints = 0;

        if (playerCard < 200) {
            playerSuit = Constants.OmiSuit.SPADES;
            cardPoints = playerCard - 100;
        }else if (playerCard < 300) {
            playerSuit = Constants.OmiSuit.HEARTS;
            cardPoints = playerCard - 200;
        }else if (playerCard < 400) {
            playerSuit = Constants.OmiSuit.CLUBS;
            cardPoints = playerCard - 300;
        }else if (playerCard < 500) {
            playerSuit = Constants.OmiSuit.DIAMONDS;
            cardPoints = playerCard - 400;
        }

        if (playerSuit != trumps && playerSuit != suit) {
            return 0;
        }

        if (playerSuit == trumps) {
            return cardPoints + Constants.Points.TRUMPS_POINTS;
        }

        return cardPoints;
    }

}
