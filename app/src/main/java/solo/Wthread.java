
package solo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
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
import org.snailclient.activity.sendCommand;
import org.snailclient.activity.utils.fps.GenericToast;
import org.snailclient.activity.utils.fps.ToastUtil;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.monitor.util.Contact;
import com.snail.util.Constants;
import com.snail.util.SendImg;

public class Wthread extends Thread {

	public static final String TAG = "Wthread";
	public static String SNAIL_TEST_FOLDER = "";
	private SimpleDateFormat picNameFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	JSONObject json;
	Socket socket;
	Context context;
	int flag = 0;
	
	public Wthread(Socket socket, Context context) {
		this.socket = socket;
		this.context = context;
	}

	@Override
	public void run() {
		Log.d(TAG, "------------------进入wthread中--------------------");
		String desResult = "";
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (IOException e) {
			desResult = "fail: 解析管理服务器字节流出错";
		}
		String line = null;
		try {
			line = in.readLine();
			Log.e(TAG, "接到指令信息：" + line + "\n");
		} catch (IOException e2) {
			desResult = "fail: 读取管理服务器字符串出错";
		}

		if (line != null) {
			try {
				json = new JSONObject(line);
				Log.d(TAG, "json result is:" + json);
				String type = json.getString("type");
				if (type.contains("ecTeststart")) {
					String apk = json.getString("packageName");
					String rid = json.getString("rid");
					String resultFlag = "";
					if ("".equals(desResult)) {
						desResult = "success ! rid is:" + rid;
						resultFlag = "success";
					}
					JSONObject response = new JSONObject();
					response.put("type", "ecTeststart");
					response.put("result", resultFlag);
					response.put("desc", desResult);
					response.put("mac", Contact.mac);
					response.put("rid", rid);
					response.put("data_url", Constants.DATA_URL + "/platform/mobileTest/index.do");
					response.put("res_url", Constants.RES_URL + "/platform/mobileTest/index.do");
					new sendCommand(response.toString(), Constants.managerPort,
							Constants.managerIP).start();

					String result = json.toString();
					
//					boolean isWriteSuccess = FileUtil.writeFile(result);
					boolean isWriteSuccess = writeFile(result);
					Log.e(TAG, "end write file" + isWriteSuccess);
					if(isWriteSuccess){
						Looper.prepare();
						GenericToast.makeText(context, "接收测试指令完毕", 500).show();
//						ToastUtil.ShowLongToast(context, "接收测试指令完毕");
						Looper.loop();
					}
					Log.d(TAG, "command is:" + result + "/n and download id is:");
				} else if (type.contains("screen")) {
					Log.d(TAG, "当前版本号为:" + Constants.versionCode);
					Log.d(TAG, "receiver message is:" + json.toString());
					dataString = json.toString();
					final String screenShotUrl = Constants.RES_URL
							+ "/platform/mobileTest/index.do";
					String result = "";
					if(Constants.versionCode < 21){
						Log.d(TAG, "--------------版本过低，无法启动截图功能---------------");
						ToastUtil.ShowLongToast(context, "当期系统版本过低，无法截图");
						result = "fail and url is:" + screenShotUrl;
						PrintStream data = new PrintStream(socket.getOutputStream());
						data.print(result);
						data.flush();
						return;
					}
					
					if(!checkIsScreenReady()){
						Log.d(TAG, "--------------截图准备工作没有准备好---------------");
//						ToastUtil.ShowLongToast(context, "截图准备工作没有准备好");
						Looper.prepare();
						GenericToast.makeText(context, "截图准备工作没有准备好", 500).show();
						Looper.loop();
						result = "fail and url is:" + screenShotUrl;
						PrintStream data = new PrintStream(socket.getOutputStream());
						data.print(result);
						data.flush();
						return;
					}
					
					startVirtual();
			        new Thread(){
			        	public void run() {
			        		try {
								Thread.sleep(500);
								Date date = new Date();
								if(TextUtils.isEmpty(SNAIL_TEST_FOLDER)){
									SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
									Date currentDate = new Date();
									String currentDateName = format.format(currentDate);
									String fileFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + 
				    			            File.separator+
				    			            "snailtest"+
				    			            File.separator + 
				    			            currentDateName;
									nameImage = fileFolder + "/" + picNameFormat.format(date) + ".jpg";
								}else{
									nameImage = SNAIL_TEST_FOLDER +"/"+ picNameFormat.format(date) +".jpg";
								}
								startCapture();
								stopVirtual();
								tearDownMediaProjection();
							} catch (Exception e) {
								Log.d(TAG, "------------------出现异常，异常信息为:" + e.getMessage());
							}
			        	};
			        }.start();
					
					result = "success and url is:" + screenShotUrl;
					Log.d(TAG, "result is:" + result);
					PrintStream data = new PrintStream(socket.getOutputStream());
					data.print(result);
					data.flush();
				} else {
					Intent intent = new Intent();
					intent.setAction("com.snailgame.test");
					intent.putExtra("command", json.toString());
					context.sendBroadcast(intent);
				}
			} catch (Exception e) {
				Log.d(TAG, "----------------------处理指令出现异常，异常信息为:" + e.getMessage());
				desResult = "fail: 保存服务器信息失败";
			}
		} 
	}
	
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
    private int screenOrientation = 0;

	@TargetApi(21)
    public void startVirtual(){
		Log.d(TAG, "-----------------进入到startVirtual方法中---------------");
    	windowWidth = Constants.takeshotScreenSize.y;
    	windowHeight = Constants.takeshotScreenSize.x;
    	screenOrientation = 0;

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
//        mMediaProjection = ScreenService.mMediaProjectionManager1.getMediaProjection(ScreenService.mResultCode, ScreenService.mResultData);
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
                    context.sendBroadcast(media);
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
			final String screenShotUrl = Constants.DATA_URL
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
    
    /**
     * 为测试类建立对应的文件夹
     * @param content
     * @return 是否写文件成功
     */
    private boolean writeFile(String content) {
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    	Date currentDate = new Date();
    	String currentDateName = format.format(currentDate);
    	String fileFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + 
    			            File.separator+
    			            "snailtest"+
    			            File.separator + 
    			            currentDateName;
    	FileWriter fileWriter = null;
    	
        try {
        	boolean isFolderExists = FileUtil.checkFolderExists(fileFolder);
        	Log.d(TAG, "-------writeFile------- folder state IS:" + isFolderExists);
        	if(isFolderExists){
        		SNAIL_TEST_FOLDER = fileFolder;
        		String filepath = SNAIL_TEST_FOLDER + File.separator + "command.txt";
        		Log.d(TAG, "-------writeFile------- file path is:" + filepath + "and content is:" + content);
        		fileWriter = new FileWriter(filepath, false);
        		fileWriter.write(content);
        		fileWriter.close();
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
}
