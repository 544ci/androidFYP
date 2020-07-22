package com.example.fyp_android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.TimerTask;

import static com.example.fyp_android.App.CONNECTION_CHANNEL_ID;
import static com.example.fyp_android.App.LOCATION_CHANNEL_ID;
import static com.example.fyp_android.App.VOICE_CHANNEL_ID;


public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Auth auth = new Auth(context);
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
            auth.startServices();
    }







}
