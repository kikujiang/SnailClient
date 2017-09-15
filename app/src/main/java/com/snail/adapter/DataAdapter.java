package com.snail.adapter;

import java.util.List;

import org.androidpn.demoapp.R;
import org.snailclient.activity.utils.upload.bean.ReportBean;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.monitor.util.Programe;

/**
 * 选择框的自定义adapter
 * 
 * @author wubo1
 * 
 * @param <T>
 */
public class DataAdapter<T> extends BaseAdapter {

	private List<T> dataList = null;
	private Context dataContext = null;

	public DataAdapter(Context mContext, List<T> data) {
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
		if (getItem(pos) instanceof ReportBean) {
			holder.tvDataName
					.setText(((ReportBean) getItem(pos)).getName());
		} else if (getItem(pos) instanceof Programe) {
			holder.tvDataName.setText(((Programe) getItem(pos))
					.getProcessName());
		} else {
			holder.tvDataName.setText(getItem(pos).toString());
		}
		holder.tvDataName.setTextSize(16);
		return convertView;
	}
}

class ViewHolder {
	public TextView tvDataName;
}

