package com.example.evernymfluttertest;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import io.flutter.app.FlutterActivity;

public class BaseActivity extends FlutterActivity {
    protected MainApplication mainApp;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainApp = (MainApplication)this.getApplicationContext();
    }
    protected void onResume() {
        super.onResume();
        mainApp.setCurrentActivity(this);
    }
    protected void onPause() {
        clearReferences();
        super.onPause();
    }
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }





    private void clearReferences(){
        Activity currActivity = mainApp.getCurrentActivity();
        if (this.equals(currActivity)) {
            mainApp.setCurrentActivity(null);
        }
    }
}