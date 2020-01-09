package com.example.myapplication1;

import com.smarteye.adapter.BVCU_EntityInfo;
import com.smarteye.adapter.BVCU_MediaDir;
import com.smarteye.adapter.BVCU_PUCFG_ChannelInfo;
import com.smarteye.adapter.BVCU_PUCFG_DeviceInfo;
import com.smarteye.adapter.BVCU_PU_ONLINE_THROUGH;
import com.smarteye.adapter.BVCU_SubDev;
import com.smarteye.adapter.BVPU_ServerParam;


public class PUDeviceInfo {
    public int getMediaDir() {
        return iMediaDir;
    }

    public void setMediaDir(int iMediaDir) {
        this.iMediaDir = iMediaDir;
    }

    private int iMediaDir = 0;

    static void initPUEntityInfo(BVCU_EntityInfo entityInfo, BVPU_ServerParam bvpuServerParam){
        entityInfo.iBootDuration = 0;
        entityInfo.iChannelCount = 2;
        entityInfo.iLatitude = 0;
        entityInfo.iLongitude = 0;
        entityInfo.iOnlineThrough = BVCU_PU_ONLINE_THROUGH.BVCU_PU_ONLINE_THROUGH_WIFI;
        entityInfo.pChannelList = new BVCU_PUCFG_ChannelInfo[2];
        entityInfo.pChannelList[0] = new BVCU_PUCFG_ChannelInfo();
        entityInfo.pChannelList[0].iChannelIndex = BVCU_SubDev.BVCU_SUBDEV_INDEXMAJOR_MIN_CHANNEL;
        entityInfo.pChannelList[0].iMediaDir = bvpuServerParam.iMediaDir;
        entityInfo.pChannelList[0].iPTZIndex = 0;
        entityInfo.pChannelList[0].szName = "video";

        entityInfo.pChannelList[1] = new BVCU_PUCFG_ChannelInfo();
        entityInfo.pChannelList[1].iChannelIndex = BVCU_SubDev.BVCU_SUBDEV_INDEXMAJOR_MIN_GPS;
        entityInfo.pChannelList[1].iMediaDir = BVCU_MediaDir.BVCU_MEDIADIR_DATASEND;
        entityInfo.pChannelList[1].iPTZIndex = 0;
        entityInfo.pChannelList[1].szName = "gps";
        entityInfo.pPUInfo = new BVCU_PUCFG_DeviceInfo();
        entityInfo.pPUInfo.iPUType = 0;
        entityInfo.pPUInfo.iLanguage = new int[4];
        entityInfo.pPUInfo.iLanguage[0] = 1;
        entityInfo.pPUInfo.iLanguage[1] = 2;
        entityInfo.pPUInfo.iLanguage[2] = 3;
        entityInfo.pPUInfo.iLanguage[3] = 4;
        entityInfo.pPUInfo.iLanguageIndex = 1;
        entityInfo.pPUInfo.szName = bvpuServerParam.szDeviceName;
        entityInfo.pPUInfo.iWIFICount = 0;
        entityInfo.pPUInfo.iRadioCount = 0;
        entityInfo.pPUInfo.iChannelCount = 1;
        entityInfo.pPUInfo.iVideoInCount = 1;
        entityInfo.pPUInfo.iAudioInCount = 1;
        entityInfo.pPUInfo.iAudioOutCount = 1;
        entityInfo.pPUInfo.iPTZCount = 0;
        entityInfo.pPUInfo.iSerialPortCount = bvpuServerParam.iSerialPortCount;
        entityInfo.pPUInfo.iAlertInCount = 1;
        entityInfo.pPUInfo.iAlertOutCount = 0;
        entityInfo.pPUInfo.iStorageCount = 0;
        entityInfo.pPUInfo.iGPSCount = 1;
        entityInfo.pPUInfo.bSupportSMS = 1;
        entityInfo.pPUInfo.iPresetCount = 0;
        entityInfo.pPUInfo.iCruiseCount = 0;
        entityInfo.pPUInfo.iAlarmLinkActionCount = 0;
        entityInfo.pPUInfo.iLongitude = bvpuServerParam.iLongitude;
        entityInfo.pPUInfo.iLatitude = bvpuServerParam.iLatitude;
    }
}
