package org.androidpn.demoapp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * 类的描述：bug选择图片界面
 *
 * @author zhll
 * @Time 2015-07-08
 */
public class MyFileManager extends AppCompatActivity {
    private List<String> paths = null;
    private String rootPath = "/";
    private String curPath = "/";
    Adapter adapter;
    GridView list;
    int level = 0;
    ArrayList<String> selectFiles = new ArrayList<String>();
    private final static String TAG = "zlulan";

    @Override
    protected void onCreate(Bundle icicle) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);  
        super.onCreate(icicle);
        setContentView(R.layout.file_select);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (rootPath.equals("/storage/emulated/0")) {
            rootPath = "/mnt/sdcard";
        }
        list = (GridView) findViewById(R.id.gredview);
//        list.setOnItemClickListener(new OnItemClickListener(){
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//				// TODO Auto-generated method stub
//				//Toast.makeText(MyFileManager.this, "点中"+MyFileManager.this.toString(),Toast.LENGTH_SHORT).show();
//				// TODO Auto-generated method stub
//				String ff = paths.get(arg2);  
//				File file=new File(ff);
//		        if (file.isDirectory()) {  
//		            curPath = paths.get(arg2);  
//		            getFileDir(paths.get(arg2));
//		           //adapter.notifyDataSetChanged();
//		           
//		        } else {  
//		            Intent data = new Intent(MyFileManager.this,BugActivity.class);  
//		            Bundle bundle = new Bundle();  
//		            bundle.putString("file", file.getAbsolutePath());  
//		            data.putExtras(bundle);  
//		            setResult(2, data);  
//		        	 finish(); 
//		              
//		        }  
//		    }
//       	 
//        });

        getFileDir(rootPath);

    }

    private void getFileDir(String filePath) {
        level = count(filePath, File.separator);
        paths = new ArrayList<String>();
        getImage(filePath, paths);
        adapter = new Adapter(MyFileManager.this, paths);
        list.setAdapter(adapter);
    }

    //    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {  
//        File file = new File(paths.get(position));  
//        if (file.isDirectory()) {  
//            curPath = paths.get(position);  
//            getFileDir(paths.get(position));  
//        } else {  
//            //可以打开文件  
//        }  
//    }  
    public void getImage(String path, List<String> images) {
        File f = new File(path);
        if (f.isDirectory()) {
            if (count(f.getAbsolutePath(), File.separator) < (level + 3)) {
                File[] files = f.listFiles();
                for (File file : files) {
                    getImage(file.getAbsolutePath(), images);
                }
            }
        } else {
            String name = f.getName();
            if (name.endsWith("png") || name.endsWith("jpg") || name.endsWith("jpeg") || name.endsWith("gif") || name.endsWith("tif") || name.endsWith("tiff")) {
                images.add(f.getAbsolutePath());
            }
        }
    }

    public int count(String file, String c) {
        int pos = -2;
        int n = 0;

        while (pos != -1) {
            if (pos == -2) {
                pos = -1;
            }
            pos = file.indexOf(c, pos + 1);
            if (pos != -1) {
                n++;
            }
        }
        return n;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.oabug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.confirm:
                if (paths != null && paths.size() > 0) {
                    Intent data = new Intent(MyFileManager.this, OaBugActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("files", selectFiles);
                    data.putExtras(bundle);
                    setResult(2, data);
                }
                finish();
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class Adapter extends BaseAdapter {
        String pos = null;
        private LayoutInflater mInflater;

        //private List<String> items;
        private List<String> paths;
        StringBuffer selectFile = new StringBuffer();

        public Adapter(Context context, List<String> pa) {
            mInflater = LayoutInflater.from(context);
            //items = it;
            paths = pa;
//        mIcon1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.back01);  
//        mIcon2 = BitmapFactory.decodeResource(context.getResources(),R.drawable.back02);  
//        mIcon3 = BitmapFactory.decodeResource(context.getResources(),R.drawable.folder);  
//        mIcon4 = BitmapFactory.decodeResource(context.getResources(),R.drawable.doc);  
        }

        public int getCount() {
            return paths.size();
        }

        public Object getItem(int position) {
            return paths.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            pos = paths.get(position).toString();
            ViewHolder holder;
            convertView = mInflater.inflate(R.layout.file_row, null);
            holder = new ViewHolder();
            holder.text = (CheckBox) convertView.findViewById(R.id.imageCheckBox);
            holder.icon = (ImageView) convertView.findViewById(R.id.gredimageView);
            holder.text.setOnCheckedChangeListener(new checkChange(pos));
            convertView.setTag(holder);
            if (selectFiles.contains(pos)) {
                holder.text.setChecked(true);
                selectFiles.remove(pos);
            }
            File f = new File(pos);
            Bitmap bit = BitmapFactory.decodeFile(f.getAbsolutePath());
            bit = ThumbnailUtils.extractThumbnail(bit, 300, 300);
            holder.icon.setImageBitmap(bit);
            return convertView;
        }

        class checkChange implements OnCheckedChangeListener {
            String pos;

            public checkChange(String pos) {
                this.pos = pos;
            }

            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                // TODO Auto-generated method stub
                if (arg1) {
                    selectFiles.add(pos);
                } else {
                    selectFiles.remove(pos);
                }
            }

        }

        class ViewHolder {
            CheckBox text;
            ImageView icon;
        }
    }
}  

