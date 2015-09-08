package com.ssgames.com.omiplus.model;

import com.ssgames.com.omiplus.util.Constants;

/**
 * Created by sura on 9/5/15.
 */
public class OmiHand {
    private Constants.OmiSuit suit = Constants.OmiSuit.NONE;
    private int shuffledPlayerNo = 0;
    private OmiRound currentRound;
    private OmiRound lastRound;
    private int player1Wins = 0;
    private int player2Wins = 0;
    private int player3Wins = 0;
    private int player4Wins = 0;

    /*

    1. Player y(Hosted player or next) is informed to shuffle and informed others about shuffling the pack

    2. Others are informed about the shuffling (host is informed)

    3. Spread out the cards

    4. Player y+1 is informed to select trumps

    5. Others are informed about player y+1 is selecting trumps

    6. Player y+1 selected trumps (informed host)

    7. Player x(won player or trumps selected player) put card c and started the hand (others are informed the hand Suit)

    8. Player x+1 played

    9. player x+2 played

    10. Player x+3 played

    11. Host decides the hand winner and informed others with the points

    12. If the cards in hands is more than 0, the winner is named as player x and back to step 7

    13. Else or all 8 cards are over the host calculate the total wins

    14. If the total wins is less than 10, player y+1 is named as y and back to step 1

    15. Else the game is over and others are informed

    16. Showing winning or loss screens

    17. Ask host to start a new game. If host decide to new game then player y+1 is named as y and back to step 1

    18. Else exist the game

     */

    public Constants.OmiSuit getSuit() {
        return suit;
    }

    public void setSuit(Constants.OmiSuit suit) {
        this.suit = suit;
    }

    public int getShuffledPlayerNo() {
        return shuffledPlayerNo;
    }

    public void setShuffledPlayerNo(int shuffledPlayerNo) {
        this.shuffledPlayerNo = shuffledPlayerNo;
    }

    public OmiRound getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(OmiRound currentRound) {
        this.currentRound = currentRound;
    }

    public OmiRound getLastRound() {
        return lastRound;
    }

    public void setLastRound(OmiRound lastRound) {
        this.lastRound = lastRound;
    }

    public int getPlayer1Wins() {
        return player1Wins;
    }

    public int getPlayer2Wins() {
        return player2Wins;
    }

    public int getPlayer3Wins() {
        return player3Wins;
    }

    public int getPlayer4Wins() {
        return player4Wins;
    }

    public void addWinToPlayer(int playerNo) {
        switch (playerNo) {
            case 1:
                player1Wins++;
                break;
            case 2:
                player2Wins++;
                break;
            case 3:
                player3Wins++;
                break;
            case 4:
                player4Wins++;
                break;
            default:
                break;
        }
    }
}
