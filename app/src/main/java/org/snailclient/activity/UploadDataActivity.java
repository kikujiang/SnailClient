
package org.snailclient.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.monitor.util.Contact;
import com.monitor.util.ProcessInfo;
import com.monitor.util.Programe;
import com.snail.util.Constants;

import org.androidpn.demoapp.R;
import org.json.JSONException;
import org.json.JSONObject;
import org.snailclient.activity.utils.fps.ToastUtil;
import org.snailclient.activity.utils.upload.MyAdapter;
import org.snailclient.activity.utils.upload.bean.ImageFloder;
import org.snailclient.activity.utils.upload.bean.ReportBean;
import org.snailclient.activity.utils.upload.bean.ResponseReportBean;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solo.HttpUtilForWired;

public class UploadDataActivity extends AppCompatActivity implements OnClickListener {

	public static final String TAG = "UploadDataActivity";
	
	private static String TARGET_PIC_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ "snail_screen" + File.separator;

	private String picPath = "";

	public static final int MSG_SHOW_PIC = 10001;
	public static final int MSG_UPLOAD_END = 10002;
	public static final int MSG_UPLOAD_PIC = 10003;
	public static final int MSG_UPLOAD_PIC_START = 10004;
	public static final int MSG_UPLOAD_PIC_END_SUCCESS = 10005;
	public static final int MSG_UPLOAD_PIC_END_FAIL = 10006;
	public static final int MSG_UPLOAD_FILE_START = 10007;
	public static final int MSG_UPLOAD_FILE_END_FAIL = 10008;
	public static final int MSG_GET_PROJECT_SUCCESS = 10009;
	public static final int MSG_GET_PROJECT_FAIL = 10010;

	private Spinner spinnerReportType;
	private Spinner spinnerReportName;
	private Spinner spinnerAppName;
	private List<String> reportTpyeList = new ArrayList<String>();
	private List<ReportBean> reportNameList = new ArrayList<ReportBean>();
	private List<Programe> appNameList = new ArrayList<Programe>();
	private ProgressDialog mProgressDialog;
	private Button submitBtn;
	private ReportBean currentSelectedReport = null;
	private Programe currentSelectedApp = null;
	
	private String currentPackageName = "";
	private String currentReportType = "compatible";

	private View viewNoPic;
	
	/**
	 * 存储文件夹中的图片数量
	 */
	private int mPicsSize;
	/**
	 * 图片数量最多的文件夹
	 */
	private File mImgDir;
	/**
	 * 所有的图片
	 */
	private List<String> mImgs = null;

	private GridView mGirdView;
	private MyAdapter mAdapter = null;
	private DataAdapter<ReportBean> mReportAdapter = null;
	/**
	 * 临时的辅助类，用于防止同一个文件夹的多次扫描
	 */
	// private HashSet<String> mDirPaths = new HashSet<String>();
	private String uploadUrl = "";

	/**
	 * 扫描拿到所有的图片文件夹
	 */
	private List<ImageFloder> mImageFloders = null;

	private Gson gson;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_SHOW_PIC:
				mProgressDialog.dismiss();
				// 为View绑定数据
				data2View();
				break;

