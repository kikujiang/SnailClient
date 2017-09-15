package com.snail.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.androidpn.demoapp.R;
import org.androidpn.demoapp.SubmitLaunchResultActivity;
import org.snailclient.activity.utils.action.ActionBean;
import org.snailclient.activity.utils.action.ActionDataUtil;
import org.snailclient.activity.utils.fps.ToastUtil;

import solo.FileUtil;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.monitor.util.MemoryInfo;
import com.monitor.util.ProcessInfo;
import com.snail.adapter.BaseSpinnerAdpater;
import com.snail.util.Constants;
import com.snail.util.SnailApplication;

/**
 * 
 * 类的描述：一个飘浮窗口 在通过测试报告打开应用时会显示该飘浮窗口 通过飘浮窗口可以打开提交测试报告界面
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class SubmitReportService extends Service implements OnClickListener {
	
	private static final String TAG = "SubmitReportService";
	
	private WindowManager windowManager;
	private WindowManager.LayoutParams wmParams;
	private float x;
	private float y;
	private float mTouchStartX;
	private float mTouchStartY;
	private View viFloatingWindow;
	String packageName = null;
	String id = null;
	Intent intent;

	private ImageView imgStartAction;
	private ImageView imgTakeShot;
	private ImageView imgExit;
	private Spinner spinnerActionList;
	private TextView tvLoading;
	private TextView tvMemory;
	private static String mark = "start01";
	private int pid;
	private String processName = "";
	private DecimalFormat fomart;
	private MemoryInfo memoryInfo;
	private Handler handler = new Handler();
	private boolean isServiceStop = false;
	
	/**
	 * -----------------------------屏幕截图功能----------------------------
	 *
	 */
    private String nameImage = null;

    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;

    private int windowWidth = 0;
    private int windowHeight = 0;
    private ImageReader mImageReader = null;
	
    private String dataString = "";
    private String test_screen_dir = "";
    private SimpleDateFormat picNameFormat = new SimpleDateFormat("HH-mm-ss");
    
    private ActionBean currentAction = null;
    
	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "---------SubmitReportService onBind called!");
		return null;
	}

	private void initFloatingView(){
		Log.d(TAG, "---------SubmitReportService initFloatingView called!");
		viFloatingWindow = LayoutInflater.from(this).inflate(
				R.layout.submitresult, null);
		
		imgStartAction = (ImageView) viFloatingWindow
				.findViewById(R.id.start_action);
		imgTakeShot = (ImageView) viFloatingWindow.findViewById(R.id.photo);
		imgExit = (ImageView) viFloatingWindow.findViewById(R.id.exit);
		spinnerActionList = (Spinner) viFloatingWindow.findViewById(R.id.action_list);
		tvLoading = (TextView) viFloatingWindow.findViewById(R.id.tv_load);
		tvMemory = (TextView) viFloatingWindow.findViewById(R.id.memunused);
		tvMemory.setTextColor(Color.RED);

		imgStartAction.setOnClickListener(this);
		imgTakeShot.setOnClickListener(this);
		imgExit.setOnClickListener(this);
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			tvLoading.setVisibility(View.GONE);
			currentAction = ActionDataUtil.responseActionDataList.get(0);
			BaseSpinnerAdpater adapter = new BaseSpinnerAdpater(
					SubmitReportService.this, ActionDataUtil.actionListStr);
			// 绑定 Adapter到控件
			spinnerActionList.setAdapter(adapter);
			spinnerActionList
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(
								AdapterView<?> arg0, View arg1,
								int pos, long arg3) {
							currentAction = ActionDataUtil.responseActionDataList.get(pos);
						}

						@Override
						public void onNothingSelected(
								AdapterView<?> arg0) {

						}
					});
			spinnerActionList.setDropDownVerticalOffset(30);
		};
	};
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.start_action:
			mark = currentAction.getCode();
			ToastUtil.ShowLongToast(
			SnailApplication.getContext(), "添加当前行动点");
			break;
		case R.id.photo:
			if(!checkIsScreenReady()){
				Log.d(TAG, "--------------截图准备工作没有准备好---------------");
				return;
			}
			
			startVirtual();
	        new Thread(){
	        	public void run() {
	        		try {
						Thread.sleep(500);
						Date date = new Date();
						String imageName = mark + "+" +picNameFormat.format(date)+ ".jpg";
						if(TextUtils.isEmpty(test_screen_dir)){
							boolean isFloderExists = checkFloderExists();
							if(isFloderExists){
								nameImage = test_screen_dir + "/" + imageName ;
							}
						}else{
							nameImage = test_screen_dir +"/"+ imageName;
						}
						startCapture();
						stopVirtual();
						tearDownMediaProjection();
					} catch (Exception e) {
						Log.d(TAG, "------------------出现异常，异常信息为:" + e.getMessage());
					}
	        	};
	        }.start();
			break;
		case R.id.exit:
			if (windowManager != null) {
				stopSelf();
				Constants.endTestTime = new Date();
				windowManager.removeView(viFloatingWindow);
				intent = new Intent(getBaseContext(),
						SubmitLaunchResultActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				intent.putExtra("packageName", packageName);
				intent.putExtra("id", id);
				intent.putExtra("imagePath", test_screen_dir);
				getApplication().startActivity(intent);
			}
			break;
		}
	}
	

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "---------SubmitReportService onStart called!");
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras == null) {
				Log.e(TAG, "No extras provided");
				return;
			}
			packageName = intent.getExtras().getString("packageName");
			processName = intent.getExtras().getString("processName");
			if(pid == 0){
				pid = intent.getExtras().getInt("pid");
				int uid = intent.getExtras().getInt("");
				ProcessInfo processInfo = new ProcessInfo();
				pid = processInfo.getPid(getBaseContext(), uid, packageName);
			}
			
			id = intent.getExtras().getString("id");
			isServiceStop = false;
			if(!"".equals(processName)){
				new Thread(){
					public void run() {
						ActionDataUtil.getInstance().getActionDataFromServer(processName);
						mHandler.sendEmptyMessage(0);
					};
				}.start();
			}
			memoryInfo = new MemoryInfo();
			fomart = new DecimalFormat();
			fomart.setMaximumFractionDigits(2);
			fomart.setMinimumFractionDigits(0);
			createFloatingWindow();
			if(pid != 0){
				handler.postDelayed(task, 1000);
			}else{
				
			}
		}
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "---------SubmitReportService destory called!");
		super.onDestroy();
		if(pid != 0){
			handler.removeCallbacks(task);
		}
