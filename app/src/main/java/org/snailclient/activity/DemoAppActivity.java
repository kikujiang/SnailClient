/*
 * Copyright (C) 2010 Moduad Co., Ltd.
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
package org.snailclient.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.androidpn.demoapp.AllTestListActivity;
import org.androidpn.demoapp.OaBugActivity;
import org.androidpn.demoapp.R;
import org.androidpn.demoapp.SubmitLaunchResultActivity;
import org.json.JSONException;
import org.json.JSONObject;
import org.snailclient.activity.utils.ConnectStatus;
import org.snailclient.activity.utils.Login2Activity;
import org.snailclient.activity.utils.fps.GTFrameUtils;
import org.snailclient.activity.utils.fps.GenericToast;
import org.snailclient.activity.utils.fps.ProcessUtils;
import org.snailclient.activity.utils.fps.ToastUtil;

import rx.functions.Action1;
import solo.BackService;
import solo.FileUtil;
import solo.HttpUtilForWired;
import solo.IBackService;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.monitor.util.Contact;
import com.monitor.util.CustomProgressDialog;
import com.monitor.util.ProcessInfo;
import com.snail.service.SocketService;
import com.snail.util.Constants;
import com.snailgame.sdkcore.aas.logic.LoginDispatcher;
import com.tbruyelle.rxpermissions.Permission;
import com.tbruyelle.rxpermissions.RxPermissions;

/**
 * 类的描述：主界面
 *
 * @author zhll
 * @Time 2015-07-08
 */
public class DemoAppActivity extends AppCompatActivity {
    public static final String TAG = "test";
    public static final int Request_LOGIN_CODE = 10002;
    public static Context context;
    private MenuItem itemUserInfo;
    private MenuItem userState;
    int from = 0;
    // private Properties props;
    Intent intent;
    Intent resource;
    int testType = 0;
    Handler myhandler;
    PackageInfo pkg = null;
    // private updateManager mUpdateManager;
    CustomProgressDialog dialog;
    Button page;
    Button creatBug;
    Button allTest;
    Button LaunchTest;
    Button srp;
    Button info;
    LoginDispatcher dispatcher;
    TelephonyManager tel;
    // NetworkConnectChangedReceiver receiver;

    private int REQUEST_MEDIA_PROJECTION = 1;
    // private MediaProjectionManager mMediaProjectionManager;

