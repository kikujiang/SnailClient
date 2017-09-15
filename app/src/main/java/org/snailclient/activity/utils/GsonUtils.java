package org.snailclient.activity.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.snailclient.activity.bean.ReportStandardBean;

import java.util.List;

/**
 * Created by wubo1 on 2017/8/17.
 */

public class GsonUtils {

    private static GsonUtils instance = null;

    private Gson gson;

    private GsonUtils(){
        gson = new Gson();
    }
    public static GsonUtils getInstance(){
        if(instance == null){
            synchronized (GsonUtils.class) {
                if(instance == null){
                    instance = new GsonUtils();
                }
            }
        }
        return instance;
    }

    public List<ReportStandardBean> getStandardData(String jsonData){
        return gson.fromJson(jsonData, new TypeToken<List<ReportStandardBean>>(){}.getType());
    }
}
