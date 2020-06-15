package com.example.myapplication1;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.TreeView.PuDeviceBean;
import com.example.TreeView.PuDeviceNode;
import com.example.TreeView.PuDeviceTreeAdapter;
import com.example.dialog.FileDownloadDialog;
import com.example.dialog.OsdSettingDialog;
import com.example.entity.FileDownloadEntity;
import com.smarteye.adapter.BVCU_CmdMsgContent;
import com.smarteye.adapter.BVCU_Command;
import com.smarteye.adapter.BVCU_DATA_TYPE;
import com.smarteye.adapter.BVCU_DialogControlParam;
import com.smarteye.adapter.BVCU_DialogControlParam_Network;
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
import com.smarteye.adapter.BVCU_PUCFG_EncoderChannel;
import com.smarteye.adapter.BVCU_PUCFG_GPSData;
import com.smarteye.adapter.BVCU_PUCFG_VideoIn;
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

import static com.smarteye.adapter.BVCU_EVENT_DIALOG.BVCU_EVENT_DIALOG_CLOSE;

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
	private PopupWindow popupLongWindow;
	private Button osdSettingBtn, fileDownloadBtn;
	private CheckBox talkCbx, gpsCbx;
	private TextView deviceListTitle, gpsInfoText;
	private int sizeWidth, sizeHeight;
	private OsdSettingDialog osdSettingDialog;
	private FileDownloadDialog fileDownloadDialog;
	public final static int TRANSFER_SUCCESS = 0;// 传输成功
	public final static int TRANSFER_ING = 1;// 传输中
	public final static int TRANSFER_FAIL = -1;// 传输失败
	public final static int DOWNLOAD = 0;// 下载
	public final static int UPLOAD = 1;// 上传
	private String talkDeviceId, gpsDeviceId; // 记录当前正在对讲和请求位置信息的设备ID
	private int talkToken, gpsToken;// 记录当前正在对讲和请求位置信息的Token
	private RecorderUtils recorderUtils;
	private static final String TAG = "VideoPreviewActivity";

	// 1、点击设备名可展开通道列表，点击通道即可打开该通道的视频，再次点击通道关闭视频
 	// 2、长按设备名可查看更多功能：对讲、请求设备位置、叠加信息设置、文件检索
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_preview_acitivity);
		mSurfaceView = findViewById(R.id.surface_id);
		deviceListTitle = findViewById(R.id.device_list_title);
		gpsInfoText = findViewById(R.id.gps_info_text_id);
		deviceListLayout = findViewById(R.id.device_list_layout);
		deviceListView = findViewById(R.id.device_list_view_id);
		refreshBtn = findViewById(R.id.refresh_device_button);
		showDeviceBtn = findViewById(R.id.show_device_button);
		MainActivity.instance.setBVCU_EventCallback(bvcuEventCallback);
		recorderUtils = new RecorderUtils();
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

			@Override
			public void onLongClick(PuDeviceNode node, View view, int position) {
				if (node.getpId() == 0 || node.getpId() != 1) //点击IP或者设备名直接返回，点击通道打开视频
					return;
				showPttPopupView(node.getPUID(), view, position);
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

	private void showPttPopupView(String deviceId, View view, int position) {
		int[] location = new int[2];
		view.getLocationInWindow(location);
		int x = view.getWidth() / 3;
		int y = view.getHeight() / 2;
		sizeWidth = view.getWidth() / 2;
		sizeHeight = view.getHeight();
		initPopupLongWindow(deviceId);
		popupLongWindow.showAtLocation(view, Gravity.NO_GRAVITY,
				location[0] + x, location[1] + y);
	}

	/**
	 * 创建PopupWindow
	 */
	private void initPopupLongWindow(final String deviceId) {
		if (null != popupLongWindow) {
			popupLongWindow.dismiss();
			return;
		}
		Toast.makeText(this, deviceId, Toast.LENGTH_SHORT).show();
		View popupWindow_view = getLayoutInflater().inflate(
				R.layout.device_long_menu, null, false);
		osdSettingBtn = popupWindow_view.findViewById(R.id.osd_setting_id);
		fileDownloadBtn = popupWindow_view.findViewById(R.id.file_download_id);
		talkCbx = popupWindow_view.findViewById(R.id.talk_check_id);
		gpsCbx = popupWindow_view.findViewById(R.id.gps_check_id);
		popupLongWindow = new PopupWindow(popupWindow_view, sizeWidth,
				4 * sizeHeight, true);
		talkCbx.setChecked(deviceId.equals(talkDeviceId));
		gpsCbx.setChecked(deviceId.equals(gpsDeviceId));
		osdSettingBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				osdSettingDialog = new OsdSettingDialog(VideoPreviewActivity.this, deviceId);
				osdSettingDialog.setCancelable(false);
				osdSettingDialog.show();
			}
		});
		fileDownloadBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				fileDownloadDialog = new FileDownloadDialog(VideoPreviewActivity.this, deviceId);
				fileDownloadDialog.setCancelable(false);
				fileDownloadDialog.show();
			}
		});
		talkCbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				pttCheckBoxClick(b, deviceId);
			}
		});
		gpsCbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				gpsCheckBoxClick(b, deviceId);
			}
		});
		popupWindow_view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (popupLongWindow != null && popupLongWindow.isShowing()) {
					popupLongWindow.dismiss();
					popupLongWindow = null;
				}
				return false;
			}
		});
		popupLongWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				if (popupLongWindow != null) {
					popupLongWindow = null;
				}
			}
		});
	}

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
		request.stPUListFilter.iOnlineStatus = 1;//设备在线状态 0：全部设备 1：在线设备 2：不在线设备
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
			if (iEventCode == BVCU_EVENT_DIALOG.BVCU_EVENT_DIALOG_OPEN) {
				int iResult = pParam.iResult;
				int avDir = pParam.pDialogParam.iAVStreamDir;
				if ((avDir & BVCU_MediaDir.BVCU_MEDIADIR_AUDIOSEND) == BVCU_MediaDir.BVCU_MEDIADIR_AUDIOSEND) {
					Message message = Message.obtain();
					message.what = MESSAGE_SHOW_TALK_STATUS;
					message.arg1 = iResult;
					handler.sendMessage(message);
				} else if ((avDir & BVCU_MediaDir.BVCU_MEDIADIR_VIDEORECV) == BVCU_MediaDir.BVCU_MEDIADIR_VIDEORECV
						&& (avDir & BVCU_MediaDir.BVCU_MEDIADIR_AUDIORECV) == BVCU_MediaDir.BVCU_MEDIADIR_AUDIORECV) {
					if (iResult == BVCU_Result.BVCU_RESULT_S_OK) {
						showToast(getString(R.string.openVideoSuccess));
					} else {
						showToast(getString(R.string.openVideoFail));
					}
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
						if (rsp.pPUList != null && rsp.pPUList.length > 0) { // PU列表回复
							setDeviceDate(Arrays.asList(rsp.pPUList));
							Message message = Message.obtain();
							message.what = MESSAGE_REFRESH_DEVICE_LIST;
							handler.sendMessage(message);
						}
						if (rsp.pFileInfo != null && fileDownloadDialog != null) { // 文件列表回复
							Message message = Message.obtain();
							message.what = FileDownloadDialog.UPDATE_FILE_LIST;
							message.obj = rsp.pFileInfo;
							fileDownloadDialog.handler.sendMessage(message);
						}
					} else if (pCommand.iSubMethod == BVCU_SubMethod.BVCU_SUBMETHOD_PU_ENCODERCHANNEL) {
						if (iResult != BVCU_Result.BVCU_RESULT_S_OK) {
							Log.w(TAG, "cmd query pu encoder channel response fail: " + iResult);
							if (osdSettingDialog != null) {
								showToast("查询叠加信息失败");
							}
						} else {
							try {
								BVCU_PUCFG_EncoderChannel encoderChannel = (BVCU_PUCFG_EncoderChannel) pCommand.stMsgContent.pData;
								if (osdSettingDialog != null) {
									osdSettingDialog.setEncoderChannel(encoderChannel);
								}
							} catch (Exception e) {
								Log.w(TAG, "cmd query pu encoder channel response error: " + e.toString());
							}
						}
					} else if (pCommand.iSubMethod == BVCU_SubMethod.BVCU_SUBMETHOD_PU_VIDEOIN) {
						if (iResult != BVCU_Result.BVCU_RESULT_S_OK) {
							Log.w(TAG, "cmd query pu video in response fail: " + iResult);
							if (osdSettingDialog != null) {
								showToast("查询视频输入配置失败");
							}
						} else {
							try {
								BVCU_PUCFG_VideoIn videoIn = (BVCU_PUCFG_VideoIn) (pCommand.stMsgContent.pData);
								if (osdSettingDialog != null) {
									osdSettingDialog.setBVCU_PUCFG_VideoIn(videoIn);
								}
							} catch (Exception e) {
								Log.w(TAG, "cmd query pu encoder channel response error: " + e.toString());
							}
						}
					}
					break;
				case BVCU_Method.BVCU_METHOD_CONTROL:
					if (pCommand.iSubMethod == BVCU_SubMethod.BVCU_SUBMETHOD_PU_ENCODERCHANNEL) {
						if (iResult == BVCU_Result.BVCU_RESULT_S_OK) {
							showToast("设置叠加信息成功");
						} else {
							showToast("设置叠加信息失败");
						}
					} else if (pCommand.iSubMethod == BVCU_SubMethod.BVCU_SUBMETHOD_PU_VIDEOIN) {
						if (iResult == BVCU_Result.BVCU_RESULT_S_OK) {
							showToast("设置视频输入配置成功");
						} else {
							showToast("设置视频输入配置失败");
						}
					}
					break;
				default:
					break;
			}
			return 0;
		}

		@Override
		public int DialogAfterRecv(int hDialog, BVCU_Packet bvcu_packet) {
			int iResult = BVCU_Result.BVCU_RESULT_S_OK;
			switch (bvcu_packet.iDataType) {
				case BVCU_DATA_TYPE.BVCU_DATA_TYPE_AUDIO:
					break;
				case BVCU_DATA_TYPE.BVCU_DATA_TYPE_VIDEO:
					break;
				case BVCU_DATA_TYPE.BVCU_DATA_TYPE_GPS: // 此处返回请求的设备的定位信息
					// hDialog对应请求位置时调用openDialog()返回的Token,
					// 所以在请求多个设备的定位信息时请保存token和设备的对应关系用于此处区分不同的定位信息
					Message message = Message.obtain();
					message.what = MESSAGE_SHOW_GPS_INFO;
					message.obj = bvcu_packet.pData;
					handler.sendMessage(message);
					break;
				case BVCU_DATA_TYPE.BVCU_DATA_TYPE_TSP:
					break;
				default:
					break;
			}
			return iResult;
		}

		@Override
		public void OnFileTransferInfo(BVCU_File_TransferInfos[] bvcu_file_transferInfos) {
			// 文件传输回调  调用下载方法时会返回一个token,此处根据token区分是哪一条下载的回调
			if (fileDownloadDialog == null) return;
			ArrayList<FileDownloadEntity> fileLists = fileDownloadDialog.getFileList();
			if (fileLists == null || fileLists.size() == 0) return;
			for (BVCU_File_TransferInfos info : bvcu_file_transferInfos) {
				for (FileDownloadEntity entity : fileLists) {
					if (info.token == entity.getToken()) {
						if (info.event == BVCU_EVENT_DIALOG_CLOSE && info.result >= TRANSFER_SUCCESS) {
							// 传输成功
							entity.setDownloadStatus(FileDownloadDialog.BVCU_FILE_DOWNLOAD_SUCCESS);
							entity.setDownloadPercent(100);
						} else if (info.result < TRANSFER_SUCCESS) {
							// 传输失败
							entity.setDownloadStatus(FileDownloadDialog.BVCU_FILE_DOWNLOAD_FAILED);
						} else {
							// 传输中
							int percent = (int) ((float) info.info.iTransferBytes * 100 / info.info.iTotalBytes);
							entity.setDownloadStatus(FileDownloadDialog.BVCU_FILE_DOWNLOAD_ING);
							entity.setDownloadPercent(percent);
						}
					}
				}
			}
			fileDownloadDialog.handler.sendEmptyMessage(FileDownloadDialog.UPDATE_LIST_VIEW);
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

	private static final int MESSAGE_REFRESH_DEVICE_LIST = 1001;    // 更新设备列表
	private static final int MESSAGE_SHOW_GPS_INFO = 1002;          // 显示设备GPS信息
	private static final int MESSAGE_SHOW_TALK_STATUS = 1003;       // 显示对讲状态
	private static final int MESSAGE_SHOW_TOAST_INFO = 1004;        // 显示Toast信息
	private Handler handler = new Handler(Looper.getMainLooper()) {
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
				case MESSAGE_SHOW_GPS_INFO:
					BVCU_PUCFG_GPSData gpsData = (BVCU_PUCFG_GPSData) msg.obj;
					gpsInfoText.setVisibility(View.VISIBLE);
					gpsInfoText.setText("Lat: " + gpsData.iLatitude / 10000000.0 + "\nLon: " + gpsData.iLongitude / 10000000.0);
					break;
				case MESSAGE_SHOW_TALK_STATUS:
					if (msg.arg1 == BVCU_Result.BVCU_RESULT_S_OK) {
						deviceListTitle.setText("打开对讲成功");
					} else {
						deviceListTitle.setText("打开对讲失败");
						talkDeviceId = "0000";
					}
					break;
				case MESSAGE_SHOW_TOAST_INFO:
					Toast.makeText(VideoPreviewActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};

	private void showToast(String toastInfo) {
		Message message = Message.obtain();
		message.what = MESSAGE_SHOW_TOAST_INFO;
		message.obj = toastInfo;
		handler.sendMessage(message);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "··· onDestroy");
		MainActivity.instance.setBVCU_EventCallback(null);
		recorderUtils.stopRecorder();
		closeInvite();
		if (talkToken != 0)
			BVCU.getSDK().closeDialog(talkToken);
		if (gpsToken != 0)
			BVCU.getSDK().closeDialog(gpsToken);
	}

	private void pttCheckBoxClick(boolean isChecked, String deviceID) {
		if (deviceID.equals(talkDeviceId)) { // 操作的是当前正在对讲的设备
			if (!isChecked) { // 关闭对讲
				if (talkToken != 0) {
					BVCU.getSDK().closeDialog(talkToken);
					talkToken = 0;
					talkDeviceId = "0000";
					deviceListTitle.setText("设备列表");
					recorderUtils.stopRecorder();
					if (popupLongWindow != null
							&& popupLongWindow.isShowing()) {
						popupLongWindow.dismiss();
						popupLongWindow = null;
					}
				}
			} else { // 打开对讲
				if (talkToken != 0) {
					BVCU.getSDK().closeDialog(talkToken);
					talkToken = 0;
					talkDeviceId = "0000";
					recorderUtils.stopRecorder();
				}
				int mediaDir = 0;
				mediaDir ^= BVCU_MediaDir.BVCU_MEDIADIR_AUDIORECV;
				mediaDir ^= BVCU_MediaDir.BVCU_MEDIADIR_AUDIOSEND;
				inviteTalk(mediaDir, deviceID);
				recorderUtils.startRecorder();
			}
		} else {
			if (isChecked) {
				if (talkToken != 0) {
					BVCU.getSDK().closeDialog(talkToken);
					talkToken = 0;
					talkDeviceId = "0000";
					recorderUtils.stopRecorder();
				}
				int mediaDir = 0;
				mediaDir ^= BVCU_MediaDir.BVCU_MEDIADIR_AUDIORECV;
				mediaDir ^= BVCU_MediaDir.BVCU_MEDIADIR_AUDIOSEND;
				inviteTalk(mediaDir, deviceID);
				recorderUtils.startRecorder();
			} else {
				if (talkToken != 0) {
					BVCU.getSDK().closeDialog(talkToken);
					talkToken = 0;
					talkDeviceId = "0000";
					recorderUtils.stopRecorder();
					deviceListTitle.setText("设备列表");
					if (popupLongWindow != null
							&& popupLongWindow.isShowing()) {
						popupLongWindow.dismiss();
						popupLongWindow = null;
					}
				}
			}
		}
	}

	/**
	 * 打开设备对讲
	 * 具体参数含义请参考：http://up.besovideo.com:7780/android_sdk_bvcu_api/index.html
	 *
	 * @param iAVStreamDir 媒体方向 音频结束 + 音频发送
	 * @param deviceID     目标设备ID
	 */
	private void inviteTalk(int iAVStreamDir, String deviceID) {
		BVCU_DialogInfo dialogInfo = new BVCU_DialogInfo();
		dialogInfo.stParam = new BVCU_DialogParam();
		dialogInfo.stParam.iTargetCount = 1;
		dialogInfo.stParam.pTarget = new BVCU_DialogTarget[1];
		dialogInfo.stParam.pTarget[0] = new BVCU_DialogTarget();
		dialogInfo.stParam.pTarget[0].iIndexMajor = 0;
		dialogInfo.stParam.pTarget[0].iIndexMinor = -1;
		dialogInfo.stParam.pTarget[0].szID = deviceID;
		dialogInfo.stParam.iAVStreamDir = iAVStreamDir;
		dialogInfo.stControlParam = new BVCU_DialogControlParam();
		dialogInfo.stControlParam.stRender = new BVCU_DialogControlParam_Render();
		if (dialogInfo.stControlParam.stNetwork == null)
			dialogInfo.stControlParam.stNetwork = new BVCU_DialogControlParam_Network();
		dialogInfo.stControlParam.stNetwork.iDelayMax = 5000;
		dialogInfo.stControlParam.stNetwork.iDelayMin = 500;
		dialogInfo.stControlParam.stNetwork.iDelayVsSmooth = 3; // 流畅性/实时性
		// stNetwork.iTimeOut为默认30S,底层未赋值,此处不需修改,修改也无效
		talkToken = BVCU.getSDK().openDialog(dialogInfo);
		talkDeviceId = deviceID;
		deviceListTitle.setText("正在打开对讲");
		if (popupLongWindow != null && popupLongWindow.isShowing()) {
			popupLongWindow.dismiss();
			popupLongWindow = null;
		}
	}

	/**
	 * 请求设备定位信息，可以连续请求多个设备的定位信息
	 * 此处只用于展示效果，请求定位信息时会关闭上一次的请求，防止返回太多定位信息不利于展示
	 *
	 * @param isChecked
	 * @param deviceID
	 */
	private void gpsCheckBoxClick(boolean isChecked, String deviceID) {
		if (deviceID.equals(gpsDeviceId)) { // 操作的是当前正在返回位置的设备
			if (!isChecked) {
				if (gpsToken != 0) {
					BVCU.getSDK().closeDialog(gpsToken);
					gpsToken = 0;
					gpsDeviceId = "0000";
					gpsInfoText.setText("");
					gpsInfoText.setVisibility(View.GONE);
					if (popupLongWindow != null
							&& popupLongWindow.isShowing()) {
						popupLongWindow.dismiss();
						popupLongWindow = null;
					}
				}
			} else {
				if (gpsToken != 0) {
					BVCU.getSDK().closeDialog(gpsToken);
					gpsToken = 0;
					gpsDeviceId = "0000";
				}
				inviteGPS(deviceID);
			}
		} else {
			if (isChecked) {
				if (gpsToken != 0) {
					BVCU.getSDK().closeDialog(gpsToken);
					gpsToken = 0;
					gpsDeviceId = "0000";
				}
				inviteGPS(deviceID);
			} else {
				if (gpsToken != 0) {
					BVCU.getSDK().closeDialog(gpsToken);
					gpsToken = 0;
					gpsDeviceId = "0000";
					gpsInfoText.setText("");
					gpsInfoText.setVisibility(View.GONE);
					if (popupLongWindow != null
							&& popupLongWindow.isShowing()) {
						popupLongWindow.dismiss();
						popupLongWindow = null;
					}
				}
			}
		}
	}

	/**
	 * 请求设备定位信息
	 *
	 * @param szID 目标设备ID
	 */
	private void inviteGPS(String szID) {
		BVCU_DialogInfo dialogInfo = new BVCU_DialogInfo();
		dialogInfo.stParam = new BVCU_DialogParam();
		dialogInfo.stParam.iTargetCount = 1;
		dialogInfo.stParam.pTarget = new BVCU_DialogTarget[1];
		dialogInfo.stParam.pTarget[0] = new BVCU_DialogTarget();
		dialogInfo.stParam.pTarget[0].iIndexMajor = BVCU_SubDev.BVCU_SUBDEV_INDEXMAJOR_MIN_GPS;
		dialogInfo.stParam.pTarget[0].iIndexMinor = -1;
		dialogInfo.stParam.pTarget[0].szID = szID;
		dialogInfo.stParam.iAVStreamDir = BVCU_MediaDir.BVCU_MEDIADIR_DATARECV;
		dialogInfo.stControlParam = new BVCU_DialogControlParam();
		dialogInfo.stControlParam.stRender = new BVCU_DialogControlParam_Render();
		dialogInfo.stControlParam.stRender.hWnd = null;
		if (dialogInfo.stControlParam.stNetwork == null)
			dialogInfo.stControlParam.stNetwork = new BVCU_DialogControlParam_Network();
		dialogInfo.stControlParam.stNetwork.iDelayMax = 5000;
		dialogInfo.stControlParam.stNetwork.iDelayMin = 500;
		dialogInfo.stControlParam.stNetwork.iDelayVsSmooth = 3; // 流畅性/实时性
		// stNetwork.iTimeOut为默认30S,底层未赋值,此处不需修改,修改也无效
		gpsToken = BVCU.getSDK().openDialog(dialogInfo);
		gpsDeviceId = szID;
	}

}
