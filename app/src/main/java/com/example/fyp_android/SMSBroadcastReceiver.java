package com.example.fyp_android;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;

import static com.example.fyp_android.App.ALARM_ACTION_STRING;


public class SMSBroadcastReceiver extends BroadcastReceiver {
    public static Alarm alarm;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(ALARM_ACTION_STRING)) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (sharedPreferences.getBoolean("SMSAlarmService", false)) {
                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs;
                String strMessage = "";
                String format = bundle.getString("format");
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    boolean isVersionM =
                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        if (isVersionM) {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                        } else {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        }
                        strMessage += "SMS from " + msgs[i].getOriginatingAddress();
                        strMessage += " :" + msgs[i].getMessageBody() + "\n";
                        String savedKeyword = sharedPreferences.getString("smskeyword", "");
                        if (strMessage.contains(savedKeyword)) {
                            startAlarm(context);
                        }
                        Log.d("GG", "onReceive: " + strMessage);
                    }
                }
            }
        } else {
            alarm.stop();
        }
    }

    private void startAlarm(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ringtoneStringURI = sharedPreferences.getString("sms_ringtone", "");
        Uri RingtoneUri;
        if (ringtoneStringURI.equals("")) {
            RingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        } else {
            RingtoneUri = Uri.parse(ringtoneStringURI);
        }

        int volume = sharedPreferences.getInt("SMSAlarmVolume", 100);
        boolean flash = sharedPreferences.getBoolean("SMSAlarmFlash", true);
        boolean vibration = sharedPreferences.getBoolean("SMSAlarmVibration", true);


        Intent intent = new Intent(context, AlarmScreen.class);
        intent.putExtra("ringtone",RingtoneUri.toString()).putExtra("volume",volume).putExtra("flash",flash).putExtra("vibration",vibration);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
