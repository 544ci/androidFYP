package com.example.fyp_android;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

public class RemoteTask implements EncryptionListener {
    private Context context;
    private RemoteTaskListener remoteTaskListener;
    private Encryption encryption;

    RemoteTask(Context context, RemoteTaskListener remoteTaskListener) {
        this.context = context;
        this.remoteTaskListener = remoteTaskListener;
        encryption = new Encryption(context, this, "12341234123412341234123412341234", App.FILES_TYPES_TO_ENCRYPT, App.ENCRYPTION_THREAD_COUNT);
    }


    public void execute(int taskId) {
        if (taskId == 1) {
            encryptPhone();
            return;
        } else if (taskId == 2) {
            decryptPhone();
            return;
        } else if (taskId == 3) {
            resetPhone();
            return;
        }


    }

    private void resetPhone() {
        remoteTaskListener.onTaskCompleted(3);
        DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(context.DEVICE_POLICY_SERVICE);
        ComponentName mDeviceAdmin = new ComponentName(context, WipeDataReceiver.class);
        mDPM.wipeData(0);
    }

    private void decryptPhone() {

        encryption.decryptSdCard();
//        remoteTaskListener.onTaskCompleted(2);
    }

    private void encryptPhone() {
        encryption.encryptSdCard();
//        remoteTaskListener.onTaskCompleted(1);
    }

    @Override
    public void onSDCardEncryptionComplete() {
        remoteTaskListener.onTaskCompleted(1);
    }

    @Override
    public void onSDCardEncryptionFailed(String errMessage) {
        remoteTaskListener.onTaskCompleted(1);

    }

    @Override
    public void onSDCardDecryptionComplete() {
        remoteTaskListener.onTaskCompleted(2);

    }

    @Override
    public void onSDCardDecryptionFailed(String errMessage) {
        remoteTaskListener.onTaskCompleted(2);

    }

    private class WipeDataReceiver extends DeviceAdminReceiver {
    }
}
