package com.akashapplications.dcpoint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

import com.akashapplications.dcpoint.utils.LocalPreference;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1500);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(new LocalPreference(getBaseContext()).idLoggedIn())
                            startActivity(new Intent(getBaseContext(), Home.class));
                        else
                            startActivity(new Intent(getBaseContext(), Login.class));
                        finish();
                    }
                });
            }
        }).start();
    }
}
