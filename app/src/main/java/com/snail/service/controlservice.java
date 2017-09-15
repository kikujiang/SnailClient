package com.snail.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.monitor.util.Contact;
import com.monitor.util.CpuInfo;
import com.monitor.util.MemoryInfo;
import com.monitor.util.ProcessInfo;
import com.monitor.util.SendHttp;
import com.snail.adapter.BaseSpinnerAdpater;
import com.snail.util.Constants;
import com.snail.util.SnailApplication;

import org.androidpn.demoapp.R;
import org.json.JSONException;
import org.json.JSONObject;
import org.snailclient.activity.SubmitPerformanceResult;
import org.snailclient.activity.utils.action.ActionBean;
import org.snailclient.activity.utils.action.ActionDataUtil;
import org.snailclient.activity.utils.fps.GTFrameUtils;
import org.snailclient.activity.utils.fps.ToastUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solo.FileUtil;
import solo.HttpUtilForWired;

/**
 * 类的描述：后台性能数据处理服务
 *
 * @author zhll
 * @Time 2015-07-08
 */
public class controlservice extends Service {

    public static final String TAG = "controlservice";
    //    public static final int MSG_BEGIN_TEST_SUCCESS = 100001;
    public static final int MSG_BEGIN_TEST_FAILURE = 100002;
    public static final int MSG_END_TEST_SUCCESS = 100003;