//		windowManager.removeView(viFloatingWindow);
		stopForeground(true);
	}

	private void createFloatingWindow() {
		Log.d(TAG, "---------SubmitReportService createFloatingWindow called!");
        initFloatingView();
		
		SharedPreferences shared = getSharedPreferences("float_flag",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		editor.putInt("float", 1);
		editor.commit();
		windowManager = (WindowManager) getApplicationContext()
				.getSystemService("window");
		wmParams = ((SnailApplication) getApplication()).getMywmParams();
		wmParams.type = 2002;
		wmParams.flags |= 8;
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
				case MotionEvent.ACTION_MOVE:
					updateViewPosition();
					break;

				default:
					wmParams.x = (int) Math.abs(event.getRawX()
							- viFloatingWindow.getWidth() / 2);
					// 25为状态栏高度
					wmParams.y = (int) Math.abs(event.getRawY()
							- viFloatingWindow.getHeight() / 2 - 40);
					windowManager.updateViewLayout(viFloatingWindow, wmParams);
					break;

				}
				return false;
			}
		});
	}

	   /**
     * 判断当前的截图准备工作是否完成
     * @return
     */
    private boolean checkIsScreenReady(){
    	return (Constants.takeshotResultData != null) && 
    		   (Constants.takeshotMediaProjectionManager != null) && 
    		   (Constants.takeshotWindowManager != null) && 
    		   (Constants.takeshotMediaProjectionManager != null)&& 
    		   (Constants.takeshotScreenSize != null)&& 
    		   (Constants.takeshotMetrics != null); 
    }
    
    @TargetApi(21)
    public void startVirtual(){
		Log.d(TAG, "-----------------进入到startVirtual方法中---------------");
    	windowWidth = Constants.takeshotScreenSize.y;
    	windowHeight = Constants.takeshotScreenSize.x;

        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2); //ImageFormat.RGB_565

        Log.i(TAG, "---------------------image width is:"+ windowWidth + "height is:" + windowHeight);
        
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
    public void setUpMediaProjection(){
    	Log.d(TAG, "-----------------进入到setUpMediaProjection方法中---------------");
        mMediaProjection = Constants.takeshotMediaProjectionManager.getMediaProjection(Constants.takeshotResultCode, Constants.takeshotResultData);
        Log.i(TAG, "mMediaProjection defined");
    }

    @TargetApi(21)  
    private void virtualDisplay(){
    	Log.d(TAG, "-----------------进入到virtualDisplay方法中---------------");
    	try{
    		mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
    				windowWidth, windowHeight, Constants.takeshotScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
    				mImageReader.getSurface(), null, null);
    	}catch (Exception e) {
    		Log.i(TAG, "exception message is:" + e.getMessage());
    	}
        Log.i(TAG, "virtual displayed");
    }

    @TargetApi(21)
    private void startCapture(){
    	Log.d(TAG, "-----------------进入到startCapture方法中---------------");
        Image image = mImageReader.acquireLatestImage();
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0,width, height);
        image.close();
        Log.i(TAG, "image data captured");

        if(bitmap != null) {
            try{
                File fileImage = new File(nameImage);
                if(!fileImage.exists()){
                    fileImage.createNewFile();
                    Log.i(TAG, "image file created");
                }else{
                	fileImage.delete();
                	fileImage.createNewFile();
                	Log.i(TAG, "image file recreated");
                }
                FileOutputStream out = new FileOutputStream(fileImage);
                if(out != null){
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    out.flush();
                    out.close();
                    Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(fileImage);
                    media.setData(contentUri);
                    sendBroadcast(media);
                    Log.i(TAG, "screen image saved");
                    ToastUtil.ShowLongToast(this, "截屏成功");
                }
            }catch(FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        final File imageFile = new File(nameImage);
        Log.d(TAG, "-----------------------------截图文件状态为:" + imageFile.exists()+"文件路径为:" + nameImage);
    }

    @TargetApi(21)
    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG,"mMediaProjection undefined");
    }

    @TargetApi(21)
    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        Log.i(TAG,"virtual display stopped");
    }
    
    /**
     * 为测试类建立对应的文件夹
     * @param content
     * @return 是否写文件成功
     */
    private boolean checkFloderExists() {
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    	Date currentDate = new Date();
    	String currentDateName = format.format(currentDate);
    	String fileFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + 
    			            File.separator  +
    			            "snail_screen"  +
    			            File.separator  + 
    			            currentDateName +
    			            File.separator  +
    			            "compatible" +
    			            File.separator  +
    			            packageName;
    	FileWriter fileWriter = null;
    	
        try {
        	boolean isFolderExists = FileUtil.checkFolderExists(fileFolder);
        	Log.d(TAG, "-------writeFile------- folder state is:" + isFolderExists);
        	if(isFolderExists){
        		//此時文件夹存在
        		test_screen_dir = fileFolder;
        		return true;
        	}else{
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
                	Log.d(TAG, "-------writeFile------- close writer exception is:" + e.getMessage());
                }
            }
        }
    }
    
	private void updateViewPosition() {
		wmParams.x = (int) (x - mTouchStartX);
		wmParams.y = (int) (y - mTouchStartY);
	}
	
	private void dataRefreshNew() throws IOException {
		int pidMemory = memoryInfo.getPidMemorySize(pid, getBaseContext());
		long freeMemory = memoryInfo.getFreeMemorySize(getBaseContext());
		tvMemory.setText("应用/剩余内存:"
				+ fomart.format((double) (pidMemory / 1024)) + "/"
				+ fomart.format((double) (freeMemory / 1024))
				+ "MB");
	}
	
	private Runnable task = new Runnable() {

		public void run() {
			if (!isServiceStop) {
				try {
					dataRefreshNew();
				} catch (IOException e) {
					e.printStackTrace();
				}
				handler.postDelayed(this, 5 * 1000);
			} else {
				Log.d(TAG, "--------------------control service is shutdown!");
				stopSelf();
			}
		}
	};
}
