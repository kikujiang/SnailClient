/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.snail.util;

import java.util.Date;

import android.content.Intent;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.util.Config;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * Contains the internal constants that are used in the download manager. As a
 * general rule, modifying these constants should be done with care.
 */
public class Constants {

	/** Tag used for debugging/logging */
	public static final String TAG = "DownloadManager";

	/** The column that used to be used for the HTTP method of the request */
	public static final String RETRY_AFTER_X_REDIRECT_COUNT = "method";

	/** The column that used to be used for the magic OTA update filename */
	public static final String OTA_UPDATE = "otaupdate";

	/** The column that used to be used to reject system filetypes */
	public static final String NO_SYSTEM_FILES = "no_system";

	/** The column that is used for the downloads's ETag */
	public static final String ETAG = "etag";

	/** The column that is used for the initiating app's UID */
	public static final String UID = "uid";

	/** The column that is used to count retries */
	public static final String FAILED_CONNECTIONS = "numfailed";

	/** The intent that gets sent when the service must wake up for a retry */
	public static final String ACTION_RETRY = "android.intent.action.DOWNLOAD_WAKEUP";

	/** the intent that gets sent when clicking a successful download */
	public static final String ACTION_OPEN = "android.intent.action.DOWNLOAD_OPEN";

	/** the intent that gets sent when clicking an incomplete/failed download */
	public static final String ACTION_LIST = "android.intent.action.DOWNLOAD_LIST";

	/**
	 * the intent that gets sent when deleting the notification of a completed
	 * download
	 */
	public static final String ACTION_HIDE = "android.intent.action.DOWNLOAD_HIDE";
	public static final String ACTION_MONITOR = "android.intent.action.BROADCAST";

	/**
	 * The default base name for downloaded files if we can't get one at the
	 * HTTP level
	 */
	public static final String DEFAULT_DL_FILENAME = "downloadfile";

	/**
	 * The default extension for html files if we can't get one at the HTTP
	 * level
	 */
	public static final String DEFAULT_DL_HTML_EXTENSION = ".html";

	/**
	 * The default extension for text files if we can't get one at the HTTP
	 * level
	 */
	public static final String DEFAULT_DL_TEXT_EXTENSION = ".txt";

	/**
	 * The default extension for binary files if we can't get one at the HTTP
	 * level
	 */
	public static final String DEFAULT_DL_BINARY_EXTENSION = ".bin";

	/**
	 * When a number has to be appended to the filename, this string is used to
	 * separate the base filename from the sequence number
	 */
	public static final String FILENAME_SEQUENCE_SEPARATOR = "-";

	/** Where we store downloaded files on the external storage */
	public static final String DEFAULT_DL_SUBDIR = "/download";

	/** A magic filename that is allowed to exist within the system cache */
	public static final String KNOWN_SPURIOUS_FILENAME = "lost+found";

	/** A magic filename that is allowed to exist within the system cache */
	public static final String RECOVERY_DIRECTORY = "recovery";

	/** The default user agent used for downloads */
	public static final String DEFAULT_USER_AGENT = "AndroidDownloadManager";

	/** The MIME type of APKs */
	public static final String MIMETYPE_APK = "application/vnd.android.package";

	/** The buffer size used to stream the data */
	public static final int BUFFER_SIZE = 40960;

	/**
	 * The minimum amount of progress that has to be done before the progress
	 * bar gets updated
	 */
	public static final int MIN_PROGRESS_STEP = 4096;

	/**
	 * The minimum amount of time that has to elapse before the progress bar
	 * gets updated, in ms
	 */
	public static final long MIN_PROGRESS_TIME = 1500;

	/** The maximum number of rows in the database (FIFO) */
	public static final int MAX_DOWNLOADS = 1000;

	/**
	 * The number of times that the download manager will retry its network
	 * operations when no progress is happening before it gives up.
	 */
	public static final int MAX_RETRIES = 5;

	/**
	 * The minimum amount of time that the download manager accepts for a
	 * Retry-After response header with a parameter in delta-seconds.
	 */
	public static final int MIN_RETRY_AFTER = 30; // 30s

	/**
	 * The maximum amount of time that the download manager accepts for a
	 * Retry-After response header with a parameter in delta-seconds.
	 */
	public static final int MAX_RETRY_AFTER = 24 * 60 * 60; // 24h

	/**
	 * The maximum number of redirects.
	 */
	public static final int MAX_REDIRECTS = 5; // can't be more than 7.

	/**
	 * The time between a failure and the first retry after an IOException. Each
	 * subsequent retry grows exponentially, doubling each time. The time is in
	 * seconds.
	 */
	public static final int RETRY_FIRST_DELAY = 30;

	/** Enable separate connectivity logging */
	static final boolean LOGX = false;

	/**
	 * Enable verbose logging - use with
	 * "setprop log.tag.DownloadManager VERBOSE"
	 */
	private static final boolean LOCAL_LOGV = false;
	@SuppressWarnings("unused")
	public static final boolean LOGV = Config.LOGV
			|| (Config.LOGD && LOCAL_LOGV && Log.isLoggable(TAG, Log.VERBOSE));

	/** Enable super-verbose logging */
	private static final boolean LOCAL_LOGVV = false;
	@SuppressWarnings("unused")
	public static final boolean LOGVV = LOCAL_LOGVV && LOGV;
	public static final String SHARED_PREFERENCE_NAME = "client_preferences";

	// PREFERENCE KEYS

