package com.ssgames.com.omiplus.util;

public class Constants {

	public class UserKey {
		public static final String NICK_NAME = "nick_name";
		public static final String ORI_BT_NAME = "old_bt_name";
		public static final String NEW_BT_NAME = "new_bt_name";
	}
	
	public class MultiPlayerKey {
		public static final String PLAYER_NAME_1_KEY = "pn1_k";
		public static final String PLAYER_NAME_2_KEY = "pn2_k";
        public static final String PLAYER_NAME_3_KEY = "pn3_k";
        public static final String PLAYER_NAME_4_KEY = "pn4_k";
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
    }
	
}
