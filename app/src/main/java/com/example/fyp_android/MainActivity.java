package com.example.fyp_android;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;



public class MainActivity extends AppCompatActivity {
    SecretVideoCapture s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.RECEIVE_SMS, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.SET_ALARM, Manifest.permission.VIBRATE, Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_NOTIFICATION_POLICY, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_SMS, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new BaseMultiplePermissionsListener())
                .check();

    }



    @Override
    protected void onResume() {
        super.onResume();
        Auth auth = new Auth(this);
        if (!auth.isLoggedIn()) {
            findViewById(R.id.logoutImageview).setVisibility(View.GONE);
            findViewById(R.id.logoutTextView).setVisibility(View.GONE);
            findViewById(R.id.LoginImageView).setVisibility(View.VISIBLE);
            findViewById(R.id.loginTextView).setVisibility(View.VISIBLE);
            findViewById(R.id.registerImageview).setVisibility(View.VISIBLE);
            findViewById(R.id.registerTextView).setVisibility(View.VISIBLE);

        } else {
            findViewById(R.id.LoginImageView).setVisibility(View.GONE);
            findViewById(R.id.loginTextView).setVisibility(View.GONE);
            findViewById(R.id.registerImageview).setVisibility(View.GONE);
            findViewById(R.id.registerTextView).setVisibility(View.GONE);
            findViewById(R.id.logoutImageview).setVisibility(View.VISIBLE);
            findViewById(R.id.logoutTextView).setVisibility(View.VISIBLE);
            startServices();
        }
    }

    private void startServices() {
//        Intent locationServiceIntent = new Intent(this, LocationService.class);
//        locationServiceIntent.setAction(App.LOCATION_CHANNEL_ID);
//        startService(locationServiceIntent);
        Intent connectionServiceIntent = new Intent(this, ConnectionService.class);
        connectionServiceIntent.setAction(App.CONNECTION_CHANNEL_ID);
        startService(connectionServiceIntent);

//        Intent voiceServiceIntent = new Intent(this, VoiceService.class);
//        voiceServiceIntent.setAction(App.VOICE_CHANNEL_ID);
//        startService(voiceServiceIntent);
    }

    private void stopServices() {
        Intent stopIntent = new Intent(MainActivity.this, LocationService.class);
        stopIntent.setAction(App.STOP_LOCATION_SERVICE);
        startService(stopIntent);

        stopIntent = new Intent(MainActivity.this, LocationService.class);
        stopIntent.setAction(App.STOP_CONNECTIVITY_SERVICE);
        startService(stopIntent);
    }

    public void openSettings(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        this.startActivity(i);
    }

    public void openGallery(View view) {
        Intent i = new Intent(this, Gallery.class);
        this.startActivity(i);
    }

    public void openLoginPage(View view) {
        Intent i = new Intent(this, LoginActivity.class);
        this.startActivity(i);
    }

    public void openRegisterationPage(View view) {
        Intent i = new Intent(this, RegisterActivity.class);
        this.startActivity(i);
    }

    public void logout(View view) {
        Auth auth = new Auth(this);
        auth.logout();
        findViewById(R.id.logoutImageview).setVisibility(View.GONE);
        findViewById(R.id.logoutTextView).setVisibility(View.GONE);

        findViewById(R.id.LoginImageView).setVisibility(View.VISIBLE);
        findViewById(R.id.loginTextView).setVisibility(View.VISIBLE);
        findViewById(R.id.registerImageview).setVisibility(View.VISIBLE);
        findViewById(R.id.registerTextView).setVisibility(View.VISIBLE);
        stopServices();
    }

}