    private int delaytime;
    private DecimalFormat fomart;
    private MemoryInfo memoryInfo;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
//                case MSG_BEGIN_TEST_SUCCESS:
//                    initPerformanceData();
//                    break;
                case MSG_BEGIN_TEST_FAILURE:
                    String failDesc = msg.obj.toString();
                    Toast.makeText(controlservice.this, failDesc, Toast.LENGTH_LONG).show();
                    break;
                case MSG_END_TEST_SUCCESS:
                    if (windowManager != null) {
                        Contact.isShowFloatingWindow = false;
                        // windowManager.removeView(viFloatingWindow);
                    }
                    stopSelf();
                    Intent dataIntent = new Intent(controlservice.this, SubmitPerformanceResult.class);
                    dataIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    Bundle data = new Bundle();
                    data.putString("tid", tid);
                    data.putString("packageName", packageName);
                    dataIntent.putExtras(data);
                    startActivity(dataIntent);
                    break;
            }
        }

        ;
    };
    private CpuInfo cpuInfo;
    private String time = "5";
    private String packageName, settingTempFile, startActivity;
    private View viFloatingWindow;
    private TextView txtUnusedMem;
    private TextView txtTotalMem;
    private TextView txtTraffic;
    private TextView txtFps;
    private TextView result;

    private TextView loadActionListTv;
    private Spinner actionListSpinner;
    private Button btn;
    private ImageView stopAction;
    private ImageView exitWindow;
    private ImageView startAction;
    private ImageView takepic;
    private int pid, uid;
    private boolean isServiceStop = false;
    private String sender, password, recipients, smtp;
    private String[] receivers;
    public static String mark = "start01";
    public static String processName;
    private String model = null;
    public static BufferedWriter bw;
    public static FileOutputStream out;
    public static OutputStreamWriter osw;
    public static String resultFilePath;
    public static boolean isStop = false;
    private boolean first = false;
    private String totalBatt = "0";
    private String temperature = "0";
    private String scriptStep = null;
    private BatteryInfoBroadcastReceiver batteryBroadcast = null;
    SendHttp send;
    String url = "";
    private static final int MAX_START_TIME_COUNT = 5;
    private static final String START_TIME = "#startTime";
    private int getStartTimeCount = 0;
    private boolean isGetStartTime = true;
    private String startTime = "";
    private String id = null;
    private WindowManager windowManager;
    private WindowManager.LayoutParams wmParams;
    private float startX;
    private float startY;
    private float x;
    private float y;
    private float mTouchStartX;
    private float mTouchStartY;
    //	private FpsTimerTask fpsTask = null;
    private String currentAction = "";
    private String rid = "";
    private String tid = "";

    @Override
    public void onCreate() {
        Log.d(TAG,
                "-------------------control service onCreate called!---------------------------");
        super.onCreate();
        if (TextUtils.isEmpty(Constants.DATA_URL)) {
            ToastUtil.ShowLongToast(this, Constants.GET_ADDRESS_WARNING);
            return;
        }
        url = Constants.DATA_URL
                + "/platform/MobileClientMonitor/MobileClientMonitorMaint.do";
        model = android.os.Build.MODEL;
        first = false;
        isServiceStop = false;
        isStop = false;
        // fps=new Fps();
        memoryInfo = new MemoryInfo();
        fomart = new DecimalFormat();
        fomart.setMaximumFractionDigits(2);
        fomart.setMinimumFractionDigits(0);
        batteryBroadcast = new BatteryInfoBroadcastReceiver();
        registerReceiver(batteryBroadcast, new IntentFilter(
                "android.intent.action.BATTERY_CHANGED"));
    }

    /**
     * 电池信息监控监听器
     *
     * @author andrewleo +
     */
    public class BatteryInfoBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                totalBatt = String.valueOf(level * 100 / scale);
                temperature = String.valueOf(intent.getIntExtra(
                        BatteryManager.EXTRA_TEMPERATURE, 0) * 1.0 / 10);
            }
        }
    }

    private void initData(Intent intent) {
        //intent没有数据就取消测试
        if (null == intent || null == intent.getExtras()) {
            stopSelf();
            ToastUtil.ShowLongToast(this, "传入数据有误，测试终止！");
            return;
        }

        Bundle extras = intent.getExtras();
        try {
            pid = extras.getInt("pid");
            Log.d(TAG, "===============before=================get pid is:"
                    + pid);
            if (pid == 0) {
                ProcessInfo processInfo = new ProcessInfo();
                pid = processInfo.getPid(this, uid, packageName);
                Log.d(TAG,
                        "===============after=================get pid is:"
                                + pid);
                if (pid == 0) {
                    stopSelf();
                    return;
                }
            }
        } catch (Exception e) {
            stopSelf();
            return;
        }
        uid = extras.getInt("uid");
        mark = extras.getString("mark");
        packageName = extras.getString("packageName");
        processName = extras.getString("processName");
        id = extras.getString("id");
        scriptStep = extras.getString("scriptStep");
        cpuInfo = new CpuInfo(getBaseContext(), pid, uid);
        delaytime = Integer.parseInt(time) * 1000;

        initPerformanceData();
        if (Contact.from.equals("0")) {
            tid = extras.getString("tid");
        }
    }

    private void initPerformanceData() {
        if (Contact.isShowFloatingWindow) {
            viFloatingWindow = LayoutInflater.from(this).inflate(
                    R.layout.floating, null);
            txtUnusedMem = (TextView) viFloatingWindow
                    .findViewById(R.id.memunused);
            txtTotalMem = (TextView) viFloatingWindow
                    .findViewById(R.id.memtotal);
            txtTraffic = (TextView) viFloatingWindow
                    .findViewById(R.id.traffic);
            txtFps = (TextView) viFloatingWindow
                    .findViewById(R.id.fps_data);

            loadActionListTv = (TextView) viFloatingWindow
                    .findViewById(R.id.tv_load);
            actionListSpinner = (Spinner) viFloatingWindow
                    .findViewById(R.id.sp_action_list);

            if (null != ActionDataUtil.responseActionBean) {
                // 此时表示从服务器获取的数据有值
                String result = ActionDataUtil.responseActionBean
                        .getResult();
                if (result.equals("success")) {
                    List<ActionBean> dataList = ActionDataUtil.responseActionBean
                            .getList();
                    if (dataList.size() > 0) {

                        loadActionListTv.setVisibility(View.GONE);
                        // 建立Adapter并且绑定数据源
                        BaseSpinnerAdpater adapter = new BaseSpinnerAdpater(
                                this, ActionDataUtil.actionListStr);
                        // 绑定 Adapter到控件
                        actionListSpinner.setAdapter(adapter);
                        actionListSpinner
                                .setOnItemSelectedListener(new OnItemSelectedListener() {

                                    @Override
                                    public void onItemSelected(
                                            AdapterView<?> arg0, View arg1,
                                            int pos, long arg3) {
                                        currentAction = ActionDataUtil.actionListStr
                                                .get(pos);
                                    }

                                    @Override
                                    public void onNothingSelected(
                                            AdapterView<?> arg0) {

                                    }
                                });
                        actionListSpinner.setDropDownVerticalOffset(30);

                    } else {
                        ToastUtil.ShowLongToast(controlservice.this,
                                "服务器上没有该项目的行为点");
                    }
                } else if (result.equals("failure")) {
                    ToastUtil.ShowLongToast(controlservice.this,
                            "服务器上没有该项目的行为点");
                } else {
                    ToastUtil.ShowLongToast(controlservice.this,
                            "服务器状态出现异常,值为:" + result);
                }
            } else {
                ToastUtil.ShowLongToast(controlservice.this,
                        "服务器连接出现异常，请查看");
            }

            exitWindow = (ImageView) viFloatingWindow
                    .findViewById(R.id.exit);
            stopAction = (ImageView) viFloatingWindow
                    .findViewById(R.id.stop_action);
            startAction = (ImageView) viFloatingWindow
                    .findViewById(R.id.start_action);
            takepic = (ImageView) viFloatingWindow.findViewById(R.id.photo);
            exitWindow.setOnClickListener(new OnClickListener() {

                public void onClick(View arg0) {
                    sendEndTestCommand();

//					if (fpsTask != null) {
//						fpsTask.stopCurrentTask();
//					}
                    // try {
                    // //关闭当前应用
                    // String cmd = "am force-stop " + packageName;
                    // Runtime.getRuntime().exec(cmd);
                    // } catch (Exception e) {
                    // Log.d(TAG, "error message is:" + e.getMessage());
                    // e.printStackTrace();
                    // }
                    // AppManageUtil.kill(packageName);
                }

            });
            stopAction.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    mark = "";
                    ToastUtil.ShowLongToast(SnailApplication.getContext(),
                            "停止当前行动点");
                }
            });
            startAction.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!"".equals(currentAction)) {
                        for (ActionBean currentBean : ActionDataUtil.responseActionDataList) {
                            if (currentBean.getName().equals(currentAction)) {
                                mark = currentBean.getCode();
                                break;
                            }
                        }
                        ToastUtil.ShowLongToast(
                                SnailApplication.getContext(), "添加当前行动点");
                    } else {
                        ToastUtil.ShowLongToast(
                                SnailApplication.getContext(), "当前无可添加行动点");
                    }
                }
            });

            takepic.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!checkIsScreenReady()) {
                        Log.d(TAG,
                                "--------------截图准备工作没有准备好---------------");
                        return;
                    }

                    startVirtual();
                    new Thread() {
                        public void run() {
                            try {
                                Thread.sleep(500);
                                Date date = new Date();
                                String imageName = mark + "+"
                                        + picNameFormat.format(date)
                                        + ".jpg";
                                if (TextUtils.isEmpty(test_screen_dir)) {
                                    boolean isFloderExists = checkFloderExists();
                                    if (isFloderExists) {
                                        nameImage = test_screen_dir + "/"
                                                + imageName;
                                    }
                                } else {
                                    nameImage = test_screen_dir + "/"
                                            + imageName;
                                }
                                startCapture();
                                stopVirtual();
                                tearDownMediaProjection();
                            } catch (Exception e) {
                                Log.d(TAG, "------------------出现异常，异常信息为:"
                                        + e.getMessage());
                            }
                        }

                        ;
                    }.start();
                }
            });
            txtUnusedMem.setText("计算中,请稍后...");
            txtUnusedMem.setTextColor(android.graphics.Color.RED);
            txtTotalMem.setTextColor(android.graphics.Color.RED);
            txtTraffic.setTextColor(android.graphics.Color.RED);
            // txtTraffic1.setTextColor(android.graphics.Color.RED);
            txtFps.setTextColor(android.graphics.Color.RED);
            result = (TextView) viFloatingWindow.findViewById(R.id.stop);
            result.setTextColor(android.graphics.Color.YELLOW);
            createFloatingWindow();
        }
        createResultCsv();
        initSendData();
        handler.postDelayed(task, 1000);
    }


    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "-------------------control service onStart called!---------------------------");
        initData(intent);
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                stopSelf();
            }
            try {
                pid = intent.getExtras().getInt("pid");
                Log.d(TAG, "===============before=================get pid is:"
                        + pid);
                if (pid == 0) {
                    ProcessInfo processInfo = new ProcessInfo();
                    pid = processInfo.getPid(this, uid, packageName);
                    Log.d(TAG,
                            "===============after=================get pid is:"
                                    + pid);
                    if (pid == 0) {
                        stopSelf();
                    }
                }
            } catch (Exception e) {
                stopSelf();
                Log.d(TAG, "exception is:" + e.getMessage());
            }
            id = intent.getExtras().getString("id");
            scriptStep = intent.getExtras().getString("scriptStep");
            cpuInfo = new CpuInfo(getBaseContext(), pid, uid);
            delaytime = Integer.parseInt(time) * 1000;
        }
    }

    private void createFloatingWindow() {
        SharedPreferences shared = getSharedPreferences("float_flag",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putInt("float", 1);
        editor.commit();
        windowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        wmParams = ((SnailApplication) getApplication()).getMywmParams();
        if(Constants.sdk > 25){
//            wmParams.type = WindowManager.LayoutParams.type_ap;
        }else{
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
//        wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        wmParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.format = 1;
        windowManager.addView(viFloatingWindow, wmParams);
        viFloatingWindow.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                x = event.getRawX();
                y = event.getRawY() - 25;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = x;
                        startY = y;
                        mTouchStartX = event.getX();
                        mTouchStartY = event.getY();
                        Log.d("startP", "startX" + mTouchStartX + "====startY"
                                + mTouchStartY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        updateViewPosition();
                        break;

                    case MotionEvent.ACTION_UP:
                        updateViewPosition();
                        // showImg();
                        mTouchStartX = mTouchStartY = 0;
                        break;
                }
                return true;
            }
        });
    }

    /**
     * write the test result to csv format report.
     */
    private void createResultCsv() {
        Log.d(TAG,
                "-------------------control service createResultCsv called!---------------------------");
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String mDateTime;
        if ((Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"))) {
            mDateTime = formatter.format(cal.getTime().getTime() + 8 * 60 * 60
                    * 1000);
        } else {
            mDateTime = formatter.format(cal.getTime().getTime());
        }
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            resultFilePath = android.os.Environment
                    .getExternalStorageDirectory()
                    + File.separator
                    + "zsdate_TestResult_" + mDateTime + ".txt";
        } else {
            resultFilePath = getBaseContext().getFilesDir().getPath()
                    + File.separator + "zsdate_TestResult_" + mDateTime
                    + ".txt";
        }
    }

    private Runnable task = new Runnable() {

        public void run() {
            if (!isServiceStop) {
                try {
                    dataRefreshNew();
                    if (Contact.isShowFloatingWindow) {
                        windowManager.updateViewLayout(viFloatingWindow,
                                wmParams);
                    }
                } catch (Exception e) {
                    Log.d(TAG,
                            "--------------------dataRefreshNew error message is:"
                                    + e.getMessage());
                }
                handler.postDelayed(this, delaytime);
            } else {
                Log.d(TAG, "--------------------control service is shutdown!");
                stopSelf();
            }
        }
    };

    /**
     * close all opened stream.
     */
    public static void closeOpenedStream() {
        try {
            if (bw != null) {
                bw.close();
            }
            if (osw != null)
                osw.close();
            if (out != null)
                out.close();
        } catch (Exception e) {
            Log.e("zhll", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(task);
        // handler.removeCallbacks(task);
        if (windowManager != null){
            Contact.isShowFloatingWindow = false;
            windowManager.removeView(viFloatingWindow);
        }
        closeOpenedStream();
        // replace the start time in file
        isStop = true;
        unregisterReceiver(batteryBroadcast);
        // Toast.makeText(this, "结果保存在:" + controlservice.resultFilePath,
        // Toast.LENGTH_LONG).show();
        super.onDestroy();
        stopForeground(true);
        Log.d(TAG, "==================监控结束===================");
    }

    private void updateViewPosition() {
        wmParams.x = (int) (x - mTouchStartX);
        wmParams.y = (int) (y - mTouchStartY);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * -----------------------------屏幕截图功能----------------------------
     */
    private String nameImage = null;

    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;

    private int windowWidth = 0;
    private int windowHeight = 0;
    private ImageReader mImageReader = null;

    private String dataString = "";
    private int screenOrientation = 0;
    private String test_screen_dir = "";
    private SimpleDateFormat picNameFormat = new SimpleDateFormat("HH-mm-ss");

    @TargetApi(21)
    public void startVirtual() {
        Log.d(TAG, "-----------------进入到startVirtual方法中---------------");
        windowWidth = Constants.takeshotScreenSize.y;
        windowHeight = Constants.takeshotScreenSize.x;
        screenOrientation = 0;

        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1,
                2); // ImageFormat.RGB_565

        Log.i(TAG, "---------------------image width is:" + windowWidth
                + "height is:" + windowHeight);

        if (mMediaProjection != null) {
            Log.i(TAG, "want to display virtual");
            virtualDisplay();
        } else {
            Log.i(TAG, "want to build mediaprojection and display virtual");
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    @TargetApi(21)
    public void setUpMediaProjection() {
        Log.d(TAG, "-----------------进入到setUpMediaProjection方法中---------------");
        mMediaProjection = Constants.takeshotMediaProjectionManager
                .getMediaProjection(Constants.takeshotResultCode,
                        Constants.takeshotResultData);
        Log.i(TAG, "mMediaProjection defined");
    }

    @TargetApi(21)
    private void virtualDisplay() {
        Log.d(TAG, "-----------------进入到virtualDisplay方法中---------------");
        try {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                    "screen-mirror", windowWidth, windowHeight,
                    Constants.takeshotScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
        } catch (Exception e) {
            Log.i(TAG, "exception message is:" + e.getMessage());
        }
        Log.i(TAG, "virtual displayed");
    }

    @TargetApi(21)
    private void startCapture() {
        Log.d(TAG, "-----------------进入到startCapture方法中---------------");
        Image image = mImageReader.acquireLatestImage();
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride,
                height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        Log.i(TAG, "image data captured,and name image is:" + nameImage);

        if (bitmap != null) {
            try {
                File fileImage = new File(nameImage);
                if (!fileImage.exists()) {
                    fileImage.createNewFile();
                    Log.i(TAG, "image file created");
                } else {
                    fileImage.delete();
                    fileImage.createNewFile();
                    Log.i(TAG, "image file recreated");
                }
                FileOutputStream out = new FileOutputStream(fileImage);
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    out.flush();
                    out.close();
                    Intent media = new Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(fileImage);
                    media.setData(contentUri);
                    sendBroadcast(media);
                    Log.i(TAG, "screen image saved");
                    ToastUtil.ShowLongToast(this, "截屏成功");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        final File imageFile = new File(nameImage);
        Log.d(TAG, "-----------------------------截图文件状态为:" + imageFile.exists()
                + "文件路径为:" + nameImage);
    }

    @TargetApi(21)
    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "mMediaProjection undefined");
    }

    @TargetApi(21)
    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        Log.i(TAG, "virtual display stopped");
    }

    /**
     * 为测试类建立对应的文件夹
     *
     * @return 是否写文件成功
     */
    private boolean checkFloderExists() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();
        String currentDateName = format.format(currentDate);
        String fileFolder = Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + File.separator
                + "snail_screen"
                + File.separator
                + currentDateName
                + File.separator
                + "performance" + File.separator + packageName;
        FileWriter fileWriter = null;

        try {
            boolean isFolderExists = FileUtil.checkFolderExists(fileFolder);
            Log.d(TAG, "-------writeFile------- folder state is:"
                    + isFolderExists);
            if (isFolderExists) {
                // 此時文件夹存在
                test_screen_dir = fileFolder;
                return true;
            } else {
                Log.d(TAG, "-------writeFile------- write file failed!");
                return false;
            }
        } catch (Exception e) {
            Log.d(TAG, "-------writeFile------- exception is:" + e.getMessage());
            return false;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    Log.d(TAG,
                            "-------writeFile------- close writer exception is:"
                                    + e.getMessage());
                }
            }
        }
    }

    /**
     * 判断当前的截图准备工作是否完成
     *
     * @return
     */
    private boolean checkIsScreenReady() {
        return (Constants.takeshotResultData != null)
                && (Constants.takeshotMediaProjectionManager != null)
                && (Constants.takeshotWindowManager != null)
                && (Constants.takeshotMediaProjectionManager != null)
                && (Constants.takeshotScreenSize != null)
                && (Constants.takeshotMetrics != null);
    }

