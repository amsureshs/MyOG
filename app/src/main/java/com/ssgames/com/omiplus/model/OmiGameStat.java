package com.ssgames.com.omiplus.model;

/**
 * Created by Admin on 9/8/15.
 */
public class OmiGameStat {
    private int lastShuffledPlayerNo = 0;
    private int team1Wins = 0;
    private int team2Wins = 0;
    private int player1TotalWins = 0;
    private int player2TotalWins = 0;
    private int player3TotalWins = 0;
    private int player4TotalWins = 0;

    public void setLastShuffledPlayerNo(int lastShuffledPlayerNo) {
        this.lastShuffledPlayerNo = lastShuffledPlayerNo;
    }

    public int getLastShuffledPlayerNo() {
        return lastShuffledPlayerNo;
    }

    public int getTeam1Wins() {
        return team1Wins;
    }

    public int getTeam2Wins() {
        return team2Wins;
    }

    public int getPlayer1TotalWins() {
        return player1TotalWins;
    }

    public int getPlayer2TotalWins() {
        return player2TotalWins;
    }

    public int getPlayer3TotalWins() {
        return player3TotalWins;
    }

    public int getPlayer4TotalWins() {
        return player4TotalWins;
    }

    public void addWinToTeam(int team) {
        if (team == 1) {
            team1Wins++;
        }else {
            team2Wins++;
        }
    }

    public void addWinToPlayer(int playerNo) {
        switch (playerNo) {
            case 1:
                player1TotalWins++;
                break;
            case 2:
                player2TotalWins++;
                break;
            case 3:
                player3TotalWins++;
                break;
            case 4:
                player4TotalWins++;
                break;
            default:
                break;
        }
    }
}
