package com.example.danielphillips.gameshowbuzzer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import mehdi.sakout.fancybuttons.FancyButton;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FancyButton NextButton = findViewById(R.id.main_next);
        ExtendedEditText TeamName = findViewById(R.id.main_team_name);

        NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
