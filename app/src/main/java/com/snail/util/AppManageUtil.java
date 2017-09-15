package com.snail.util;

import java.io.IOException;
import java.io.OutputStream;

public class AppManageUtil {

	private static Process process;

	/**
	 * 结束进程,执行操作调用即可
	 */
	public static void kill(String packageName) {
		 initProcess();
		 killProcess(packageName);
		 close();
//		ActivityManager am = 
//		List<RunningAppProcessInfo> myappprocess = am.getRunningAppProcesses();
//		for (RunningAppProcessInfo info : myappprocess) {
//
//			if (info.processName.equals(packageName)) {
//				Log.i("zhou", "kill----com.dailyroads.v");
//				int pid = info.pid;
//				Method method = Class.forName("android.app.ActivityManager")
//						.getMethod("forceStopPackage", String.class);
//				method.invoke(am, "com.dailyroads.v");
//
//				break;
//			}
//		}
	}

	/**
	 * 初始化进程
	 */
	private static void initProcess() {
		if (process == null)
			try {
				process = Runtime.getRuntime().exec("sh");
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/**
	 * 结束进程
	 */
	private static void killProcess(String packageName) {
		OutputStream out = process.getOutputStream();
		String cmd = "am force-stop " + packageName + " \n";
		try {
			out.write(cmd.getBytes());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭输出流
	 */
	private static void close() {
		if (process != null)
			try {
				process.getOutputStream().close();
				process = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}
