package com.example.danielphillips.gameshowbuzzer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import mehdi.sakout.fancybuttons.FancyButton;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FancyButton NextButton = findViewById(R.id.main_next);
        final ExtendedEditText TeamName = findViewById(R.id.main_team_name);
        TeamName.setText(PreferencesHandler.LoadPreference(MainActivity.this,Constants.TeamName));

        NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent IpEntryMenuIntent = new Intent(MainActivity.this, IpEntryMenu.class);
                IpEntryMenuIntent.putExtra(Constants.TeamName,TeamName.getText().toString());
                PreferencesHandler.SavePreferences(MainActivity.this,Constants.TeamName,TeamName.getText().toString());
                startActivity(IpEntryMenuIntent);
                finish();

            }
        });
    }
//
//    void SavePreferences(Context context, String TeamName){
//        SharedPreferences sharedPref = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//
//        editor.putString(Constants.TeamName, TeamName);
////        editor.putString("passwordhash", "somerandompasswordhash");
//
//        editor.apply();
//        Toast.makeText(context, "Team Name Save", Toast.LENGTH_LONG).show();
//
//
//
//    }
}
