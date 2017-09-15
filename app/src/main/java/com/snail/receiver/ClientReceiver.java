package com.snail.receiver;

import java.io.File;
import java.util.List;

import org.snailclient.activity.phone;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.monitor.util.Contact;
import com.monitor.util.ProcessInfo;
import com.monitor.util.Programe;
import com.snail.service.controlservice;
import com.snail.util.Constants;
import com.snail.util.SnailApplication;

/**
 * 
 * 类的描述：接收下载完成广播和监控数据处理广播
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class ClientReceiver extends BroadcastReceiver {
	
	public static final String TAG = "ClientReceiver";
	
	private Intent monitorService;
	private static final int TIMEOUT = 30000;
	private List<Programe> processList;
	private ProcessInfo processInfo;
	private String packageName = null;
	private CharSequence processName = "";
	private int pid = 0, uid = 0;
	private boolean lauch = false;
	private String id = null;
	private String scriptStep = "";
	phone ph = null;

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Log.e("zhll", "--------------------receive message--------------------");
		final DownloadManager manager = (DownloadManager) arg0
				.getSystemService(Context.DOWNLOAD_SERVICE);
		String action = arg1.getAction();
		Bundle extras = arg1.getExtras();
		monitorService = new Intent();
		if (extras == null) {
			Log.e("zlulan", "No extras provided");
			return;
		}
		if (action.equals("android.intent.action.BROADCAST")) {
			monitorService.setClass(SnailApplication.getContext(), controlservice.class);
			String msg = arg1.getStringExtra("msg");
			String mark = "0";
			Log.e("zhll", msg);
			if (msg.contains("environment")) {
				monitorService.setAction("android.intent.action.environment");
				if (msg.contains(":")) {
					String[] mgg = msg.split(":");
					if (mgg.length > 1) {
						String name = mgg[1];
						monitorService.putExtra("command", name);
						arg0.startService(monitorService);
					}
				}
			} else {
				monitorService.setAction("android.intent.action.service");
				if (msg.trim().equals("000")) {
					boolean dd = arg0.stopService(monitorService);
					Log.e("zlulan", "是否停止服务" + dd);
					Contact.isFirstStartMonitor = true;
				}else if("stop".equals(msg.trim())){
					controlservice.mark = "";
				} else if (msg != null) {
					if (msg.contains(":")) {
						Contact.from = "1";
						String[] mess = msg.split(":");
						packageName = mess[0];
						if (mess.length > 1) {
							mark = mess[1];
						}
						if (mess.length > 2) {
							Contact.testType = mess[2];
						}
						if (mess.length > 3) {
							Contact.testID = mess[3];
						}
						if (mess.length > 4) {
							scriptStep = mess[4];
						}
						if (mess.length > 5) {
							Contact.from = mess[5];
						}
					} else {
						packageName = Contact.packageName;
						mark = msg.trim();
					}
					id = Contact.testID;
					processInfo = new ProcessInfo();
					lauch = waitForAppStart(packageName, arg0);
//					if (lauch) {
						if(Contact.isFirstStartMonitor){
							monitorService.putExtra("pid", pid);
							monitorService.putExtra("uid", uid);
							monitorService.putExtra("packageName", packageName);
							monitorService.putExtra("processName", processName);
							monitorService.putExtra("mark", mark);
							monitorService.putExtra("id", id);
							monitorService.putExtra("scriptStep", scriptStep);
							Contact.isShowFloatingWindow = false;
							Contact.isFirstStartMonitor = false;
							arg0.startService(monitorService);
						}else{
							controlservice.mark = mark;
						}
//					}
				}
			}
		} else if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {

			long reference = arg1.getLongExtra(
					DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			Query myDownloadQuery = new Query();
			myDownloadQuery.setFilterById(reference);

			Cursor myDownload = manager.query(myDownloadQuery);
			if (myDownload.moveToFirst()) {
				int fileNameIdx = myDownload
						.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);

				String fileName = myDownload.getString(fileNameIdx);
				if (fileName != null) {
					openInstall(arg0, fileName);
				}
			}
		} else  {
			String msg = arg1.getStringExtra("msg");
			Constants.command = msg;
			monitorService.setAction("org.androidpn.client.service");
			monitorService.putExtra("command", msg);
			monitorService.putExtra("type", 0);
			arg0.startService(monitorService);
		}
	}

	private boolean waitForAppStart(String packageName, Context context) {
		Log.e("zhll", "wait for app start");
		ph = new phone(null);
		String tmp = null;
		pid = 0;
		boolean isProcessStarted = false;
//		long startTime = System.currentTimeMillis();
		processList = processInfo.getRunningProcess(SnailApplication.getContext());
		for (Programe programe : processList) {
			if ((programe.getPackageName() != null)
					&& (programe.getPackageName().equals(packageName))) {
				uid = programe.getUid();
				processName = programe.getProcessName();
				pid = processInfo.getPid(context, uid, packageName);
				Log.d(TAG, "pid is:" + pid);
				isProcessStarted = true;
//				if (pid != 0) {
//				}
				break;
			}
		}
		
//		if(pid != 0){
//			isProcessStarted = true;
//		}else{
//			while (System.currentTimeMillis() < startTime + TIMEOUT) {
//				try {
//					tmp = ph.getPid(packageName);
//					if ((tmp != null) && tmp.trim().matches("^[0-9]*[1-9][0-9]*$")) {
//						pid = Integer.parseInt(tmp);
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				Log.e("zhll", "pid:" + pid);
//				if (pid != 0) {
//					isProcessStarted = true;
//					break;
//				}
//			}
//		}
		return isProcessStarted;
	}

	public void openInstall(Context context, String project) {
		String fileName = project;
		File ff = new File(fileName);
		if (ff.exists()) {
			Intent inten = new Intent(Intent.ACTION_VIEW);
			inten.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			inten.setDataAndType(Uri.fromFile(ff),
					"application/vnd.android.package-archive");
			context.startActivity(inten);
		}
	}
}
