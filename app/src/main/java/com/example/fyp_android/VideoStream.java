package com.example.fyp_android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import cn.nodemedia.NodeCameraView;
import cn.nodemedia.NodePublisher;
import cn.nodemedia.NodePublisherDelegate;
public class VideoStream extends AppCompatActivity {
    NodePublisher nodePublisher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_stream);
        startStream();

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
        this.finish();
    }
}
