package com.example.fyp_android;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;

import static com.example.fyp_android.App.LOCATION_CHANNEL_ID;
import static com.example.fyp_android.App.STOP_LOCATION_SERVICE;

public class LocationService extends Service implements OnLocationUpdatedListener, Login {
    RequestQueue queue;
    Location location;
    Auth auth;
    private String phoneId;

    @Override
    public void onCreate() {
        super.onCreate();
        queue = Volley.newRequestQueue(this);


        phoneId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void startCollectingLocation() {
//        Timer timer = new Timer();
//        timer.schedule(new RequestLocation(this, this), 0, App.SEND_LOCATION_INTERVAL);
//        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();
//        PackageManager packageManager = this.getPackageManager();
//        boolean hasGPS = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
//        if(hasGPS)
//            smartLocation.location(new LocationManagerProvider()).config(new LocationParams.Builder()
//                    .setAccuracy(LocationParams.BEST_EFFORT.getAccuracy())
//                    .setDistance(LocationParams.BEST_EFFORT.getDistance())
//                    .setInterval(App.SEND_LOCATION_INTERVAL)
//                    .build()).start(this);
        SmartLocation.with(this).location().config(new LocationParams.Builder()
                .setAccuracy(LocationParams.NAVIGATION.getAccuracy())
                .setDistance(LocationParams.NAVIGATION.getDistance())
                .setInterval(App.SEND_LOCATION_INTERVAL)
                .build())
                .start(this);
    }


//    private class RequestLocation extends TimerTask {
//        final LocationManagerProvider provider = new LocationManagerProvider();
//        Context context;
//        SmartLocation smartLocation;
//        OnLocationUpdatedListener listener;
//
//        public RequestLocation(Context context, OnLocationUpdatedListener listener) {
//            this.listener = listener;
//            this.context = context;
//            smartLocation = new SmartLocation.Builder(context).logging(true).build();
//        }
//
//        @Override
//        public void run() {
//            PackageManager packageManager = context.getPackageManager();
//            boolean hasGPS = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
//            if(hasGPS)
//                smartLocation.location(provider).config(LocationParams.NAVIGATION).oneFix().start(listener);
//        }
//    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(STOP_LOCATION_SERVICE)) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        } else {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, LOCATION_CHANNEL_ID)
                    .setContentTitle("Location Service")
                    .setContentText("Running")
                    .setSmallIcon(R.drawable.ic_connected)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(3, notification);
            startCollectingLocation();
            auth = new Auth(this, this);
            return START_NOT_STICKY;
        }

    }


    public void stopService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        stopService(notificationIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationUpdated(Location location) {
        auth.login();
        this.location = location;
    }

    @Override
    public void onSuccessfulLogin(String token) {
        if (location != null) {
            Connection conn = new Connection(this, App.UPDATE_LOCATION_URL, new Response.Listener() {
                @Override
                public void onResponse(Object response) {
                    Log.w("com.example.location", "location updated");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.w("com.example.location", "location update error");

                }
            });
            JSONObject obj = new JSONObject();
            try {
                obj.put("Latitude", location.getLatitude());
                obj.put("Longitude", location.getLongitude());
                obj.put("PhoneRefId", phoneId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            conn.send(obj, "post", token);
        }
    }

    @Override
    public void onFailedLogin(int code) {

    }

}
