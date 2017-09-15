package com.snail.util;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.WindowManager;

public class SnailApplication extends Application {
	
	public static final String TAG = "test";
	
	public WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
	
	public WindowManager.LayoutParams getMywmParams() {
		return wmParams;
	}
	
	public static Context getContext() {
        return mContext;
    }

	public static void setContext(Context context){
		mContext = context;
	}
	
    private static Context mContext;

    @Override
    public void onCreate() {
    	Log.d(TAG, "-------------------context created------------------");
        super.onCreate();
        mContext = this;
//        CrashHandler crash = CrashHandler.getInstace();
//		crash.init(mContext);
    }
}
