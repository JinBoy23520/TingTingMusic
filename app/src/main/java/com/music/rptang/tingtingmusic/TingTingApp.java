package com.music.rptang.tingtingmusic;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.music.rptang.tingtingmusic.utils.Contant;

/**
 * 屏幕大小，宽高等公共的值也可以放这里
 */
public class TingTingApp extends Application {

    public SharedPreferences sp;
    @Override
    public void onCreate() {
        super.onCreate();
        sp=getSharedPreferences(Contant.SP_NAME, Context.MODE_PRIVATE);
    }
}
