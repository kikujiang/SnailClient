package org.androidpn.demoapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 类的描述：测试报告界面弹出的对话框
 *
 * @author zhll
 * @Time 2015-07-08
 */
public class DialogActivity extends AppCompatActivity {
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> typeAdapter;
    Spinner sp;
    String etype = null;
    Spinner selectType;
    LinearLayout lin;
    EditText ruResult;
    Button dsubmit;
    String[] result;
    SharedPreferences errorType;
    String[] type = {"崩溃", "闪退", "超时", "卡死", "内存不足", "蓝屏", "黑屏", "花屏", "UI异常", "卡顿", "网络问题", "存储空间不足", "系统问题", "硬件问题", "应用安装问题", "游戏功能问题", "其他"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        errorType = getSharedPreferences("errorType", 0);
        int count = errorType.getInt("count", 0);
        if (count != 0) {
            type = new String[count];
            for (int c = 0; c < count; c++) {
                type[c] = errorType.getString(String.valueOf(c), "");
            }
        }
        result = new String[]{"成功", "失败"};
        sp = (Spinner) findViewById(R.id.select);
        lin = (LinearLayout) findViewById(R.id.sll);
        selectType = (Spinner) findViewById(R.id.selectType);
        ruResult = (EditText) findViewById(R.id.ruResult);
        dsubmit = (Button) findViewById(R.id.dsubmit);
        dsubmit.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                String text = ruResult.getText().toString();
                if (text.length() > 1) {
                    Intent data = new Intent(DialogActivity.this, SubmitLaunchResultActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("result", ruResult.getText().toString().trim() + "," + etype);
                    data.putExtras(bundle);
                    setResult(2, data);
                    finish();
                }
            }

        });
        adapter = new SpinnerAdapter<String>(DialogActivity.this, R.layout.spinner_checked_text, result);
        typeAdapter = new SpinnerAdapter<String>(DialogActivity.this, R.layout.spinner_checked_text, type);
        adapter.setDropDownViewResource(R.layout.spinner_item_layout);
        typeAdapter.setDropDownViewResource(R.layout.spinner_item_layout);
        sp.setAdapter(adapter);
        selectType.setAdapter(typeAdapter);
        sp.setOnItemSelectedListener(new OnItemSelected());
        selectType.setOnItemSelectedListener(new OnItemSelected());
    }

    class OnItemSelected implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            if (arg0 == sp) {
                if (arg2 == 0) {
                    ruResult.setText("Success");
                    ruResult.setEnabled(false);
                    lin.setVisibility(View.GONE);
                } else {
                    Toast.makeText(DialogActivity.this, "请选择错误类型，并填写结果描述", Toast.LENGTH_LONG).show();
                    lin.setVisibility(View.VISIBLE);
                    ruResult.setEnabled(true);
                    ruResult.setText("");
                }
            } else if (arg0 == selectType) {
                etype = type[arg2];
            }

        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    class SpinnerAdapter<String> extends ArrayAdapter {
        String[] result;

        public SpinnerAdapter(Context context, int id, String[] result) {
            super(context, id, result);
            this.result = result;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = DialogActivity.this.getLayoutInflater().inflate(R.layout.spinner_item_layout, null);
            TextView label = (TextView) view
                    .findViewById(R.id.spinner_item_label);
            label.setText((CharSequence) result[position]);
            return view;
        }
    }
}