			case MSG_UPLOAD_PIC_START:
				String imageHint = "开始上传第" + msg.arg1 + "张图片...";
				if (mProgressDialog.isShowing()) {
					mProgressDialog.setMessage(imageHint);
				} else {
					mProgressDialog = ProgressDialog.show(
							UploadDataActivity.this, null, imageHint);
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
				mProgressDialog.dismiss();
				ToastUtil.ShowLongToast(UploadDataActivity.this, "上传数据成功");
				break;
			case MSG_UPLOAD_FILE_END_FAIL:
				// 上传fps文件
				mProgressDialog.dismiss();
				ToastUtil.ShowLongToast(UploadDataActivity.this, "上传数据失败");
				break;
			case MSG_GET_PROJECT_SUCCESS:
				if(mProgressDialog != null && mProgressDialog.isShowing()){
					mProgressDialog.dismiss();
				}
				currentSelectedReport = reportNameList.get(0);
				if (mReportAdapter == null) {
					// 初始化测试报告名称选择控件
					spinnerReportName = (Spinner) findViewById(R.id.spinner_report_name);
					spinnerReportName.setDropDownVerticalOffset(60);
					// 建立Adapter并且绑定数据源
					mReportAdapter = new DataAdapter<ReportBean>(
							UploadDataActivity.this, reportNameList);
					spinnerReportName.setAdapter(mReportAdapter);
					spinnerReportName
							.setOnItemSelectedListener(new OnItemSelectedListener() {
								@Override
								public void onItemSelected(
										AdapterView<?> parent, View view,
										int pos, long id) {
									currentSelectedReport = reportNameList
											.get(pos);
								}

								@Override
								public void onNothingSelected(
										AdapterView<?> parent) {
								}
							});
				} else {
					mReportAdapter.notifyDataSetChanged();
				}
				break;
			case MSG_GET_PROJECT_FAIL:
				currentSelectedReport = null;
				ToastUtil.ShowLongToast(UploadDataActivity.this,
						"查询报告失败,请确认连接是否正常");
				break;
			}
		}
	};

	@SuppressLint("NewApi") 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_upload_data);
		// 兼容性报告地址
		// String url = Constants.DATA_URL +
		// "/platform/mobileCompatibility/mobileCompatibilityReportList.do";
		// prepareData(url);
		init();
	}

	/**
	 * 对界面进行初始化操作
	 */
	private void init() {
		gson = new Gson();
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		reportTpyeList.add("兼容性测试");
		reportTpyeList.add("性能测试");
		reportTpyeList.add("稳定性测试");
		viewNoPic = findViewById(R.id.view_no_picture);
		// 初始化测试类型选择控件
		spinnerReportType = (Spinner) findViewById(R.id.spinner_report_type);
		spinnerReportType.setDropDownVerticalOffset(60);
		// 建立Adapter并且绑定数据源
		DataAdapter<String> adapter = new DataAdapter<String>(this,
				reportTpyeList);
		spinnerReportType.setAdapter(adapter);
		spinnerReportType
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						switch (pos) {
						case 0:
							uploadUrl = Constants.DATA_URL
									+ "/platform/mobileCompatibility/mobileCompatibilityReportList.do";
							prepareData(uploadUrl);
							currentReportType ="compatible";
							setImages(currentReportType, currentPackageName);
							break;
						case 1:
							uploadUrl = Constants.DATA_URL
									+ "/platform/mobileTest/mobileTestCaList.do";
							prepareData(uploadUrl);
							currentReportType ="performance";
							setImages(currentReportType, currentPackageName);
							break;
						case 2:
							uploadUrl = Constants.DATA_URL
									+ "/platform/mobileTest/mobileTestCsList.do";
							prepareData(uploadUrl);
							break;
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
		spinnerAppName = (Spinner) findViewById(R.id.spinner_app_name);
		spinnerAppName.setDropDownVerticalOffset(60);
		// 建立Adapter并且绑定数据源
		ProcessInfo info = new ProcessInfo();
		appNameList = info.getRunningProcess(this);
		currentSelectedApp = appNameList.get(0);
		currentPackageName = currentSelectedApp.getPackageName();
		if (appNameList.size() > 0) {
			DataAdapter<Programe> adapter2 = new DataAdapter<Programe>(this,
					appNameList);
			spinnerAppName.setAdapter(adapter2);
			spinnerAppName
					.setOnItemSelectedListener(new OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> parent,
								View view, int pos, long id) {
							currentSelectedApp = appNameList.get(pos);
							currentPackageName = currentSelectedApp.getPackageName();
							Log.d("TAG", "APP NAME CALLED!");
							setImages(currentReportType, currentPackageName);
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) {
						}
					});
		} else {
			ToastUtil.ShowLongToast(this, "未检测到的应用，请退出重试!");
		}

		mGirdView = (GridView) findViewById(R.id.gridView_show_pic);
		submitBtn = (Button) findViewById(R.id.button_submit_fps_file);
		submitBtn.setOnClickListener(this);
	}

	private void setImages( String type,String packageName){
		mPicsSize = 0;
		if(mImageFloders == null){
			mImageFloders = new ArrayList<ImageFloder>();
		}
		if(mImgs == null){
			mImgs = new ArrayList<String>();
		}
		Date currentDate = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateName = format.format(currentDate);
		picPath = TARGET_PIC_PATH + currentDateName + File.separator+type+File.separator+packageName+File.separator;
		Log.e("TAG", picPath);
		getImages();
	}
	
	/**
	 * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中 完成图片的扫描，最终获得jpg最多的那个文件夹
	 */
	private void getImages() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
			return;
		}
		// 显示进度条
		if(mProgressDialog == null){
			mProgressDialog = ProgressDialog.show(this, null, "正在加载...");
		}else{
			if(!mProgressDialog.isShowing()){
				mProgressDialog = ProgressDialog.show(this, null, "正在加载...");
			}
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				
				// 获取该图片的父路径名
				File parentFile = new File(picPath);
				if (parentFile.exists() && parentFile != null) {
					String dirPath = parentFile.getAbsolutePath();
					ImageFloder imageFloder = null;
					// 初始化imageFloder
					imageFloder = new ImageFloder();
					imageFloder.setDir(dirPath);

					int picSize = parentFile.list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String filename) {
							if (filename.endsWith(".jpg")
									|| filename.endsWith(".png")
									|| filename.endsWith(".jpeg"))
								return true;
							return false;
						}
					}).length;

					imageFloder.setCount(picSize);
					mImageFloders.add(imageFloder);

					if (picSize > mPicsSize) {
						mPicsSize = picSize;
						mImgDir = parentFile;
					}
				}else{
					mImageFloders = null;
					mImgDir = null;
					mPicsSize = 0;
				}

				// 通知Handler扫描图片完成
				mHandler.sendEmptyMessage(MSG_SHOW_PIC);
			}
		}).start();

	}

	/**
	 * 为View绑定数据
	 */
	private void data2View() {
		if (mImgDir == null) {
			mGirdView.setVisibility(View.GONE);
			viewNoPic.setVisibility(View.VISIBLE);
			Toast.makeText(getApplicationContext(), "一张图片没扫描到",
					Toast.LENGTH_SHORT).show();
			if(mAdapter != null){
				mAdapter = null;
			}
			return;
		}else{
			mGirdView.setVisibility(View.VISIBLE);
			viewNoPic.setVisibility(View.GONE);
		}

		mImgs = Arrays.asList(mImgDir.list());
		/**
		 * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
		 */
		mAdapter = new MyAdapter(getApplicationContext(), mImgs,
				R.layout.grid_item, mImgDir.getAbsolutePath());
		mGirdView.setAdapter(mAdapter);
	};

	
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
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_submit_fps_file:
			if (currentSelectedApp == null || currentSelectedReport == null) {
				ToastUtil.ShowLongToast(UploadDataActivity.this, "当前数据不全");
			} else {
				final int imageSize = MyAdapter.mSelectedImage.size();

				if (imageSize > 0) {
					new Thread() {
						public void run() {
							String packageName = currentSelectedApp
									.getPackageName();
							String action = "";
							String rid = currentSelectedReport.getRid();
							for (int i = 0; i < imageSize; i++) {
								String imagePath = MyAdapter.mSelectedImage
										.get(i);
								String imageName = imagePath
										.substring((picPath + "/").length());
								if (imageName.contains("+")) {
									action = imageName.split("\\+")[0];
								}
								Message msg = new Message();
								msg.arg1 = i + 1;
								msg.what = MSG_UPLOAD_PIC_START;
								mHandler.sendMessage(msg);
								Map<String, String> params = new HashMap<String, String>();
								params.put("act".trim(), "updateMonitorNew");
//								params.put("act".trim(), "updateMonitorNew");
								params.put("mac", Contact.mac);
								params.put("rid", rid);
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
								File imageFile = new File(imagePath);
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
										mHandler.sendMessage(msg1);
									} else {
										Message msg2 = new Message();
										msg2.arg1 = i + 1;
										msg2.what = MSG_UPLOAD_PIC_END_FAIL;
										mHandler.sendMessage(msg2);
									}
								} catch (JSONException e) {
									Message msg3 = new Message();
									msg3.arg1 = i + 1;
									msg3.what = MSG_UPLOAD_PIC_END_FAIL;
									mHandler.sendMessage(msg3);
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
								mHandler.sendMessage(msg);
								Map<String, String> params = new HashMap<String, String>();
								params.put("act".trim(), "updateMonitorNew");
//								params.put("act".trim(), "updateMonitorNew");
								params.put("mac", Contact.mac);
								params.put("rid", rid);
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
										mHandler.sendEmptyMessage(MSG_UPLOAD_END);
									} else {
										mHandler.sendEmptyMessage(MSG_UPLOAD_FILE_END_FAIL);
									}
								} catch (JSONException e) {
									mHandler.sendEmptyMessage(MSG_UPLOAD_FILE_END_FAIL);
								}
							} else {
								mHandler.sendEmptyMessage(MSG_UPLOAD_END);
							}
						};
					}.start();

				} else {
					new Thread() {
						public void run() {
							String packageName = currentSelectedApp
									.getPackageName();
							String action = "";
							String rid = currentSelectedReport.getRid();
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
								mHandler.sendMessage(msg);
								Map<String, String> params = new HashMap<String, String>();
//								params.put("act".trim(), "updateMonitor");
								params.put("act".trim(), "updateMonitorNew");
								params.put("mac", Contact.mac);
								params.put("rid", rid);
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
										mHandler.sendEmptyMessage(MSG_UPLOAD_END);
									} else {
										mHandler.sendEmptyMessage(MSG_UPLOAD_FILE_END_FAIL);
									}
								} catch (JSONException e) {
									mHandler.sendEmptyMessage(MSG_UPLOAD_FILE_END_FAIL);
								}
							} else {
								mHandler.sendEmptyMessage(MSG_UPLOAD_END);
							}
						}
					}.start();
				}
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 获取数据
	 * 
	 * @param url
	 *            需要获取的
	 */
	private void prepareData(final String url) {
		new Thread() {
			public void run() {
				reportNameList.clear();
				List<Map<String, String>> actionData = new ArrayList<Map<String, String>>();
				Map<String, String> type = new HashMap<String, String>();
				type.put("key", "act");
				type.put("value", "getTestList");
				actionData.add(type);
				String result = HttpUtilForWired.getInstance().sendData(url,
						actionData);
				if (!"".equals(result)) {
					try {
						ResponseReportBean responseBean = gson.fromJson(result,
								ResponseReportBean.class);
						if (responseBean.getResult().equals("success")) {
							reportNameList.addAll(responseBean.getList());
							mHandler.sendEmptyMessage(MSG_GET_PROJECT_SUCCESS);
						} else {
							// ReportBean errorBean = new ReportBean();
							// errorBean.setName("获取报告失败");
							// reportNameList.add(errorBean);
							mHandler.sendEmptyMessage(MSG_GET_PROJECT_FAIL);
						}
					} catch (Exception e) {
						Log.d("TAG", e.getMessage());
						mHandler.sendEmptyMessage(MSG_GET_PROJECT_FAIL);
						// ReportBean errorBean = new ReportBean();
						// errorBean.setName("获取报告失败");
						// reportNameList.add(errorBean);
					}
				} else {
					mHandler.sendEmptyMessage(MSG_GET_PROJECT_FAIL);
					// ReportBean errorBean = new ReportBean();
					// errorBean.setName("获取报告失败");
					// reportNameList.add(errorBean);
				}
			}
		}.start();
	}

	/**
	 * 选择框的自定义adapter
	 * 
	 * @author wubo1
	 * 
	 * @param <T>
	 */
	class DataAdapter<T> extends BaseAdapter {

		private List<T> dataList = null;
		private Context dataContext = null;

		public DataAdapter(Context mContext, List<T> data) {
			this.dataContext = mContext;
			this.dataList = data;
		}

		@Override
		public int getCount() {
			return dataList.size();
		}

		@Override
		public Object getItem(int pos) {
			return dataList.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return pos;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(dataContext).inflate(
						R.layout.item_spinner, null);
				holder.tvDataName = (TextView) convertView
						.findViewById(R.id.content);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tvDataName.setBackgroundColor(Color.parseColor("#ffffff"));
			holder.tvDataName.setTextColor(Color.parseColor("#000000"));
			if (getItem(pos) instanceof ReportBean) {
				holder.tvDataName
						.setText(((ReportBean) getItem(pos)).getName());
			} else if (getItem(pos) instanceof Programe) {
				holder.tvDataName.setText(((Programe) getItem(pos))
						.getProcessName());
			} else {
				holder.tvDataName.setText(getItem(pos).toString());
			}
			holder.tvDataName.setTextSize(16);
			return convertView;
		}
	}

	class ViewHolder {
		public TextView tvDataName;
	}
}
