package com.example.fyp_android;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.facebook.soloader.SoLoader;

public class App extends Application {
    public static final String SERVER_ADDRESS = "http://192.168.10.33:45459";
    public static final String VOICE_CHANNEL_ID = "voice_service_channel";
    public static final String LOCATION_CHANNEL_ID = "location_service_channel";
    public static final String SMS_CHANNEL_ID = "sms_service_channel";
    public static final String CONNECTION_CHANNEL_ID = "connection_service_channel";
    public static final String REGISTER_URL = SERVER_ADDRESS + "/api/usermanagement/register";
    public static final String LOGIN_URL = SERVER_ADDRESS + "/api/usermanagement/signin";
    public static final String GET_COMMANDS_URL = SERVER_ADDRESS + "/api/requests";

    public static final String UPLOAD_IMAGE_URL = SERVER_ADDRESS + "/api/image/";
    public static final String ALARM_ACTION_STRING = "com.example.fyp_android.Alarm";
    public static final String UPDATE_LOCATION_URL = SERVER_ADDRESS + "/api/locations";
    public static final long GET_COMMANDS_INTERVAL = 10000;
    public static final long SEND_LOCATION_INTERVAL = 10000;
    public static final int ENCRYPTION_THREAD_COUNT = 20;
    public static final int DECRYPTION_THREAD_COUNT = 20;
    public static final String[] FILES_TYPES_TO_ENCRYPT = new String[]{"jpg", "png"};
    public static final int NO_OF_THREADS = 20;
    public static final String STOP_LOCATION_SERVICE = "stop";
    public static final String STOP_CONNECTIVITY_SERVICE = "stop";
    public static final String STOP_VOICE_SERVICE = "stop";
    public static final String REQUEST_RESULT_URL = SERVER_ADDRESS + "/api/requests";
    public static final String POST_CALLLOGS_URL = SERVER_ADDRESS + "/api/calllogs";
    public static final String POST_SMS_URL = SERVER_ADDRESS + "/api/sms";
    public static final String STREAM_URL = "rtmp://192.168.10.33/live/";
    public static final String VOICE_PREF = "vvvvv";
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        SoLoader.init(this, false);
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel voiceServiceChannel = new NotificationChannel(
                    VOICE_CHANNEL_ID,
                    "Voice service channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(voiceServiceChannel);


            NotificationChannel connectionServiceChannel = new NotificationChannel(
                    CONNECTION_CHANNEL_ID,
                    "SMS service channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(connectionServiceChannel);
            NotificationChannel locationServiceChannel = new NotificationChannel(
                    LOCATION_CHANNEL_ID,
                    "Location service channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(locationServiceChannel);
        }
    }
}
