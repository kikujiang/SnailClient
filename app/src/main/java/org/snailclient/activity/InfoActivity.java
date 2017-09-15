package org.snailclient.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.androidpn.demoapp.R;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.snailclient.activity.utils.DeviceInfo;
import org.snailclient.activity.utils.fps.ToastUtil;

import solo.FileUtil;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.monitor.util.Contact;
import com.monitor.util.SendHttp;
import com.snail.util.Constants;

/**
 * 
 * 类的描述：显示手机信息界面
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
@SuppressLint("SimpleDateFormat") public class InfoActivity extends AppCompatActivity {

	public static final String TAG = "InfoActivity";

	//设备型号textview
	private TextView tvDeviceModel;
	//设备品牌textview
	private TextView tvDeviceBrand;
	//设备系统版本textview
	private TextView tvDeviceOSName;
	//设备自带系统textview
	private TextView tvDeviceCustomOSName;
	//设备cpu架构textview
	private TextView tvDeviceCpuStructure;
	//设备cpu类型textview
	private TextView tvDeviceCpuType;
	//设备cpu逻辑核数textview
	private TextView tvDeviceCpuCoreLogic;
	//设备cpu物理核数textview
	private TextView tvDeviceCpuCorepPhysics;
	//设备cpu频率textview
	private TextView tvDeviceCpuFrequency;
	//设备gpu型号textview
	private TextView tvDeviceGpuType;
	//设备运行内存textview
	private TextView tvDeviceRamMemory;
	//设备sd内存textview
	private TextView tvDeviceDiskMemory;
	//设备总内存textview
	private TextView tvDeviceTotalMemory;
	//设备屏幕尺寸textview
	private TextView tvDeviceScreenSize;
	//设备屏幕高度textview
	private TextView tvDeviceScreenHeight;
	//设备屏幕宽度textview
	private TextView tvDeviceScreenWidth;
	//设备的uuid的textview
	private TextView tvDeviceUUID;
	//设备mac地址的textview
	private TextView tvDeviceMacAddress;
	//设备手机号码的textview
	private TextView tvDevicePhone;
	//设备ip地址的textview
	private TextView tvDeviceIP;
	
	private Button btn_submit;
	private Button bt;
	
	private DeviceInfo deviceInfo;//设备信息对象
	private String deviceModel;//设备型号
	private String deviceOSName;//设备系统名称
	private String deviceCustomOSName;//设备自带系统名称
	private String deviceCpuStructure = "";//设备cpu架构
	private String deviceCpuFrequeccy = "";//设备cpu频率
	private String deviceRamMemory;//设备内存大小
	private String deviceDiskAvailableMemory;//设备可用内存大小
	private String deviceDiskMemory;//设备sd卡大小
	private String deviceTotalMemory;//设备内存总大小
	private String deviceCpuType;//设备cpu型号
	private String deviceCpuCoreNum;//设备cpu内核数
	private String deviceGpuType;//设备gpu型号
	private String deviceScreenInches; //屏幕物理尺寸
	private int deviceWidth;//设备宽度
	private int deviceHeight;//设备高度
	private String deviceUUID;//设备uuid
	private String deviceMacAddress;//设备mac地址
	private String devicePhone;//设备手机号码
	private String deviceIP;//设备IP地址
	private String deviceBrand;//设备品牌
	
	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			String result = (String) msg.obj;
			Toast.makeText(InfoActivity.this, "返回结果：" + result,
					Toast.LENGTH_LONG).show();
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = LayoutInflater.from(this).inflate(R.layout.activity_info, null);
		
		setContentView(view);
		getDeviceInfo();
		initActionBar();
		initView();
		insertData();
	}

	/**
	 * 初始化actionbar
	 */
	private void initActionBar(){
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
//		actionBar.setDisplayUseLogoEnabled(true);
	}
	
	/**
	 * 获取设备相关信息
	 */
	@SuppressLint("NewApi") 
	private void getDeviceInfo(){
		Bundle gpuInfo = getIntent().getExtras();
		deviceGpuType = (String) gpuInfo.get("gpu_name");
		deviceInfo = new DeviceInfo(InfoActivity.this);
		
		deviceModel = Build.MODEL;
		deviceOSName = Build.VERSION.RELEASE;
		deviceCustomOSName = deviceInfo.getCustomOsVersion();
		
		setCpuAbis();
		
		deviceCpuFrequeccy = deviceInfo.getMaxCpuFreq();
		deviceCpuType = deviceInfo.getCpuType();
		deviceCpuCoreNum = String.valueOf(deviceInfo.getNumCores());
		
		deviceRamMemory = deviceInfo.getTotalRAM();
		deviceDiskMemory = "0";
		deviceTotalMemory = deviceInfo.getTotalExternalMemorySize();
		deviceDiskAvailableMemory = deviceInfo.getAvailableMemorySize();
		
		deviceScreenInches = deviceInfo.getScreenSize();
		deviceWidth = deviceInfo.getScreenWidthNew();
		deviceHeight = deviceInfo.getScreenHeightNew();
		deviceUUID = Build.SERIAL;
		if(null == Contact.mac || "".equals(Contact.mac)){
			deviceMacAddress = deviceInfo.getMacAddress();
		}else{
			deviceMacAddress = Contact.mac;
		}
		
//		devicePhone = deviceInfo.readSIMCard();
		deviceIP = deviceInfo.getLocalIpAddress();
		deviceBrand = deviceInfo.getBuildBrand();
	}
	
	/**
	 * 初始化控件
	 */
	private void initView() {
		tvDeviceModel = (TextView) findViewById(R.id.device_model);
		tvDeviceBrand = (TextView) findViewById(R.id.device_brand);
		tvDeviceOSName = (TextView) findViewById(R.id.device_os_version);
		tvDeviceCustomOSName = (TextView) findViewById(R.id.device_custom_os_version);
		tvDeviceCpuType = (TextView) findViewById(R.id.device_cpu_type);
		tvDeviceCpuStructure = (TextView) findViewById(R.id.device_cpu_structure);
		tvDeviceCpuCoreLogic = (TextView) findViewById(R.id.device_cpu_logic_core);
		tvDeviceCpuCorepPhysics = (TextView) findViewById(R.id.device_cpu_physics_core);
		tvDeviceCpuFrequency = (TextView) findViewById(R.id.device_cpu_frequency);
		tvDeviceGpuType = (TextView) findViewById(R.id.device_gpu_type);
		tvDeviceRamMemory = (TextView) findViewById(R.id.device_ram_memory);
		tvDeviceTotalMemory = (TextView) findViewById(R.id.device_total_memory);
		tvDeviceDiskMemory = (TextView) findViewById(R.id.device_disk_memory);
		tvDeviceScreenSize = (TextView) findViewById(R.id.device_screen_size);
		tvDeviceScreenHeight = (TextView) findViewById(R.id.device_screen_height);
		tvDeviceScreenWidth = (TextView) findViewById(R.id.device_screen_width);
		tvDeviceUUID = (TextView) findViewById(R.id.device_uuid);
		tvDeviceMacAddress = (TextView) findViewById(R.id.device_mac);
		tvDevicePhone = (TextView) findViewById(R.id.device_phone);
		tvDeviceIP = (TextView) findViewById(R.id.device_ip);
		
		btn_submit = (Button) findViewById(R.id.btn_submit);
		btn_submit.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				sendUpdateServer();
			}
		});
		bt = (Button) findViewById(R.id.beginTest);
		bt.setOnClickListener(new OnClickListener() {

			@TargetApi(21) public void onClick(View arg0) {
				Constants.takeshotMediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
				startActivityForResult(Constants.takeshotMediaProjectionManager.createScreenCaptureIntent(), 1);
			}
		});
	}

	/**
	 * 为控件初始化数据
	 */
	private void insertData() {
		tvDeviceModel.setText(deviceModel);
		tvDeviceBrand.setText(deviceBrand);
		tvDeviceOSName.setText(deviceOSName);
		tvDeviceCustomOSName.setText(deviceCustomOSName);
		
		tvDeviceCpuStructure.setText(deviceCpuStructure);
		if("".equals(deviceCpuType.trim())){
			tvDeviceCpuType.setText("获取CPU型号为空");
		}else{
			tvDeviceCpuType.setText(deviceCpuType);
		}
		tvDeviceCpuCoreLogic.setText(deviceCpuCoreNum);
		tvDeviceCpuCorepPhysics.setText(deviceCpuCoreNum);
		tvDeviceCpuFrequency.setText(deviceCpuFrequeccy + "GHz");
		
		tvDeviceGpuType.setText(deviceGpuType);
		tvDeviceRamMemory.setText(deviceRamMemory + "GB");
		tvDeviceTotalMemory.setText(deviceTotalMemory + "GB (剩余："
				+ deviceDiskAvailableMemory + "GB)");
		tvDeviceDiskMemory.setText(deviceDiskMemory + "GB");
		
		tvDeviceScreenSize.setText(deviceScreenInches + "英寸");
		tvDeviceScreenHeight.setText("" + deviceHeight);
		tvDeviceScreenWidth.setText("" + deviceWidth);
		tvDeviceUUID.setText(deviceUUID);
		tvDeviceMacAddress.setText(deviceMacAddress);
		if(null == devicePhone || "".equals(devicePhone.trim())){
			tvDevicePhone.setText("获取设备号码为空");
			devicePhone = "";
		}else{
			tvDevicePhone.setText(devicePhone);
		}
		if(null == deviceIP || "".equals(deviceIP.trim())){
			tvDeviceIP.setText("获取ip地址为空");
			deviceIP = "";
		}else{
			tvDeviceIP.setText(deviceIP);
		}
	}

	/**
	 * 发送数据到服务器
	 */
	public void sendUpdateServer() {
		if(TextUtils.isEmpty(Constants.DATA_URL)){
			ToastUtil.ShowLongToast(InfoActivity.this, Constants.GET_ADDRESS_WARNING);
			return;
		}
		String url = Constants.DATA_URL + "/platform/serverManage/mobileMaint.do";
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("act", "updateServer"));
		nameValuePairs.add(new BasicNameValuePair("mobile_model", deviceModel));
		nameValuePairs.add(new BasicNameValuePair("osName", deviceOSName));
		nameValuePairs.add(new BasicNameValuePair("derive_os", deviceCustomOSName));
		nameValuePairs.add(new BasicNameValuePair("cpuName", deviceCpuType));
		nameValuePairs.add(new BasicNameValuePair("cpu_clock_speed", deviceCpuFrequeccy));
		nameValuePairs.add(new BasicNameValuePair("cpuCoreLogic", deviceCpuCoreNum));
		nameValuePairs.add(new BasicNameValuePair("cpuCorepPhysics", deviceCpuCoreNum));
		nameValuePairs.add(new BasicNameValuePair("gpu_model", deviceGpuType));
		nameValuePairs.add(new BasicNameValuePair("memory", deviceRamMemory));
		nameValuePairs.add(new BasicNameValuePair("mobile_disk", deviceDiskMemory));
		nameValuePairs.add(new BasicNameValuePair("mobile_sd_card", deviceDiskMemory));
		nameValuePairs.add(new BasicNameValuePair("mobile_battery", ""));
		nameValuePairs.add(new BasicNameValuePair("display_size", deviceScreenInches));
		nameValuePairs.add(new BasicNameValuePair("display_height", "" + deviceHeight));
		nameValuePairs.add(new BasicNameValuePair("display_width", "" + deviceWidth));
		nameValuePairs.add(new BasicNameValuePair("m_uuid", deviceUUID));
		nameValuePairs.add(new BasicNameValuePair("cpuStructure", deviceCpuStructure));
		nameValuePairs.add(new BasicNameValuePair("mac", deviceMacAddress));
		nameValuePairs.add(new BasicNameValuePair("mobile_phone_no", devicePhone));
		nameValuePairs.add(new BasicNameValuePair("ip", deviceIP));
		nameValuePairs.add(new BasicNameValuePair("mobile_model", deviceBrand));
		SendHttp sp = new SendHttp(url, nameValuePairs, InfoActivity.this,myHandler);
		sp.start();
	}

	@SuppressLint("NewApi") 
	private void setCpuAbis(){
		if(Build.VERSION.SDK_INT < 21){
			deviceCpuStructure = Build.CPU_ABI;
		}else{
			String[] allABIs = Build.SUPPORTED_ABIS;
			int abiSize = allABIs.length;
			if(abiSize > 0){
				for (int i = 0; i < abiSize - 1; i++) {
					deviceCpuStructure += allABIs[i] + ",";
				}
				deviceCpuStructure += allABIs[abiSize -1];
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode != AppCompatActivity.RESULT_OK) {
                return;
            }else if(data != null && resultCode != 0){
                Log.i(TAG, "user agree the application to capture screen");
                Constants.takeshotResultCode = resultCode;
                Constants.takeshotResultData = data;
                Constants.takeshotWindowManager = (WindowManager)getApplication().getSystemService(Context.WINDOW_SERVICE);
                Constants.takeshotScreenSize = new Point();
                Constants.takeshotWindowManager.getDefaultDisplay().getSize(Constants.takeshotScreenSize);
                
                Constants.takeshotMetrics = new DisplayMetrics();
                Constants.takeshotWindowManager.getDefaultDisplay().getMetrics(Constants.takeshotMetrics);
                Constants.takeshotScreenDensity = Constants.takeshotMetrics.densityDpi;
                
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		    	Date currentDate = new Date();
		    	String currentDateName = format.format(currentDate);
		    	String fileFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + 
		    			            File.separator+
		    			            "snailtest"+
		    			            File.separator + 
		    			            currentDateName;
		    	String filepath = fileFolder + File.separator + "command.txt";
				String command = "";
				try{
					command = FileUtil.readFile(filepath).toString();
				}catch(Exception e){
					command = "";
				}
				Log.d(TAG, "command is :" + command);
				if (!"".equals(command)) {
					try {
						Process pro = Runtime
								.getRuntime()
								.exec("am instrument --user 0 -w com.snailgame.test/android.test.InstrumentationTestRunner");
						BufferedReader bf = new BufferedReader(
								new InputStreamReader(pro.getInputStream()));
						String lint = null;
						while ((lint = bf.readLine()) != null) {
							Log.d(TAG, lint);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else{
					ToastUtil.ShowLongToast(InfoActivity.this, "当前目录下暂无指令文件");
				}
            }
        }
    }
}
