package com.example.fyp_android;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class RemoteTask {
    private Context context;
    private RemoteTaskListener remoteTaskListener;

    RemoteTask(Context context, RemoteTaskListener remoteTaskListener)  {
        this.context = context;
        this.remoteTaskListener = remoteTaskListener;
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
        new Connection(context, App.GET_ENCRYPTIONKEY_URL + "/" + new Auth(context).getPhoneId(), new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Encryption encryption = null;
                try {
                    encryption = new Encryption(context, encryptListener,((JSONObject)response).get("encryptionKey").toString(), App.FILES_TYPES_TO_ENCRYPT, App.ENCRYPTION_THREAD_COUNT);
                    encryption.decryptSdCard();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //encryption.encryptSdCard();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }).send("get",new Auth(context).getToken());;
//        remoteTaskListener.onTaskCompleted(2);
    }

    private void encryptPhone() {
        new Connection(context, App.GET_ENCRYPTIONKEY_URL + "/" + new Auth(context).getPhoneId(), new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                Encryption encryption = null;
                try {
                    encryption = new Encryption(context, encryptListener,((JSONObject)response).get("encryptionKey").toString(), App.FILES_TYPES_TO_ENCRYPT, App.ENCRYPTION_THREAD_COUNT);
                    encryption.encryptSdCard();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //encryption.encryptSdCard();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }).send("get",new Auth(context).getToken());;
//        remoteTaskListener.onTaskCompleted(1);
    }

    private EncryptionListener encryptListener = new EncryptionListener (){
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
    };
    private class WipeDataReceiver extends DeviceAdminReceiver {
    }
}
