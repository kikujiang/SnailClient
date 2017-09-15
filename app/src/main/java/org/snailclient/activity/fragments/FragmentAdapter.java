package org.snailclient.activity.fragments;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.monitor.util.Programe;

import org.androidpn.demoapp.R;
import org.snailclient.activity.bean.ReportStandardBean;
import org.snailclient.activity.utils.upload.bean.ReportBean;

import java.util.List;

/**
 * Created by wubo1 on 2017/8/16.
 */

public class FragmentAdapter<T> extends BaseAdapter {
    private List<T> dataList = null;
    private Context dataContext = null;

    public FragmentAdapter(Context mContext, List<T> data) {
        this.dataContext = mContext;
        this.dataList = data;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int pos) {
        return dataList.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(dataContext).inflate(
                    R.layout.item_spinner, null);
            holder.tvDataName = (TextView) convertView
                    .findViewById(R.id.content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvDataName.setBackgroundColor(Color.parseColor("#ffffff"));
        holder.tvDataName.setTextColor(Color.parseColor("#000000"));
        Object data = getItem(pos);
        if (data instanceof ReportBean) {
            holder.tvDataName.setText(((ReportBean) data).getName());
        } else if(data instanceof ReportStandardBean){
            holder.tvDataName.setText(((ReportStandardBean) data).getName());
        } else {
            holder.tvDataName.setText(data.toString());
        }
        holder.tvDataName.setTextSize(16);
        return convertView;
    }

    class ViewHolder {
        public TextView tvDataName;
    }
}
