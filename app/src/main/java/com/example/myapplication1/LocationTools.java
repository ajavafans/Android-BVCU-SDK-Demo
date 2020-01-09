package com.example.myapplication1;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import java.util.Calendar;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.smarteye.adapter.BVCU_PUCFG_GPSData;
import com.smarteye.adapter.BVCU_WallTime;
import com.smarteye.sdk.BVCU;

public class LocationTools {
    private static final String TAG = "LocationTools";
    private LocationClient locationClient;
    private MyLocationListener listener;
    private Context mContext;
    private String timeFormat;
    public LocationTools(Context context){
        mContext = context;
        ContentResolver mResolver= context.getContentResolver();
        timeFormat = android.provider.Settings.System.getString(mResolver,android.provider.Settings.System.TIME_12_24);
        locationClient = new LocationClient(context.getApplicationContext());
        listener = new MyLocationListener();
        locationClient.registerLocationListener(listener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setNeedDeviceDirect(true);
        locationClient.setLocOption(option);
    }

    public void startLocation(){
        locationClient.start();
    }

    public void stopLocation(){
        if(locationClient != null){
            locationClient.stop();
        }
    }

    class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            double latitude = bdLocation.getLatitude();
            double longitude = bdLocation.getLongitude();
            double altitude = bdLocation.getAltitude();
            int satelliteNumber = bdLocation.getSatelliteNumber();
            float direction = bdLocation.getDirection();
            float speed = bdLocation.getSpeed();
            String addrStr = bdLocation.getAddrStr();
            int locType = bdLocation.getLocType();
            Log.d("MainActivity","latitude:"+latitude+
                    ",longitude:"+longitude+
                    ",altitude:"+altitude+
                    ",satelliteNumber:"+satelliteNumber+
                    ",direction:"+direction+
                    ",speed:"+speed+
                    ",addrStr:"+addrStr+
                    ",locType:"+locType);

            BVCU_PUCFG_GPSData bvcuPucfgGpsData = new BVCU_PUCFG_GPSData();
            BVCU_WallTime bvcuWallTime = new BVCU_WallTime();

            Calendar calendar = Calendar.getInstance();
            bvcuWallTime.iYear = calendar.get(Calendar.YEAR);
            bvcuWallTime.iMonth = calendar.get(Calendar.MONTH) + 1;
            bvcuWallTime.iDay = calendar.get(Calendar.DAY_OF_MONTH);
            if(calendar.get(Calendar.AM_PM) == Calendar.AM){
                bvcuWallTime.iHour = calendar.get(Calendar.HOUR);
            }else{
                bvcuWallTime.iHour = calendar.get(Calendar.HOUR) + 12;
            }

            bvcuWallTime.iMinute = calendar.get(Calendar.MINUTE);
            bvcuWallTime.iSecond = calendar.get(Calendar.SECOND);
            bvcuPucfgGpsData.stTime = bvcuWallTime;
            bvcuPucfgGpsData.iLatitude = ((int)latitude*10000000);
            bvcuPucfgGpsData.iLongitude = ((int)longitude*10000000);
            bvcuPucfgGpsData.iHeight = ((int)altitude*100);
            bvcuPucfgGpsData.iAngle = ((int)direction);
            bvcuPucfgGpsData.iSpeed = ((int)speed*1000);
            bvcuPucfgGpsData.iStarCount = satelliteNumber;
            bvcuPucfgGpsData.iAngle = ((int)direction * 1000);
            bvcuPucfgGpsData.bAntennaState = 1;
            bvcuPucfgGpsData.bOrientationState = 1;

            BVCU.getData().inputGPSData(bvcuPucfgGpsData,true);
        }
    }
}
