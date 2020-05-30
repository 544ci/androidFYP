package com.example.fyp_android;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmScreen extends AppCompatActivity {
   // SMSBroadcastReceiver broadcastReceiver = new SMSBroadcastReceiver();
    private Alarm alarm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_screen);


        String ringtoneUri = getIntent().getStringExtra("ringtone");
        int volume = getIntent().getIntExtra("volume",10);
        boolean flash = getIntent().getBooleanExtra("flash",true);
        boolean vibration = getIntent().getBooleanExtra("vibration",true);

        alarm = new Alarm(this,Uri.parse(ringtoneUri),volume,flash,vibration);
        alarm.start();



//        IntentFilter filter = new IntentFilter(App.ALARM_ACTION_STRING);
//        registerReceiver(broadcastReceiver, filter);
//




//
//                CFAlertDialog.Builder builder = new CFAlertDialog.Builder(this)
//                .setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT)
//                .setTitle("You've hit the limit")
//                .setMessage("Looks like you've hit your usage limit. Upgrade to our paid plan to continue without any limits.")
//                .addButton("UPGRADE", Color.parseColor("#FFFFFF"), Color.parseColor("#429ef4"), CFAlertDialog.CFAlertActionStyle.POSITIVE, CFAlertDialog.CFAlertActionAlignment.CENTER, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//             //   Toast.makeText(this , "Upgrade tapped", Toast.LENGTH_SHORT).show();
//                dialog.dismiss();
//            }
//        });
//
//// Show the alert
//        builder.show();
    }


    public void stopAlarm(View view) {
//        Intent intent = new Intent(App.ALARM_ACTION_STRING);
//        sendBroadcast(intent);
        alarm.stop();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      //  unregisterReceiver(broadcastReceiver);
    }
}
