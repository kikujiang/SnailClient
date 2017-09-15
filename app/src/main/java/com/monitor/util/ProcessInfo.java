package com.monitor.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.snailclient.activity.phone;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * 
 * 类的描述：读取进程信息
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class ProcessInfo {
	private static final String LOG_TAG = "ProcessInfo";

	private static final String PACKAGE_NAME = "com.netease.qa.emmagee";
	
	private static final int TIMEOUT = 10 * 1000;

	/**
	 * get information of all running processes,including package name ,process
	 * name ,icon ,pid and uid.
	 * 
	 * @param context
	 *            context of activity
	 * @return running processes list
	 */
	public List<Programe> getRunningProcess(Context context) {
		Log.e(LOG_TAG, "get running processes");
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		PackageManager pm = context.getPackageManager();
		List<Programe> progressList = new ArrayList<Programe>();

		for (ApplicationInfo appinfo : getPackagesInfo(context)) {
			Programe programe = new Programe();
			if (((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
					|| ((appinfo.processName != null) && (appinfo.processName
							.equals(PACKAGE_NAME)))) {
				continue;
			}
			programe.setUid(appinfo.uid);
			programe.setPackageName(appinfo.processName);
			programe.setProcessName(appinfo.loadLabel(pm).toString());
			programe.setIcon(appinfo.loadIcon(pm));
			progressList.add(programe);
		}
		Collections.sort(progressList);
		return progressList;
	}

	private int getPidByUid(Context context, int uid) {
		ActivityManager mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
				.getRunningAppProcesses()) {
			if (appProcess.uid == uid) {
				int currentPid = appProcess.pid;
				return currentPid;
			}
		}
		return 0;
	}
	
	/**
	 * 获取应用的pid信息
	 * @param currentContext 当前的上下文信息
	 * @param uid 当前的用户id
	 * @param packageName 当前的游戏包名
	 * @return 
	 */
	public int getPid(Context currentContext,int uid,String packageName){
		int pid = 0;
		
		pid = getPidByUid(currentContext, uid);
		
		if(pid == 0){
			pid = getPidByPackageName(packageName);
		}
		
		return pid;
	}
	
	private int getPidByPackageName(String packageName){
		int pid = 0;
		phone currentPhone = new phone(null);
		long startTime = System.currentTimeMillis();
		
		while (System.currentTimeMillis() < startTime + TIMEOUT) {
			try {
				String pidData = currentPhone.getPid(packageName);
				if ((pidData != null)
						&& pidData.trim().matches("^[0-9]*[1-9][0-9]*$")) {
					pid = Integer.parseInt(pidData);
				}
			} catch (IOException e) {
				pid = 0;
			}
		}
		return pid;
	}

	/**
	 * get information of all applications.
	 * 
	 * @param context
	 *            context of activity
	 * @return packages information of all applications
	 */
	private List<ApplicationInfo> getPackagesInfo(Context context) {
		PackageManager pm = context.getApplicationContext().getPackageManager();
		List<ApplicationInfo> appList = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		return appList;
	}
}
