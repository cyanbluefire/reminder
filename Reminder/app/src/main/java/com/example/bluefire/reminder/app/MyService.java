package com.example.bluefire.reminder.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDNotifyListener;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.example.bluefire.reminder.activity.LocationActivity;

public class MyService extends Service {
    private static final String TAG = "MyService";

    /**
     * 位置提醒
     * @param savedInstanceState
     */
//    private NotiftLocationListener listener;
    private double longitude,latitude;
    public static NotifyLister mNotifyLister;
    private Vibrator mVibrator;
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        startNotifyLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    private void startNotifyLocation(){
        Log.i(TAG, "startNotifyLocation()");
//        latitude = 42.03249652949337;
//        longitude = 113.3129895882556;
        mNotifyLister = new NotifyLister();
        Log.i(TAG,"latitude"+latitude+" longitude"+longitude);
        mNotifyLister.SetNotifyLocation(latitude,longitude, 1000,tempcoor);//4个参数代表要位置提醒的点的坐标，具体含义依次为：纬度，经度，距离范围，坐标系类型(gcj02,gps,bd09,bd09ll)
        mLocationClient.registerNotify(mNotifyLister);
        mLocationClient.start();
        Log.i(TAG, "Location notify start");
    }

    /**
     * 位置到达提醒处理
     */
    public class NotifyLister extends BDNotifyListener {
        public void onNotify(BDLocation mlocation, float distance){
            Log.i(TAG,"onNotify");
            mVibrator.vibrate(1000);//振动提醒已到设定位置附近
            Toast.makeText(MyApplication.getContext(), "你的目的地到啦~", Toast.LENGTH_SHORT).show();
        }
    }

    private void startGeoCoder(String city,String address){
        mSearch.geocode(new GeoCodeOption()
                .city(city)
                .address(address));
    }
}
