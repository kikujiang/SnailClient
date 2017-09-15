package org.androidpn.demoapp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.snailclient.activity.utils.fps.ToastUtil;

import solo.HttpUtilForWired;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.monitor.util.Contact;
import com.monitor.util.CustomProgressDialog;
import com.monitor.util.SendHttp;
import com.snail.util.Constants;

/**
 * 
 * 类的描述：提交测试报告界面
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class SubmitLaunchResultActivity extends AppCompatActivity {
	String InstallDesc;
	String LaunchDesc;
	String RunDesc;
	String UninstallDesc;
	EditText etInstall;
	EditText etLaunch;
	EditText etRun;
	EditText etUninstall;
	Button submit;
	String Iet = "success";
	String Let = "success";
	String Ret = "success";
	String Uet = "success";
	Spinner InT;
	Spinner LaT;
	Spinner RuT;
	Spinner UnT;
	ArrayAdapter<String> adapter;
	TextView tvStartDate;
	TextView tvStartTime;
	TextView tvEndDate;
	TextView tvEndTime;
	Calendar calendar;
	TimePickerDialog timePickerDialog;
	DatePickerDialog dialog;
	CustomProgressDialog dlg;
	String packageName;
	String id;
	Button sb;
	Handler handler;
	InputMethodManager imm;
	SharedPreferences errorType;

	// 存image的地址路径
	private String imagePath = null;
	public static final int MSG_UPLOAD_END = 10002;
	public static final int MSG_UPLOAD_PIC = 10003;
	public static final int MSG_UPLOAD_PIC_START = 10004;
	public static final int MSG_UPLOAD_PIC_END_SUCCESS = 10005;
	public static final int MSG_UPLOAD_PIC_END_FAIL = 10006;
	public static final int MSG_UPLOAD_FILE_START = 10007;
	public static final int MSG_UPLOAD_FILE_END_FAIL = 10008;
	private ProgressDialog mProgressDialog;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);
		setContentView(R.layout.activity_submit_launch_result);
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		dlg = new CustomProgressDialog(this, "服务器处理中", R.anim.frame);
		Bundle extras = this.getIntent().getExtras();
		if (extras == null) {
			Log.e("zlulan", "No extras provided");
			return;
		}else{
			packageName = extras.getString("packageName");
			id = extras.getString("id");
			imagePath = extras.getString("imagePath");
		}
		handler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 1:
//					String value = (String) msg.obj;
//					isNull("报告添加结果：" + value);
					break;
				case MSG_UPLOAD_PIC_START:
					String imageHint = "开始上传第" + msg.arg1 + "张图片...";
					if (mProgressDialog.isShowing()) {
						mProgressDialog.setMessage(imageHint);
					} else {
						mProgressDialog = ProgressDialog.show(
								SubmitLaunchResultActivity.this, null,
								imageHint);
					}
					break;
				case MSG_UPLOAD_PIC_END_SUCCESS:
					String succesMessage = "第" + msg.arg1 + "张图片上传成功";
					mProgressDialog.setMessage(succesMessage);
					break;
				case MSG_UPLOAD_PIC_END_FAIL:
					String failMessage = "第" + msg.arg1 + "张图片上传失败";
					mProgressDialog.setMessage(failMessage);
					break;
				case MSG_UPLOAD_FILE_START:
					String fileStartMessage = "开始上传fps文件...";
					mProgressDialog.setMessage(fileStartMessage);
					break;
				case MSG_UPLOAD_END:
					// 上传fps文件
					dlg.cancel();
					mProgressDialog.dismiss();
					ToastUtil.ShowLongToast(SubmitLaunchResultActivity.this,
							"上传数据成功");
					break;
				case MSG_UPLOAD_FILE_END_FAIL:
					// 上传fps文件
					dlg.cancel();
					mProgressDialog.dismiss();
					ToastUtil.ShowLongToast(SubmitLaunchResultActivity.this,
							"上传数据失败");
					break;
				}
			}

		};
		etInstall = (EditText) findViewById(R.id.InstallResult);
		etInstall.setOnClickListener(new OnCk(0));
		etLaunch = (EditText) findViewById(R.id.launchResult);
		etLaunch.setOnClickListener(new OnCk(0));
		etRun = (EditText) findViewById(R.id.runResult);
		etRun.setOnClickListener(new OnCk(0));
		etUninstall = (EditText) findViewById(R.id.unInstallResult);
		etUninstall.setOnClickListener(new OnCk(0));
		tvStartDate = (TextView) findViewById(R.id.bdate);
		tvStartTime = (TextView) findViewById(R.id.btime);
		tvEndDate = (TextView) findViewById(R.id.edate);
		tvEndTime = (TextView) findViewById(R.id.etime);

		sb = (Button) findViewById(R.id.sb);
		tvStartDate.setOnClickListener(new OnCk(1));
		tvStartTime.setOnClickListener(new OnCk(2));
		tvEndDate.setOnClickListener(new OnCk(1));
		tvEndTime.setOnClickListener(new OnCk(2));
		sb.setOnClickListener(new OnCk(0));
		initData();
	}

	public void isNull(String s) {
		new AlertDialog.Builder(SubmitLaunchResultActivity.this).setTitle("提示")
				.setMessage(s).setPositiveButton("确定", null).show();
	}

	private void initData() {
		etInstall.setText("Success");
		etLaunch.setText("Success");
		etRun.setText("Success");
		etUninstall.setText("Success");

		String startDate = dateFormat.format(Constants.startTestTime);
		String startTime = timeFormat.format(Constants.startTestTime);
		String endDate = dateFormat.format(Constants.endTestTime);
		String endTime = timeFormat.format(Constants.endTestTime);

		tvStartDate.setText(startDate);
		tvStartTime.setText(startTime);
		tvEndDate.setText(endDate);
		tvEndTime.setText(endTime);
	}

	public void sendFileToServer() {
		// 上传文件
		if (imagePath != null) {
			mProgressDialog = ProgressDialog.show(SubmitLaunchResultActivity.this, null, "正在加载...");
			File imageDir = new File(imagePath);
			if(!imageDir.exists()){
				
				String logPath = Environment
						.getExternalStorageDirectory()
						.getAbsolutePath()
						+ File.separator
						+ "Android/data/"
						+ packageName + "/files/fps.log";
				final String logName = "fps.log";
				final File logFile = new File(logPath);
				if (logFile.exists()) {
					new Thread(){
						public void run() {
							sendFpsFile(logName,logFile);
						};
					}.start();
				}
				return;
			}
			final File[] images = imageDir.listFiles();
			if (images.length > 0) {
				new Thread() {
					public void run() {
						int imageSize = images.length;
						String action = "";

						for (int i = 0; i < imageSize; i++) {
							File imageFile = images[i];
							String imageName = imageFile
									.getAbsolutePath().substring(
											(imagePath + "/").length());
							if (imageName.contains("+")) {
								action = imageName.split("\\+")[0];
							}
							Message msg = new Message();
							msg.arg1 = i + 1;
							msg.what = MSG_UPLOAD_PIC_START;
							handler.sendMessage(msg);
							Map<String, String> params = new HashMap<String, String>();
//							params.put("act".trim(), "updateMonitor");
							params.put("act".trim(), "updateMonitorNew");
							params.put("mac", Contact.mac);
							params.put("rid", id);
							params.put("auto", "2");
							params.put("behaviorId", action);
							params.put("allBehavior", action);
							Date currentDate = new Date();
							SimpleDateFormat format = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							params.put("picTime",
									format.format(currentDate));
							params.put("picName", imageName);
							params.put("packagename", packageName);
							String url = Constants.DATA_URL
									+ "/platform/mobileTest/index.do";
							String result = HttpUtilForWired.getInstance()
									.sendFileToServer(url, "pic",
											imageFile, params.entrySet());
							try {
								JSONObject resultData = new JSONObject(
										result);
								String uploadResult = (String) resultData
										.get("result");
								if (uploadResult.equals("success")) {
									Message msg1 = new Message();
									msg1.arg1 = i + 1;
									msg1.what = MSG_UPLOAD_PIC_END_SUCCESS;
									handler.sendMessage(msg1);
								} else {
									Message msg2 = new Message();
									msg2.arg1 = i + 1;
									msg2.what = MSG_UPLOAD_PIC_END_FAIL;
									handler.sendMessage(msg2);
								}
							} catch (JSONException e) {
								Message msg3 = new Message();
								msg3.arg1 = i + 1;
								msg3.what = MSG_UPLOAD_PIC_END_FAIL;
								handler.sendMessage(msg3);
							}
						}
						String logPath = Environment
								.getExternalStorageDirectory()
								.getAbsolutePath()
								+ File.separator
								+ "Android/data/"
								+ packageName + "/files/fps.log";
						String logName = "fps.log";
						File logFile = new File(logPath);
						if (logFile.exists()) {
							Message msg = new Message();
							msg.what = MSG_UPLOAD_FILE_START;
							handler.sendMessage(msg);
							Map<String, String> params = new HashMap<String, String>();
//							params.put("act".trim(), "updateMonitor");
							params.put("act".trim(), "updateMonitorNew");
							params.put("mac", Contact.mac);
							params.put("rid", id);
							params.put("auto", "2");
							params.put("behaviorId", "");
							params.put("allBehavior", "");
							Date currentDate = new Date();
							SimpleDateFormat format = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							params.put("picTime",
									format.format(currentDate));
							params.put("picName", logName);
							params.put("packagename", packageName);
							String url = Constants.DATA_URL
									+ "/platform/mobileTest/index.do";
							String result = HttpUtilForWired.getInstance()
									.sendFileToServer(url, "fps", logFile,
											params.entrySet());
							try {
								JSONObject resultData = new JSONObject(
										result);
								String uploadResult = (String) resultData
										.get("result");
								if (uploadResult.equals("success")) {
									handler.sendEmptyMessage(MSG_UPLOAD_END);
								} else {
									handler.sendEmptyMessage(MSG_UPLOAD_FILE_END_FAIL);
								}
							} catch (JSONException e) {
								handler.sendEmptyMessage(MSG_UPLOAD_FILE_END_FAIL);
							}
						} else {
							handler.sendEmptyMessage(MSG_UPLOAD_END);
						}
					};
				}.start();
			}
		}
	}
	
	private void sendFpsFile(String logName,File logFile){
			Message msg = new Message();
			msg.what = MSG_UPLOAD_FILE_START;
			handler.sendMessage(msg);
			Map<String, String> params = new HashMap<String, String>();
			params.put("act".trim(), "updateMonitor");
//			params.put("act".trim(), "updateMonitorNew");
			params.put("mac", Contact.mac);
			params.put("rid", id);
			params.put("behaviorId", "");
			params.put("allBehavior", "");
			Date currentDate = new Date();
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			params.put("picTime",
					format.format(currentDate));
			params.put("picName", logName);
			params.put("packagename", packageName);
			String url = Constants.DATA_URL
					+ "/platform/mobileTest/index.do";
			String result = HttpUtilForWired.getInstance()
					.sendFileToServer(url, "fps", logFile,
							params.entrySet());
			try {
				JSONObject resultData = new JSONObject(
						result);
				String uploadResult = (String) resultData
						.get("result");
				if (uploadResult.equals("success")) {
					handler.sendEmptyMessage(MSG_UPLOAD_END);
				} else {
					handler.sendEmptyMessage(MSG_UPLOAD_FILE_END_FAIL);
				}
			} catch (JSONException e) {
				handler.sendEmptyMessage(MSG_UPLOAD_FILE_END_FAIL);
			}
	}
	
	class OnCk implements OnClickListener {
		int select = 1;

		public OnCk(int select) {
			this.select = select;
		}

		public void onClick(View view) {
			if (view == tvStartDate) {
				setText(tvStartDate, select);
			} else if (view == tvStartTime) {
				setText(tvStartTime, select);
			} else if (view == tvEndDate) {
				setText(tvEndDate, select);
			} else if (view == tvEndTime) {
				setText(tvEndTime, select);
			} else if (view == sb) {
				dlg.show();
				if (TextUtils.isEmpty(Constants.DATA_URL)) {
					ToastUtil.ShowLongToast(SubmitLaunchResultActivity.this,
							Constants.GET_ADDRESS_WARNING);
					return;
				}
				String url = Constants.DATA_URL
						+ "/platform/mobileCompatibility/mobileCompatibilityReportMaint.do";
				NameValuePair act;
				NameValuePair mac;
				NameValuePair rid;
				NameValuePair testPackageName;
				NameValuePair installTime;
				NameValuePair installCpu;
				NameValuePair installDesc;
				NameValuePair installFailType;
				NameValuePair uninstallDesc;
				NameValuePair uninstallFailType;
				NameValuePair launcherTime;
				NameValuePair launcherDesc;
				NameValuePair launcherFailType;
				NameValuePair runCpuAvg;
				NameValuePair runCpuMax;
				NameValuePair runMemAvg;
				NameValuePair runMemMax;
				NameValuePair runDesc;
				NameValuePair runFailType;
				NameValuePair testBeginDay;
				NameValuePair testEndDay;
				List<NameValuePair> nameValuePairs;
				nameValuePairs = new ArrayList<NameValuePair>();
				act = new BasicNameValuePair("act", "addItemResult");
				mac = new BasicNameValuePair("mac", Contact.mac);
				rid = new BasicNameValuePair("rid", id);
				testPackageName = new BasicNameValuePair("testPackageName",
						packageName);
				installTime = new BasicNameValuePair("installTime", "0");
				installCpu = new BasicNameValuePair("installCpu", "0");
				installDesc = new BasicNameValuePair("installDesc", etInstall
						.getText().toString());
				installFailType = new BasicNameValuePair("installFailType", Iet);
				launcherTime = new BasicNameValuePair("lanucherTime", "0");
				launcherDesc = new BasicNameValuePair("lanucherDesc", etLaunch
						.getText().toString());
				launcherFailType = new BasicNameValuePair("lanucherFailType",
						Let);
				runCpuAvg = new BasicNameValuePair("runCpuAvg", "0");
				runCpuMax = new BasicNameValuePair("runCpuMax", "0");
				runMemAvg = new BasicNameValuePair("runMemAvg", "0");
				runMemMax = new BasicNameValuePair("runMemMax", "0");
				runDesc = new BasicNameValuePair("runDesc", etRun.getText()
						.toString());
				runFailType = new BasicNameValuePair("runFailType", Ret);
				uninstallDesc = new BasicNameValuePair("uninstallDesc",
						etUninstall.getText().toString());
				uninstallFailType = new BasicNameValuePair("uninstallFailType",
						Uet);
				testBeginDay = new BasicNameValuePair("testBeginDay",
						tvStartDate.getText().toString() + " "
								+ tvStartTime.getText().toString());
				testEndDay = new BasicNameValuePair("testEndDay", tvEndDate
						.getText().toString()
						+ " "
						+ tvEndTime.getText().toString());
				nameValuePairs.add(act);
				nameValuePairs.add(mac);
				nameValuePairs.add(rid);
				nameValuePairs.add(testPackageName);
				nameValuePairs.add(installTime);
				nameValuePairs.add(installCpu);
				nameValuePairs.add(installDesc);
				nameValuePairs.add(installFailType);
				nameValuePairs.add(uninstallDesc);
				nameValuePairs.add(uninstallFailType);
				nameValuePairs.add(launcherTime);
				nameValuePairs.add(launcherDesc);
				nameValuePairs.add(launcherFailType);
				nameValuePairs.add(runCpuAvg);
				nameValuePairs.add(runCpuMax);
				nameValuePairs.add(runMemAvg);
				nameValuePairs.add(runMemMax);
				nameValuePairs.add(runDesc);
				nameValuePairs.add(runFailType);
				nameValuePairs.add(testBeginDay);
				nameValuePairs.add(testEndDay);

				new SendHttp(url, nameValuePairs, null, handler).start();
				
				sendFileToServer();

			} else if (view == etInstall) {
				imm.hideSoftInputFromWindow(etInstall.getWindowToken(), 0);
				startActivityForResult(new Intent(
						SubmitLaunchResultActivity.this, DialogActivity.class),
						1);
			} else if (view == etLaunch) {
				imm.hideSoftInputFromWindow(etLaunch.getWindowToken(), 0);
				startActivityForResult(new Intent(
						SubmitLaunchResultActivity.this, DialogActivity.class),
						2);
			} else if (view == etRun) {
				imm.hideSoftInputFromWindow(etRun.getWindowToken(), 0);
				startActivityForResult(new Intent(
						SubmitLaunchResultActivity.this, DialogActivity.class),
						3);
			} else if (view == etUninstall) {
				imm.hideSoftInputFromWindow(etUninstall.getWindowToken(), 0);
				startActivityForResult(new Intent(
						SubmitLaunchResultActivity.this, DialogActivity.class),
						4);
			}
		}

		public void setText(final TextView txt, int select) {
			calendar = Calendar.getInstance();
			if (select == 1) {
				dialog = new DatePickerDialog(SubmitLaunchResultActivity.this,
						new DatePickerDialog.OnDateSetListener() {

							public void onDateSet(DatePicker arg0, int arg1,
									int arg2, int arg3) {
								txt.setText(arg1 + "-" + (arg2 + 1) + "-"
										+ arg3);

							}
						}, calendar.get(Calendar.YEAR),
						calendar.get(Calendar.MONTH),
						calendar.get(Calendar.DAY_OF_MONTH));
				dialog.show();
			} else if (select == 2) {
				timePickerDialog = new TimePickerDialog(
						SubmitLaunchResultActivity.this,
						new TimePickerDialog.OnTimeSetListener() {
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {
								txt.setText(" " + hourOfDay + ":" + minute
										+ ":00");
							}
						}, calendar.get(Calendar.HOUR_OF_DAY),
						calendar.get(Calendar.MINUTE), true);

				timePickerDialog.show();

			}
		}
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bundle bundle = null;
		String fp = null;
		if (data != null && (bundle = data.getExtras()) != null) {
			fp = bundle.getString("result");
		}
		if (fp != null) {
			switch (requestCode) {
			case 1:
				String name = fp.substring(0, fp.indexOf(","));
				etInstall.setText(name);
				Iet = fp.substring(fp.indexOf(",") + 1);
				break;
			case 2:
				etLaunch.setText(fp.substring(0, fp.indexOf(",")));
				Let = fp.substring(fp.indexOf(",") + 1);
				break;
			case 3:
				etRun.setText(fp.substring(0, fp.indexOf(",")));
				Ret = fp.substring(fp.indexOf(",") + 1);
				break;
			case 4:
				etUninstall.setText(fp.substring(0, fp.indexOf(",")));
				Uet = fp.substring(fp.indexOf(",") + 1);
				break;
			}
		}
	}

}
