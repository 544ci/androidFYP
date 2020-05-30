package com.example.fyp_android;

public interface RemoteTaskListener {
    public void onTaskCompleted(int taskId);

    public void onTaskFailed(int taskId);
}
