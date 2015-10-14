package com.example.bluefire.reminder.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.HashMap;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDNotifyListener;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.bluefire.reminder.R;
import com.example.bluefire.reminder.app.MyApplication;
import com.example.bluefire.reminder.util.ActivityCollector;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

public class LocationActivity extends Activity{


    private AutoCompleteTextView autoTxt;
    protected static final String TAG = "LocationActivity";
    ArrayList<String> arrayList_autoStrs = new ArrayList<String>();
    String location = "";
    private SuggestionSearch mSuggestionSearch = null;
    private SimpleAdapter adapter_recLocation;
    private ArrayAdapter<String> adapter_hisLocation;

    private List<Map<String, Object>> list_aoutoCom;
    private ListView lv_recLocation;

    private String city ="";
    private EditText et_city;

    /**
     * 定位
     * @param savedInstanceState
     */

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener;
//    public BDLocationListener myListener = new MyLocationListener();
    private LocationMode tempMode = LocationMode.Hight_Accuracy;
    private String tempcoor="gps";
    /**
     * 位置提醒
     * @param savedInstanceState
     */
//    private NotiftLocationListener listener;
    private double longitude,latitude;
    public static NotifyLister mNotifyLister;
    private Vibrator mVibrator;

    /**
     * 地理编码
     * @param savedInstanceState
     */
    GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        Log.v(TAG, "start oncreate selectLocation");
        SDKInitializer.initialize(getApplicationContext());					//初始化sdk引用的context信息，必需在setContentView之前
        setContentView(R.layout.activity_location);

        initSuggestion();
        initLocation();
        initLocationNotify();
        initGeoCoder();
    }

    private void initGeoCoder() {
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(geoListener);
    }

    /**
     * 初始化位置提醒
     */
    private void initLocationNotify() {
//        listener = new NotiftLocationListener();
        mVibrator = (Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);

//        mLocationClient.registerLocationListener(listener);
    }

    /**
     * 初始化建议查找
     */
    private void initSuggestion() {
        /*
		 * 建议查找
		 */
        OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {

            @Override
            public void onGetSuggestionResult(SuggestionResult res) {
                // TODO Auto-generated method stub
                if (res == null || res.getAllSuggestions() == null) {
                    return;
                }
                list_aoutoCom.clear();
                for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
                    if (info.key != null){
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("prompt", info.key);
                        list_aoutoCom.add(map);
                    }
                }
                adapter_recLocation.notifyDataSetChanged();
                Log.v(TAG,"notifyDataSetChanged");
            }
        };
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(listener);
        /**
         * 选择城市
         */
        et_city = (EditText)findViewById(R.id.et_city);
        et_city.setText("深圳");
		/*
		 * 显示百度推荐地点
		 */
        autoTxt = (AutoCompleteTextView)findViewById(R.id.searchkey);
        list_aoutoCom = new ArrayList<Map<String,Object>>();
        adapter_recLocation = new SimpleAdapter(this, list_aoutoCom, R.layout.item_set_location, new String[]{"prompt"}, new int[]{R.id.tv_prompt});
        lv_recLocation = (ListView) findViewById(R.id.lv_prompt);
        lv_recLocation.setAdapter(adapter_recLocation);
        autoTxt.addTextChangedListener(watcher);
        lv_recLocation.setOnItemClickListener(itemlistener);
    }

    /*
     * 建议地点列表监听
     */
    public OnItemClickListener itemlistener =  new OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                long arg3) {
            // TODO Auto-generated method stub
            hideInputMethod(arg1);
            location = list_aoutoCom.get(position).get("prompt").toString();

            startGeoCoder(city,location);
            Log.i(TAG, "city=="+city+" location==" + location);
            Intent intent = new Intent();
            intent.putExtra("type", "位置");
            intent.putExtra("content", location);
            setResult(RESULT_OK, intent);

            //开始定位
//            mLocationClient.start();
//            Log.i(TAG, "Location start");
            //开始位置提醒
            startNotifyLocation();

            finish();
        }

    };
    /*
     * 自动填充监听
     */
    private TextWatcher watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub

        }
        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            // TODO Auto-generated method stub

            city = et_city.getText().toString();
        }

        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub
//			lv_historyLocation.setVisibility(8);				//隐藏历史地点列表(不可见且不占原来布局空间)，只显示百度推荐地点列表
            location = arg0.toString();
            Log.v(TAG, "afterTextChanged "+location);
            mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                    .city(city)
                    .keyword(location));

        }
    };
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        //hideInputMethod();
        super.onDestroy();
        mSuggestionSearch.destroy();
        mSearch.destroy();
        ActivityCollector.removeActivity(this);
    }

    private void hideInputMethod(View view) {
        InputMethodManager manager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(manager.isActive()){
            manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * 地理编码listener
     */
    OnGetGeoCoderResultListener geoListener = new OnGetGeoCoderResultListener() {
        @Override
        public void onGetGeoCodeResult(GeoCodeResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(LocationActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            latitude= result.getLocation().latitude;
            longitude = result.getLocation().longitude;
            String strInfo = String.format("提醒位置的纬度：%f 经度：%f",
                    result.getLocation().latitude, result.getLocation().longitude);
            Log.i(TAG,strInfo);
//            Toast.makeText(LocationActivity.this, strInfo, Toast.LENGTH_LONG).show();

        }

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

        }


    };
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    public class NotifyLister extends BDNotifyListener {
        public void onNotify(BDLocation mlocation, float distance){
            mVibrator.vibrate(1000);//振动提醒已到设定位置附近
            Toast.makeText(LocationActivity.this, "震动提醒", Toast.LENGTH_SHORT).show();
        }
    }

    private void startGeoCoder(String city,String address){
        mSearch.geocode(new GeoCodeOption()
                .city(city)
                .address(address));
    }
    private void startNotifyLocation(){
        Log.i(TAG,"startNotifyLocation()");
//        latitude = 42.03249652949337;
//        longitude = 113.3129895882556;
        mNotifyLister = new NotifyLister();
        mNotifyLister.SetNotifyLocation(latitude,longitude, 200,tempcoor);//4个参数代表要位置提醒的点的坐标，具体含义依次为：纬度，经度，距离范围，坐标系类型(gcj02,gps,bd09,bd09ll)
        mLocationClient.registerNotify(mNotifyLister);
        mLocationClient.start();
        Log.i(TAG, "Location notify start");
    }

    private void initLocation(){
        Log.i(TAG,"initLocation()");
        mLocationClient = MyApplication.mLocationClient;
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType(tempcoor);//可选，默认gcj02，设置返回的定位结果坐标系，
        int span=10000;                 //定位时间间隔
//        try {
//            span = Integer.valueOf(frequence.getText().toString());
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
//        option.setIsNeedAddress(checkGeoLocation.isChecked());//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mLocationClient.setLocOption(option);


    }
}
