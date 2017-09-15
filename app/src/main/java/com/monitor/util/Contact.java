package com.monitor.util;

import java.util.Properties;

import android.content.Context;
import android.util.Log;

public class Contact {
	public static final String[] actionArrays = new String[]{"start01","login01","xinshou01","ui01","tongping01","switch01","scene01","fight01","action01"};
	public static String testMessage = null;
	public static String testID = null;
	public static String packageName = null;
	public static String testType = null;
	public static String mac = null;
	public static String userName = null;
    //是否显示悬浮窗
	public static boolean isShowFloatingWindow = false;
	public static String from = "1";
	public static boolean ispack = true;
	public static boolean isFirstStartMonitor = true;

	public static Properties loadProperties(Context context) {
		Properties props = new Properties();
		try {
			int id = context.getResources().getIdentifier("androidpn", "raw",
					context.getPackageName());
			props.load(context.getResources().openRawResource(id));
		} catch (Exception e) {
			Log.e("zlulan", "Could not find the properties file.", e);
		}
		return props;
	}
}
