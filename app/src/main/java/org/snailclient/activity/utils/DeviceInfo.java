package org.snailclient.activity.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * 获取手机的设备信息
 * 
 * @author wubo1
 * 
 */
@SuppressLint("NewApi")
public class DeviceInfo {

	private static final float SIZE = 1024;

	private final String RINGER_MODE_NORMAL = "Normal";
	private final String RINGER_MODE_SILENT = "Silent";
	private final String RINGER_MODE_VIBRATE = "Vibrate";

	private final String PHONE_TYPE_GSM = "GSM";
	private final String PHONE_TYPE_CDMA = "CDMA";
	private final String PHONE_TYPE_NONE = "Unknown";

	private final String NETWORK_TYPE_2G = "2G";
	private final String NETWORK_TYPE_3G = "3G";
	private final String NETWORK_TYPE_4G = "4G";
	private final String NETWORK_TYPE_WIFI_WIFIMAX = "WiFi";

	private final String NOT_FOUND_VAL = "unknown";

	private Context context;

	private DecimalFormat format;

	/**
	 * 构造方法
	 * 
	 * @param context
	 */
	public DeviceInfo(Context context) {
		this.context = context;
		format = new DecimalFormat("##0.00");
	}

	/**
	 * 获取硬件制造商版本号
	 * 
	 * @return 硬件制造商版本号
	 */
	public final String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return model;
		} else {
			return manufacturer + " " + model;
		}
	}

	public final String getReleaseBuildVersion() {
		return Build.VERSION.RELEASE;
	}

	public final String getBuildVersionCodeName() {
		return Build.VERSION.CODENAME;
	}

	public final String getManufacturer() {
		return Build.MANUFACTURER;
	}

	public final String getModel() {
		return Build.MODEL;
	}

	public final String getCustomOsVersion() {
		return Build.DISPLAY;
	}

	/**
	 * 获取手机制造商
	 * 
	 * @return 手机制造商
	 */
	public String getProduct() {
		return Build.PRODUCT;
	}

	public final String getFingerprint() {
		return Build.FINGERPRINT;
	}

	public final String getHardware() {
		return Build.HARDWARE;
	}

	public final String getDevice() {
		return Build.DEVICE;
	}

	public final String getBoard() {
		return Build.BOARD;
	}

	public final String getDisplayVersion() {
		return Build.DISPLAY;
	}

	public final String getBuildBrand() {
		return Build.BRAND;
	}

	public final String getBuildHost() {
		return Build.HOST;
	}

	public final long getBuildTime() {
		return Build.TIME;
	}

	public final String getBuildUser() {
		return Build.USER;
	}

	public final String getSerial() {
		return Build.SERIAL;
	}

	public final String getOSVersion() {
		return Build.VERSION.RELEASE;
	}

	public final String getLanguage() {
		return Locale.getDefault().getLanguage();
	}

	public final int getSdkVersion() {
		return Build.VERSION.SDK_INT;
	}

	public String getScreenDensity() {
		int density = context.getResources().getDisplayMetrics().densityDpi;
		String scrType = "";
		switch (density) {
		case DisplayMetrics.DENSITY_LOW:
			scrType = "ldpi";
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			scrType = "mdpi";
			break;
		case DisplayMetrics.DENSITY_HIGH:
			scrType = "hdpi";
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			scrType = "xhdpi";
			break;
		default:
			scrType = "other";
			break;
		}
		return scrType;
	}

	public int getScreenHeight() {
		int height = 0;
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		height = size.y;
		return height;
	}

	public int getScreenWidth() {
		int width = 0;
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		return width;
	}

	/* App Info: */
	public String getVersionName() {
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return pInfo.versionName;
		} catch (Exception e1) {
			return null;
		}
	}

	public Integer getVersionCode() {
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return pInfo.versionCode;
		} catch (Exception e1) {
			return null;
		}
	}

	public String getPackageName() {
		return context.getPackageName();
	}

	public String getActivityName() {
		return context.getClass().getSimpleName();
	}

	public String getAppName() {
		PackageManager packageManager = context.getPackageManager();
		ApplicationInfo applicationInfo = null;
		try {
			applicationInfo = packageManager.getApplicationInfo(
					context.getApplicationInfo().packageName, 0);
		} catch (final PackageManager.NameNotFoundException e) {
		}
		return (String) (applicationInfo != null ? packageManager
				.getApplicationLabel(applicationInfo) : NOT_FOUND_VAL);
	}

	public final boolean isAppInstalled(String packageName) {
		return context.getPackageManager().getLaunchIntentForPackage(
				packageName) != null;
	}

	public boolean isRunningOnEmulator() {
		return Build.FINGERPRINT.startsWith("generic")
				|| Build.FINGERPRINT.startsWith("unknown")
				|| Build.MODEL.contains("google_sdk")
				|| Build.MODEL.contains("Emulator")
				|| Build.MODEL.contains("Android SDK built for x86")
				|| Build.MANUFACTURER.contains("Genymotion")
				|| (Build.BRAND.startsWith("generic") && Build.DEVICE
						.startsWith("generic"))
				|| "google_sdk".equals(Build.PRODUCT)
				|| Build.PRODUCT.contains("vbox86p")
				|| Build.DEVICE.contains("vbox86p")
				|| Build.HARDWARE.contains("vbox86");
	}

	public String getDeviceRingerMode() {
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		switch (audioManager.getRingerMode()) {
		case AudioManager.RINGER_MODE_SILENT:
			return RINGER_MODE_SILENT;
		case AudioManager.RINGER_MODE_VIBRATE:
			return RINGER_MODE_VIBRATE;
		default:
			return RINGER_MODE_NORMAL;
		}
	}

	public final boolean isDeviceRooted() {
		String[] paths = { "/system/app/Superuser.apk", "/sbin/su",
				"/system/bin/su", "/system/xbin/su", "/data/local/xbin/su",
				"/data/local/bin/su", "/system/sd/xbin/su",
				"/system/bin/failsafe/su", "/data/local/su", "/su/bin/su" };
		for (String path : paths) {
			if (new File(path).exists())
				return true;
		}
		return false;
	}

	public final String getAndroidId() {
		String androidId = Settings.Secure.getString(
				context.getContentResolver(), Settings.Secure.ANDROID_ID);
		return androidId;
	}

	public String getInstallSource() {
		PackageManager pm = context.getPackageManager();
		String installationSource = pm.getInstallerPackageName(context
				.getPackageName());
		return installationSource;
	}

	public final String getUserAgent() {
		final String systemUa = System.getProperty("http.agent");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return WebSettings.getDefaultUserAgent(context) + "__" + systemUa;
		}
		return new WebView(context).getSettings().getUserAgentString() + "__"
				+ systemUa;
	}

	public final String getGSFId() {
		Uri URI = Uri.parse("content://com.google.android.gsf.gservices");
		String ID_KEY = "android_id";
		String params[] = { ID_KEY };
		Cursor c = context.getContentResolver().query(URI, null, null, params,
				null);

		if (!c.moveToFirst() || c.getColumnCount() < 2) {
			c.close();
			return NOT_FOUND_VAL;
		}
		try {
			String gsfId = Long.toHexString(Long.parseLong(c.getString(1)));
			c.close();
			return gsfId;
		} catch (NumberFormatException e) {
			c.close();
			return NOT_FOUND_VAL;
		}
	}

	public boolean hasExternalSDCard() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public final String getTotalRAM() {
		long totalMemory = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
			ActivityManager activityManager = (ActivityManager) context
					.getSystemService(Activity.ACTIVITY_SERVICE);
			activityManager.getMemoryInfo(mi);
			float ramMemory = mi.totalMem / SIZE / SIZE / SIZE;
			if(ramMemory > 2.0){
				return String.valueOf(Math.ceil(ramMemory));
			}else{
				return format.format(ramMemory);
			}
		}
		try {
			RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r");
			String load = reader.readLine().replaceAll("\\D+", "");
			totalMemory = (long) Integer.parseInt(load);
			reader.close();
			float ramMemory = totalMemory / SIZE / SIZE / SIZE;
			if(ramMemory > 2.0){
				return String.valueOf(Math.ceil(ramMemory));
			}else{
				return format.format(ramMemory);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "0l";
		}
	}
	
	

	@SuppressLint("NewApi")
	public final String getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize, availableBlocks;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			blockSize = stat.getBlockSizeLong();
			availableBlocks = stat.getAvailableBlocksLong();
		} else {
			blockSize = stat.getBlockSize();
			availableBlocks = stat.getAvailableBlocks();
		}
		return format.format(availableBlocks * blockSize);
	}

	@SuppressLint("NewApi")
	public final String getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize;
		long totalBlocks;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			blockSize = stat.getBlockSizeLong();
			totalBlocks = stat.getBlockCountLong();
		} else {
			blockSize = stat.getBlockSize();
			totalBlocks = stat.getBlockCount();
		}
		return format.format(totalBlocks * blockSize / SIZE / SIZE / SIZE);
	}

	@SuppressLint("NewApi")
	public final String getTotalMemorySize() {
		float allSize = Float.parseFloat(getTotalInternalMemorySize())
				+ Float.parseFloat(getTotalExternalMemorySize());
		return String.valueOf(allSize);
//		File path = Environment.getExternalStorageDirectory();
//		StatFs stat = new StatFs(path.getPath());
//		long blockSize;
//		long totalBlocks;
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//			blockSize = stat.getBlockSizeLong();
//			totalBlocks = stat.getBlockCountLong();
//		} else {
//			blockSize = stat.getBlockSize();
//			totalBlocks = stat.getBlockCount();
//		}
//		return format.format(totalBlocks * blockSize / SIZE / SIZE / SIZE);
	}

	@SuppressLint("NewApi")
	public final String getAvailableExternalMemorySize() {
		if (hasExternalSDCard()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize;
			long availableBlocks;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				blockSize = stat.getBlockSizeLong();
				availableBlocks = stat.getAvailableBlocksLong();
			} else {
				blockSize = stat.getBlockSize();
				availableBlocks = stat.getAvailableBlocks();
			}
			return format.format(availableBlocks * blockSize / SIZE / SIZE
					/ SIZE);
		}
		return "0";
	}

	public final String getAvailableMemorySize() {
		float internalSize = Float.parseFloat(getAvailableInternalMemorySize());
		float externalSize = Float.parseFloat(getAvailableExternalMemorySize());
		return format.format((internalSize + externalSize)/ SIZE / SIZE / SIZE);
	}
	
	@SuppressLint("NewApi")
	public final String getTotalExternalMemorySize() {
		if (hasExternalSDCard()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize;
			long totalBlocks;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
				blockSize = stat.getBlockSizeLong();
				totalBlocks = stat.getBlockCountLong();
			} else {
				blockSize = stat.getBlockSize();
				totalBlocks = stat.getBlockCount();
			}
			return format.format(totalBlocks * blockSize / SIZE / SIZE / SIZE);
		}
		return "0";
	}

	public final String getIMSI() {
		TelephonyManager telephonyMgr = (TelephonyManager) context
				.getSystemService(Activity.TELEPHONY_SERVICE);
		return telephonyMgr.getSubscriberId();
	}

	public final String getPhoneType() {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		switch (tm.getPhoneType()) {
		case TelephonyManager.PHONE_TYPE_GSM:
			return PHONE_TYPE_GSM;
		case TelephonyManager.PHONE_TYPE_CDMA:
			return PHONE_TYPE_CDMA;
		case TelephonyManager.PHONE_TYPE_NONE:
		default:
			return PHONE_TYPE_NONE;
		}
	}

	public String getOperator() {
		String operatorName;
		TelephonyManager telephonyManager = ((TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE));
		operatorName = telephonyManager.getNetworkOperatorName();
		if (operatorName == null)
			operatorName = telephonyManager.getSimOperatorName();
		return operatorName;
	}

	public final String getSIMSerial() {
		TelephonyManager telephonyManager = ((TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE));
		return telephonyManager.getSimSerialNumber();
	}

	public final boolean isSimNetworkLocked() {
		TelephonyManager telephonyManager = ((TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE));
		return telephonyManager.getSimState() == TelephonyManager.SIM_STATE_NETWORK_LOCKED;
	}

	public final boolean isNfcPresent() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
			NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
			return nfcAdapter != null;
		}
		return false;
	}

	public final boolean isNfcEnabled() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
			NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
			return nfcAdapter != null && nfcAdapter.isEnabled();
		}
		return false;
	}

	public final boolean isWifiEnabled() {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return wifiManager.isWifiEnabled();
	}

	@SuppressWarnings("MissingPermission")
	public final boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) context
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnected();
	}

	public String getNetworkClass() {
		TelephonyManager mTelephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = mTelephonyManager.getNetworkType();
		switch (networkType) {
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_EDGE:
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return NETWORK_TYPE_2G;
		case TelephonyManager.NETWORK_TYPE_UMTS:
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
		case TelephonyManager.NETWORK_TYPE_HSDPA:
		case TelephonyManager.NETWORK_TYPE_HSUPA:
		case TelephonyManager.NETWORK_TYPE_HSPA:
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
		case TelephonyManager.NETWORK_TYPE_EHRPD:
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			return NETWORK_TYPE_3G;
		case TelephonyManager.NETWORK_TYPE_LTE:
			return NETWORK_TYPE_4G;
		default:
			return NOT_FOUND_VAL;
		}
	}

	public final String getNetworkType() {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork == null)
			return NOT_FOUND_VAL;

		else if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
				|| activeNetwork.getType() == ConnectivityManager.TYPE_WIMAX) {
			return NETWORK_TYPE_WIFI_WIFIMAX;
		} else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
			return getNetworkClass();
		}
		return NOT_FOUND_VAL;
	}

	/**
	 * 获取设备cpu的架构
	 * 
	 * @return 设备cpu的架构
	 */
	public final String getCpuStructure() {
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader("/proc/cpuinfo");
			br = new BufferedReader(fr);
			String text = br.readLine();
			String[] array = text.split(":\\s+", 2);
			for (int i = 0; i < array.length; i++) {
			}
			return array[1];
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public final String getMaxCpuFreq() {
		String result = "";
		ProcessBuilder cmd;
		try {
			String[] args = { "/system/bin/cat",
					"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
			cmd = new ProcessBuilder(args);
			Process process = cmd.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[24];
			while (in.read(re) != -1) {
				result = result + new String(re);
			}
			in.close();
			if("".equals(result)){
				return "0";
			}
			float cpuFre = Float.parseFloat(result.trim()) / 1024 / 1024;
			String cpuFreDeal = format.format(cpuFre);
			return cpuFreDeal;
		} catch (IOException ex) {
			ex.printStackTrace();
			return "N/A";
		}
	}

	/**
	 * 获取屏幕的物理尺寸
	 * 
	 * @return 屏幕的物理尺寸
	 */
	public final String getScreenSize() {
		Point point = new Point();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getRealSize(point);
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		double x = Math.pow(point.x / dm.xdpi, 2);
		double y = Math.pow(point.y / dm.ydpi, 2);
		double screenInches = Math.sqrt(x + y);
		String screenSize = format.format(screenInches);
		return screenSize;
	}

	/**
	 * 获取cpu的型号
	 * 
	 * @return cpu的型号
	 */
	public final String getCpuType() {
		String cpuInfoFilePath = "/proc/cpuinfo";
		String cpuType = "";
		try {
			FileReader cpuInfoFile = new FileReader(cpuInfoFilePath);
			BufferedReader cpuInfoFileReader = new BufferedReader(cpuInfoFile,
					8192);
			String cpuInfoFileLine = null;
			while ((cpuInfoFileLine = cpuInfoFileReader.readLine()) != null) {// 使用readLine方法，一次读一行
				if (cpuInfoFileLine.contains("Hardware")) {
					String cpuTypeFullName = cpuInfoFileLine.split(":")[1];
					if (!"".equals(cpuTypeFullName) && cpuTypeFullName.contains(",")) {
						String cpuTypeStr = cpuTypeFullName.split(",")[1]
								.trim();
						cpuType = cpuTypeStr.split(" ")[1].trim();
					}else{
						cpuType = cpuTypeFullName.trim();
					}
					break;
				}
			}
			cpuInfoFileReader.close();
		} catch (Exception e) {
			cpuType = "";
		}
		return cpuType;
	}

	/**
	 * 获取cpu核数
	 * 
	 * @return cpu核数
	 */
	public final int getNumCores() {
		// Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}

	/*
	 * ****************************************************************
	 * 子函数：获得本地MAC地址
	 * ****************************************************************
	 */
	public final String getMacAddress() {
		String result = "";
		String Mac = "";
		result = callCmd("busybox ifconfig", "HWaddr");

		// 如果返回的result == null，则说明网络不可取
		if (result == null) {
			return "网络出错，请检查网络";
		}

		// 对该行数据进行解析
		// 例如：eth0 Link encap:Ethernet HWaddr 00:16:E8:3E:DF:67
		if (result.length() > 0 && result.contains("HWaddr") == true) {
			Mac = result.substring(result.indexOf("HWaddr") + 6,
					result.length() - 1);
			Log.i("test", "Mac:" + Mac + " Mac.length: " + Mac.length());

			if (Mac.length() > 1) {
				Mac = Mac.replaceAll(" ", "");
				result = "";
				String[] tmp = Mac.split(":");
				for (int i = 0; i < tmp.length; ++i) {
					result += tmp[i];
				}
			}
			Log.i("test", result + " result.length: " + result.length());
		}
		return result;
	}

	private String callCmd(String cmd, String filter) {
		String result = "";
		String line = "";
		try {
			Process proc = Runtime.getRuntime().exec(cmd);
			InputStreamReader is = new InputStreamReader(proc.getInputStream());
			BufferedReader br = new BufferedReader(is);
			// 执行命令cmd，只取结果中含有filter的这一行
			while ((line = br.readLine()) != null
					&& line.contains(filter) == false) {
				// result += line;
				Log.i("test", "line: " + line);
			}
			result = line;
			Log.i("test", "result: " + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String readSIMCard() {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);// 取得相关系统服务
//		tm.get
		return tm.getLine1Number();
	}
	
	private String int2ip(int ipInt) {  
        StringBuilder sb = new StringBuilder();  
        sb.append(ipInt & 0xFF).append(".");  
        sb.append((ipInt >> 8) & 0xFF).append(".");  
        sb.append((ipInt >> 16) & 0xFF).append(".");  
        sb.append((ipInt >> 24) & 0xFF);  
        return sb.toString();  
    }  
  
    /** 
     * 获取当前ip地址 
     *  
     * @param context 
     * @return 
     */  
    public String getLocalIpAddress() {  
        try {  
            WifiManager wifiManager = (WifiManager) context  
                    .getSystemService(Context.WIFI_SERVICE);  
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();  
            int i = wifiInfo.getIpAddress();  
            return int2ip(i);  
        } catch (Exception ex) {  
            return "";  
        }  
    }
    
    public int getScreenWidthNew(){
    	DisplayMetrics dm = new DisplayMetrics();
    	((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
    	return dm.widthPixels;
    }
    public int getScreenHeightNew(){
    	DisplayMetrics dm = new DisplayMetrics();
    	((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
    	return dm.heightPixels;
    }
}