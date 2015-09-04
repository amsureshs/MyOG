package com.ssgames.com.omiplus.bluetooth;

import android.util.Log;

import org.json.JSONObject;

public class BTDataPacket {

    private static final String TAG = BTDataPacket.class.getSimpleName();

	public static final String OP_CODE = "op_code";
    public static final String BODY = "body";

	private int opCode;
	private String body;
	private JSONObject jsonObject;

	public BTDataPacket() {

	}

	public BTDataPacket(String jsonString) {
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(jsonString);
		}catch (Exception e) {}

		if (jsonObject != null) {
			this.jsonObject = jsonObject;
			int opCode = jsonObject.optInt(OP_CODE, 0);
			this.opCode = opCode;

            JSONObject bodyObj = jsonObject.optJSONObject(BODY);
            if (bodyObj == null) {
                String body = jsonObject.optString(BODY, "");
                this.body = body;
            }else {
                this.body = bodyObj.toString();
            }
		}
	}

	public BTDataPacket(JSONObject jsonObject) {
		if (jsonObject != null) {
			this.jsonObject = jsonObject;
			int opCode = jsonObject.optInt(OP_CODE, 0);
			this.opCode = opCode;
            JSONObject bodyObj = jsonObject.optJSONObject(BODY);
            if (bodyObj == null) {
                String body = jsonObject.optString(BODY, "");
                this.body = body;
            }else {
                this.body = bodyObj.toString();
            }
		}
	}

	public BTDataPacket(byte[] buffer) {
		String jsonString = null;
		try {
			jsonString = new String(buffer, "UTF-8");
		} catch (Exception e) {
			Log.v(TAG, "Received data exception creating jsonString");
		}

		Log.v(TAG, "Received data: " + jsonString);

		if (jsonString != null) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(jsonString);
			}catch (Exception e) {}

			if (jsonObject != null) {
				this.jsonObject = jsonObject;
				int opCode = jsonObject.optInt(OP_CODE, 0);
				this.opCode = opCode;
				String body = jsonObject.optString(BODY, "");
				this.body = body;
			}else {
				Log.v(TAG, "Received data: " + "Json object is null");
			}
		}
	}
	
	public byte[] getBuffer() {

		StringBuilder stringBuilder = new StringBuilder("{");
		stringBuilder.append("\"" + OP_CODE + "\":" + opCode + ",");

        if (body != null) {
            JSONObject testJson = null;

            try {
                testJson = new JSONObject(body);
            }catch (Exception e){}

            if (testJson == null) {
                stringBuilder.append("\"" + BODY + "\": \"" + body + "\"}");
            }else {
                stringBuilder.append("\"" + BODY + "\":" + body + "}");
            }
        }else {
            stringBuilder.append("\"" + BODY + "\": \"\"}");
        }

		String jsonString = stringBuilder.toString();

        Log.v(TAG, "jsonString: " + jsonString);

		byte[] buffer = null;
		try {
			buffer = jsonString.getBytes("UTF-8");
		} catch (Exception e) {}

		return buffer;
	}

	public int getOpCode() {
		return opCode;
	}

	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public JSONObject getJsonObject() {
		return jsonObject;
	}
}
