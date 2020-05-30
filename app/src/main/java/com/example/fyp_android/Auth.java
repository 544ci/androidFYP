package com.example.fyp_android;

import android.content.Context;
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
        return true;
//        String email = sharedPreferences.getString("email", "");
//        String pass = sharedPreferences.getString("pass", "");
//        if (email.equals("") || pass.equals(""))
//            return false;
//        else {
//            String token = sharedPreferences.getString("token", "");
//            if (token.equals("")) {
//                return false;
//            } else {
//                try {
////                    DecodedJWT jwt = JWT.decode(token);
////                    Date exp = jwt.getExpiresAt();
////                    if (exp.compareTo(new Date()) > 0) {
////                        return true;
////                    } else {
////                        return false;
////                    }
//                    JWT jwt = new JWT(token);
//                    Date exp = jwt.getExpiresAt();
//                    if (exp == null)
//                        return true;
//                    if (exp.compareTo(new Date()) > 0) {
//                        return true;
//                    } else {
//                        return false;
//                    }
//
//                } catch (JWTDecodeException exception) {
//                    return false;
//                }
//            }
//        }
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

    private String getEmail() {
        return sharedPreferences.getString("email", "");

    }

    private String getPass() {
        return sharedPreferences.getString("pass", "");
    }

    public String getPhoneId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void logout() {
        editor.remove("email");
        editor.remove("pass");
        editor.remove("token");
    }
}
