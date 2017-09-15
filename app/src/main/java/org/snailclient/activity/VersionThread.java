package org.snailclient.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.snail.util.Constants;
/**
 * 
 * 类的描述：判断与服务器端版本是否一致
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class VersionThread extends Thread {
	
	public static final String TAG = "VersionThread";
	
	Handler handler;

	public VersionThread(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void run() {
		
		String currentVersion = "21";
		URL serverUrl;
		InputStream stream = null;
		BufferedReader reader = null;
		try {
			serverUrl = new URL(Constants.VERSION_URL);
			HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
			connection.setConnectTimeout(10 * 1000);
			connection.setDoInput(true);
			connection.setRequestMethod("GET");
			connection.setReadTimeout(10 * 1000);
			stream = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(stream));
			String temp = reader.readLine();
			if(null != temp){
				currentVersion = temp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(reader != null){
						reader.close();
				}
				
				if(stream != null){
					stream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Log.d(TAG, "--------------------------------current version is:" + currentVersion);
		Message msg = handler.obtainMessage();
		msg.what = 1;
		msg.obj = currentVersion;
		handler.sendMessage(msg);
	}

}
