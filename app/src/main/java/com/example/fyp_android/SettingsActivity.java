package com.example.fyp_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.takisoft.preferencex.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment(this))
                .commit();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        public SharedPreferences.OnSharedPreferenceChangeListener listener;
        private Context context;
        public SettingsFragment(Context context){
            this.context=context;
        }
        @Override
        public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                    if (s.equals("SMSAlarmService")) {
                        Log.w("asdasdasdasd","SMSALARMSERVERICE");
                    } else if (s.equals("VoiceAlarmService")) {
                        boolean voiceServiceEnabled = sharedPreferences.getBoolean("VoiceAlarmService",false);
                        Log.w("com.example.gg","gg "+voiceServiceEnabled);
                        if(voiceServiceEnabled){
                            Intent voiceServiceIntent = new Intent(context, VoiceService.class);
                            voiceServiceIntent.setAction(App.VOICE_CHANNEL_ID);
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                context.startForegroundService(voiceServiceIntent);
                            else
                                context.startService(voiceServiceIntent);
                        }else{
                            Intent stopIntent = new Intent(context, VoiceService.class);
                            stopIntent.setAction(App.STOP_VOICE_SERVICE);
                            context.startService(stopIntent);
                        }


                    }
                }
            };
            // additional setup
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);

        }

        @Override
        public void onDetach() {
            super.onDetach();


        }


    }
}