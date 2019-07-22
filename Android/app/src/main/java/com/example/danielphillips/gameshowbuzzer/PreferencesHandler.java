package com.example.danielphillips.gameshowbuzzer;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class PreferencesHandler {

    static void SavePreferences(Context context, String Constant, String Value){
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(Constant, Value);
//        editor.putString("passwordhash", "somerandompasswordhash");

        editor.apply();
//        Toast.makeText(context, Constant + " Saved", Toast.LENGTH_LONG).show();



    }
    static  String LoadPreference(Context context,String itemName){
        //TODO: Convert To Constant >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>v
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

        String loadedString = sharedPref.getString(itemName, "");
        return loadedString;

    }

}
