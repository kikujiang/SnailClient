package org.androidpn.demoapp;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Decoder.BASE64Encoder;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.monitor.util.SendHttp;
import com.snail.util.Constants;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class OaBugActivity extends AppCompatActivity {
    Handler myHandler;
    HashMap<String, String> project = null;
    HashMap<String, String> version = null;
    Spinner sp;
    Spinner ver;
    ArrayAdapter adapter;
    String projectId = null;
    String versionId = null;
    String[] pl = null;
    String[] vl = null;
    ImageView selectFile;
    int FILE_RESULT_CODE = 1;
    List<NameValuePair> nameValuePairs;
    ArrayList<String> shotId = new ArrayList<String>();
    Button submit;
    EditText gs;
    EditText jcr;
    SharedPreferences mySharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = Constants.OaBugUrl + "app/bug/projectList";
        setContentView(R.layout.activity_oabug);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        mySharedPreferences = getSharedPreferences("snailclient",
                AppCompatActivity.MODE_PRIVATE);
        myHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        project = (HashMap<String, String>) getProjectId(msg.obj.toString());
                        if (!project.isEmpty()) {
                            int k = 1;
                            pl = new String[project.size() + 1];
                            pl[0] = "请选择项目";
                            for (String key : project.keySet()) {
                                pl[k] = key;
                                k++;
                            }
                            sp = (Spinner) findViewById(R.id.projectList);

                            adapter = new ArrayAdapter<String>(OaBugActivity.this, R.layout.spinner_checked_text, pl) {
                                @Override
                                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                    View view = OaBugActivity.this.getLayoutInflater().inflate(R.layout.spinner_item_layout, null);
                                    TextView label = (TextView) view
                                            .findViewById(R.id.spinner_item_label);
                                    label.setText(pl[position]);
                                    return view;
                                }

                            };
                            adapter.setDropDownViewResource(R.layout.spinner_item_layout);
                            sp.setAdapter(adapter);
                            int pos = mySharedPreferences.getInt("projectId", 0);
                            Log.e("zlulan", "projectId:" + pos);
                            if (pos != 0 && pl.length > pos) {
                                sp.setSelection(pos);
                            }
                            sp.setOnItemSelectedListener(new OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                                           int arg2, long arg3) {
                                    // TODO Auto-generated method stub

                                    NameValuePair nameValuePairDumps = new BasicNameValuePair("projectId",
                                            project.get(pl[arg2]));
                                    projectId = project.get(pl[arg2]);
                                    SharedPreferences.Editor editor = mySharedPreferences.edit();
                                    editor.putInt("projectId", arg2);
                                    editor.commit();
                                    nameValuePairs = new ArrayList<NameValuePair>();
                                    nameValuePairs.add(nameValuePairDumps);
                                    String versionUrl = Constants.OaBugUrl + "app/bug/getVersionAndModule";
                                    new SendHttp(versionUrl, nameValuePairs, null, myHandler, 2).start();

                                }

                                public void onNothingSelected(AdapterView<?> arg0) {
                                    // TODO Auto-generated method stub

                                }

                            });
                            //设置默认值
                            sp.setVisibility(View.VISIBLE);
                        }
                        break;
                    case 2:
                        version = (HashMap<String, String>) getVersionId(msg.obj.toString());
                        if (!version.isEmpty()) {
                            int k = 0;
                            vl = new String[version.size()];
                            for (String key : version.keySet()) {
                                vl[k] = key;
                                k++;
                            }
                            ver = (Spinner) findViewById(R.id.versionList);

                            adapter = new ArrayAdapter<String>(OaBugActivity.this, R.layout.spinner_checked_text, vl) {
                                @Override
                                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                    View view = OaBugActivity.this.getLayoutInflater().inflate(R.layout.spinner_item_layout, null);
                                    TextView label = (TextView) view
                                            .findViewById(R.id.spinner_item_label);
                                    label.setText(vl[position]);
                                    return view;
                                }

                            };
                            adapter.setDropDownViewResource(R.layout.spinner_item_layout);
                            ver.setAdapter(adapter);
                            int pos = mySharedPreferences.getInt("versionId", 0);
                            Log.e("zlulan", "versionId:" + pos);
                            if (pos != 0 && vl.length > pos) {
                                ver.setSelection(pos);
                            }
                            ver.setOnItemSelectedListener(new OnItemSelectedListener() {
                                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                                           int arg2, long arg3) {
                                    // TODO Auto-generated method stub

                                    versionId = version.get(vl[arg2]);
                                    SharedPreferences.Editor editor = mySharedPreferences.edit();
                                    editor.putInt("versionId", arg2);
                                    editor.commit();
                                    //	Log.i("zlulan", "bug等级是"+pl[arg2]+"versionId"+versionId);
                                }

                                public void onNothingSelected(AdapterView<?> arg0) {
                                    // TODO Auto-generated method stub

                                }

                            });
                            //设置默认值
                            ver.setVisibility(View.VISIBLE);
                        }
                        break;
                    case 3:
                        shotId.add(getShotId(msg.obj.toString()));
                        break;
                    case 4:
                        String result = msg.obj.toString();
                        try {
                            JSONObject ob = new JSONObject(result);
                            result = ob.getString("message");
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        isNull(result);
                        gs.setText("");
                        //jcr.setText("");
                        selectFile.setImageResource(R.drawable.upload);
                        shotId.clear();
                        break;
                }
                super.handleMessage(msg);
            }
        };

        new SendHttp(url, null, null, myHandler).start();
        selectFile = (ImageView) findViewById(R.id.scjti);
        selectFile.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(OaBugActivity.this, MyFileManager.class);
                startActivityForResult(intent, FILE_RESULT_CODE);
            }

        });
        gs = (EditText) findViewById(R.id.gse);
        jcr = (EditText) findViewById(R.id.jcre);
        String creator = mySharedPreferences.getString("creator", "");
        if (creator.length() > 1) {
            jcr.setText(creator);
        }
        submit = (Button) findViewById(R.id.tjb);
        submit.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (gs.getText().toString().length() < 1) {
                    isNull("标题不能为空");
                    return;
                } else if (jcr.getText().toString().length() < 1) {
                    isNull("创建人不能为空");
                    return;
                } else if (projectId == null) {
                    isNull("请选择项目");
                    return;
                } else if (versionId == null) {
                    isNull("请选择版本");
                    return;
                } else if (shotId.size() < 1) {
                    isNull("没有图片");
                    return;
                }
                Log.e("zlulan", "shotId:" + shotId.toString());
                String subUrl = Constants.OaBugUrl + "app/bug/save";
                NameValuePair nameValueSummary = new BasicNameValuePair("summary", gs.getText().toString());
                NameValuePair nameValueCreator = new BasicNameValuePair("creator", jcr.getText().toString());
                nameValuePairs = new ArrayList<NameValuePair>();
                for (int i = 0; i < shotId.size(); i++) {
                    NameValuePair nameValueScreenshotIds = new BasicNameValuePair("screenshotIds", shotId.get(i));
                    nameValuePairs.add(nameValueScreenshotIds);
                }

                NameValuePair nameValueProjectId = new BasicNameValuePair("projectId", projectId);
                NameValuePair nameValueVersionId = new BasicNameValuePair("versionId", versionId);

                nameValuePairs.add(nameValueSummary);
                nameValuePairs.add(nameValueCreator);

                nameValuePairs.add(nameValueProjectId);
                nameValuePairs.add(nameValueVersionId);
                SharedPreferences.Editor editor = mySharedPreferences.edit();
                editor.putString("creator", jcr.getText().toString());
                editor.commit();
                new SendHttp(subUrl, nameValuePairs, null, myHandler, 4).start();
            }

        });
        Button cancel = (Button) findViewById(R.id.qxb);
        cancel.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                gs.setText("");
                jcr.setText("");
                selectFile.setImageResource(R.drawable.upload);
                shotId.clear();
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (FILE_RESULT_CODE == requestCode) {
            Bundle bundle = null;
            StringBuffer ab = new StringBuffer();
            if (data != null && (bundle = data.getExtras()) != null) {
                //  Log.i("zlulan","选择文件夹为："+bundle.getString("file"));
                ArrayList<String> list = bundle.getStringArrayList("files");
                String fp = list.get(0);
                Bitmap bm = BitmapFactory.decodeFile(fp);
                Bitmap by = ThumbnailUtils.extractThumbnail(bm, 500, 500);
                selectFile.setImageBitmap(by);
                String imgUrl = Constants.OaBugUrl + "app/bug/screenshot/upload";
                for (int i = 0; i < list.size(); i++) {
                    String img = (bitmapToBase64(BitmapFactory.decodeFile(list.get(i))) + ",");
                    NameValuePair nameValuePairDumps = new BasicNameValuePair("imageStr", img);
                    nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(nameValuePairDumps);
                    new SendHttp(imgUrl, nameValuePairs, null, myHandler, 3).start();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public Map<String, String> getProjectId(String result) {
        Map<String, String> projectlist = new HashMap<String, String>();
        try {
            JSONObject root = new JSONObject(result);
            JSONArray array = (JSONArray) root.get("projects");
            for (int i = 0; i < array.length(); i++) {
                JSONObject tmp = array.getJSONObject(i).getJSONObject("project");
                projectlist.put(tmp.getString("sprojectName"), tmp.getString("nprojectId"));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return projectlist;
    }

    public Map<String, String> getVersionId(String result) {
        Map<String, String> versionlist = new HashMap<String, String>();
        try {
            JSONObject root = new JSONObject(result);
            JSONArray array = (JSONArray) root.get("versions");
            for (int i = 0; i < array.length(); i++) {
                versionlist.put(array.getJSONObject(i).getString("versionName"), array.getJSONObject(i).getString("versionId"));
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return versionlist;
    }

    public String getShotId(String result) {
        String shotId = null;
        JSONObject root;
        try {
            root = new JSONObject(result);
            shotId = root.get("shotId").toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return shotId;
    }

    public String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public String GetImageStr(String imgFile) {//将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;
        //读取图片字节数组
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        String rlt = encoder.encode(data);//返回Base64编码过的字节数组字符串
        return rlt;
    }

    public void isNull(String s) {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage(s)
                .setPositiveButton("确定", null)
                .show();
    }
}
