package org.snailclient.activity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.monitor.util.Contact;
import com.snail.util.Constants;

import org.androidpn.demoapp.R;
import org.json.JSONException;
import org.json.JSONObject;
import org.snailclient.activity.utils.fps.ToastUtil;
import org.snailclient.activity.utils.upload.bean.ReportBean;
import org.snailclient.activity.utils.upload.bean.ResponseReportBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solo.HttpUtilForWired;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ReportExistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportExistFragment extends Fragment {
    private static final String TAG = "ReportExistFragment";
    private static final String ARG_TID = "tid";
    public static final int MSG_GET_PROJECT_SUCCESS = 10009;
    public static final int MSG_START = 10011;
    public static final int MSG_END = 10012;
    private static final int MSG_SUBMIT_SUCCESS = 10013;
    private static final int MSG_ERROR = 10014;

    private String tid;
    private String rid;
    private Gson gson;

    private ProgressBar viewLoading;
    private LinearLayout contentLayout;
    private Spinner spinnerTestType;
    private Spinner spinnerReportList;
    private Button submitBtn;
    private Context mContext;
    private FragmentAdapter<ReportBean> reportAdapter;
    private List<ReportBean> reportBeanList;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_START:
                    showProgress(true);
                    break;
                case MSG_END:
                    showProgress(false);
                    break;
                case MSG_SUBMIT_SUCCESS:
                    String submitResult = msg.obj.toString();
                    try {
                        JSONObject resultArray = new JSONObject(submitResult);
                        String submitResultStatus = resultArray.getString("result");
                        if (submitResultStatus.equals("success")){
                            ToastUtil.ShowLongToast(mContext,"提交报告成功");
                            getActivity().finish();
                        }else{
                            ToastUtil.ShowLongToast(mContext,"解析服务器返回数据错误");
                        }
                    } catch (JSONException e) {
                        ToastUtil.ShowLongToast(mContext,"解析服务器返回数据错误");
                    }
                    break;
                case MSG_ERROR:
                    String errorMsg = msg.obj.toString();
                    ToastUtil.ShowLongToast(mContext,
                            errorMsg);
                    break;
                case MSG_GET_PROJECT_SUCCESS:
                    String result = msg.obj.toString();
                    ResponseReportBean responseBean = gson.fromJson(result,
                            ResponseReportBean.class);
                    reportBeanList.addAll(responseBean.getList());
                    if (reportAdapter == null) {
                        // 建立Adapter并且绑定数据源
                        spinnerReportList.setDropDownVerticalOffset(60);
                        reportAdapter = new FragmentAdapter<ReportBean>(mContext, reportBeanList);
                        spinnerReportList.setAdapter(reportAdapter);
                    } else {
                        reportAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    private void showProgress(boolean isShow) {
        if (isShow) {
            viewLoading.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        } else {
            viewLoading.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }

    public ReportExistFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ReportExistFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportExistFragment newInstance(String param1) {
        ReportExistFragment fragment = new ReportExistFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TID, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (getArguments() != null) {
            tid = getArguments().getString(ARG_TID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View currentView = inflater.inflate(R.layout.fragment_report_exist, container, false);
        initData(currentView);
        return currentView;
    }

    private void initData(View currentView) {
        gson = new Gson();
        viewLoading = (ProgressBar) currentView.findViewById(R.id.pb_loading);
        contentLayout = (LinearLayout) currentView.findViewById(R.id.contentView);
        spinnerReportList = (Spinner) currentView.findViewById(R.id.spinner_report_list);
        spinnerTestType = (Spinner) currentView.findViewById(R.id.spinner_type);
        submitBtn = (Button) currentView.findViewById(R.id.btn_submit);

        initDataValue();
    }

    private void initDataValue() {

        final List<String> testTypeList = new ArrayList<>();
        testTypeList.add("性能测试");
        testTypeList.add("兼容性测试");
        testTypeList.add("稳定性测试");

        spinnerTestType.setDropDownVerticalOffset(60);
        spinnerTestType.setAdapter(new FragmentAdapter<String>(mContext, testTypeList));
        spinnerTestType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String uploadUrl;
                switch (position) {
                    case 0:
                        uploadUrl = Constants.DATA_URL
                                + "/platform/mobileTest/mobileTestCaList.do";
                        prepareData(uploadUrl);
                        break;
                    case 1:
                        uploadUrl = Constants.DATA_URL
                                + "/platform/mobileCompatibility/mobileCompatibilityReportList.do";
                        prepareData(uploadUrl);
                        break;
                    case 2:
                        uploadUrl = Constants.DATA_URL
                                + "/platform/mobileTest/mobileTestCsList.do";
                        prepareData(uploadUrl);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerReportList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ReportBean currentReportBean = reportBeanList.get(position);
                rid = currentReportBean.getRid();
                Log.e(TAG, "current rid is: " + rid);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitResult();
            }
        });

        String performanceUrl = Constants.DATA_URL
                + "/platform/mobileTest/mobileTestCaList.do";
        prepareData(performanceUrl);
    }

    /**
     * 提交新建报告数据
     */
    private void submitResult(){

        new Thread(){
            @Override
            public void run() {
                mHandler.sendEmptyMessage(MSG_START);
                String url = Constants.DATA_URL + "/platform/mobileTest/index.do";
                String data = "act,genReportFromClient,mac," + Contact.mac + ",tid," + tid + ",rid,"+rid;
                Log.e(TAG, "send url is:" + url + ",send data is:" + data );
                String result = HttpUtilForWired.getInstance().sendData2Web(url,data);
                Log.e(TAG, "get submit result is:" + result);
                if (result == null || "".equals(result)) {
                    sendError("请求服务器数据失败");
                    mHandler.sendEmptyMessage(MSG_END);
                    return;
                }
                Message currentMsg = Message.obtain();
                currentMsg.what = MSG_SUBMIT_SUCCESS;
                currentMsg.obj = result;
                mHandler.sendMessage(currentMsg);
                mHandler.sendEmptyMessage(MSG_END);
            }
        }.start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void prepareData(final String url) {
        if(reportBeanList != null){
            reportBeanList.clear();
        }else{
            reportBeanList = new ArrayList<>();
        }
        new Thread() {
            public void run() {
                mHandler.sendEmptyMessage(MSG_START);
                List<Map<String, String>> actionData = new ArrayList<Map<String, String>>();
                Map<String, String> type = new HashMap<String, String>();
                type.put("key", "act");
                type.put("value", "getTestList");
                actionData.add(type);
                String result = HttpUtilForWired.getInstance().sendData(url,
                        actionData);
                Log.e(TAG, "all test result is:"+result );
                if (!"".equals(result)) {
                    try {
                        ResponseReportBean responseBean = gson.fromJson(result,
                                ResponseReportBean.class);
                        if (responseBean.getResult().equals("success")) {
                            Message currentMsg = Message.obtain();
                            currentMsg.what = MSG_GET_PROJECT_SUCCESS;
                            currentMsg.obj = result;
                            mHandler.sendMessage(currentMsg);
                        } else {
                            sendError("查询报告失败,返回消息:" + result);
                        }
                    } catch (Exception e) {
                        sendError("错误原因:" + e.getMessage());
                    }
                } else {
                    sendError("查询报告失败,请确认连接是否正常");
                }
                mHandler.sendEmptyMessage(MSG_END);
            }
        }.start();
    }

    private void sendError(String message){
        Message errorMsg = Message.obtain();
        errorMsg.obj = message;
        errorMsg.what = MSG_ERROR;
        mHandler.sendMessage(errorMsg);
    }
}
