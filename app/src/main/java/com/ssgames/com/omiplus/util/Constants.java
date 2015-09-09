package com.ssgames.com.omiplus.util;

public class Constants {

	public class UserKey {
		public static final String NICK_NAME = "nick_name";
		public static final String ORI_BT_NAME = "old_bt_name";
		public static final String NEW_BT_NAME = "new_bt_name";
	}
	
	public class OmiJsonKey {
		public static final String PLAYER_NAME_1_KEY = "p1_k";
		public static final String PLAYER_NAME_2_KEY = "p2_k";
        public static final String PLAYER_NAME_3_KEY = "p3_k";
        public static final String PLAYER_NAME_4_KEY = "p4_k";

        public static final String PLAYER_NUMBER_KEY = "pn_k";
        public static final String CARD_NUMBER_KEY = "cn_k";
        public static final String SELECTED_TRUMPS_KEY = "tr_k";
        public static final String OPTION_KEY = "op_k";
	}
	
	public class ExtraKey {
		public static final String EXTRA_KEY_GAME_TYPE = "extra_key_game_type";
		public static final String EXTRA_KEY_HOST_OR_JOIN = "host_or_join_extra";
	}

    public class OpCodes {
        public static final int OPCODE_NONE = 0;
        public static final int OPCODE_START_GAME = 1;
        public static final int OPCODE_GAME_END = 2;
        public static final int OPCODE_SET_PLAYER_NAMES = 3;
        public static final int OPCODE_SHUFFLE_PACK = 4;
        public static final int OPCODE_PLAYER_SHUFFLING_PACK = 5;
        public static final int OPCODE_CARDS_AVAILABLE = 6;
        public static final int OPCODE_SELECT_TRUMPS = 7;
        public static final int OPCODE_PLAYER_SELECTING_TRUMPS = 8;
        public static final int OPCODE_PLAYER_SELECTED_TRUMPS = 9;
        public static final int OPCODE_PLAYER_PLAYED_CARD = 10;
        public static final int OPCODE_PLAYER_WON_HAND = 11;
    }

//    public enum OmiSuit {
//        SPADES,// 1
//        HEARTS,// 2
//        CLUBS,// 3
//        DIAMONDS,// 4
//        NONE
//    }

    public class OmiSuit {
        public static final int SPADES = 1;
        public static final int HEARTS = 2;
        public static final int CLUBS = 3;
        public static final int DIAMONDS = 4;
        public static final int NONE = 0;
    }

    public enum OmiTeam {
        TEAM_A,
        TEAM_B
    }

    public class Points {
        public static final int TRUMPS_POINTS = 20;
    }
}
