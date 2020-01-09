package com.example.myapplication1;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.TreeView.PuDeviceBean;
import com.example.TreeView.PuDeviceNode;
import com.example.TreeView.PuDeviceTreeAdapter;
import com.smarteye.adapter.BVCU_CmdMsgContent;
import com.smarteye.adapter.BVCU_Command;
import com.smarteye.adapter.BVCU_DialogControlParam;
import com.smarteye.adapter.BVCU_DialogControlParam_Render;
import com.smarteye.adapter.BVCU_DialogInfo;
import com.smarteye.adapter.BVCU_DialogParam;
import com.smarteye.adapter.BVCU_DialogTarget;
import com.smarteye.adapter.BVCU_Display_Param;
import com.smarteye.adapter.BVCU_EVENT_DIALOG;
import com.smarteye.adapter.BVCU_EventCode;
import com.smarteye.adapter.BVCU_Event_DialogCmd;
import com.smarteye.adapter.BVCU_File_TransferInfos;
import com.smarteye.adapter.BVCU_MediaDir;
import com.smarteye.adapter.BVCU_Method;
import com.smarteye.adapter.BVCU_Online_Status;
import com.smarteye.adapter.BVCU_PUChannelInfo;
import com.smarteye.adapter.BVCU_Packet;
import com.smarteye.adapter.BVCU_Result;
import com.smarteye.adapter.BVCU_SEARCH_TYPE;
import com.smarteye.adapter.BVCU_SearchInfo;
import com.smarteye.adapter.BVCU_Search_PUListFilter;
import com.smarteye.adapter.BVCU_Search_Request;
import com.smarteye.adapter.BVCU_Search_Response;
import com.smarteye.adapter.BVCU_SessionInfo;
import com.smarteye.adapter.BVCU_SubDev;
import com.smarteye.adapter.BVCU_SubMethod;
import com.smarteye.bean.JNIMessage;
import com.smarteye.sdk.BVCU;
import com.smarteye.sdk.BVCU_EventCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoPreviewActivity extends AppCompatActivity {
	private PuDeviceTreeAdapter puDeviceTreeAdapter;
	private ListView deviceListView;
	private List<PuDeviceBean> datas = new ArrayList<PuDeviceBean>();
	private String ipStr;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private LinearLayout deviceListLayout;
	private Button refreshBtn, showDeviceBtn;
	private int videoToken; // 正在被打开设备视频的Token 记录此变量用于关闭设备视频
	private String videoPUID; // 正在被打开的设备ID
	private static final String TAG = "VideoPreviewActivity";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_preview_acitivity);
		mSurfaceView = findViewById(R.id.surface_id);
		deviceListLayout = findViewById(R.id.device_list_layout);
		deviceListView = findViewById(R.id.device_list_view_id);
		refreshBtn = findViewById(R.id.refresh_device_button);
		showDeviceBtn = findViewById(R.id.show_device_button);
		BVCU.getSDK().setEventCallback(bvcuEventCallback);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.setKeepScreenOn(true);
		mSurfaceHolder.addCallback(surfaceHolderCallback);
		Bundle bundle = this.getIntent().getExtras();
		ipStr = bundle.getString("ip");
		initData();
		initAction();
		queryPUList();
	}

	private void initData() {
		try {
			puDeviceTreeAdapter = new PuDeviceTreeAdapter(deviceListView, this, datas, 1);
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}
		puDeviceTreeAdapter.setOnTreeNodeClickListener(new PuDeviceTreeAdapter.OnTreeNodeClickListener() {
			@Override
			public void onClick(PuDeviceNode node, int position) {
				if (node.getpId() == 0 || node.getpId() == 1) //点击IP或者设备名直接返回，点击通道打开视频
					return;
				if (videoToken != 0 && videoToken != -1) {
					String openedPUID = videoPUID;
					closeInvite();
					if (!node.getPUID().equals(openedPUID)) {
						openInvite(node);
					}
				} else {
					openInvite(node);
				}
			}
		});
		deviceListView.setAdapter(puDeviceTreeAdapter);
	}

	private void initAction() {
		refreshBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				queryPUList();
			}
		});
		showDeviceBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (deviceListLayout.getVisibility() == View.VISIBLE) {
					deviceListLayout.setVisibility(View.GONE);
					showDeviceBtn.setText(getString(R.string.showList));
				} else {
					deviceListLayout.setVisibility(View.VISIBLE);
					showDeviceBtn.setText(getString(R.string.closeList));
				}
			}
		});
		mSurfaceView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (deviceListLayout.getVisibility() == View.VISIBLE) {
					deviceListLayout.setVisibility(View.GONE);
					showDeviceBtn.setText(getString(R.string.showList));
				}
			}
		});
	}

	private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
		@Override
		public void surfaceCreated(SurfaceHolder surfaceHolder) {
			Log.d(TAG, "··· surfaceCreated");
			if (videoToken != 0 && videoToken != -1) {
				BVCU_DialogControlParam stControlParam = new BVCU_DialogControlParam();
				stControlParam.stRender = new BVCU_DialogControlParam_Render();
				stControlParam.stRender.hWnd = mSurfaceHolder.getSurface();
				BVCU_Display_Param display_param = new BVCU_Display_Param();
				display_param.fMulZoom = 1;
				display_param.iAngle = 90;
				stControlParam.stRender.stDisplayParam = display_param;
				BVCU.getSDK().controlDialog(videoToken, stControlParam);
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
			Log.d(TAG, "··· surfaceChanged");
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
			Log.d(TAG, "··· surfaceDestroyed");
			if (videoToken != 0 && videoToken != -1) {
				BVCU_DialogControlParam stControlParam = new BVCU_DialogControlParam();
				stControlParam.stRender = new BVCU_DialogControlParam_Render();
				stControlParam.stRender.hWnd = null;
				BVCU.getSDK().controlDialog(videoToken, stControlParam);
			}
		}
	};

	/**
	 * 筛选设备信息放入datas,填充treeListView
	 * 将BVCU_PUChannelInfo 转化为 PuDeviceBean，用户可根据自己需求实现，此demo中转化是为了实现折叠自定义ListView
	 *
	 * @param bvcu_PUChannelInfos 已获取的所有在线设备信息集合
	 */
	private void setDeviceDate(List<BVCU_PUChannelInfo> bvcu_PUChannelInfos) {
		datas.clear();
		datas.add(new PuDeviceBean(1, 0, ipStr, null, 0, 0, null, 0));//第一行IP/端口信息栏
		if (bvcu_PUChannelInfos.size() > 0) {
			List<BVCU_PUChannelInfo> bvcu_lists = new ArrayList<BVCU_PUChannelInfo>();
			for (BVCU_PUChannelInfo bvcu_puChannelInfo : bvcu_PUChannelInfos) {//筛选出在线设备先添加
				if (bvcu_puChannelInfo.iOnlineStatus == BVCU_Online_Status.BVCU_ONLINE_STATUS_OFFLINE) {
					continue;
				}
				bvcu_lists.add(bvcu_puChannelInfo);
			}
			int k = 2;//从2开始，前面服务器地址作为第一行
			for (int i = 0; i < bvcu_lists.size(); i++) {
				datas.add(new PuDeviceBean(k, 1, bvcu_lists.get(i).szPUName,
						bvcu_lists.get(i).szPUID, bvcu_lists.get(i).iOnlineStatus, 0, null, 0));
				k++;
			}
			for (int i = 0; i < bvcu_lists.size(); i++) {
				for (int j = 0; j < bvcu_lists.get(i).pChannel.size(); j++) {
					if (bvcu_lists.get(i).pChannel.get(j).iChannelIndex >= BVCU_SubDev.BVCU_SUBDEV_INDEXMAJOR_MIN_CHANNEL
							&& bvcu_lists.get(i).pChannel.get(j).iChannelIndex <= BVCU_SubDev.BVCU_SUBDEV_INDEXMAJOR_MAX_CHANNEL) {
						datas.add(new PuDeviceBean(k, i + 2, bvcu_lists.get(i).pChannel.get(j).szName,
								bvcu_lists.get(i).szPUID, 0, bvcu_lists.get(i).pChannel.get(j).iChannelIndex,
								bvcu_lists.get(i).szPUName, bvcu_lists.get(i).pChannel.get(j).iMediaDir));
						k++;
					}
				}
			}
		}
	}

	/**
	 * 打开设备视频
	 *
	 * @param node 参数实体
	 */
	private void openInvite(PuDeviceNode node) {
		int iDir = node.getiAVStreamDir();
		if (node.getiAVStreamDir() != -1) { //当前设备媒体最大能力
			if (iDir != 0) {
				if ((iDir & BVCU_MediaDir.BVCU_MEDIADIR_AUDIORECV) == BVCU_MediaDir.BVCU_MEDIADIR_AUDIORECV
						&& (iDir & BVCU_MediaDir.BVCU_MEDIADIR_VIDEORECV) == BVCU_MediaDir.BVCU_MEDIADIR_VIDEORECV) {
					iDir = 10;
				}
			}
		}
		videoToken = 0;
		videoPUID = null;
		BVCU_DialogInfo dialogInfo = new BVCU_DialogInfo();
		dialogInfo.stParam = new BVCU_DialogParam();
		dialogInfo.stParam.iTargetCount = 1;
		dialogInfo.stParam.pTarget = new BVCU_DialogTarget[1];
		dialogInfo.stParam.pTarget[0] = new BVCU_DialogTarget();
		dialogInfo.stParam.pTarget[0].iIndexMajor = node.getTargetIndex();
		dialogInfo.stParam.pTarget[0].iIndexMinor = -1;
		dialogInfo.stParam.pTarget[0].szID = node.getPUID();
		dialogInfo.stParam.iAVStreamDir = iDir;
		dialogInfo.stControlParam = new BVCU_DialogControlParam();
		dialogInfo.stControlParam.stRender = new BVCU_DialogControlParam_Render();
		dialogInfo.stControlParam.stRender.hWnd = mSurfaceHolder.getSurface();
		BVCU_Display_Param display_param = new BVCU_Display_Param();
		display_param.fMulZoom = 1;
		display_param.iAngle = 270;
		dialogInfo.stControlParam.stRender.stDisplayParam = display_param;
		videoToken = BVCU.getSDK().openDialog(dialogInfo);
		videoPUID = node.getPUID();
		Log.d(TAG, "videoToken : " + videoToken);
	}

	/**
	 * 关闭设备视频
	 */
	private void closeInvite() {
		if (videoToken != 0 && videoToken != -1) {
			BVCU.getSDK().closeDialog(videoToken);
			videoToken = 0;
			videoPUID = null;
		}
	}

	/**
	 * 查询Pu设备列表
	 *
	 * @return
	 */
	private void queryPUList() {
		BVCU_Command command = new BVCU_Command();
		command.iMethod = BVCU_Method.BVCU_METHOD_QUERY;
		command.iSubMethod = BVCU_SubMethod.BVCU_SUBMETHOD_SEARCH_LIST;
		BVCU_Search_Request request = new BVCU_Search_Request();
		request.stSearchInfo = new BVCU_SearchInfo();
		request.stSearchInfo.iPostition = 0; // 查询起始位置
		request.stSearchInfo.iType = BVCU_SEARCH_TYPE.BVCU_SEARCH_TYPE_PULIST;
		request.stSearchInfo.iCount = 256; // 一次查询数目，可循环查询，查询到的总数目为下一次查询的起始位置
		request.stPUListFilter = new BVCU_Search_PUListFilter();
		request.stPUListFilter.iOnlineStatus = 1;//备在线状态 0：全部设备 1：在线设备 2：不在线设备
		request.stPUListFilter.szIDOrName = ""; // 设备名称(或PU ID）  空：不作为索引条件 （名称或ID配置）
		command.stMsgContent = new BVCU_CmdMsgContent();
		command.stMsgContent.pData = request;
		command.stMsgContent.iDataCount = 1;
		BVCU.getSDK().sendCmd(command);
	}

	private BVCU_EventCallback bvcuEventCallback = new BVCU_EventCallback() {
		@Override
		public void OnSessionEvent(int hSession, int iEventCode, int iResult, BVCU_SessionInfo bvcu_sessionInfo) {
			Log.d(TAG, "hSession=" + hSession + ",iEventCode=" + iEventCode + ",iResult=" + iResult);

			//iEventCode=2,iResult=0 注销
			if (iEventCode == BVCU_EventCode.BVCU_EVENT_SESSION_OPEN && iResult == BVCU_Result.BVCU_RESULT_S_OK) {
				Log.d(TAG, "登录成功");
			} else {
				Log.d(TAG, "下线");
				finish();
			}
		}

		@Override
		public int OnSessionCommand(int i, BVCU_Command bvcu_command) {
			return 0;
		}

		@Override
		public int OnPasvDialogCmd(int i, int i1, BVCU_DialogParam bvcu_dialogParam) {
			return 0;
		}

		@Override
		public void OnPasvDialogEvent(int i, int i1, BVCU_Event_DialogCmd bvcu_event_dialogCmd) {

		}

		@Override
		public void OnDialogEvent(int hDialog, int iEventCode, BVCU_Event_DialogCmd pParam) {
			Log.d(TAG, "DIALOG_OPEN命令 " + iEventCode);
			if (iEventCode == BVCU_EVENT_DIALOG.BVCU_EVENT_DIALOG_OPEN) {
				int iResult = pParam.iResult;
				if (iResult == BVCU_Result.BVCU_RESULT_S_OK) {
					Toast.makeText(VideoPreviewActivity.this, getString(R.string.openVideoSuccess), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(VideoPreviewActivity.this, getString(R.string.openVideoFail), Toast.LENGTH_SHORT).show();
				}
			}
		}

		@Override
		public void OnGetDialogInfo(int i, BVCU_DialogInfo bvcu_dialogInfo) {

		}

		@Override
		public int OnCmdEvent(int hCmd, BVCU_Command pCommand, int iResult) {
			Log.d(TAG, "OnCmdEvent");
			switch (pCommand.iMethod) {
				case BVCU_Method.BVCU_METHOD_QUERY:
					if (pCommand.iSubMethod == BVCU_SubMethod.BVCU_SUBMETHOD_SEARCH_LIST) {
						Log.d(TAG, "查询列表 回复");
						BVCU_Search_Response rsp = (BVCU_Search_Response) pCommand.stMsgContent.pData;
						if (rsp.pPUList != null && rsp.pPUList.length > 0) {
							setDeviceDate(Arrays.asList(rsp.pPUList));
							Message message = Message.obtain();
							message.what = MESSAGE_REFRESH_DEVICE_LIST;
							handler.sendMessage(message);
						}
					}
					break;
				case BVCU_Method.BVCU_METHOD_CONTROL:
					break;
				default:
					break;
			}
			return 0;
		}

		@Override
		public int DialogAfterRecv(int i, BVCU_Packet bvcu_packet) {
			return 0;
		}

		@Override
		public void OnFileTransferInfo(BVCU_File_TransferInfos[] bvcu_file_transferInfos) {

		}

		@Override
		public void OnElecMapAlarm(int i) {

		}

		@Override
		public void OnElecMapConfigUpdate(String s, String s1) {

		}

		@Override
		public void OnNotifyMessage(JNIMessage jniMessage) {

		}
	};

	private static final int MESSAGE_REFRESH_DEVICE_LIST = 1001;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case MESSAGE_REFRESH_DEVICE_LIST:
					try {
						puDeviceTreeAdapter.refreshUI(datas);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					puDeviceTreeAdapter.notifyDataSetChanged();
					break;
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "··· onDestroy");
		closeInvite();
	}
}
