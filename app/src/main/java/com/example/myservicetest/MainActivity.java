package com.example.myservicetest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.myservicetest.download.DownloadActivity;

public class MainActivity extends AppCompatActivity {

    private Button serviceTest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceTest = findViewById(R.id.service_test);
        serviceTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
               startActivity(intent);

            }
        });

    }

}