	public static final String CALLBACK_ACTIVITY_PACKAGE_NAME = "CALLBACK_ACTIVITY_PACKAGE_NAME";

	public static final String CALLBACK_ACTIVITY_CLASS_NAME = "CALLBACK_ACTIVITY_CLASS_NAME";

	public static final String API_KEY = "API_KEY";

	public static final String VERSION = "VERSION";

	public static final String XMPP_HOST = "XMPP_HOST";

	public static final String XMPP_PORT = "XMPP_PORT";

	public static final String XMPP_USERNAME = "XMPP_USERNAME";

	public static final String XMPP_PASSWORD = "XMPP_PASSWORD";

	// public static final String USER_KEY = "USER_KEY";

	public static final String DEVICE_ID = "DEVICE_ID";

	public static final String EMULATOR_DEVICE_ID = "EMULATOR_DEVICE_ID";

	public static final String NOTIFICATION_ICON = "NOTIFICATION_ICON";

	public static final String SETTINGS_NOTIFICATION_ENABLED = "SETTINGS_NOTIFICATION_ENABLED";

	public static final String SETTINGS_SOUND_ENABLED = "SETTINGS_SOUND_ENABLED";

	public static final String SETTINGS_VIBRATE_ENABLED = "SETTINGS_VIBRATE_ENABLED";

	public static final String SETTINGS_TOAST_ENABLED = "SETTINGS_TOAST_ENABLED";

	// NOTIFICATION FIELDS

	public static final String NOTIFICATION_ID = "NOTIFICATION_ID";

	public static final String NOTIFICATION_API_KEY = "NOTIFICATION_API_KEY";

	public static final String NOTIFICATION_TITLE = "NOTIFICATION_TITLE";

	public static final String NOTIFICATION_MESSAGE = "NOTIFICATION_MESSAGE";

	public static final String NOTIFICATION_URI = "NOTIFICATION_URI";

	public static final String PACKET_ID = "PACKET_ID";

	public static final String NOTIFICATION_FROM = "NOTIFICATION_FROM";

	// INTENT ACTIONS

	public static final String ACTION_SHOW_NOTIFICATION = "org.androidpn.client.SHOW_NOTIFICATION";

	public static final String ACTION_NOTIFICATION_CLICKED = "org.androidpn.client.NOTIFICATION_CLICKED";

	public static final String ACTION_NOTIFICATION_CLEARED = "org.androidpn.client.NOTIFICATION_CLEARED";


	public static String download = null;
	public static String socket = null;
	public static String socketport = null;
//	public static String sendhp = null;
//	public static String sendhtp = null;
//	public static String sendhep = null;
	public static String update = null;
	public static int countdown = 0;
	// public static boolean ismonitor=false;
	public static int sdk = android.os.Build.VERSION.SDK_INT;
	public static String project = null;
	public static String command = null;
	public static boolean ilaunch = true;// 是否启动辅助服务
	public static boolean iscxml = true;
	public static float scale=0;
	public static int fps=0;
	public static boolean isopen=false; //是否可以读取控件
	public static int port=4724;
	public static String downApk="http://10.206.0.23:8080/examples/apk/";
	public static boolean islaunch=false;
//	public static String url="http://10.206.0.23:8088/";
//	public static String url = "http://172.36.0.22:80/";
//	public static String data_url="http://10.206.0.51:8080/";
//	public static String url="http://10.206.0.51:8080/";
//	public static String url="http://10.206.0.53:8080/";
	public static String GET_URL_ADDRESS = "http://172.36.0.22:80/platform/mobileTest/index.do";
//	public static String GET_URL_ADDRESS = "http://10.206.2.132:8080/SystemTestPlatform/platform/mobileTest/index.do";
//	public static String GET_URL_ADDRESS = "http://172.19.26.98:8080/SystemTestPlatform/platform/mobileTest/index.do";
//	public static String GET_URL_ADDRESS = "http://10.206.2.77:8088/SystemTestPlatform/platform/mobileTest/index.do";
//	public static String GET_URL_ADDRESS = "http://172.19.26.210:8083/SystemTestPlatform/platform/mobileTest/index.do";
	public static String DATA_URL = "";
	public static String RES_URL = "";
	public static String MONITOR_SERVER_URL = "";
	public static String MAIN_SERVER_URL = "";
	public static String OaBugUrl = "http://ver.mysnail.com/";
	public static String downpath = "/MyDownload/";
	public static String  folder = Environment.getExternalStoragePublicDirectory(downpath).getAbsolutePath()+"/";
	public static String managerIP="10.206.2.162";
//	public static String managerIP="172.35.1.55";
//	public static String managerIP="172.17.4.80";
	public static int managerPort = 5581;
	public static final String VERSION_URL = "http://10.206.2.162:8080/examples/update/version";
	public static final String APK_URL = "http://10.206.2.162:8080/examples/update/SnailClient.apk";
	public static final String GET_ADDRESS_WARNING = "获取服务器地址失败，请手动点击获取地址按钮";
	public static int versionCode = 0;
	public static int appVersionCode;
	//截图保存的根目录
	public static final String TAKESHOT_SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	public static int takeshotResultCode = 0;
    public static Intent takeshotResultData = null;
    public static MediaProjectionManager takeshotMediaProjectionManager = null;
    public static WindowManager takeshotWindowManager = null;
    public static Point takeshotScreenSize = null;
    public static DisplayMetrics takeshotMetrics = null;
    public static int takeshotScreenDensity = 0;
    
    public static Date startTestTime;
    public static Date endTestTime;
}
