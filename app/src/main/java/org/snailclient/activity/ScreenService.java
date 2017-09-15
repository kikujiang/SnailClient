package org.snailclient.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.snailclient.activity.utils.fps.ToastUtil;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.snail.util.Constants;
import com.snail.util.SendImg;

public class ScreenService extends Service {

	private static final String TAG = "ScreenService";

    public static String pathImage = null;
    private String nameImage = null;

    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;

    public static int mResultCode = 0;
    public static Intent mResultData = null;
    public static MediaProjectionManager mMediaProjectionManager1 = null;

    public static WindowManager mWindowManager1 = null;
    private int windowWidth = 0;
    private int windowHeight = 0;
    public static int height = 0;
    public static int width = 0;
    private ImageReader mImageReader = null;
    public static DisplayMetrics metrics = null;
    public static int mScreenDensity = 0;
    
    private String dataString = "";
    private int screenOrientation = 1;
	
    @Override
    public void onCreate() {
    	super.onCreate();
    	Log.d(TAG, "------------------------进行初始化操作----------------------");
    	createVirtualEnvironment();
    }
    
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@TargetApi(21)
	private void createVirtualEnvironment(){
        pathImage = Environment.getExternalStorageDirectory().getAbsolutePath();
        mMediaProjectionManager1 = (MediaProjectionManager)getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mWindowManager1 = (WindowManager)getApplication().getSystemService(Context.WINDOW_SERVICE);
        Point screenSize = new Point();
        mWindowManager1.getDefaultDisplay().getSize(screenSize);
        width = screenSize.x;
    	height = screenSize.y;
        
        metrics = new DisplayMetrics();
        mWindowManager1.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        ToastUtil.ShowLongToast(this, "启动截屏服务成功");
    }

	@TargetApi(21)
    public void startVirtual(){
		
		Configuration mConfiguration = this.getResources().getConfiguration();
        if(mConfiguration.orientation == mConfiguration.ORIENTATION_LANDSCAPE){
        	windowWidth = height;
        	windowHeight = width;
        	screenOrientation = 0;
        }
        if(mConfiguration.orientation == mConfiguration.ORIENTATION_PORTRAIT){
        	windowWidth = width;
        	windowHeight = height;
        	screenOrientation = 1;
        }
		
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
        mMediaProjection = mMediaProjectionManager1.getMediaProjection(mResultCode, mResultData);
        Log.i(TAG, "mMediaProjection defined");
    }

    @TargetApi(21)  
    private void virtualDisplay(){
    	try{
    		mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
    				windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
    				mImageReader.getSurface(), null, null);
    	}catch (Exception e) {
    		Log.i(TAG, "exception message is:" + e.getMessage());
    	}
        Log.i(TAG, "virtual displayed");
    }

    @TargetApi(21)
    private void startCapture(){
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
                    this.sendBroadcast(media);
                    Log.i(TAG, "screen image saved");
                }
            }catch(FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
//        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/screen_shot.jpg";
        final File imageFile = new File(nameImage);
        Log.d(TAG, "-----------------------------截图文件状态为:" + imageFile.exists()+"文件路径为:" + nameImage);
        if(imageFile.exists() && !"".equals(dataString)){
			final String screenShotUrl = Constants.RES_URL
					+ "/platform/mobileTest/index.do";
			JSONObject picJson;
			try {
				picJson = new JSONObject(dataString);
    			List<String> keyList = new ArrayList();
				Iterator<String> keys = picJson.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					if (!key.equals("type")) {
						keyList.add(key);
					}
				}
				final Map<String, String> params = new HashMap<String, String>();
				for (String key : keyList) {
			       params.put(key,picJson.get(key) + "");
				}
				params.put("act","updateMonitor");
				params.put("picVeer",screenOrientation + "");
    			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    			String picTime = format.format(new Date(imageFile.lastModified()));
    			params.put("picTime",picTime);
    			Log.d(TAG, "------------------截图上传服务器的数据为:" +params.toString());
    			new SendImg(screenShotUrl, params.entrySet(), imageFile).run();
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        tearDownMediaProjection();
        Log.i(TAG, "screenService destroy");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	if(null != intent.getExtras()){
    		String command = intent.getExtras().getString("command","");
    		if(!"".equals(command)){
    			Log.d(TAG, "----------------开始截屏操作------------------");
    			dataString = intent.getExtras().getString("data","");
    			screen();
    		}
    	}
    	return super.onStartCommand(intent, flags, startId);
    }
    
    public void screen(){
//    	Handler handler1 = new Handler();
//        handler1.postDelayed(new Runnable() {
//            public void run() {
//                //start virtual
//                startVirtual();
//            }
//        }, 1500);
//
//        Handler handler2 = new Handler();
//        handler2.postDelayed(new Runnable() {
//            public void run() {
//                //capture the screen
//            	try {
//            		startCapture();
//				} catch (Exception e) {
//					Log.d(TAG, "-------------startCapture exception-------------" + e.getMessage());
//					stopSelf();
//				}
//            }
//        }, 2000);
//       	
//        Handler handler3 = new Handler();
//        handler3.postDelayed(new Runnable() {
//            public void run() {
//                stopVirtual();
//            }
//        }, 1000);
        
        startVirtual();
        new Thread(){
        	public void run() {
        		try {
					Thread.sleep(1000);
					Date date = new Date();
					nameImage = pathImage+"/"+date.getTime()+".jpg";
					startCapture();
					stopVirtual();
				} catch (Exception e) {
					Log.d(TAG, "------------------出现异常，异常信息为:" + e.getMessage());
					stopSelf();
				}
        	};
        }.start();
    }
}
