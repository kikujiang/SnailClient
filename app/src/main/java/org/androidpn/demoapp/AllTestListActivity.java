package org.androidpn.demoapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.monitor.util.CustomProgressDialog;
import com.snail.util.Constants;

import org.snailclient.activity.utils.fps.GenericToast;
import org.snailclient.activity.utils.upload.bean.ResponseReportBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solo.HttpUtilForWired;

/**
 * 
 * 类的描述：测试报告列表界面
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class AllTestListActivity extends AppCompatActivity {
	ListView list;
	String str;
	Handler myhandler;
	ListAdapter adapter;
	CustomProgressDialog dialog;
	Map<String, TextView> hp;
	private static final String LOG_TAG = "AllTestListActivity";
	private Gson gson;
	private String getReportUrl = null;
	private int REQUEST_MEDIA_PROJECTION = 1;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_test_list);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		hp = new HashMap<String, TextView>();
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		str = bundle.getString("list");
		list = (ListView) findViewById(R.id.allTestList);
		myhandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					dialog.cancel();
					String value = (String) msg.obj;
					if ("".equals(value)) {
						Toast.makeText(AllTestListActivity.this, "服务器返回空字符串",
								Toast.LENGTH_SHORT).show();
						return;
					}
                    if(adapter == null){
                    	adapter = new ListAdapter(value, AllTestListActivity.this,
                    			hp, myhandler);
                    	list.setAdapter(adapter);
                    }else{
                    	adapter.notifyDataSetChanged();
                    }
					break;
				case 2:
					String rlt = msg.obj.toString();
					String id = rlt.substring(0, rlt.indexOf(","));
					String pecent = rlt.substring(rlt.indexOf(",") + 1);
					TextView tv = hp.get(id);
					if (pecent.equals("100")) {
						tv.setText("安装");
					} else {
						tv.setText(pecent);
					}
				}
				super.handleMessage(msg);
			}
		};
		getReportUrl = Constants.DATA_URL
				+ "/platform/mobileCompatibility/mobileCompatibilityReportList.do";
		gson = new Gson();
		prepareData(getReportUrl);
		
		if (Constants.versionCode > 20) {
			Constants.takeshotMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
			startActivityForResult(
					Constants.takeshotMediaProjectionManager.createScreenCaptureIntent(),
					REQUEST_MEDIA_PROJECTION);
		}
	}

//	public void control(String list) {
//		String url = null;
//		if (TextUtils.isEmpty(Constants.MAIN_SERVER_URL)) {
//			ToastUtil.ShowLongToast(this, Constants.GET_ADDRESS_WARNING);
//			return;
//		}
//		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//		NameValuePair nameValuePairAct = null;
//		if ("allTest".trim().equals(list.trim())) {
//			nameValuePairAct = new BasicNameValuePair("act", "getVoteList");
//			url = Constants.MAIN_SERVER_URL
//					+ "/platform/employee/employeeVoteList.do";
//			Contact.testType = "4";
//		} else if ("LaunchTest".trim().equals(list.trim())) {
//			nameValuePairAct = new BasicNameValuePair("act", "getTestList");
//			url = Constants.MAIN_SERVER_URL
//					+ "/platform/mobileCompatibility/mobileCompatibilityReportList.do";
//			Contact.testType = "2";
//		}
//		nameValuePairs.add(nameValuePairAct);
//		new SendHttp(url, nameValuePairs, null, myhandler).start();
//	}

	@Override
	public void onResume() {
		super.onResume();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.alltest, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.fresh:
			prepareData(getReportUrl);
			return true;
		case android.R.id.home:
			this.finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * 获取数据
	 * 
	 * @param url
	 *            需要获取的
	 */
	private void prepareData(final String url) {
		dialog = new CustomProgressDialog(this, "服务器处理中", R.anim.frame);
		dialog.show();
		new Thread() {
			public void run() {
				List<Map<String, String>> actionData = new ArrayList<Map<String, String>>();
				Map<String, String> type = new HashMap<String, String>();
				type.put("key", "act");
				type.put("value", "getTestList");
				actionData.add(type);
				String result = HttpUtilForWired.getInstance().sendData(url,
						actionData);
				Log.d(LOG_TAG, "result is:" + result);
				if (!"".equals(result)) {
					try {
						ResponseReportBean responseBean = gson.fromJson(result,
								ResponseReportBean.class);
						if (responseBean.getResult().equals("success")) {
							Message msg = myhandler.obtainMessage();
							msg.what = 1;
							msg.obj = result;
							myhandler.sendMessage(msg);
						} else {
							myhandler.sendEmptyMessage(4);
						}
					} catch (Exception e) {
						Log.d("TAG", e.getMessage());
						myhandler.sendEmptyMessage(5);
					}
				} else {
					myhandler.sendEmptyMessage(6);
				}
 			};
		}.start();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
				GenericToast.makeText(AllTestListActivity.this, "启动截屏服务成功", 500).show();
			}
		}
	}
}
