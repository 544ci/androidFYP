package com.example.fyp_android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.Voice;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.kaldi.Assets;
import org.kaldi.KaldiRecognizer;
import org.kaldi.Model;
import org.kaldi.RecognitionListener;
import org.kaldi.SpeechRecognizer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.example.fyp_android.App.LOCATION_CHANNEL_ID;
import static com.example.fyp_android.App.STOP_LOCATION_SERVICE;
import static com.example.fyp_android.App.STOP_VOICE_SERVICE;
import static com.example.fyp_android.App.VOICE_CHANNEL_ID;


public class VoiceService extends Service implements RecognitionListener {
    static {
        System.loadLibrary("kaldi_jni");
    }
    private SharedPreferences sharedPreferences;
    private Model model;
    private SpeechRecognizer recognizer;
    NotificationCompat.Builder builder;
    public static Alarm alarm;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
         builder = new NotificationCompat.Builder(this, VOICE_CHANNEL_ID)
                .setContentTitle("Voice Service")
                .setContentText("Initializing")
                .setSmallIcon(R.drawable.ic_connected)
                .setContentIntent(pendingIntent);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(App.STOP_VOICE_SERVICE)) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        } else {
            startForeground(4, builder.build());
            startRecognition();
            return START_NOT_STICKY;
        }

    }

    private void startRecognition() {
        new SetupTask(this).execute();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onPartialResult(String s) {

    }

    @Override
    public void onResult(String s) {
        //builder.setContentText(s);
        JSONObject json;

        try {
            json = new JSONObject(s);
            String parsedText =json.getString("text");
            Log.d("!!!!!",parsedText);
            String storedKeyword =sharedPreferences.getString("VoiceKeyword","");
            Log.d("!!!!!stored", storedKeyword);

            if(!parsedText.equals("")){
                if(parsedText.equals(storedKeyword))
                    startAlarm(this);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //startForeground(4, builder.build());

    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onTimeout() {

    }




    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<VoiceService> serviceReference;

        SetupTask(VoiceService activity) {
            this.serviceReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(serviceReference.get());
                File assetDir = assets.syncAssets();
                Log.d("!!!!", assetDir.toString());
                serviceReference.get().model = new Model(assetDir.toString() + "/model-android");
            } catch (IOException e) {
                Log.e("com.example.voiee",e.getMessage());
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result == null) {
                serviceReference.get().setReady();
            }
        }
    }

    private void setReady() {
        //builder.setContentText("listening");
        //startForeground(4, builder.build());
        try {
            recognizer = new SpeechRecognizer(model);
            recognizer.addListener(this);
            recognizer.startListening();
        } catch (IOException e) {
            Log.e("com.example.voice",e.getMessage());
        }
    }

    private void startAlarm(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ringtoneStringURI = sharedPreferences.getString("voice_ringtone", "");
        Uri RingtoneUri;
        if (ringtoneStringURI.equals("")) {
            RingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        } else {
            RingtoneUri = Uri.parse(ringtoneStringURI);
        }

        int volume = sharedPreferences.getInt("VoiceAlarmVolume", 100);
        boolean flash = sharedPreferences.getBoolean("VoiceAlarmFlash", true);
        boolean vibration = sharedPreferences.getBoolean("VoiceAlarmVibration", true);




        Intent intent = new Intent(context, AlarmScreen.class);
        intent.putExtra("ringtone",RingtoneUri.toString()).putExtra("volume",volume).putExtra("flash",flash).putExtra("vibration",vibration);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

}
