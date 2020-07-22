package com.example.fyp_android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.auth0.android.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import static com.example.fyp_android.App.CONNECTION_CHANNEL_ID;
import static com.example.fyp_android.App.LOCATION_CHANNEL_ID;
import static com.example.fyp_android.App.VOICE_CHANNEL_ID;

public class Auth implements Response.Listener, Response.ErrorListener {
    SharedPreferences.Editor editor;
    private Context context;
    private Login login;
    private String userEmail;
    private String password;
    private SharedPreferences sharedPreferences;

    Auth(Context context, Login login) {
        this.context = context;
        this.login = login;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();

    }

    Auth(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
    }

    public void login(String email, String pass) {
        loginUser(email, pass);
    }


    public boolean isLoggedIn() {
        //return true;
        String email = sharedPreferences.getString("email", "");
        String pass = sharedPreferences.getString("pass", "");
        if (email.equals("") || pass.equals(""))
            return false;
        else {
            String token = sharedPreferences.getString("token", "");
            if (token.equals("")) {
                return false;
            } else {
                try {
//                    DecodedJWT jwt = JWT.decode(token);
//                    Date exp = jwt.getExpiresAt();
//                    if (exp.compareTo(new Date()) > 0) {
//                        return true;
//                    } else {
//                        return false;
//                    }
                    JWT jwt = new JWT(token);
                    Date exp = jwt.getExpiresAt();
                    if (exp == null)
                        return true;
                    if (exp.compareTo(new Date()) > 0) {
                        return true;
                    } else {
                        return false;
                    }

                } catch (JWTDecodeException exception) {
                    return false;
                }
            }
        }
    }

    public void login() {
        if (isLoggedIn())
            login.onSuccessfulLogin(getToken());
        else {
            String e = getEmail();
            String p = getPass();
            if (e.equals("") || p.equals(""))
                login.onFailedLogin(0);
            else
                loginUser(e, p);
        }


    }

    private void loginUser(String email, String pass) {
        this.userEmail = email;
        this.password = pass;
        final String phoneId = getPhoneId();
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("password", pass);
            json.put("DeviceId", phoneId);
            json.put("Manufacturer", manufacturer);
            json.put("Model", model);
        } catch (Exception e) {
            login.onFailedLogin(3);
        }
        Connection c = new Connection(context, App.LOGIN_URL, this, this);
        c.send(json, "post");
    }

    public void register(String email, String pass) {

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        login.onFailedLogin(2);
    }


    @Override
    public void onResponse(Object response) {
        try {
            String token = ((JSONObject) response).get("token").toString();
            setToken(token);
            setEmailPass(userEmail, password);
            login.onSuccessfulLogin("Bearer " + token);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getToken() {
        return sharedPreferences.getString("token", "");
    }

    private void setToken(String token) {

        editor.putString("token", token);
        editor.apply();
    }

    private void setEmailPass(String email, String pass) {
        editor.putString("email", email);
        editor.putString("pass", pass);
        editor.apply();
    }

    public String getEmail() {
        return sharedPreferences.getString("email", "");

    }

    private String getPass() {
        return sharedPreferences.getString("pass", "");
    }

    public String getPhoneId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void logout() {
        stopServices();
        editor.remove("email");
        editor.remove("pass");
        editor.remove("token");
        editor.commit();
    }
    private void stopServices() {


        Intent stopIntent = new Intent(context, LocationService.class);
        stopIntent.setAction(App.STOP_LOCATION_SERVICE);
        context.startService(stopIntent);

        stopIntent = new Intent(context, ConnectionService.class);
        stopIntent.setAction(App.STOP_CONNECTIVITY_SERVICE);
        context.startService(stopIntent);


    }
    public void startServices() {
        createNotificationChannels(context);
        if (isLoggedIn()){
            Intent connectionServiceIntent = new Intent(context, ConnectionService.class);
            connectionServiceIntent.setAction(CONNECTION_CHANNEL_ID);
            startService(context, connectionServiceIntent);

            Intent locationServiceIntent = new Intent(context, LocationService.class);
            locationServiceIntent.setAction(App.LOCATION_CHANNEL_ID);
            startService(context, locationServiceIntent);
        }
        boolean voiceServiceEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("VoiceAlarmService",false);
        if(voiceServiceEnabled){
            Intent voiceServiceIntent = new Intent(context, VoiceService.class);
            voiceServiceIntent.setAction(App.VOICE_CHANNEL_ID);
            startService(context ,voiceServiceIntent);
        }
    }
    private void startService(Context context,Intent intent){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intent);
        else
            context.startService(intent);
    }
    private void createNotificationChannels(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            NotificationChannel voiceServiceChannel = new NotificationChannel(
                    VOICE_CHANNEL_ID,
                    "Voice service channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
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
