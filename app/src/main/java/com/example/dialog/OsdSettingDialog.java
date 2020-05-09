package com.example.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication1.R;
import com.smarteye.adapter.BVCU_CmdMsgContent;
import com.smarteye.adapter.BVCU_Command;
import com.smarteye.adapter.BVCU_Method;
import com.smarteye.adapter.BVCU_PUCFG_EncoderChannel;
import com.smarteye.adapter.BVCU_PUCFG_OVERLAY;
import com.smarteye.adapter.BVCU_PUCFG_VideoIn;
import com.smarteye.adapter.BVCU_SubMethod;
import com.smarteye.sdk.BVCU;

/**
 * OSD相关设置Dialog
 */
public class OsdSettingDialog extends Dialog implements View.OnClickListener {
	private static final String TAG = "OsdSettingDialog";
	private Context context;
	private String deviceID;
	private TextView osdSettingTitle, timeXtext, timeYtext, gpsXtext, gpsYtext, wordsXtext,
			wordYtext, channelXtext, channelYtext;
	private CheckBox timeEnableCbx, alarmEnableCbx, gpsEnableCbx, wordsEnableCbx, channelEnableCbx;
	private EditText editText;
	private BVCU_PUCFG_EncoderChannel encoderChannel;
	private BVCU_PUCFG_VideoIn bvcu_PUCFG_VideoIn;
	private int tempNum;
	private Button cancelBtn, okBtn;

