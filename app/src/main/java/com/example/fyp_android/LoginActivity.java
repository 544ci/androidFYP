package com.example.fyp_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity implements Login {
    private TextView email;
    private TextView password;
    private TextView userLoggedInTitle;
    private TextView userLoggedInEmail;
    private TextView registerTextView;

    private Button loginButton;
    private EditText e;
    private EditText p;
    private String pass;
    private String eml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.username);
        password = findViewById(R.id.password);
        registerTextView = findViewById(R.id.textView2);
        userLoggedInEmail = findViewById(R.id.loggedInEmail);
        userLoggedInTitle = findViewById(R.id.LoggedInTitle);
        e=findViewById(R.id.username);
        p=findViewById(R.id.password);
        loginButton=findViewById(R.id.register);
        Auth a = new Auth(this);
        if(a.isLoggedIn())
            userIsLoggedIn(a.getEmail());

    }
    public void userIsLoggedIn(String email){
        userLoggedInTitle.setVisibility(View.VISIBLE);
        userLoggedInEmail.setVisibility(View.VISIBLE);
        userLoggedInEmail.setText(email);
        registerTextView.setVisibility(View.GONE);
        e.setVisibility(View.GONE);
        p.setVisibility(View.GONE);
        loginButton.setVisibility(View.GONE);


    }
    public void signIn(View view) {
        eml = email.getText().toString();
        pass = password.getText().toString();
        if (eml.length() == 0) {
            makeToast("Email required");
        } else if (pass.length() == 0) {
            makeToast("Password required");
        } else {
//            final String phoneId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//            String manufacturer = Build.MANUFACTURER;
//            String model = Build.MODEL;
//
//            JSONObject json = new JSONObject();
//            try {
//                json.put("email", eml);
//                json.put("password", pass);
//                json.put("DeviceId",phoneId);
//                json.put("Manufacturer",manufacturer);
//                json.put("Model",model);
//            }catch (Exception e){
//
//            }
//            Connection c = new Connection(this,App.LOGIN_URL,this,this);
//            c.send(json,"post");

            Auth auth = new Auth(this, this);
            auth.login(eml, pass);
        }
    }

    private void makeToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(),
                message,
                Toast.LENGTH_SHORT);
        toast.show();
    }

//    @Override
//    public void onErrorResponse(VolleyError error) {
//        makeToast(error.getMessage());
//
//    }

//    @Override
//    public void onResponse(Object response) {
//        String token ;
//        try{
//            token = ((JSONObject)response).get("token").toString();
//            SharedPreferences s = this.getSharedPreferences("sharedPreferences", this.MODE_PRIVATE);
//            SharedPreferences.Editor e = s.edit();
//            e.putString("token",token );
//            e.putString("email",eml );
//            e.putString("pass",pass );
//            e.apply();
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }

    @Override
    public void onSuccessfulLogin(String token) {
        new Auth(this).startServices();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onFailedLogin(int code) {
        makeToast("Invalid Credentials");
    }

    public void register(View view){
        Intent i = new Intent(this, RegisterActivity.class);
        this.startActivity(i);
    }


}