    private IBackService iBackService;
    private ServiceConnection conn = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            iBackService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            iBackService = IBackService.Stub.asInterface(service);
        }
    };
    private Intent mServiceIntent;

    Intent screenIntent = null;
    private int version = 0;

    public static boolean isScreen = false;

    Thread screenThread = null;

    @Override
    @TargetApi(21)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constants.versionCode = android.os.Build.VERSION.SDK_INT;
        if(Constants.versionCode > 22){
            checkPermission();
        }else{
            initStart();
        }

    }
    @TargetApi(21)
    private void initStart(){
        ActionBar actionBar = getSupportActionBar();
//		actionBar.setLogo(R.drawable.logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        context = this;
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }
        Constants.appVersionCode = pi.versionCode;

        init();

        System.setProperty("http.keepAlive", "false");
        setContentView(R.layout.main);
        tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        dispatcher = LoginDispatcher.getInstance();
        dispatcher.setContext(getApplicationContext());
        dialog = new CustomProgressDialog(this, "服务器处理中", R.anim.frame);
        page = (Button) findViewById(R.id.page);
        creatBug = (Button) findViewById(R.id.creatBug);
        allTest = (Button) findViewById(R.id.allTest);
        info = (Button) findViewById(R.id.info);
        LaunchTest = (Button) findViewById(R.id.launchTest);
        srp = (Button) findViewById(R.id.srp);
        page.setOnClickListener(new controlClickListener());
        creatBug.setOnClickListener(new controlClickListener());
        allTest.setOnClickListener(new controlClickListener());
        info.setOnClickListener(new controlClickListener());
        LaunchTest.setOnClickListener(new controlClickListener());
        srp.setOnClickListener(new controlClickListener());
        myhandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        dialog.cancel();
                        String value = (String) msg.obj;
                        Intent intent = new Intent(DemoAppActivity.this,
                                AllTestListActivity.class);
                        intent.putExtra("list", value);
                        Constants.islaunch = true;
                        startActivity(intent);
                        break;
                    case 10086:
                        String result = msg.obj.toString();
                        if(result != null && !"".equals(result)){
                            try {
                                JSONObject resultJson = new JSONObject(result);
                                String status = resultJson.getString("result");
                                String desc = resultJson.getString("desc");
                                if(status.equals("success")){
                                    itemUserInfo.setTitle("未登录");
                                    userState.setTitle("登录");
                                    Contact.userName = null;
                                    ToastUtil.ShowLongToast(DemoAppActivity.this,"注销成功");
                                }else{
                                    ToastUtil.ShowLongToast(DemoAppActivity.this,desc);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 9527:
                        String checkResult = msg.obj.toString();
                        if(checkResult != null && !"".equals(checkResult)){
                            try {
                                JSONObject resultJson = new JSONObject(checkResult);
                                String desc = resultJson.getString("desc");

                                if(!desc.equals("")){
                                    Contact.userName = desc;
                                    itemUserInfo.setTitle(Contact.userName);
                                    userState.setTitle("注销");
                                }else{
                                    itemUserInfo.setTitle("未登录");
                                    userState.setTitle("登录");
                                    Contact.userName = null;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
        try {
            pkg = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        mServiceIntent = new Intent(this, BackService.class);

        bindService(mServiceIntent, conn, BIND_AUTO_CREATE);

        Intent monitorService = new Intent();
        monitorService.setClass(DemoAppActivity.this, SocketService.class);
        startService(monitorService);

        if (Constants.versionCode > 20) {
            screenThread = new Thread() {
                public void run() {
                    while (true) {
                        if (isScreen) {
                            Constants.takeshotMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                            startActivityForResult(
                                    Constants.takeshotMediaProjectionManager
                                            .createScreenCaptureIntent(),
                                    REQUEST_MEDIA_PROJECTION);
                            break;
                        }
                    }
                }
            };
            screenThread.start();
        }
        initMacAddress();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    class controlClickListener implements OnClickListener {
        public void onClick(View arg0) {
            if (arg0 == srp) {
                intent = new Intent(DemoAppActivity.this,
                        SubmitLaunchResultActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//				intent.putExtra("packageName", Contact.packageName);
//				intent.putExtra("id", Contact.testID);
                startActivity(intent);
            } else if (arg0 == LaunchTest) {
                Intent intent = new Intent(DemoAppActivity.this,
                        AllTestListActivity.class);
                intent.putExtra("list", "LaunchTest");
                Constants.islaunch = true;
                startActivity(intent);
            } else if (arg0 == allTest) {
                Intent intent = new Intent(DemoAppActivity.this,
                        AllTestListActivity.class);
                intent.putExtra("list", "allTest");
                startActivity(intent);
            } else if (arg0 == creatBug) {
                intent = new Intent(DemoAppActivity.this, OaBugActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intent.putExtra("id", Contact.testID);
                startActivity(intent);
            } else if (arg0 == page) {
                intent = new Intent();
                intent.setAction("android.intent.action.BROADCAST");
                intent.setPackage("com.snail.receiver");
                intent.putExtra("msg", "000");
                sendBroadcast(intent);
                Intent intn = new Intent(DemoAppActivity.this,
                        MainPageActivity.class);
                intn.setClass(DemoAppActivity.this, MainPageActivity.class);
                intn.putExtra("from", String.valueOf(from));
                intn.putExtra("testType", String.valueOf(testType));
                startActivity(intn);
            } else if (arg0 == info) {
                startActivity(new Intent(DemoAppActivity.this,
                        GetGpuInfoActivity.class));
            }
        }
    }

    public void init() {
        getAddress();
    }

    private void saveData(){
        SharedPreferences userInfo = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = userInfo.edit();
        if (Contact.mac != null) {
            edit.putString("mac", Contact.mac);
        }else{
            edit.remove("mac");
        }
        if(Contact.userName != null){
            edit.putString("username",Contact.userName);
        }else{
            edit.remove("username");
        }
        edit.apply();
    }

    private void loadData(){
        SharedPreferences userInfo = getSharedPreferences("user_info",Context.MODE_PRIVATE);
        Contact.mac = userInfo.getString("mac",null);
        Contact.userName = userInfo.getString("username",null);
    }

    private void checkUserStatus(){
        Log.e(TAG, "==================checkUserStatus: called" );
        if(Contact.mac != null) {
            new Thread() {
                @Override
                public void run() {
                    String url = Constants.DATA_URL + "/userLogin.do";
                    String sendData = "act,checkLoginStatusFromClient,mac," + Contact.mac;
                    String result = HttpUtilForWired.getInstance().sendData2Web(url, sendData);
                    Log.d(TAG, "------------result is:" + result);
                    Message msg = Message.obtain();
                    msg.obj = result;
                    msg.what = 9527;
                    myhandler.sendMessage(msg);
                }
            }.start();
        }
    }

    private void initMacAddress(){
        loadData();
        if(Contact.mac != null){
            Log.d(TAG, "current mac is:" + Contact.mac);
            return;
        }

        int currentSDK = Build.VERSION.SDK_INT;
        Log.d(TAG, "currentSDK is:" + currentSDK);
        if (currentSDK > 22) {
            Contact.mac = getMacAddress();
            if (Contact.mac != null) {
                final String macFilePath = Environment.getExternalStorageDirectory() + "/mac";
                new Thread() {
                    @Override
                    public void run() {
                        String mac = FileUtil.readFile(macFilePath);
                        if (mac == null) {
                            FileUtil.writeFile(macFilePath, Contact.mac + "," + Build.VERSION.SDK_INT);
                        }
                    }
                }.start();
            }
        } else {
            try {
                Contact.mac = readMacInfo();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                Log.d(TAG, "get mac error,error is:" + e1.getMessage());
            }
        }
    }

    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface
                    .getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0"))
                    continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    String num = Integer.toHexString(b & 0xFF);
                    if (num.length() == 1) {
                        num = "0" + num;
                    }
                    res1.append(num + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "";
    }

    public String readMacInfo() throws InterruptedException {
        String mac = null;
        String cmd = null;
        String cmd1 = null;
        String re = "^([0-9a-fA-F]{2})(([/\\s:][0-9a-fA-F]{2}){5})$";
        cmd = "cat /sys/class/net/eth0/address";
        cmd1 = "cat /sys/class/net/wlan0/address";
        mac = mac(cmd1);
        if (mac != null) {
            if (!mac.matches(re)) {
                mac = mac(cmd);
            }
        }
        return mac;
    }

    private String mac(String cmd) {
        String ma = null;
        try {
            Process pro = Runtime.getRuntime().exec(cmd);
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    pro.getInputStream()));
            String line = bf.readLine();
            if (line != null) {
                if (line.contains(":")) {
                    ma = line.substring(0, 17);
                }
                return ma;
            }
            bf.close();
        } catch (IOException e) {
            return ma;
        }
        return ma;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e(TAG, "============onCreateOptionsMenu: called");
        getMenuInflater().inflate(R.menu.main, menu);
        itemUserInfo = menu.findItem(R.id.action_userName);
        userState = menu.findItem(R.id.user_state);
        checkUserStatus();
        return true;
    }

    private void checkPermission(){
        ProcessInfo info = new ProcessInfo();
        info.getRunningProcess(this);
        RxPermissions.getInstance(this).request(Manifest.permission.SEND_SMS,Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean isGranted) {
                if(!isGranted){
                    ToastUtil.ShowLongToast(DemoAppActivity.this,"请确认权限开启，获取不到权限将无法使用工具");
                }else {
                    initStart();
                }
            }
        });
    }

    @TargetApi(21)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_state:
                if (userState.getTitle().equals("登录")) {
                    Intent loginIntent = new Intent(DemoAppActivity.this, Login2Activity.class);
                    startActivityForResult(loginIntent, Request_LOGIN_CODE);
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            String url = Constants.DATA_URL + "/userLogin.do";
                            String sendData = "act,logoutFromClient,mac," + Contact.mac;
                            String result = HttpUtilForWired.getInstance().sendData2Web(url, sendData);
                            Log.d(TAG, "------------result is:" + result);
                            Message msg = Message.obtain();
                            msg.obj = result;
                            msg.what = 10086;
                            myhandler.sendMessage(msg);
                        }
                    }.start();
                }
                return true;
            case R.id.update:
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            String lastestVersion = msg.obj.toString().trim();
                            version = Integer.parseInt(lastestVersion.trim());
                            if (version > Constants.appVersionCode) {
                                updateManager update = new updateManager(
                                        DemoAppActivity.this, Constants.APK_URL);
                                update.checkUpdateInfo();
                            } else {
                                ToastUtil.ShowLongToast(context, "当前已是最新版本");
                            }
                        }
                    }
                };
                new VersionThread(handler).start();

                return true;
            case R.id.info:
                startActivity(new Intent(DemoAppActivity.this,
                        UploadDataActivity.class));
                return true;
            case R.id.action_settings:
                isScreen = true;
                return true;
            case R.id.get_address:
                getAddress();
                if (!TextUtils.isEmpty(Constants.DATA_URL)) {
                    ToastUtil.ShowLongToast(this, "连接成功");
                }
                return true;
