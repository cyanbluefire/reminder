package com.example.bluefire.reminder.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by cyan on 2015/8/25.
 */
public class MyApplication extends Application {
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MyApplication","MyApplication onCreate");
        mContext = getApplicationContext();
    }
    public static Context getContext(){
        if(mContext != null){
            Log.i("MyApplication","return Context");
            return mContext;
        }else{
            return null;
        }
    }
}
