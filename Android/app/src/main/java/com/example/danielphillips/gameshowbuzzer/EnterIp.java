package com.example.danielphillips.gameshowbuzzer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import mehdi.sakout.fancybuttons.FancyButton;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;

public class EnterIp extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_ip);

        ExtendedEditText IpAddress = findViewById(R.id.e_ip_address);
        ExtendedEditText IpPort = findViewById(R.id.e_ip_port);
        FancyButton Connect = findViewById(R.id.e_ip_connect);

        Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