	public OsdSettingDialog(@NonNull Context context, String deviceId) {
		super(context, R.style.login_dialog_style);
		this.context = context;
		this.deviceID = deviceId;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.osd_setting_dialog_layout);
		osdSettingTitle = findViewById(R.id.osd_setting_title_id);
		timeXtext = findViewById(R.id.time_x_text_id);
		timeYtext = findViewById(R.id.time_y_text_id);
		gpsXtext = findViewById(R.id.gps_x_text_id);
		gpsYtext = findViewById(R.id.gps_y_text_id);
		wordsXtext = findViewById(R.id.words_x_text_id);
		wordYtext = findViewById(R.id.words_y_text_id);
		channelXtext = findViewById(R.id.channel_x_text_id);
		channelYtext = findViewById(R.id.channel_y_text_id);
		timeEnableCbx = findViewById(R.id.time_enable_cbx);
		alarmEnableCbx = findViewById(R.id.alarm_enable_cbx);
		gpsEnableCbx = findViewById(R.id.gps_enable_cbx);
		wordsEnableCbx = findViewById(R.id.words_enable_cbx);
		channelEnableCbx = findViewById(R.id.channels_enable_cbx);
		editText = findViewById(R.id.edit_text_id);
		cancelBtn = findViewById(R.id.cancel_btn_id);
		okBtn = findViewById(R.id.ok_btn_id);
		cancelBtn.setOnClickListener(this);
		okBtn.setOnClickListener(this);
		queryOsdConfig(deviceID, BVCU_SubMethod.BVCU_SUBMETHOD_PU_ENCODERCHANNEL);
		queryOsdConfig(deviceID, BVCU_SubMethod.BVCU_SUBMETHOD_PU_VIDEOIN);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == cancelBtn.getId()) {
			this.dismiss();
		} else if (view.getId() == okBtn.getId()) {
			if (bvcu_PUCFG_VideoIn != null) {
				String oldTitle = bvcu_PUCFG_VideoIn.szOSDTitle;
				int startIndex = oldTitle.indexOf("\n");
				if (startIndex >= 0) {
					bvcu_PUCFG_VideoIn.szOSDTitle = editText.getText().toString() + "\n" + oldTitle.substring(startIndex);
				}
				controlOsdInfoConfig();
			}
			controlOsdEnableConfig();
		}
	}

	private void showOsdContent() {
		if (bvcu_PUCFG_VideoIn != null && encoderChannel != null) {
			osdSettingTitle.setText("叠加信息设置");
		}
		if (encoderChannel != null) {
			// 只设置传输流
			if (encoderChannel.pParams.length == 2) {
				tempNum = 1;
			} else if (encoderChannel.pParams.length == 1) {
				tempNum = 0;
			}
			int overlay = encoderChannel.pParams[tempNum].pstParams[0].iOverlay;
			if ((overlay & BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_TIME) == BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_TIME) {
				timeEnableCbx.setChecked(true);
			}
			if ((overlay & BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_TEXT) == BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_TEXT) {
				wordsEnableCbx.setChecked(true);
			}
			if ((overlay & BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_GPS) == BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_GPS) {
				gpsEnableCbx.setChecked(true);
			}
			if ((overlay & BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_ALARM) == BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_ALARM) {
				alarmEnableCbx.setChecked(true);
			}
			if ((overlay & BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_NAME) == BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_NAME) {
				channelEnableCbx.setChecked(true);
			}
		}
		if (bvcu_PUCFG_VideoIn != null) {
			timeXtext.setText("X : " + bvcu_PUCFG_VideoIn.stOSDTimePos.iLeft);
			timeYtext.setText("Y : " + bvcu_PUCFG_VideoIn.stOSDTimePos.iTop);
			gpsXtext.setText("X : " + bvcu_PUCFG_VideoIn.stOSDGPSPos.iLeft);
			gpsYtext.setText("Y : " + bvcu_PUCFG_VideoIn.stOSDGPSPos.iTop);
			wordsXtext.setText("X : " + bvcu_PUCFG_VideoIn.stOSDTitlePos.iLeft);
			wordYtext.setText("Y : " + bvcu_PUCFG_VideoIn.stOSDTitlePos.iTop);
			channelXtext.setText("X :" + bvcu_PUCFG_VideoIn.stOSDNamePos.iLeft);
			channelYtext.setText("Y :" + bvcu_PUCFG_VideoIn.stOSDNamePos.iTop);
			Log.d(TAG, "szOSDTitle : " + bvcu_PUCFG_VideoIn.szOSDTitle);
			String[] osdTitleArr = bvcu_PUCFG_VideoIn.szOSDTitle.split("\n"); // 叠加文字和报警文字信息均保存在szOSDTitle字段中，使用"\n"分隔
			if (osdTitleArr.length >= 1) editText.setText(osdTitleArr[0]);
		}
	}

	/**
	 * 查询OSD信息，叠加信息使能信息和叠加位置是不同的命令
	 * BVCU_SUBMETHOD_PU_ENCODERCHANNEL：包含使能信息   BVCU_SUBMETHOD_PU_VIDEOIN：包含叠加位置、叠加文字等信息
	 *
	 * @param deviceID   设备ID
	 * @param iSubMethod 子方法
	 */
	public static void queryOsdConfig(String deviceID, int iSubMethod) {
		BVCU_Command command = new BVCU_Command();
		command.iMethod = BVCU_Method.BVCU_METHOD_QUERY;
		command.iSubMethod = iSubMethod;
		command.szTargetID = deviceID;
		command.iTargetIndex = 0;
		int token = BVCU.getSDK().sendCmd(command);
		Log.d(TAG, "queryOsdConfig token : " + token);
	}

	/**
	 * 设置OSD使能状态
	 * 输入类型：BVCU_PUCFG_EncoderChannel
	 */
	public void controlOsdEnableConfig() {
		if (encoderChannel == null) return;
		BVCU_Command command = new BVCU_Command();
		command.iMethod = BVCU_Method.BVCU_METHOD_CONTROL;
		command.iSubMethod = BVCU_SubMethod.BVCU_SUBMETHOD_PU_ENCODERCHANNEL;
		command.szTargetID = deviceID;
		Log.i(TAG, "deviceId--->" + deviceID);
		command.iTargetIndex = 01;// 当前第几通道-1
		command.stMsgContent = new BVCU_CmdMsgContent();
		if (encoderChannel.pParams.length == 2) {
			tempNum = 1;
		} else if (encoderChannel.pParams.length == 1) {
			tempNum = 0;
		}
		int tempOverlay = 0;
		if (timeEnableCbx.isChecked()) {
			tempOverlay = tempOverlay + BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_TIME;
		}
		if (wordsEnableCbx.isChecked()) {
			tempOverlay = tempOverlay + BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_TEXT;
		}
		if (gpsEnableCbx.isChecked()) {
			tempOverlay = tempOverlay + BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_GPS;
		}
		if (alarmEnableCbx.isChecked()) {
			tempOverlay = tempOverlay + BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_ALARM;
		}
		if (channelEnableCbx.isChecked()) {
			tempOverlay = tempOverlay + BVCU_PUCFG_OVERLAY.BVCU_PUCFG_OVERLAY_NAME;
		}
		encoderChannel.pParams[tempNum].pstParams[0].iOverlay = tempOverlay;
		command.stMsgContent.pData = encoderChannel;
		command.stMsgContent.iDataCount = 1;
		int token = BVCU.getSDK().sendCmd(command);
		Log.d(TAG, "controlOsdEnableConfig token : " + token);
	}

	/**
	 * 设置OSD位置、文字信息
	 * 输入类型：BVCU_PUCFG_VideoIn
	 */
	private void controlOsdInfoConfig() {
		if (bvcu_PUCFG_VideoIn == null) return;
		BVCU_Command command2 = new BVCU_Command();
		command2.iMethod = BVCU_Method.BVCU_METHOD_CONTROL;
		command2.iSubMethod = BVCU_SubMethod.BVCU_SUBMETHOD_PU_VIDEOIN;
		command2.szTargetID = deviceID;
		command2.iTargetIndex = 0;// 当前第几通道-1
		command2.stMsgContent = new BVCU_CmdMsgContent();
		bvcu_PUCFG_VideoIn.stChannelDevConnectCfg.stParam = bvcu_PUCFG_VideoIn.stChannelDevConnectCfg.stCMOSChannelConfig;
		command2.stMsgContent.pData = bvcu_PUCFG_VideoIn;
		command2.stMsgContent.iDataCount = 1;
		int token = BVCU.getSDK().sendCmd(command2);
		Log.d(TAG, "controlOsdInfoConfig token : " + token);
	}

	public void setEncoderChannel(BVCU_PUCFG_EncoderChannel encoderChannel) {
		this.encoderChannel = encoderChannel;
		showOsdContent();
	}

	public void setBVCU_PUCFG_VideoIn(BVCU_PUCFG_VideoIn bvcu_PUCFG_VideoIn) {
		this.bvcu_PUCFG_VideoIn = bvcu_PUCFG_VideoIn;
		showOsdContent();
	}

	@Override
	public void setOnDismissListener(@Nullable OnDismissListener listener) {
		super.setOnDismissListener(listener);
		encoderChannel = null;
	}

}
