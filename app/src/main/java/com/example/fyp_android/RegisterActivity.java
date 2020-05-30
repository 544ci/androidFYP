package com.example.fyp_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity implements Listener, ErrorListener {
    private TextView emailTextView;
    private TextView passTextView;
    private TextView confirmPassTextView;
    private Button registerButtonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        emailTextView = findViewById(R.id.inputEmail);
        passTextView = findViewById(R.id.inputPass);
        confirmPassTextView = findViewById(R.id.inputConfirmPass);
        registerButtonView = findViewById(R.id.registerButton);
    }


    public void register(View view) {
        String email = emailTextView.getText().toString();
        String pass = passTextView.getText().toString();
        String cPass = confirmPassTextView.getText().toString();
        if (email.length() == 0) {
            makeToast("Email required");
        } else if (pass.length() == 0) {
            makeToast("Password required");
        } else if (!pass.equals(cPass)) {
            makeToast("Passwords do not match");
        } else {
            JSONObject json = new JSONObject();
            try {
                json.put("Email", email);
                json.put("Password", pass);
            } catch (Exception e) {

            }
            Connection c = new Connection(this, App.REGISTER_URL, this, this);
            c.send(json, "post");
        }

    }

    private void makeToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(),
                message,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onResponse(Object response) {
        Toast.makeText(this, "You Have been registered.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Email already registered.", Toast.LENGTH_SHORT).show();
    }
}