//    private void sendBeginTestCommand() {
//        final List<Map<String, String>> beginTestData = new ArrayList<Map<String, String>>();
//        Map<String, String> dataFrom = new HashMap<String, String>();
//        Map<String, String> dataAct = new HashMap<String, String>();
//        Map<String, String> dataMacAddress = new HashMap<String, String>();
//        Map<String, String> dataProjectName = new HashMap<String, String>();
//        Map<String, String> testType = new HashMap<String, String>();
//
//        dataAct.put("key", "act");
//        dataAct.put("value", "beginTest");
//        beginTestData.add(dataAct);
//
//        dataFrom.put("key", "from");
//        dataFrom.put("value", Contact.from);
//        beginTestData.add(dataFrom);
//
//        dataMacAddress.put("key", "mac");
//        dataMacAddress.put("value", Contact.mac.trim());
//        beginTestData.add(dataMacAddress);
//
//        dataProjectName.put("key", "projectName");
//        dataProjectName.put("value", processName.trim());
//        beginTestData.add(dataProjectName);
//
//        testType.put("key", "testType");
//        testType.put("value", Contact.testType);
//        beginTestData.add(testType);
//        Log.e(TAG, "=======begin test data =======url is:" + url
//                + "===send data is:" + beginTestData.toString());
//
//        new Thread() {
//            public void run() {
//                String beginTestResult = HttpUtilForWired.getInstance().sendData(url,
//                        beginTestData);
//                Log.e(TAG, " begin test result is:" + beginTestResult);
//                try {
//                    JSONObject resultData = new JSONObject(beginTestResult);
//                    String result = resultData.getString("result");
//                    if (result.equals("success")) {
//                        tid = resultData.getString("tid");
//                        Log.e(TAG, "tid is:" + tid);
//                        handler.sendEmptyMessage(MSG_BEGIN_TEST_SUCCESS);
//                    } else {
//                        String desc = resultData.getString("desc");
//                        Message currentMsg = Message.obtain();
//                        currentMsg.what = MSG_BEGIN_TEST_FAILURE;
//                        currentMsg.obj = desc;
//                        handler.sendMessage(currentMsg);
//                    }
//
//                } catch (Exception e) {
//                    Log.d(TAG, "exception is:" + beginTestResult);
//                    Message currentMsg = Message.obtain();
//                    currentMsg.what = MSG_BEGIN_TEST_FAILURE;
//                    currentMsg.obj = beginTestResult;
//                    handler.sendMessage(currentMsg);
//                }
//            }
//
//            ;
//        }.start();
//    }

    private void sendEndTestCommand() {
        final List<Map<String, String>> endTestData = new ArrayList<Map<String, String>>();
        Map<String, String> endFrom = new HashMap<String, String>();
        Map<String, String> endTid = new HashMap<String, String>();
        Map<String, String> endAct = new HashMap<String, String>();
        Map<String, String> endMac = new HashMap<String, String>();

        endAct.put("key", "act");
        endAct.put("value", "endTest");
        endTestData.add(endAct);

        endFrom.put("key", "from");
        endFrom.put("value", Contact.from);
        endTestData.add(endFrom);

        endTid.put("key", "tid");
        endTid.put("value", tid);
        endTestData.add(endTid);

        endMac.put("key", "mac");
        endMac.put("value", Contact.mac.trim());
        endTestData.add(endMac);

        Log.e(TAG, "=======end test data =======url is:" + url
                + "===send data is:" + endTestData.toString());
        new Thread() {
            public void run() {
                String endTestResult = HttpUtilForWired.getInstance().sendData(
                        url, endTestData);
                Log.e(TAG, "end test result is:" + endTestResult);

                try {
                    JSONObject resultData = new JSONObject(endTestResult);
                    String result = resultData.getString("result");
                    if (result.equals("success")) {
                        handler.sendEmptyMessage(MSG_END_TEST_SUCCESS);
                    } else {
                        String desc = resultData.getString("desc");
                        Message currentMsg = Message.obtain();
                        currentMsg.what = MSG_BEGIN_TEST_FAILURE;
                        currentMsg.obj = desc;
                        handler.sendMessage(currentMsg);
                    }

                } catch (Exception e) {
                    Log.d(TAG, "exception is:" + endTestResult);
                    Message currentMsg = Message.obtain();
                    currentMsg.what = MSG_BEGIN_TEST_FAILURE;
                    currentMsg.obj = endTestResult;
                    handler.sendMessage(currentMsg);
                }

            }
        }.start();
    }

    private List<Map<String, String>> sendDataList;
    private Map<String, String> dataFrom;
    private Map<String, String> dataTestType;
    private Map<String, String> dataModel;
    private Map<String, String> dataAct;
    private Map<String, String> dataMacAddress;
    private Map<String, String> dataProjectName;
    private Map<String, String> dataCpu;
    private Map<String, String> dataCpuAll;
    private Map<String, String> dataMemory;
    private Map<String, String> dataMemoryFree;
    private Map<String, String> dataFps;
    private Map<String, String> dataTemperature;
    private Map<String, String> dataElectricity;
    private Map<String, String> dataFluxUp;
    private Map<String, String> dataFluxDown;
    private Map<String, String> dataTime;
    private Map<String, String> dataBehavior;
    private Map<String, String> dataId;
    private Map<String, String> dataTid;
    private Map<String, String> dataScriptStep;

    private void initSendData() {
        Log.d(TAG,
                "-------------------control service initSendData called!---------------------------");
        sendDataList = new ArrayList<Map<String, String>>();

        dataFrom = new HashMap<String, String>();
        dataTestType = new HashMap<String, String>();
        dataModel = new HashMap<String, String>();
        dataAct = new HashMap<String, String>();
        dataMacAddress = new HashMap<String, String>();
        dataProjectName = new HashMap<String, String>();
        dataCpu = new HashMap<String, String>();
        dataCpuAll = new HashMap<String, String>();
        dataMemory = new HashMap<String, String>();
        dataMemoryFree = new HashMap<String, String>();
        dataFps = new HashMap<String, String>();
        dataTemperature = new HashMap<String, String>();
        dataElectricity = new HashMap<String, String>();
        dataFluxUp = new HashMap<String, String>();
        dataFluxDown = new HashMap<String, String>();
        dataTime = new HashMap<String, String>();
        dataBehavior = new HashMap<String, String>();
        dataId = new HashMap<String, String>();
        dataTid = new HashMap<String, String>();
        dataScriptStep = new HashMap<String, String>();

        dataFps.put("key", "fps");
        dataFps.put("value", "0");
        sendDataList.add(dataFps);

        dataAct.put("key", "act");
        dataAct.put("value", "add");
        sendDataList.add(dataAct);

        dataFrom.put("key", "from");
        dataFrom.put("value", Contact.from);
        sendDataList.add(dataFrom);

        dataTestType.put("key", "testType");
        dataTestType.put("value", Contact.testType);
        sendDataList.add(dataTestType);

        dataModel.put("key", "model");
        dataModel.put("value", model.trim());
        sendDataList.add(dataModel);

        dataMacAddress.put("key", "mac");
        dataMacAddress.put("value", Contact.mac.trim());
        sendDataList.add(dataMacAddress);

        dataProjectName.put("key", "projectName");
        dataProjectName.put("value", processName.trim());
        sendDataList.add(dataProjectName);

        dataBehavior.put("key", "behavior");
        dataCpu.put("key", "cpu");
        dataCpuAll.put("key", "cpuAll");
        dataMemory.put("key", "memory");
        dataMemoryFree.put("key", "memoryFree");
        dataTemperature.put("key", "temperature");
        dataElectricity.put("key", "electricity");
        dataFluxUp.put("key", "fluxUp");
        dataFluxDown.put("key", "fluxDown");
        dataTime.put("key", "time");
        dataScriptStep.put("key", "scriptStep");
    }

    private void dataRefreshNew() throws IOException {
        Log.d(TAG,
                "-------------------control service dataRefreshNew start!---------------------------");
        int pidMemory = memoryInfo.getPidMemorySize(pid, getBaseContext());
        long freeMemory = memoryInfo.getFreeMemorySize(getBaseContext());
        ArrayList<String> processInfo = cpuInfo.getCpuRatioInfo(totalBatt,
                temperature);
        String processCpuRatio = "0";
        String totalCpuRatio = "0";
        String sendtrafficSize = "0";
        String recivetrafficSize = "0";
        if (!processInfo.isEmpty()) {
            Log.d(TAG,
                    "-------------------control service dataRefreshNew start01!---------------------------");
            processCpuRatio = processInfo.get(0);
            totalCpuRatio = processInfo.get(1);
            sendtrafficSize = processInfo.get(2);
            recivetrafficSize = processInfo.get(3);
            String dtime = processInfo.get(4);
            if ((pidMemory == 0) && "0.00".equals(processCpuRatio)) {
                // closeOpenedStream();
                isServiceStop = true;
                return;
            }
            if (Contact.isShowFloatingWindow) {
                if (processCpuRatio != null && totalCpuRatio != null) {
                    txtUnusedMem.setText("应用/剩余内存:"
                            + fomart.format((double) (pidMemory / 1024)) + "/"
                            + fomart.format((double) (freeMemory / 1024))
                            + "MB");
                    txtTotalMem.setText("应用/总体CPU:" + processCpuRatio + "%/"
                            + totalCpuRatio + "%");
                    String batt = "电量:" + totalBatt + "%";
                    if (GTFrameUtils.isHasSu()) {
                        txtFps.setText("fps:" + Constants.fps);
                    } else {
                        txtFps.setVisibility(View.GONE);
                    }
                    txtTraffic.setText(batt);
                    if (SendHttp.result.contains("result")) {
                        try {
                            Log.d(TAG, "result is:" + SendHttp.result);
                            JSONObject resultData = new JSONObject(
                                    SendHttp.result);
                            String resultStatus = resultData
                                    .getString("result");
                            result.setText("数据发送结果: " + resultStatus);
                        } catch (JSONException e) {
                            Log.e(TAG, "error is:" + e.getMessage());
                        }
                    } else {
                        result.setText(SendHttp.result);
                    }
                    // SendHttp.result = "数据交互中...";
                    // } else if (isMb)
                    // txtTraffic.setText(batt + ",流量:" +
                    // fomart.format(trafficMb) + "MB");
                    // else
                    // txtTraffic.setText(batt + ",流量:" + trafficSize + "KB");
                }
            }
            if (first) {
                Log.d(TAG,
                        "-------------------control service dataRefreshNew start02!---------------------------");
                first = false;
            } else {
                Log.d(TAG,
                        "-------------------control service dataRefreshNew start03!---------------------------");
                if (Contact.mac == null) {
                    SharedPreferences userInfo = getSharedPreferences(
                            "user_info", 0);
                    SharedPreferences.Editor edt = userInfo.edit();
                    Contact.mac = userInfo.getString("mac", "");
                }
                if (processName != null) {
                    Log.d(TAG,
                            "-------------------control service dataRefreshNew start04!---------------------------");

                    dataBehavior.put("value", mark.trim());
                    sendDataList.add(dataBehavior);
                    dataCpu.put("value", processCpuRatio.trim());
                    sendDataList.add(dataCpu);
                    dataCpuAll.put("value", totalCpuRatio.trim());
                    sendDataList.add(dataCpuAll);
                    dataMemory.put("value", String.valueOf(pidMemory).trim());
                    sendDataList.add(dataMemory);
                    dataMemoryFree.put("value", String.valueOf(freeMemory)
                            .trim());
                    sendDataList.add(dataMemoryFree);
                    dataTemperature.put("value", temperature.trim());
                    sendDataList.add(dataTemperature);
                    dataElectricity.put("value", totalBatt.trim());
                    sendDataList.add(dataElectricity);
                    dataFluxUp.put("value", sendtrafficSize.trim());
                    sendDataList.add(dataFluxUp);
                    dataFluxDown.put("value", recivetrafficSize.trim());
                    sendDataList.add(dataFluxDown);
                    dataTime.put("value", dtime.trim());
                    sendDataList.add(dataTime);
                    dataScriptStep.put("value", scriptStep);
                    sendDataList.add(dataScriptStep);
                    if (id != null) {
                        String[] ids = id.split(",");
                        rid = ids[0];
                        tid = ids[1];
                        dataId.put("key", "rid");
                        dataId.put("value", rid);
                        dataTid.put("key", "tid");
                        dataTid.put("value", tid);
                        sendDataList.add(dataId);
                        sendDataList.add(dataTid);
                    } else {
                        dataTid.put("key", "tid");
                        dataTid.put("value", tid);
                        sendDataList.add(dataTid);
                    }
                    // bw.write(dtime.trim() + "   "
                    // + String.valueOf(pidMemory).trim() + "KB   "
                    // + String.valueOf(freeMemory).trim() + "KB   "
                    // + processCpuRatio.trim() + "%   "
                    // + totalCpuRatio.trim() + "%   "
                    // + sendtrafficSize.trim() + "KB   "
                    // + recivetrafficSize.trim() + "KB   "
                    // + totalBatt.trim() + "%   " + temperature.trim()
                    // + "℃\r\n");
                    new Thread() {
                        public void run() {
                            SendHttp.result = "数据交互中...";
                            Log.e(TAG, "send url is:" + url + "send data is:"
                                    + sendDataList.toString());
                            SendHttp.result = HttpUtilForWired.getInstance()
                                    .sendData(url, sendDataList);
                            Log.e(TAG, "result is:" + SendHttp.result);
                        }
                    }.start();
                }
            }
            // 当内存为0切cpu使用率为0时则是被测应用退出
        }
    }
}