//            case R.id.submit:
//                Intent intent = new Intent(DemoAppActivity.this,SubmitPerformanceResult.class);
//                startActivity(intent);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getAddress() {
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        Map<String, String> addressData = new HashMap<String, String>();
        addressData.put("key", "act");
        addressData.put("value", "getResUrl");
        data.add(addressData);
        HttpUtilForWired.getInstance().sendDatatoWeb(Constants.GET_URL_ADDRESS,
                data);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy method called");
        saveData();
        super.onDestroy();
        // 通知总控制器连接断开
        ConnectStatus.setConnectStatus("false");
        // 对发送的心跳的服务进行释放
        unbindService(conn);
        if (screenIntent != null) {
            stopService(screenIntent);
        }
        if (screenThread != null) {
            screenThread = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != AppCompatActivity.RESULT_OK) {
                return;
            } else if (data != null && resultCode != 0) {
                Log.i(TAG, "user agree the application to capture screen");
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
                GenericToast.makeText(context, "启动截屏服务成功", 500).show();
            }
        } else if (requestCode == Request_LOGIN_CODE) {
            if (resultCode == 2 && data != null) {
                String userName = data.getStringExtra("userName");
                Contact.userName = userName;
                itemUserInfo.setTitle(userName);
                userState.setTitle("注销");
            }
        }
    }
}