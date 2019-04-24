package com.android.chatapp;

public interface AudioCallbacks {
    void onUpdate(int percentage);
    void onPause();
    void onStop();
}
