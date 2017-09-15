package org.androidpn.demoapp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.monitor.util.Contact;
import com.monitor.util.ProcessInfo;
import com.monitor.util.Programe;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.snail.service.SubmitReportService;
import com.snail.util.Constants;
import com.snail.util.DownSys;

import org.snailclient.activity.phone;

/**
 * 
 * 类的描述：测试报告列表适配类
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class ListAdapter extends BaseAdapter {
	public static final String TAG = "ListAdapter";
	Project pro = null;
	String packageName = null;
	String id = null;
	String mark = "0";
	int pid;
	int uid;
	Viewholder holder;
	Handler myHandler;
	Map<String, TextView> hp;
	private List<Programe> processList;
	private ProcessInfo processInfo;
	private static final int TIMEOUT = 20000;
	PackageManager pm;
	List<ApplicationInfo> appList;
	DisplayImageOptions options;
	TextPaint tp;
	String list;
	List<Project> programe;
	SharedPreferences errorType;
	Context context;
	phone ph = null;
//	private Intent monitorService;
	private Intent sub;
	ImageLoader imageLoader;
	DownloadManager manager;

	class Viewholder {
		TextView txtAppName;
		ImageView imgViAppIcon;
		TextView version;
		TextView rp;
		Button od;
	}

	public ListAdapter(String list, Context context, Map<String, TextView> hp,
			Handler myHandler) {
		errorType = context.getSharedPreferences("errorType", 0);
		this.list = list;
		manager = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		imageLoader = ImageLoader.getInstance();
		programe = controlAllList(list);
		this.context = context;
		processInfo = new ProcessInfo();
		this.hp = hp;
		this.myHandler = myHandler;
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.logo)
				.showImageForEmptyUri(R.drawable.logo)
				.showImageOnFail(R.drawable.logo).cacheInMemory(true)
				.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();
		imageLoader.init(ImageLoaderConfiguration.createDefault(context));
		pm = context.getPackageManager();
		appList = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		ph = new phone(null);
	}

	public int getCount() {
		return programe.size();
	}

	public Object getItem(int position) {

		return programe.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "method getView called!");
		holder = new Viewholder();
		convertView = ((Activity) context).getLayoutInflater().inflate(
				R.layout.allitem, null);
		holder.txtAppName = (TextView) convertView.findViewById(R.id.pj);
		holder.imgViAppIcon = (ImageView) convertView.findViewById(R.id.rb);
		holder.version = (TextView) convertView.findViewById(R.id.version);
		holder.rp = (TextView) convertView.findViewById(R.id.rp);
		holder.od = (Button) convertView.findViewById(R.id.od);
		// Programe pr = (Programe) programe.get(position);
		// holder.imgViAppIcon.setImageDrawable(pr.getIcon());
		holder.rp.setText(programe.get(position).getAppVersion()
				+ programe.get(position).getVersionType());
		holder.txtAppName.setText(programe.get(position).getPro());
		holder.version.setText("大小:" + programe.get(position).getAppSize()
				+ "M");
		imageLoader.displayImage(programe.get(position).getApkLogo(),
				holder.imgViAppIcon, options);
		String filename = programe.get(position).getUrl();
		filename = filename.substring(filename.lastIndexOf(File.separator) + 1);
		Log.d(TAG, "file name is:" + filename);
		if (isprocessName(programe.get(position).packageName)) {
			holder.od.setText("运行");
			programe.get(position).setIsRun(true);
		} else if (exist(filename, programe.get(position).getAppSize())) {
			holder.od.setText("安装");
		} else {
			holder.od.setText("下载");
			programe.get(position).setIsRun(false);
		}
		holder.od.setOnClickListener(new onClickListener(position));
		return convertView;
	}

	class myThread extends TimerTask {
		Handler btn;
		long id;
		Message msg;
		Timer tm;

		public myThread(Handler arg0, long id, Timer tm) {
			this.btn = arg0;
			this.id = id;
			this.tm = tm;

		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterById(id);
			msg = new Message();
			Cursor c = manager.query(query);
			if (c != null && c.moveToFirst()) {
				int status = c.getInt(c
						.getColumnIndex(DownloadManager.COLUMN_STATUS));

				int reasonIdx = c.getColumnIndex(DownloadManager.COLUMN_REASON);
				int titleIdx = c.getColumnIndex(DownloadManager.COLUMN_TITLE);
				int fileSizeIdx = c
						.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
				int bytesDLIdx = c
						.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
				String title = c.getString(titleIdx);
				int fileSize = c.getInt(fileSizeIdx);
				int bytesDL = c.getInt(bytesDLIdx);
				String rlt = String
						.valueOf((int) (((float) bytesDL / (float) fileSize) * 100));
				if (fileSize == bytesDL) {
					tm.cancel();
				}
				// Translate the pause reason to friendly text.
				int reason = c.getInt(reasonIdx);
				StringBuilder sb = new StringBuilder();
				sb.append(title).append("\n");
				sb.append("Downloaded ").append(bytesDL).append(" / ")
						.append(fileSize);

				// Display the status
				Log.e("zlulan", rlt + "");
				switch (status) {
				case DownloadManager.STATUS_PAUSED:
					Log.e("zlulan", "STATUS_PAUSED");
					break;
				case DownloadManager.STATUS_PENDING:
					Log.e("zlulan", "STATUS_PENDING");
					break;
				case DownloadManager.STATUS_RUNNING:
					// 正在下载，不做任何事情
					Log.v("zlulan", "STATUS_RUNNING");
					msg.what = 2;
					msg.obj = String.valueOf(id) + "," + rlt + "%";
					btn.sendMessage(msg);
					break;
				case DownloadManager.STATUS_SUCCESSFUL:
					// 完成
					Log.v("zlulan", "下载完成");
					// dowanloadmanager.remove(lastDownloadId);
					break;
				case DownloadManager.STATUS_FAILED:
					// 清除已下载的内容，重新下载
					Log.e("zlulan", "STATUS_FAILED");
					msg.what = 2;
					msg.obj = String.valueOf(id) + ",下载";
					btn.sendMessage(msg);
					tm.cancel();
					break;
				}
			}
		}

	}

	class onClickListener implements OnClickListener {
		int position;

		public onClickListener(int positon) {
			this.position = positon;
		}

		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (programe.get(position).getIsRun()) {
				run(programe.get(position));
			} else if (((TextView) arg0).getText().toString().contains("%")) {
				((TextView) arg0).setText("下载");
				manager.remove(programe.get(position).getId());
				programe.get(position).setId(0);
			} else if (((TextView) arg0).getText().toString().contains("安装")) {
				String filename = programe.get(position).url;
				filename = filename.substring(filename
						.lastIndexOf(File.separator) + 1);
				openInstall(context, Constants.folder + filename);
			} else {
				DownloadManager down = (DownloadManager) context
						.getSystemService(context.DOWNLOAD_SERVICE);
				long id = new DownSys(down, programe.get(position).url,
						programe.get(position).getPro()).download();
				programe.get(position).setId(id);
				// queryDownloadStatus(((TextView) arg0),id);
				hp.put(String.valueOf(id), ((TextView) arg0));
				Timer tm = new Timer();
				tm.schedule(new myThread(myHandler, id, tm), 1000, 2000);
			}

		}

	}

	private List<Project> controlAllList(String list) {
		List<Project> lp = new ArrayList<Project>();
		List<ProjectNew> newlp = new ArrayList<ProjectNew>();
		ProjectResponse response = new ProjectResponse();
		SharedPreferences.Editor editor = errorType.edit();
		int m = 0;
		Gson gson = new Gson();
		response = gson.fromJson(list, ProjectResponse.class);
		if (response.getResult().equals("success")) {
			// 此时表明数据获取成功
			newlp = response.getList();

			if (newlp.size() < 1) {
				return lp;
			}

			for (ProjectNew projectNew : newlp) {
				String report = projectNew.getName();
				String rid = projectNew.getRid();
				String pro = projectNew.getProjectName();
				String url = projectNew.getApkUrl();
				String apkLogo = projectNew.getApkLogo();
				String appVersion = projectNew.getAppVersion();
				String appSize = projectNew.getApkSize();
				String versionType = projectNew.getVersionType();
				String packageName = projectNew.getPackageName();
				lp.add(new Project(url, rid, report, pro, apkLogo, appVersion,
						appSize, versionType,packageName));
			}

			if (null == response.getErrorType()
					|| response.getErrorType().size() < 1) {
				return lp;
			}

			for (int i = 0; i < response.getErrorType().size(); i++) {
				editor.putString(i + "", response.getErrorType().get(i));
				m++;
			}
			editor.putInt("count", m);
			editor.commit();
		}

		return lp;
	}

	public void run(Project pro) {
		if (isprocessName(pro.getPackageName())) {
			Intent intent = context.getPackageManager()
					.getLaunchIntentForPackage(packageName);
			String startActivity = "";
			id = pro.getRid();
			Contact.testID = id;
			Contact.packageName = packageName;
			mark = "0";
			Log.d("zlulan", packageName);
			// clear logcat
			try {
				Runtime.getRuntime().exec("logcat -c");
			} catch (IOException e) {
				Log.d("zlulan", e.getMessage());
			}
			try {
				startActivity = intent.resolveActivity(
						context.getPackageManager()).getShortClassName();
				context.startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(context, "启动应用", Toast.LENGTH_LONG).show();
				return;
			}
			if (waitForAppStart(packageName)) {
				Constants.startTestTime = new Date();
				sub = new Intent();
				sub.setClass(context, SubmitReportService.class);
				sub.putExtra("packageName", packageName);
				sub.putExtra("id", pro.getRid());
				sub.putExtra("processName", pro.getPro());
				sub.putExtra("pid", pid);
				sub.putExtra("uid", uid);
				context.startService(sub);
			}
		} else {
			Toast.makeText(context, "手机不存在该应用请先下载", Toast.LENGTH_LONG).show();

		}
	}

	private boolean waitForAppStart(String packageName) {
		boolean isProcessStarted = false;
		processList = processInfo.getRunningProcess(context);
		for (Programe programe : processList) {
			if ((programe.getPackageName() != null)
					&& (programe.getPackageName().equals(packageName))) {
				uid = programe.getUid();
				pid = processInfo.getPid(context, uid, packageName);
				Log.d(TAG, "pid:" + pid);
				isProcessStarted = true;
				break;
			}
		}
//		while (System.currentTimeMillis() < startTime + TIMEOUT) {
//			if (isProcessStarted) {
//				break;
//			}
//		}
		return isProcessStarted;
	}

	public boolean isprocessName(String processName) {
		for (ApplicationInfo appinfo : appList) {
			String appName = appinfo.packageName;
			if (appName.equals(processName)) {
				packageName = appinfo.packageName;
				return true;
			}
		}
		return false;
	}

	public boolean exist(String name, String size) {
		int i = 0;
		File folder = Environment
				.getExternalStoragePublicDirectory(Constants.downpath);
		if (folder.exists() && folder.isDirectory()) {
			File[] list = folder.listFiles();
			for (; i < list.length; i++) {
				if (list[i].getName().equals(name)) {
					return true;
				}
			}
			if (i >= list.length) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}

	}

	public void openInstall(Context context, String project) {
		// if (Constants.sdk > 14) {
		// Constants.ilaunch = true;
		String fileName = project;
		// MyAccessibility.INVOKE_TYPE = MyAccessibility.TYPE_INSTALL_APP;
		// MyAccessibility.isback = true;
		File ff = new File(fileName);
		if (ff.exists()) {
			Intent inten = new Intent(Intent.ACTION_VIEW);
			inten.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			inten.setDataAndType(Uri.fromFile(ff),
					"application/vnd.android.package-archive");
			context.startActivity(inten);
		}
		// } else {
		// Intent intent = new Intent();
		// intent.setAction("org.androidpn.client.service");
		// intent.putExtra("type", 2);
		// context.startService(intent);
		// }
	}

}
