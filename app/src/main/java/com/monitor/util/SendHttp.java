package com.monitor.util;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.androidpn.client.NotificationDetailsActivity;
import org.androidpn.demoapp.R;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.snail.util.Constants;

/**
 * 
 * 类的描述：发送http协议类
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class SendHttp extends Thread {
	public static final String TAG = "SendHttp";
	private HttpResponse httpResponse = null;
	List<NameValuePair> nameValuePairs;
	String url = null;
	Context context;
	Handler myHandler = null;
	int what = 1;
	public static String result = "";

	public SendHttp(String url, List<NameValuePair> nameValuePairs,
			Context context) {
		this.nameValuePairs = nameValuePairs;
		this.url = url;
		this.context = context;
	}

	public SendHttp(String url, List<NameValuePair> nameValuePairs,
			Context context, Handler myHandler) {
		this.nameValuePairs = nameValuePairs;
		this.url = url;
		this.context = context;
		this.myHandler = myHandler;
	}

	public SendHttp(String url, List<NameValuePair> nameValuePairs,
			Context context, Handler myHandler, int what) {
		this.nameValuePairs = nameValuePairs;
		this.url = url;
		this.context = context;
		this.myHandler = myHandler;
		this.what = what;
	}

	@Override
	public void run() {
		result = "数据交互中...";
		HttpPost httpPost = new HttpPost(url);
		Log.d(TAG, "url is:" + url);
		if (nameValuePairs != null) {
			Log.e(TAG, "nameValuePairs is:" + nameValuePairs.toString());
			HttpEntity requestEntity = null;
			try {
				requestEntity = new UrlEncodedFormEntity(nameValuePairs,
						HTTP.UTF_8);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			httpPost.setEntity(requestEntity);
		}else{
			Log.d(TAG, "nameValuePairs is null");
		}

		HttpClient httpClient = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 20 * 1000);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), 10 * 1000);
		try {
			httpResponse = httpClient.execute(httpPost);
			result = httpResponse.getStatusLine().toString();
			Log.d(TAG, "origin result is:" + result);
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				Log.e(TAG, httpResponse.getStatusLine().toString());
			} else {
				result = EntityUtils
						.toString(httpResponse.getEntity(), "UTF-8");
				Log.e(TAG, "result is:" + result);
			}
			
			Log.e(TAG, "ddsds:" + (context != null));
			JSONObject resultObj = new JSONObject(SendHttp.result);
			if (context != null) {
				Notification.Builder builder = new Notification.Builder(context);
				builder.setContentTitle("SnailClient结果通知");
				builder.setContentText("发送结果" + resultObj.getString("result"));
				builder.setSmallIcon(R.drawable.ok);
				builder.setTicker("测试完成");
				builder.setDefaults(Notification.DEFAULT_SOUND);
				builder.setDefaults(Notification.DEFAULT_VIBRATE);
				builder.setDefaults(Notification.DEFAULT_ALL);

				Intent intent = new Intent(context,
						NotificationDetailsActivity.class);
				intent.putExtra(Constants.NOTIFICATION_MESSAGE, "测试完成"
						+ resultObj.getString("result"));
				PendingIntent contentIntent = PendingIntent.getActivity(
						context, 0, intent, 0);
				builder.setContentIntent(contentIntent);

				NotificationManager mNotificationManager = (NotificationManager) context
						.getSystemService(context.NOTIFICATION_SERVICE);
				mNotificationManager.notify(1, builder.build());
				Log.d(TAG, "发送通知！");
			}
			if (myHandler != null) {
				Message msg = new Message();
				msg.what = what;
				if (result.contains("get voteList success")) {
					msg.obj = result.substring(result.indexOf("["),
							result.indexOf("]"));
				} else {
					msg.obj = resultObj.getString("desc");
				}
				myHandler.sendMessage(msg);
			}
		} catch (Exception e) {
			String e1 = e.getMessage();
			Log.d(TAG, "message is:" + e1);
		}
	}

}
