package com.example.bluefire.reminder.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.example.bluefire.reminder.R;
import com.example.bluefire.reminder.activity.LocationActivity;
import com.example.bluefire.reminder.activity.MainActivity;

public class MyService extends Service {
    private static final String TAG = "MyService";

    /**
     * 位置提醒
     * @param savedInstanceState
     */
//    private NotiftLocationListener listener;
    private double longitude,latitude;
    private String location="",thing="";
    public static NotifyLister mNotifyLister;
    private Vibrator mVibrator;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener;
    private String tempcoor="bd09ll";       //gcj02,gps,bd09,bd09ll,百度

    /**
     * 通知栏
     */
    NotificationManager nm;
    Notification mNotification;
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onStart(Intent intent, int startId) {

    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand");
        latitude = intent.getDoubleExtra("latitude",0);
        longitude = intent.getDoubleExtra("longitude",0);
        location = intent.getStringExtra("location");
        thing = intent.getStringExtra("thing");
        startNotifyLocation();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy()");
        super.onDestroy();
        mLocationClient.removeNotifyEvent(mNotifyLister);
        mLocationClient.stop();

    }

    /**
     * 位置提醒
     * 有的移动设备锁屏后为了省电会自动关闭网络连接，此时网络定位模式的定位失效。此外，锁屏后移动设备若进入cpu休眠，定时定位功能也失效。若您需要实现在cpu休眠状态仍需定时定位，可以用alarmManager 实现1个cpu可叫醒的timer，定时请求定位。
     */
    private void startNotifyLocation(){
        Log.i(TAG, "startNotifyLocation()");
        mLocationClient = MyApplication.mLocationClient;
        mVibrator = (Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);

//        latitude = 42.03249652949337;
//        longitude = 113.3129895882556;
        if(longitude == 0 ){
            Log.i(TAG,"longitude == 0");
            Toast.makeText(MyApplication.getContext(),"未找到你所输入的地点",Toast.LENGTH_LONG).show();
            return;
        }
        mNotifyLister = new NotifyLister();
        Log.i(TAG,"tempcoor=="+tempcoor);
        mNotifyLister.SetNotifyLocation(latitude, longitude, 300, tempcoor);//4个参数代表要位置提醒的点的坐标，具体含义依次为：纬度，经度，距离范围，坐标系类型(gcj02,gps,bd09,bd09ll)
        mLocationClient.registerNotify(mNotifyLister);
        mLocationClient.start();

//        initNotificationTab();

    }

    /**
     * 位置到达提醒处理
     */
    public class NotifyLister extends BDNotifyListener {
        public void onNotify(BDLocation mlocation, float distance) {
            Log.i(TAG, "onNotify");
//            mVibrator.vibrate(1000);//振动提醒已到设定位置附近
            Toast.makeText(MyApplication.getContext(), "你的目的地到啦~", Toast.LENGTH_SHORT).show();

            initNotificationTab();
        }
    }

    /**
     * 初始化通知栏
     */
    private void initNotificationTab() {
        Log.i(TAG,"initNotificationTab()");
        nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification = new Notification(R.drawable.songshu, "你的目的地到啦", System.currentTimeMillis());
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
        mNotification.defaults=Notification.DEFAULT_ALL;

        Intent mNotificationInent = new Intent(MyService.this, MainActivity.class);//如果在当前Task中，有要启动的Activity，那么把该Acitivity之前的所有Activity都关掉，并把此Activity置前以避免创建Activity的实例
        mNotificationInent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);//如果要以该Intent启动一个Activity，一定要设置 Intent.FLAG_ACTIVITY_NEW_TASK 标记。
        //系统会检查当前所有已创建的Task中是否有该要启动的Activity的Task，若有，则在该Task上创建Activity，若没有则新建具有该Activity属性的Task，并在该新建的Task上创建Activity

        //pendingIntent
        PendingIntent contentIntent = PendingIntent.getActivity(MyService.this,R.string.app_name,mNotificationInent,PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap serviceBitmap =BitmapFactory.decodeResource(getResources(),R.drawable.bell36);
        Notification.Builder builder = new Notification.Builder(MyService.this);
        builder.setSmallIcon(getNotificationIcon())
                .setLargeIcon(serviceBitmap)
                .setContentTitle("万能提醒")
                .setContentText(location+"到啦")
                .setContentInfo(thing)
                .setTicker("ticker")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        Notification notification = builder.getNotification();
        nm.notify(R.string.app_name,notification);

    }

    /**
     * android 5.0之后notification图标
     * @return
     */
    private int getNotificationIcon(){
        boolean whiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        Log.i(TAG,"isUp5.0"+whiteIcon);
        return whiteIcon ?  R.mipmap.ic_launcher:R.drawable.bell24 ;
    }

}
