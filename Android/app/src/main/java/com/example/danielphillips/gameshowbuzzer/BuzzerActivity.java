package com.example.danielphillips.gameshowbuzzer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class BuzzerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buzzer_activity);

        TextView SeasonText = findViewById(R.id.buzzer_season);
        TextView QuestionText = findViewById(R.id.buzzer_question);
        TextView StatusText = findViewById(R.id.buzzer_status_text);


    }
}
