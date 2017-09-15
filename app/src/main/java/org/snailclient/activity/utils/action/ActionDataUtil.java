package org.snailclient.activity.utils.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import solo.HttpUtilForWired;
import android.util.Log;

import com.monitor.util.Contact;
import com.snail.util.Constants;

public class ActionDataUtil {

public static final String TAG = "ActionDataUtil";
	
    public static ResponseActionBean responseActionBean = null;
    
    public static List<String> actionListStr = null;

    public static List<ActionBean> responseActionDataList = null;
    
	private static ActionDataUtil instance;
	
	private ActionDataUtil(){
		
	}
	
	public static ActionDataUtil getInstance(){
		if(instance == null){
			synchronized (HttpUtilForWired.class) {
				if(instance == null){
					instance = new ActionDataUtil();
				}
			}
		}
		return instance;
	}
	
	public void getActionDataFromServer(final String project){
		//开启一个线程来获取行为点的列表
		String actionUrl =Constants.DATA_URL + "/platform/projectBehavior/projectBehaviorList.do";
		List<Map<String, String>> actionData = new ArrayList<Map<String,String>>();
		Map<String, String> projectName = new HashMap<String, String>();
		projectName.put("key", "projectName");
		projectName.put("value", project);
		Map<String, String> type = new HashMap<String, String>();
		type.put("key", "act");
		type.put("value", "getBehaviorList");
		actionData.add(type);
		actionData.add(projectName);
		String result = HttpUtilForWired.getInstance().sendData(actionUrl, actionData);
		Log.d(TAG, "action data result is:" + result);
		if(null != result){
			try {
				JSONObject responseBean = new JSONObject(result);
				responseActionBean = new ResponseActionBean();
				actionListStr = new ArrayList<String>();
				responseActionBean.setResult(responseBean.getString("result"));
				
				JSONArray listData = responseBean.getJSONArray("list");
				int length = listData.length();
				if(length < 1){
					//该情况下没有行为点的数据,设置列表为null
					responseActionBean.setList(null);
				}
				//此时列表长度大于0，则可以将数据插入actionBean对象的列表，从而放入本地对象中
				responseActionDataList = new ArrayList<ActionBean>();
				for (int i = 0; i < length; i++) {
					JSONObject action = (JSONObject) listData.get(i);
					String code = action.getString("code");
					for(int j = 0;j<Contact.actionArrays.length;j++){
						if(Contact.actionArrays[j].equals(code)){
							ActionBean actionBean = new ActionBean();
							actionBean.setName(action.getString("name"));
							actionBean.setCode(code);
							actionBean.setId(action.getInt("id"));
							responseActionDataList.add(actionBean);
							actionListStr.add(action.getString("name"));
						}
					}
				}
				responseActionBean.setList(responseActionDataList);
			} catch (JSONException e) {
				Log.d(TAG, "exception message is:" + e.getMessage());
				responseActionBean = null;
			}
		}else{
			responseActionBean = null;
		}
	}
}
