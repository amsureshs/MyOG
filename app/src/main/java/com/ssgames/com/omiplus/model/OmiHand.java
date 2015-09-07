package com.ssgames.com.omiplus.model;

import com.ssgames.com.omiplus.util.Constants;

/**
 * Created by sura on 9/5/15.
 */
public class OmiHand {
    private Constants.OmiSuit suit = Constants.OmiSuit.NONE;

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

}
