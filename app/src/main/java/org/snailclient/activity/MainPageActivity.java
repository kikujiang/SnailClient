package org.snailclient.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.monitor.util.Contact;
import com.monitor.util.ProcessInfo;
import com.monitor.util.Programe;
import com.snail.service.controlservice;
import com.snail.util.Constants;

import org.androidpn.demoapp.R;
import org.json.JSONObject;
import org.snailclient.activity.utils.action.ActionDataUtil;
import org.snailclient.activity.utils.fps.GenericToast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solo.HttpUtilForWired;

/**
 * 
 * 类的描述：性能测试选择应用界面
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class MainPageActivity extends AppCompatActivity {

	private static final String LOG_TAG = "MainPageActivity";

	private int REQUEST_MEDIA_PROJECTION = 1;

	phone ph = null;
	private List<Programe> processList;
	private ProcessInfo processInfo;
	private Intent monitorService;
	private ListView lstViProgramme;
	private Button btnTest;
	private boolean isRadioChecked = false;
	private int pid, uid;
	private String processName, packageName, mark, id;
	private boolean isServiceStop = false;
	private UpdateReceiver receiver;
	private String testType;
	private String from;
	private String tid;

	public static final int MSG_BEGIN_TEST_SUCCESS = 100001;
	public static final int MSG_BEGIN_TEST_FAILURE = 100002;
	public static final int MSG_APP_START = 100003;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what){
				case MSG_APP_START:
					sendBeginTestCommand();
					break;
				case MSG_BEGIN_TEST_SUCCESS:
					if(Constants.sdk > 22){
						requestAlertWindowPermission();
					}else{
						launchApp();
					}
					break;
				case MSG_BEGIN_TEST_FAILURE:
					String failDesc = msg.obj.toString();
					Toast.makeText(MainPageActivity.this, failDesc, Toast.LENGTH_LONG).show();
					break;
			}
		}
	};

	private static final int REQUEST_CODE = 200;
	private  void requestAlertWindowPermission() {
		Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
		intent.setData(Uri.parse("package:" + getPackageName()));
		startActivityForResult(intent, REQUEST_CODE);
	}

	private void launchApp(){
		Intent intent = getPackageManager().getLaunchIntentForPackage(
				packageName);
		String startActivity = "";
		id = null;
		mark = "start01";
		Log.d(LOG_TAG, packageName);
		try {
			Runtime.getRuntime().exec("logcat -c");
		} catch (IOException e) {
			Log.d(LOG_TAG, e.getMessage());
		}
		try {
			startActivity = intent.resolveActivity(getPackageManager())
					.getShortClassName();
			Log.d("controlservice", "activity name is:" + startActivity);
			startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(MainPageActivity.this, "启动应用失败",
					Toast.LENGTH_LONG).show();
			return;
		}
		if (waitForAppStart(packageName)) {
			monitorService.putExtra("processName", processName);
			monitorService.putExtra("pid", pid);
			monitorService.putExtra("uid", uid);
			monitorService.putExtra("tid", tid);
			monitorService.putExtra("packageName", packageName);
			monitorService.putExtra("mark", mark);
			monitorService.putExtra("id", id);
			// monitorService.putExtra("testType",testType);
			Contact.testType = testType;
			Contact.from = from;
			Contact.isShowFloatingWindow = true;
			// monitorService.putExtra("from",from);
			startService(monitorService);
			finish();
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		Log.i("zhll", "MainActivity::onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_page);
		ph = new phone(null);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);
		Intent intent = getIntent();
		testType = intent.getStringExtra("testType");
		from = intent.getStringExtra("from");
		// createNewFile();
		processInfo = new ProcessInfo();
		lstViProgramme = (ListView) findViewById(R.id.processList);
		btnTest = (Button) findViewById(R.id.test);
		btnTest.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				monitorService = new Intent();
				monitorService.setClass(MainPageActivity.this,
						controlservice.class);
				if ("START".equals(btnTest.getText().toString())) {
					if (isRadioChecked) {
						new Thread() {
							public void run() {
								if (!TextUtils.isEmpty(processName)) {
									ActionDataUtil.getInstance()
											.getActionDataFromServer(
													processName);
									mHandler.sendEmptyMessage(MSG_APP_START);
								}
							};
						}.start();
					} else {
						Toast.makeText(MainPageActivity.this, "请选择应用",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		lstViProgramme.setAdapter(new ListAdapter());

		if (Constants.versionCode > 20) {
			Constants.takeshotMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
			startActivityForResult(
					Constants.takeshotMediaProjectionManager
							.createScreenCaptureIntent(),
					REQUEST_MEDIA_PROJECTION);
		}
	}

	private void sendBeginTestCommand() {
		final String url = Constants.DATA_URL
				+ "/platform/MobileClientMonitor/MobileClientMonitorMaint.do";
		final List<Map<String, String>> beginTestData = new ArrayList<Map<String, String>>();
		Map<String, String> dataFrom = new HashMap<String, String>();
		Map<String, String> dataAct = new HashMap<String, String>();
		Map<String, String> dataMacAddress = new HashMap<String, String>();
		Map<String, String> dataProjectName = new HashMap<String, String>();
		Map<String, String> testType = new HashMap<String, String>();

		dataAct.put("key", "act");
		dataAct.put("value", "beginTest");
		beginTestData.add(dataAct);

		dataFrom.put("key", "from");
		dataFrom.put("value", Contact.from);
		beginTestData.add(dataFrom);

		dataMacAddress.put("key", "mac");
		dataMacAddress.put("value", Contact.mac.trim());
		beginTestData.add(dataMacAddress);

		dataProjectName.put("key", "projectName");
		dataProjectName.put("value", processName.trim());
		beginTestData.add(dataProjectName);

		testType.put("key", "testType");
		testType.put("value", Contact.testType);
		beginTestData.add(testType);
		Log.e(LOG_TAG, "=======begin test data =======url is:" + url
				+ "===send data is:" + beginTestData.toString());

		new Thread() {
			public void run() {
				String beginTestResult = HttpUtilForWired.getInstance().sendData(url,
						beginTestData);
				Log.e(LOG_TAG, " begin test result is:" + beginTestResult);
				try {
					JSONObject resultData = new JSONObject(beginTestResult);
					String result = resultData.getString("result");
					if (result.equals("success")) {
						tid = resultData.getString("tid");
						Log.e(LOG_TAG, "tid is:" + tid);
						mHandler.sendEmptyMessage(MSG_BEGIN_TEST_SUCCESS);
					} else {
						String desc = resultData.getString("desc");
						Message currentMsg = Message.obtain();
						currentMsg.what = MSG_BEGIN_TEST_FAILURE;
						currentMsg.obj = desc;
						mHandler.sendMessage(currentMsg);
					}

				} catch (Exception e) {
					Log.d(LOG_TAG, "exception is:" + beginTestResult);
					Message currentMsg = Message.obtain();
					currentMsg.what = MSG_BEGIN_TEST_FAILURE;
					currentMsg.obj = beginTestResult;
					mHandler.sendMessage(currentMsg);
				}
			}

			;
		}.start();
	}

	/**
	 * customized BroadcastReceiver
	 * 
	 * @author andrewleo
	 */
	public class UpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			isServiceStop = intent.getExtras().getBoolean("isServiceStop");
			if (isServiceStop) {
				btnTest.setText("START");
			}
		}
	}

	@Override
	protected void onStart() {
		Log.d("zhll", "onStart");
		receiver = new UpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.netease.action.emmageeService");
		this.registerReceiver(receiver, filter);
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume");
		if (controlservice.isStop) {
			btnTest.setText("START");
		}
	}

	/**
	 * create new file to reserve setting data.
	 */
	// private void createNewFile() {
	// Log.i(LOG_TAG, "create new file to save setting data");
	// settingTempFile = getBaseContext().getFilesDir().getPath() +
	// "\\EmmageeSettings.properties";
	// Log.i(LOG_TAG, "settingFile = " + settingTempFile);
	// File settingFile = new File(settingTempFile);
	// if (!settingFile.exists()) {
	// try {
	// settingFile.createNewFile();
	// Properties properties = new Properties();
	// properties.setProperty("interval", "5");
	// properties.setProperty("isfloat", "true");
	// properties.setProperty("sender", "");
	// properties.setProperty("password", "");
	// properties.setProperty("recipients", "");
	// properties.setProperty("smtp", "");
	// FileOutputStream fos = new FileOutputStream(settingTempFile);
	// properties.store(fos, "Setting Data");
	// fos.close();
	// } catch (IOException e) {
	// Log.d(LOG_TAG, "create new file exception :" + e.getMessage());
	// }
	// }
	// }

	/**
	 * wait for test application started.
	 * 
	 * @param packageName
	 *            package name of test application
	 */
	private boolean waitForAppStart(String packageName) {
		Log.d(LOG_TAG, "wait for app start");
		String tmp = null;
		pid = 0;
		boolean isProcessStarted = false;
		long startTime = System.currentTimeMillis();
		// while (System.currentTimeMillis() < startTime + TIMEOUT) {
		processList = processInfo.getRunningProcess(getBaseContext());
		for (Programe programe : processList) {
			if ((programe.getPackageName() != null)
					&& (programe.getPackageName().equals(packageName))) {
				uid = programe.getUid();
				pid = processInfo.getPid(MainPageActivity.this, uid, packageName);
				Log.d(LOG_TAG, "pid is:" + pid + ", uid is:" + uid);
				isProcessStarted = true;
				if (pid != 0) {
					break;
				}
			}

		}
		return isProcessStarted;
	}

	/**
	 * show a dialog when click return key.
	 * 
	 * @return Return true to prevent this event from being propagated further,
	 *         or false to indicate that you have not handled this event and it
	 *         should continue to be propagated.
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			showDialog(0);
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * set menu options,including cancel and setting options.
	 * 
	 * @return true
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.add(0, Menu.FIRST, 0,
		// "閫��?).setIcon(android.R.drawable.ic_menu_delete);
		// menu.add(0, Menu.FIRST, 1,
		// "璁剧�?).setIcon(android.R.drawable.ic_menu_directions);
		return true;
	}

	/**
	 * trigger menu options.
	 * 
	 * @return false
	 */
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getOrder()) {
	// case 0:
	// showDialog(0);
	// break;
	// case 1:
	// Intent intent = new Intent();
	// intent.setClass(MainPageActivity.this, SettingsActivity.class);
	// intent.putExtra("settingTempFile", settingTempFile);
	// startActivityForResult(intent, Activity.RESULT_FIRST_USER);
	// break;
	// default:
	// break;
	// }
	// return false;
	// }

	/**
	 * create a dialog.
	 * 
	 * @return a dialog
	 */
	// protected Dialog onCreateDialog(int id) {
	// switch (id) {
	// case 0:
	// return new
	// AlertDialog.Builder(this).setTitle("纭畾閫�嚭绋嬪簭锛�?.setPositiveButton("纭�?,
	// new android.content.DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// if (monitorService != null) {
	// Log.d(LOG_TAG, "stop service");
	// stopService(monitorService);
	// }
	// Log.d(LOG_TAG, "exit Emmagee");
	// EmmageeService.closeOpenedStream();
	// finish();
	// System.exit(0);
	// }
	// }).setNegativeButton("鍙栨�?, null).create();
	// default:
	// return null;
	// }

	/**
	 * customizing adapter.
	 * 
	 * @author andrewleo
	 */
	class ListAdapter extends BaseAdapter {
		List<Programe> programe;
		int tempPosition = -1;

		/**
		 * save status of all installed processes
		 * 
		 * @author andrewleo
		 */
		class Viewholder {
			TextView txtAppName;
			ImageView imgViAppIcon;
			RadioButton rdoBtnApp;
		}

		public ListAdapter() {
			programe = processInfo.getRunningProcess(getBaseContext());
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
			Viewholder holder = new Viewholder();
			final int i = position;
			convertView = MainPageActivity.this.getLayoutInflater().inflate(
					R.layout.list_item, null);
			holder.imgViAppIcon = (ImageView) convertView
					.findViewById(R.id.image);
			holder.txtAppName = (TextView) convertView.findViewById(R.id.text);
			holder.rdoBtnApp = (RadioButton) convertView.findViewById(R.id.rb);
			holder.rdoBtnApp.setId(position);
			holder.rdoBtnApp
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								isRadioChecked = true;
								// Radio function
								if (tempPosition != -1) {
									RadioButton tempButton = (RadioButton) findViewById(tempPosition);
									if ((tempButton != null)
											&& (tempPosition != i)) {
										tempButton.setChecked(false);
									}
								}

								tempPosition = buttonView.getId();
								packageName = programe.get(tempPosition)
										.getPackageName();
								processName = programe.get(tempPosition)
										.getProcessName();
							}
						}
					});
			if (tempPosition == position) {
				if (!holder.rdoBtnApp.isChecked())
					holder.rdoBtnApp.setChecked(true);
			}
			Programe pr = (Programe) programe.get(position);
			holder.imgViAppIcon.setImageDrawable(pr.getIcon());
			holder.txtAppName.setText(pr.getProcessName());
			return convertView;
		}
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void onStop() {
		unregisterReceiver(receiver);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE) {
			if(Constants.sdk > 22){
				if (Settings.canDrawOverlays(this)) {
					launchApp();
				}
			}
		}

		if (requestCode == REQUEST_MEDIA_PROJECTION) {
			if (resultCode != AppCompatActivity.RESULT_OK) {
				return;
			} else if (data != null && resultCode != 0) {

				Log.i(LOG_TAG, "user agree the application to capture screen");
				Constants.takeshotResultCode = resultCode;
				Constants.takeshotResultData = data;
				Constants.takeshotWindowManager = (WindowManager) getApplication()
						.getSystemService(Context.WINDOW_SERVICE);
				Constants.takeshotScreenSize = new Point();
				Constants.takeshotWindowManager.getDefaultDisplay().getSize(
						Constants.takeshotScreenSize);

				Constants.takeshotMetrics = new DisplayMetrics();
				Constants.takeshotWindowManager.getDefaultDisplay().getMetrics(
						Constants.takeshotMetrics);
				Constants.takeshotScreenDensity = Constants.takeshotMetrics.densityDpi;
				// ToastUtil.ShowLongToast(DemoAppActivity.this, "启动截屏服务成功");
				GenericToast.makeText(MainPageActivity.this, "启动截屏服务成功", 500)
						.show();
			}
		}
	}
}
