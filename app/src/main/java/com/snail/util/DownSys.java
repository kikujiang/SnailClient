package com.snail.util;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
/**
 * 
 * 类的描述：调用系统下载类
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class DownSys {
	String url;
	String filename;
	private DownloadManager downloadManager;
   String pro;

	public DownSys(DownloadManager downloadManager, String url,String pro) {
		this.downloadManager = downloadManager;
		this.url = url;
		filename = url.substring(url.lastIndexOf("/") + 1);
		this.pro=pro;
	}

	@SuppressLint("NewApi")
	public long download() {
		boolean idown = isFolderExist(Constants.downpath);
		if (idown) {
			Uri resource = Uri.parse(url);
			DownloadManager.Request request = new DownloadManager.Request(
					resource);
			request.setAllowedNetworkTypes(Request.NETWORK_MOBILE
					| Request.NETWORK_WIFI);
			request.setAllowedOverRoaming(false);
			// 设置文件类型
			MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
			String mimeString = mimeTypeMap
					.getMimeTypeFromExtension(MimeTypeMap
							.getFileExtensionFromUrl(url));
			request.setMimeType(mimeString);
			// 在通知栏中显示
			request.setShowRunningNotification(true);
			request.setVisibleInDownloadsUi(false);
			// sdcard的目录下的download文件夹
			request.setDestinationInExternalPublicDir(Constants.downpath, filename);
			request.setTitle(pro+"下载");
			long id = downloadManager.enqueue(request);
			Log.d("zlulan", "dddid:" + id);
			//Constants.countdown++;
			return id;
		} else {
			return 0;
		}
	}

	private boolean isFolderExist(String dir) {
		File folder = Environment.getExternalStoragePublicDirectory(dir);
		if (folder.exists() && folder.isDirectory()) {
			File[] list = folder.listFiles();
			for (File f : list) {
				if (f.getName().equals(filename)) {
					f.delete();
					return true;
				}
			}
		} else {
			folder.mkdirs();
		}
		return true;
	}
}
