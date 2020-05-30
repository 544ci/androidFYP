package com.example.fyp_android;

import android.app.NotificationManager;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.noob.noobcameraflash.managers.NoobCameraManager;

import java.io.Serializable;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class Alarm implements Serializable {
    public String type;
    Ringtone ringtone;
    private Context context;
    private Thread vibrationThread;
    private Thread flashThread;
    private boolean flash;
    private boolean vibration;
    private Uri ringtoneUri;
    private int volume;
    private int prevVolume;

    public Alarm(Context context, Uri ringtoneUri, int volume, boolean flash, boolean vibration) {
        this.type = type;
        this.context = context;
        this.ringtoneUri = ringtoneUri;
        this.vibration = vibration;
        this.flash = flash;
        this.volume = volume;
        vibrationThread = new Thread(new vibrate());
        flashThread = new Thread(new flash());
        try {
            NoobCameraManager.getInstance().init(context);
        } catch (CameraAccessException e) {

        }
    }

    public void start() {
        playAlarm();
        if (vibration)
            vibrationThread.start();
        if (flash)
            flashThread.start();


    }

    public void stop() {
        ringtone.stop();
        if (vibration)
            vibrationThread.interrupt();
        if (flash)
            flashThread.interrupt();
        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM, // Stream type
                prevVolume, // Index
                AudioManager.FLAG_SHOW_UI // Flags
        );

    }

    public void playAlarm() {
        changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_ALL);

        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        prevVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        int newVolume = (int) ((((float) volume / 100)) * maxVolume);
        audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM, // Stream type
                newVolume, // Index
                AudioManager.FLAG_SHOW_UI // Flags
        );
        ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
        AudioAttributes attr = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build();
        ringtone.setAudioAttributes(attr);
        ringtone.play();
    }

    private void changeInterruptionFiler(int interruptionFilter) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (mNotificationManager.isNotificationPolicyAccessGranted()) {
            mNotificationManager.setInterruptionFilter(interruptionFilter);
        } else {
//            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
//            startActivity(intent);
        }
    }

    public class vibrate implements Runnable {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        @Override
        public void run() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    while (true) {
                        Thread.sleep(1000);
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                } else {
                    while (true) {
                        Thread.sleep(1000);
                        v.vibrate(500);
                    }
                }
            } catch (InterruptedException e) {

            }
        }
    }

    public class flash implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    NoobCameraManager.getInstance().turnOnFlash();
                    Thread.sleep(200);
                    NoobCameraManager.getInstance().turnOffFlash();
                }
            } catch (InterruptedException e) {
                try {
                    NoobCameraManager.getInstance().turnOffFlash();
                } catch (CameraAccessException ex) {
                    ex.printStackTrace();
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public class ringer implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    NoobCameraManager.getInstance().turnOnFlash();
                    Thread.sleep(200);
                    NoobCameraManager.getInstance().turnOffFlash();
                }
            } catch (InterruptedException e) {
                try {
                    NoobCameraManager.getInstance().turnOffFlash();
                } catch (CameraAccessException ex) {
                    ex.printStackTrace();
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
