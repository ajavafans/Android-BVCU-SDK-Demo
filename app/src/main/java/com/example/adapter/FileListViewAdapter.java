package com.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.dialog.FileDownloadDialog;
import com.example.entity.FileDownloadEntity;
import com.example.myapplication1.R;
import com.example.myapplication1.VideoPreviewActivity;
import com.smarteye.sdk.BVCU;

import java.util.ArrayList;

public class FileListViewAdapter extends BaseAdapter {
	private ArrayList<FileDownloadEntity> fileLists = new ArrayList<FileDownloadEntity>();
	private LayoutInflater inflater = null;

	public FileListViewAdapter(Context context, ArrayList<FileDownloadEntity> fileLists) {
		this.fileLists = fileLists;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return fileLists.size();
	}

	@Override
	public Object getItem(int i) {
		return fileLists.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(
					R.layout.file_list_item_layout, null);
			holder.downloadBtn = convertView.findViewById(R.id.download_btn_id);
			holder.fileNameText = convertView.findViewById(R.id.file_name_id);
			holder.downloadPercentText = convertView.findViewById(R.id.download_percent_id);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// 设置list中TextView的显示
		holder.fileNameText.setText(fileLists.get(position).getFileName());
		holder.downloadPercentText.setText(fileLists.get(position).getDownloadPercent() + "%");
		if (fileLists.get(position).getDownloadStatus() == FileDownloadDialog.BVCU_FILE_DOWNLOAD_FAILED
				|| fileLists.get(position).getDownloadStatus() == FileDownloadDialog.BVCU_FILE_DOWNLOAD_NORMAL) {
			holder.downloadBtn.setVisibility(View.VISIBLE);
			holder.downloadPercentText.setVisibility(View.GONE);
		} else {
			holder.downloadBtn.setVisibility(View.GONE);
			holder.downloadPercentText.setVisibility(View.VISIBLE);
		}
		final int tempPosition = position;
		holder.downloadBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int token = downLoadFile(fileLists.get(tempPosition));
				fileLists.get(tempPosition).setToken(token);
			}
		});
		return convertView;
	}

	public static class ViewHolder {
		TextView fileNameText;
		TextView downloadPercentText;
		Button downloadBtn;
	}

	/**
	 * 下载所需参数
	 * 1、上传/下载标志位：  0 下载  1 上传
	 * 2、本地路径： 下载时文件本地存储路径，文件夹需要自己新建，demo中使用的是默认download文件夹
	 * 3、远程路径： 查询回复函数的负载中有
	 * 4、szSourceID： 查询回复函数的负载中有
	 * @param fileDownloadEntity
	 * @return
	 */
	private int downLoadFile(FileDownloadEntity fileDownloadEntity) {
		int token = BVCU.getSDK().openFileTransfer(VideoPreviewActivity.DOWNLOAD,
				fileDownloadEntity.getSzRemoteFilePath(),
				fileDownloadEntity.getSzLocalFilePath(),
				fileDownloadEntity.getSzSourceID());
		return token;
	}
}
