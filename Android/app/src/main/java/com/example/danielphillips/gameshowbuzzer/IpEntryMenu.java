package com.example.danielphillips.gameshowbuzzer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

public class IpEntryMenu extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ip_entry_menu);

        LinearLayout ScanIPBarcode = findViewById(R.id.ip_e_menu_scan_btn);
        LinearLayout EnterIpAddress = findViewById(R.id.ip_e_menu_enter_ip_btn);

        ScanIPBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        EnterIpAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent EnterIpIntent = new Intent(IpEntryMenu.this,EnterIp.class);
                startActivity(EnterIpIntent);
                finish();

            }
        });

    }
}
