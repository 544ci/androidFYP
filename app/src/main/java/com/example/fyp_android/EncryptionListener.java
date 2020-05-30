package com.example.fyp_android;

public interface EncryptionListener {
    public void onSDCardEncryptionComplete();

    public void onSDCardEncryptionFailed(String errMessage);

    public void onSDCardDecryptionComplete();

    public void onSDCardDecryptionFailed(String errMessage);
}
