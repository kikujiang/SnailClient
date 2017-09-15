package solo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.snailclient.activity.utils.fps.ToastUtil;

import android.util.Log;

import com.snail.util.Constants;
import com.snail.util.SnailApplication;

public class HttpUtilForWired {

	public static final String TAG = "HttpUtilForWired";
	
	public static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");
	public static final MediaType MEDIA_TYPE_TXT = MediaType.parse("text/plain");
	public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
	
	private static HttpUtilForWired instance;
	
	private OkHttpClient client;
	
	private HttpUtilForWired(){
		initClient();
	}
	
	private void initClient(){
		client = new OkHttpClient.Builder()
		.connectTimeout(30,TimeUnit.SECONDS)
		.readTimeout(30, TimeUnit.SECONDS)
		.build();
	}
	
	public static HttpUtilForWired getInstance(){
		if(instance == null){
			synchronized (HttpUtilForWired.class) {
				if(instance == null){
					instance = new HttpUtilForWired();
				}
			}
		}
		
		return instance;
	}

	public String sendData2Web(String url,String data){
		String result = null;
		String sendDataArray[] = data.split(",");
		int dataLength = sendDataArray.length;
		Log.d(TAG,"length is:" + dataLength);
		int dataPairLength = dataLength / 2;
		FormBody.Builder builder = new FormBody.Builder();
		for(int i = 0;i<dataPairLength;i++){
			builder.add(sendDataArray[i*2],sendDataArray[i*2+1]);
		}

		Request request = new Request.Builder()
				.addHeader("accept", "*/*")
				.addHeader("connection", "Keep-Alive")
				.url(url)
				.post(builder.build())
				.build();
		try {
			Response response = client.newCall(request).execute();
			result = response.body().string();
		} catch (IOException e) {
			result = null;
			Log.e(TAG, "exception message is:" + e.getMessage());
		}
		return result;
	}

	public void sendDatatoWeb(String url, List<Map<String, String>> data){
		RequestBody body = changeData(data);
			
		Request request = new Request.Builder()
		  .addHeader("accept", "*/*")
		  .addHeader("connection", "Keep-Alive")
	      .url(url)
	      .post(body)
	      .build();
		client.newCall(request).enqueue(new Callback() {
			
			@Override
			public void onResponse(Call arg0, Response response) throws IOException {
				String result = response.body().string();
				Log.d(TAG , "服务器返回的结果是:"+ result );
				try {
					JSONObject resultData = new JSONObject(result);
					String flag = resultData.getString("result");
					if(flag.equals("success")){
						Constants.MAIN_SERVER_URL = resultData.getString("mainServer");
						Constants.DATA_URL = resultData.getString("monitorServer");
						Constants.RES_URL = resultData.getString("resServer");
					}
				} catch (JSONException e) {
					Log.d(TAG , "解析json遇到异常 :"+ e.getMessage());
					ToastUtil.ShowLongToast(SnailApplication.getContext(), "解析服务器数据失败");
					Constants.DATA_URL = "";
					Constants.RES_URL = "";
					Constants.MAIN_SERVER_URL = "";
				}
			}
			
			@Override
			public void onFailure(Call arg0, IOException arg1) {
				ToastUtil.ShowLongToast(SnailApplication.getContext(), "服务器返回异常");
				Log.d(TAG , "服务器返回的异常是 :"+ arg1.getMessage());
				Constants.DATA_URL = "";
				Constants.RES_URL = "";
				Constants.MAIN_SERVER_URL = "";
			}
		});
	}
	
	
   public String sendData(String url, List<Map<String, String>> data){
		
		RequestBody body = changeData(data);
			
		Request request = new Request.Builder()
		  .addHeader("accept", "*/*")
		  .addHeader("connection", "Keep-Alive")
	      .url(url)
	      .post(body)
	      .build();
		
		Response response = null;
		String result = null;
		try {
			response = client.newCall(request).execute();
			result = response.body().string();
		} catch (IOException e) {
			result = "";
			Log.e(TAG, "exception message is:" + e.getMessage());
		}
		return result;
	}
	
    /**
     * 将数据转化为表单对象
     * @param data
     * @return
     */
	private RequestBody changeData(List<Map<String, String>> data){
		Builder bodyBuilder = new FormBody.Builder();
		for(Map<String, String> element: data){
			if(null != element.get("value")){
				bodyBuilder.add(element.get("key"), element.get("value"));
			}else{
				bodyBuilder.add(element.get("key"), "");
			}
		}
		return bodyBuilder.build();
	}
	
	/**
	 * 
	 * @param url
	 * @param file
	 */
	public String sendFileToServer(String url,String type,File file,Set<Entry<String, String>> params){
		
		MultipartBody.Builder builder = new MultipartBody.Builder();

		for (Entry<String, String> param : params) {
			builder.addFormDataPart(param.getKey(), param.getValue());
		}
		
		if("pic".equals(type)){
			builder.setType(MultipartBody.FORM).addFormDataPart(type, file.getName(),
					RequestBody.create(MEDIA_TYPE_JPG, file));
		}else{
			builder.setType(MultipartBody.FORM).addFormDataPart(type, file.getName(),
					RequestBody.create(MEDIA_TYPE_TXT, file));
		}
		

		Request request = new Request.Builder()
        .url(url)
        .post(builder.build())
        .build();

	    Response response;
		try {
			response = client.newCall(request).execute();
			String result = response.body().string();
			Log.e(TAG, "response result is:" + result);
			return result;
		} catch (IOException e) {
			Log.e(TAG, "response error message is:" + e.getMessage());
			return "";
		}
	}
}
