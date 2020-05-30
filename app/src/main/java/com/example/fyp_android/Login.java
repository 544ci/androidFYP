package com.example.fyp_android;

public interface Login {
    public void onSuccessfulLogin(String token);

    public void onFailedLogin(int code);
}
