package org.snailclient.activity.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.monitor.util.Contact;
import com.snail.util.Constants;

import org.androidpn.demoapp.R;
import org.androidpn.demoapp.SubmitLaunchResultActivity;
import org.json.JSONException;
import org.json.JSONObject;
import org.snailclient.activity.bean.ReportStandardBean;
import org.snailclient.activity.bean.TestType;
import org.snailclient.activity.utils.GsonUtils;
import org.snailclient.activity.utils.fps.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import solo.HttpUtilForWired;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ReportNewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportNewFragment extends Fragment {

    private static final String TAG = "ReportNewFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TID = "tid";
    private static final String ARG_PACKAGE = "packageName";

    private ProgressBar viewLoading;
    private LinearLayout contentLayout;
    private EditText etVersionName;
    private EditText etApkName;
    private EditText etVersionInfo;
    private EditText etVersionApkSize;
    private EditText etVersionDate;
    private Spinner spinnerStandard;
    private Spinner spinnerTestType;
    private Button submitBtn;
    private Context mContext;

    private static final int DATA_LOAD_START = 100001;
    private static final int DATA_LOAD_FINISH = 100002;
    private static final int DATA_LOAD_ERROR = 100003;
    private static final int DATA_STANDARD_LIST_SUCCESS = 100004;
    private static final int DATA_SUBMIT_SUCCESS = 100005;

    private String tid;
    private String packageName;
    private String reportName;
    private String versionInfo;
    private String apkSize;
    private int standardId;
    private String publishDate;
    private int testType;
    private String apkName;

    private List<ReportStandardBean> reportStandardList = null;
    private DatePickerDialog datePickerDialog = null;

    private Handler dataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DATA_LOAD_START:
                    showProgress(true);
                    break;
                case DATA_LOAD_FINISH:
                    showProgress(false);
                    break;
                case DATA_LOAD_ERROR:
                    String errorMessage = msg.obj.toString();
                    ToastUtil.ShowLongToast(mContext, errorMessage);
                    break;
                case DATA_STANDARD_LIST_SUCCESS:
                    String result = msg.obj.toString();
                    reportStandardList = GsonUtils.getInstance().getStandardData(result);
                    FragmentAdapter<ReportStandardBean> adapter = new FragmentAdapter<>(mContext, reportStandardList);
                    spinnerStandard.setAdapter(adapter);
                    break;
                case DATA_SUBMIT_SUCCESS:
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
            }
        }
    };

    public ReportNewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReportNewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportNewFragment newInstance(String param1, String param2) {
        ReportNewFragment fragment = new ReportNewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TID, param1);
        args.putString(ARG_PACKAGE, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (getArguments() != null) {
            tid = getArguments().getString(ARG_TID);
            packageName = getArguments().getString(ARG_PACKAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View currentView = inflater.inflate(R.layout.fragment_report_new, container, false);
        initData(currentView);
        return currentView;
    }

    private void initData(View currentView) {
        viewLoading = (ProgressBar) currentView.findViewById(R.id.pb_loading);
        contentLayout = (LinearLayout) currentView.findViewById(R.id.contentView);
        etVersionName = (EditText) currentView.findViewById(R.id.et_version_name);
        etApkName = (EditText) currentView.findViewById(R.id.et_apk_name);
        etVersionInfo = (EditText) currentView.findViewById(R.id.et_version_info);
        etVersionDate = (EditText) currentView.findViewById(R.id.et_version_date);
        etVersionApkSize = (EditText) currentView.findViewById(R.id.et_version_size);
        spinnerStandard = (Spinner) currentView.findViewById(R.id.spinner_version_standard);
        spinnerTestType = (Spinner) currentView.findViewById(R.id.spinner_test_type);
        submitBtn = (Button) currentView.findViewById(R.id.btn_submit);

        initDataValue();
    }

    private void initDataValue() {
        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        publishDate = format.format(currentDate);
        etVersionDate.setText(publishDate);
        etVersionDate.setFocusable(false);
        etVersionDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                datePickerDialog = new DatePickerDialog(mContext,
                        new DatePickerDialog.OnDateSetListener() {

                            public void onDateSet(DatePicker arg0, int arg1,
                                                  int arg2, int arg3) {
                                publishDate = arg1 + "-" + (arg2 + 1) + "-"
                                        + arg3;
                                etVersionDate.setText(publishDate);
                            }
                        }, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        spinnerStandard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (reportStandardList != null && reportStandardList.size() > 0) {
                    ReportStandardBean currentBean = reportStandardList.get(position);
                    standardId = currentBean.getId();
                    Log.e(TAG, "onItemSelected: standardId is" + standardId );
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final List<String> testTypeList = new ArrayList<>();
        testTypeList.add("性能测试");
        testTypeList.add("兼容性测试");
        testTypeList.add("稳定性测试");
        spinnerStandard.setDropDownVerticalOffset(60);
        spinnerTestType.setDropDownVerticalOffset(60);
        spinnerTestType.setAdapter(new FragmentAdapter<String>(mContext, testTypeList));
        spinnerTestType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                testType = position + 1;
                Log.e(TAG, "onItemSelected: testType is"+testType );
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

        loadStandardList();
    }

    /**
     * 提交新建报告数据
     */
    private void submitResult(){
        reportName = etVersionName.getText().toString();
        if (TextUtils.isEmpty(reportName)){
            ToastUtil.ShowLongToast(mContext,"报告名称不能为空");
            return;
        }

        apkName = etApkName.getText().toString();
        if (TextUtils.isEmpty(apkName)){
            ToastUtil.ShowLongToast(mContext,"应用名称不能为空");
            return;
        }

        versionInfo = etVersionInfo.getText().toString();
        if (TextUtils.isEmpty(versionInfo)){
            ToastUtil.ShowLongToast(mContext,"版本信息不能为空");
            return;
        }
        apkSize = etVersionApkSize.getText().toString();
        if (TextUtils.isEmpty(apkSize)){
            ToastUtil.ShowLongToast(mContext,"版本大小不能为空");
            return;
        }

        new Thread(){
            @Override
            public void run() {
                dataHandler.sendEmptyMessage(DATA_LOAD_START);
                String url = Constants.DATA_URL + "/platform/mobileTest/index.do";
                String data = "act,genReportFromClient,appType,3,mac," + Contact.mac + ",tid," + tid + ",appReportName," + reportName +
                        ",appVersion,"+versionInfo+",publishDate,"+publishDate+",testType,"+testType+",clientTestStandId,"+standardId+",appSize,"+apkSize+",packageNames,"+packageName+",apkName,"+apkName;
                Log.e(TAG, "send url is:" + url + ",send data is:" + data );
                String result = HttpUtilForWired.getInstance().sendData2Web(url,data);
                Log.e(TAG, "get submit result is:" + result);
                if (result == null || "".equals(result)) {
                    Message errorMsg = Message.obtain();
                    errorMsg.what = DATA_LOAD_ERROR;
                    errorMsg.obj = "请求服务器数据失败";
                    dataHandler.sendMessage(errorMsg);
                    dataHandler.sendEmptyMessage(DATA_LOAD_FINISH);
                    return;
                }
                Message currentMsg = Message.obtain();
                currentMsg.what = DATA_SUBMIT_SUCCESS;
                currentMsg.obj = result;
                dataHandler.sendMessage(currentMsg);
                dataHandler.sendEmptyMessage(DATA_LOAD_FINISH);
            }
        }.start();
    }

    private void loadStandardList() {
        new Thread() {
            @Override
            public void run() {
                dataHandler.sendEmptyMessage(DATA_LOAD_START);
                String url = Constants.DATA_URL + "/platform/testStand/testStandClientMobileList.do";
                String data = "act,getClientMobileTestStandList";
                String result = HttpUtilForWired.getInstance().sendData2Web(url, data);
                Log.e(TAG, "get standard list result is:" + result);
                if (result == null || "".equals(result)) {
                    Message errorMsg = Message.obtain();
                    errorMsg.what = DATA_LOAD_ERROR;
                    errorMsg.obj = "请求服务器数据失败";
                    dataHandler.sendMessage(errorMsg);
                    dataHandler.sendEmptyMessage(DATA_LOAD_FINISH);
                    return;
                }
                Message currentMsg = Message.obtain();
                currentMsg.what = DATA_STANDARD_LIST_SUCCESS;
                currentMsg.obj = result;
                dataHandler.sendMessage(currentMsg);
                dataHandler.sendEmptyMessage(DATA_LOAD_FINISH);
            }
        }.start();
    }

    private void showProgress(boolean isShow) {
        if (isShow) {
            viewLoading.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        } else {
            viewLoading.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e(TAG, "onAttach: ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
