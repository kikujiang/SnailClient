package com.snail.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.snail.service.ScreenIntentService;
import com.snail.util.Constants;
import com.snail.util.SnailApplication;

public class ScreenShotReceiver extends BroadcastReceiver {

	private static final String TAG = "ScreenShotReceiver";
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Log.d(TAG, "---------------------------收到截图指令信息-----------------------");
		String action = arg1.getAction();
		Bundle extras = arg1.getExtras();
		if(action.equals("android.intent.action.SNAILTEST.TAKEPIC")){
			//收到无线截图指令的信息,进行处理
			String command = extras.getString("picContent");
			Log.d(TAG, "---------------------------收到了无线截图指令的消息，进行截屏操作，收到的指令为:" + command);
			if(Constants.versionCode < 21){
				Log.d(TAG, "---------------------------当前的系统不支持截屏操作");
				return;
			}
			Intent intent = new Intent(SnailApplication.getContext(), ScreenIntentService.class);
            Bundle screenData = new Bundle();
            screenData.putString("command", "screen");
            screenData.putString("data", command);
            intent.putExtras(screenData);
            SnailApplication.getContext().startService(intent);
		}
	}

}
