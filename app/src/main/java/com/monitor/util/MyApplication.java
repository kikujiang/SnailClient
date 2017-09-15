package com.monitor.util;

import android.app.Application;
import android.view.WindowManager;

public class MyApplication extends Application {
	public WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

	public WindowManager.LayoutParams getMywmParams() {
		return wmParams;
	}
}
