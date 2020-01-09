package com.example.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.adapter.FileListViewAdapter;
import com.example.entity.FileDownloadEntity;
import com.example.myapplication1.R;
import com.smarteye.adapter.BVCU_CmdMsgContent;
import com.smarteye.adapter.BVCU_Command;
import com.smarteye.adapter.BVCU_Method;
import com.smarteye.adapter.BVCU_SEARCH_TYPE;
import com.smarteye.adapter.BVCU_STORAGE_FILE_TYPE;
import com.smarteye.adapter.BVCU_SearchInfo;
import com.smarteye.adapter.BVCU_Search_FileFilter;
import com.smarteye.adapter.BVCU_Search_FileInfo;
import com.smarteye.adapter.BVCU_Search_Request;
import com.smarteye.adapter.BVCU_SubMethod;
import com.smarteye.sdk.BVCU;

import java.util.ArrayList;

/**
 * 文件检索相关Dialog
 */
public class FileDownloadDialog extends Dialog implements View.OnClickListener {
	private static final String TAG = "FileDownloadDialog";
	private Context context;
	private String deviceID;
	private TextView titleTextView;
	private ListView fileListView;
	private FileListViewAdapter fileListViewAdapter;
	private ArrayList<FileDownloadEntity> fileLists = new ArrayList<FileDownloadEntity>();
	private Button cancelBtn, okBtn;
	public static final int BVCU_FILE_DOWNLOAD_FAILED = -1; //下载失败
	public static final int BVCU_FILE_DOWNLOAD_NORMAL = 0;  //文件未下载
	public static final int BVCU_FILE_DOWNLOAD_WAITING = 1; //等待下载
	public static final int BVCU_FILE_DOWNLOAD_ING = 2;//正在下载
	public static final int BVCU_FILE_DOWNLOAD_SUCCESS = 3; //下载成功

	public FileDownloadDialog(@NonNull Context context, String deviceId) {
		super(context, R.style.login_dialog_style);
		this.context = context;
		this.deviceID = deviceId;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file__download_dialog_layout);
		cancelBtn = findViewById(R.id.download_cancel_btn_id);
		okBtn = findViewById(R.id.download_ok_btn_id);
		fileListView = findViewById(R.id.file_list_id);
		titleTextView = findViewById(R.id.file_download_title_id);
		cancelBtn.setOnClickListener(this);
		okBtn.setOnClickListener(this);
		long filterEndTime = System.currentTimeMillis();
		long filterStartTime = filterEndTime - 86400000;
		BVCU_Search_FileFilter search_fileFilter = new BVCU_Search_FileFilter();
		search_fileFilter.szPUID = deviceID; // 设备ID
		search_fileFilter.iTimeBegin = filterStartTime / 1000; // 截止时间
		search_fileFilter.iTimeEnd = filterEndTime / 1000; // 开始时间
		search_fileFilter.iFileType = BVCU_STORAGE_FILE_TYPE.BVCU_STORAGE_FILE_TYPE_ALL; // 文件类型
		queryFileList(search_fileFilter);
	}

	/**
	 * 查询文件
	 *
	 * @param fileFilter 筛选条件
	 * @return
	 */
	public int queryFileList(BVCU_Search_FileFilter fileFilter) {
		BVCU_Command command = new BVCU_Command();
		command.iMethod = BVCU_Method.BVCU_METHOD_QUERY;
		command.iSubMethod = BVCU_SubMethod.BVCU_SUBMETHOD_SEARCH_LIST;
		command.szTargetID = "NRU_"; // 平台检索填"NRU_"即可
		command.iTargetIndex = 0; // 通道号
		BVCU_Search_Request request = new BVCU_Search_Request();
		request.stSearchInfo = new BVCU_SearchInfo();
		request.stSearchInfo.iPostition = 0; // 起始索引
		request.stSearchInfo.iType = BVCU_SEARCH_TYPE.BVCU_SEARCH_TYPE_FILE; // 查询文件
		request.stSearchInfo.iCount = 10; // 一次查询10条
		request.stFileFilter = fileFilter;
		command.stMsgContent = new BVCU_CmdMsgContent();
		command.stMsgContent.pData = request;
		command.stMsgContent.iDataCount = 1;
		int token = BVCU.getSDK().sendCmd(command);
		Log.d(TAG, "查询文件 token ：" + token);
		return token;
	}

	public void setFileLists(BVCU_Search_FileInfo[] pFileInfo) {
		fileLists.clear();
		if (pFileInfo.length > 0) {
			titleTextView.setText("文件检索");
			for (int i = 0; i < pFileInfo.length; i++) {
				FileDownloadEntity fileDownloadEntity = new FileDownloadEntity();
				fileDownloadEntity.setDownloadStatus(BVCU_FILE_DOWNLOAD_NORMAL);
				String[] fileArr = pFileInfo[i].szFilePath.split("/");
				fileDownloadEntity.setSzLocalFilePath(Environment.getExternalStorageDirectory() + "/download/" + fileArr[fileArr.length - 1]);
				fileDownloadEntity.setSzRemoteFilePath(pFileInfo[i].szFilePath);
				fileDownloadEntity.setFileName(fileArr[fileArr.length - 1]);
				fileDownloadEntity.setSzSourceID(pFileInfo[i].szSourceID);
				fileDownloadEntity.setToken(0);
				fileLists.add(fileDownloadEntity);
			}
		} else {
			titleTextView.setText("文件检索(无文件)");
		}
		fileListViewAdapter = new FileListViewAdapter(context, fileLists);
		fileListView.setAdapter(fileListViewAdapter);
	}

	public ArrayList<FileDownloadEntity> getFileList() {
		return fileLists;
	}

	@Override
	public void setOnDismissListener(@Nullable OnDismissListener listener) {
		super.setOnDismissListener(listener);
	}

	public static final int UPDATE_FILE_LIST = 1;
	public static final int UPDATE_LIST_VIEW = 2;
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case UPDATE_FILE_LIST:
					BVCU_Search_FileInfo[] pFileInfo = (BVCU_Search_FileInfo[]) msg.obj;
					setFileLists(pFileInfo);
					break;
				case UPDATE_LIST_VIEW:
					fileListViewAdapter.notifyDataSetChanged();
					break;
				default:
					break;
			}
		}
	};

	@Override
	public void onClick(View view) {
		if (view.getId() == cancelBtn.getId()) {
			this.dismiss();
		} else if (view.getId() == okBtn.getId()) {
			this.dismiss();
		}
	}
}
