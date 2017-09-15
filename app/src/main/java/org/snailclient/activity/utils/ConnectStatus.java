package org.snailclient.activity.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.snailclient.activity.sendCommand;

import com.monitor.util.Contact;
import com.snail.util.Constants;

public class ConnectStatus {

	public static final String TAG = "ConnectStatus";
	
	public static void setConnectStatus(String status){
		JSONObject connect = new JSONObject();
		try {
			connect.put("result", status);
			connect.put("type", "connect");
			connect.put("mac", Contact.mac);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		new sendCommand(connect.toString(), Constants.managerPort,
				Constants.managerIP).start();
	}
	
	public static void setHeartBeats(String status){
		JSONObject connect = new JSONObject();
		try {
			connect.put("type", "heart");
			connect.put("mac", Contact.mac);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		new sendCommand(connect.toString(), Constants.managerPort,
				Constants.managerIP).start();
	}
}
