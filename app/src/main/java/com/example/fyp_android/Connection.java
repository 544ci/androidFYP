package com.example.fyp_android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class Connection {
    private String URL;
    private RequestQueue queue;
    private Context context;
    private Listener responseListener;
    private ErrorListener errorListener;


    Connection(Context context, String url, Listener responseListener, ErrorListener errorListener) {
        this.URL = url;
        this.context = context;
        queue = Volley.newRequestQueue(context);
        this.responseListener = responseListener;
        this.errorListener = errorListener;
    }

    public void send(JSONObject message, String method) {
        if (isNetworkConnected()) {
            int met = Request.Method.GET;
            if (method.equals("post"))
                met = Request.Method.POST;
            else if (method.equals("delete"))
                met = Request.Method.DELETE;
            else if (method.equals("put"))
                met = Request.Method.PUT;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(met, URL, message, responseListener, errorListener);

            queue.add(jsonObjectRequest);
        } else {
            Toast toast = Toast.makeText(context,
                    "No internet connection",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void send(JSONObject message, String method, final String token) {
        if (isNetworkConnected()) {
            int met = Request.Method.GET;
            if (method.equals("post"))
                met = Request.Method.POST;
            else if (method.equals("delete"))
                met = Request.Method.DELETE;
            else if (method.equals("put"))
                met = Request.Method.PUT;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(met, URL, message, responseListener, errorListener) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headerMap = new HashMap<>();
                    headerMap.put("Content-Type", "application/json");
                    headerMap.put("Authorization", "Bearer " + token);
                    return headerMap;
                }
            };
            queue.add(jsonObjectRequest);
        } else {
            Toast toast = Toast.makeText(context,
                    "No internet connection",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void send(String method, final String token) {
        if (isNetworkConnected()) {
            int met = Request.Method.GET;
            if (method.equals("post"))
                met = Request.Method.POST;
            else if (method.equals("delete"))
                met = Request.Method.DELETE;
            else if (method.equals("put"))
                met = Request.Method.PUT;
            JsonObjectRequest stringRequest = new JsonObjectRequest(met, URL, new JSONObject(), responseListener, errorListener) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headerMap = new HashMap<>();
                    headerMap.put("Content-Type", "application/json");
                    headerMap.put("Authorization", "Bearer " + token);
                    return headerMap;
                }
            };
            queue.add(stringRequest);
        } else {
//            Toast toast = Toast.makeText(context,
//                    "No internet connection",
//                    Toast.LENGTH_SHORT);
//            toast.show();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
