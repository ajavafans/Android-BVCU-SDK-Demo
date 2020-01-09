package com.example.TreeView;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication1.R;

import java.util.List;

public class PuDeviceTreeAdapter<T> extends PuDeviceTreeListViewAdapter {
	/**
	 * @param mTree
	 * @param context
	 * @param datas
	 * @param defaultExpandLevel 默认展开几级树
	 */
	public PuDeviceTreeAdapter(ListView mTree, Context context, List datas, int defaultExpandLevel) throws IllegalArgumentException, IllegalAccessException {
		super(mTree, context, datas, defaultExpandLevel);
	}

	@Override
	public View getConvertView(PuDeviceNode node, int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.device_listgroup, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.label = (TextView) convertView
					.findViewById(R.id.devicename_text);
			viewHolder.icon = (ImageView) convertView
					.findViewById(R.id.device_expand_image);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (node.getIcon() == -1) {
			viewHolder.icon.setVisibility(View.INVISIBLE);
		} else {
			viewHolder.icon.setVisibility(View.VISIBLE);
			viewHolder.icon.setImageResource(node.getIcon());
		}
		if (node.getName() == null || node.getName().equals("")){
			viewHolder.label.setText(node.getPUID());
		}else {
			viewHolder.label.setText(node.getName());
		}
		return convertView;
	}

	private final class ViewHolder {
		TextView label;
		ImageView icon;
	}
}
