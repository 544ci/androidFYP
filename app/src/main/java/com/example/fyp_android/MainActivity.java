package com.example.fyp_android;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.DeviceCredentialHandlerActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleObserver;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;


public class MainActivity extends AppCompatActivity {
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private boolean useBiometric;
    private boolean usePassword;
    private boolean userAuthenticated;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        useBiometric=false;
        usePassword=false;
        userAuthenticated=false;
        BiometricManager biometricManager = BiometricManager.from(this);
        new Auth(this).startServices();

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.RECEIVE_SMS, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.SET_ALARM, Manifest.permission.VIBRATE, Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_NOTIFICATION_POLICY, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_SMS, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new BaseMultiplePermissionsListener())
                .check();



        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt =  new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                MainActivity.this.userAuthenticated=true;
            }

        });

                promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("PSearch")
                .setSubtitle("Verify identity")
                .setDeviceCredentialAllowed(true)
                .build();

    }

    public void openSettings(View view) {
        if(userAuthenticated)
            openSettings();
        else
            biometricPrompt.authenticate(promptInfo);


    }
    public void openSettings() {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(i);

    }

    public void openGallery(View view) {
        if(userAuthenticated)
            openGallery();
        else
            biometricPrompt.authenticate(promptInfo);

    }
    public void openGallery(){
        Intent i = new Intent(MainActivity.this, Gallery.class);
        MainActivity.this.startActivity(i);
    }
    private void startServices() {
        Intent locationServiceIntent = new Intent(this, LocationService.class);
        locationServiceIntent.setAction(App.LOCATION_CHANNEL_ID);
        startService(locationServiceIntent);
        Intent connectionServiceIntent = new Intent(this, ConnectionService.class);
        connectionServiceIntent.setAction(App.CONNECTION_CHANNEL_ID);
        startService(connectionServiceIntent);
    }

}

