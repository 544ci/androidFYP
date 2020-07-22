package com.example.fyp_android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

import cn.nodemedia.NodeCameraView;
import cn.nodemedia.NodePublisher;
import cn.nodemedia.NodePublisherDelegate;
public class VideoStream extends AppCompatActivity {
    NodePublisher nodePublisher;
    Connection connection;
    Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_stream);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        startStream();
        initializeConnection();
        timer = new Timer();
        timer.scheduleAtFixedRate(new VideoStream.GetStatus(this, connection), 0, App.GET_STREAM_STATUS_INTERVAL);

    }
    private void initializeConnection(){
        connection = new Connection(this, App.GET_STREAM_STATUS_URL + '/' + new Auth(this).getPhoneId(), new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Log.e("com.example.fyp.gg",response.toString());
                VideoStream.this.stop();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("com.example.fyp.gg",error.toString());

            }
        });

    }
    private void startStream() {
        nodePublisher = new NodePublisher(this,"M2FmZTEzMGUwMC00ZTRkNTMyMS1jbi5ub2RlbWVkaWEucWxpdmU=-OTv6MJuhXZKNyWWMkdKJWsVKmLHwWPcPfnRbbWGIIf+8t39TqL/mW2f5O5WdT/W8JJE7ePvkvKaS371xVckAZ/U00dSwPp8ShB8Yic2W1GhwCyq04DYETsrGnkOWrhARH7nzNhd3Eq6sVC1Fr74GCEUHbDSCZnCfhcEnzGU9InRiQJ2PImtHORahN3blAGlHb6LZmdnobw5odvKEeUhbkhxYf8S1Fv4VRnSpDCSS3LZ2U3Mp6MfGDA1ZXPadmgdwaJitIrnWA2zP/yqmlUHjMtTv8PzGcc73Tm5k5q+OMbKCJsPq8KSEpFthncvaGZJ2kS2GHx6V5TqYZglBrTx61g==");
        nodePublisher.setNodePublisherDelegate(new NodePublisherDelegate() {
            @Override
            public void onEventCallback(NodePublisher streamer, int event, String msg) {

            }
        });

        nodePublisher.setOutputUrl(App.STREAM_URL+ Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));
        nodePublisher.setCameraPreview((NodeCameraView) findViewById(R.id.push_surface), NodePublisher.CAMERA_FRONT, true);
        nodePublisher.setVideoParam(12,15 , 150000, 0, false);

        nodePublisher.setKeyFrameInterval(1);
        nodePublisher.setAudioParam(32000, 1, 44100);
        nodePublisher.setDenoiseEnable(true);
        nodePublisher.setHwEnable(true);
        nodePublisher.setBeautyLevel(0);
        nodePublisher.setCryptoKey("");
        nodePublisher.startPreview();

        nodePublisher.start();

    }

    public void stop(View view) {
        nodePublisher.stop();
        timer.cancel();
        this.finish();

    }

    public void stop(){
        nodePublisher.stop();
        timer.cancel();
        this.finish();
    }
    private class GetStatus extends TimerTask implements Login {
        Context context;
        Connection connection;
        Auth auth;

        public GetStatus(Context context, Connection connection) {
            this.context = context;
            this.connection = connection;
            auth = new Auth(context, this);
        }

        @Override
        public void run() {
            auth.login();
        }

        @Override
        public void onSuccessfulLogin(String token) {
            connection.send("get", token);
        }

        @Override
        public void onFailedLogin(int code) {

        }
    }
}
