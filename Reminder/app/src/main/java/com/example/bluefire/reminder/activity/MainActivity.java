package com.example.bluefire.reminder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.example.bluefire.reminder.R;
import com.example.bluefire.reminder.app.MyApplication;
import com.example.bluefire.reminder.app.MyService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG ="MainActivity" ;
    private Button btn_location;
    private TextView tv_remind_type;
    private TextView tv_remind_content;
    private Button btn_stop_location;
    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
    }

    private void init() {
        btn_location = (Button)findViewById(R.id.btn_location);
        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, LocationActivity.class), 1);
            }
        });
        btn_stop_location = (Button)findViewById(R.id.btn_stop_location);
        btn_stop_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mLocationClient = MyApplication.mLocationClient;
                if(mLocationClient == null){
                    Toast.makeText(MainActivity.this,"定位提醒早已结束",Toast.LENGTH_LONG).show();
                    return;
                }

                mLocationClient.removeNotifyEvent(MyService.mNotifyLister);
                mLocationClient.stop();
                stopService(new Intent(MainActivity.this, MyService.class));
                Toast.makeText(MainActivity.this,"定位提醒已经结束",Toast.LENGTH_LONG).show();
            }
        });

        tv_remind_type = (TextView)findViewById(R.id.tv_remind_type);
        tv_remind_content = (TextView)findViewById(R.id.tv_remind_content);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mLocationClient.removeNotifyEvent(LocationActivity.mNotifyLister);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /**
     * activity返回值
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case 1:
                if(resultCode == RESULT_OK){
                    String type = data.getStringExtra("type");
                    String content = data.getStringExtra("content");
                    Log.i(TAG,"type="+type+" content="+content);
                    tv_remind_type.setText(type);
                    tv_remind_content.setText(content);

//                    if(type.equals("location")){
//                        String returnedData = data.getStringExtra("location");
//                        Log.i(TAG, "afterTextChanged" + returnedData);
//                        tv_location.setText(returnedData);
//                        location = returnedData;
//                        MapUtils.perfomZoom(mBaiduMap);
//                        mSearch.geocode(new GeoCodeOption().city(city).address(location));
//                    }
                }
                break;
        }
    }
}
