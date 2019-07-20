package com.example.danielphillips.gameshowbuzzer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;

import static com.example.danielphillips.gameshowbuzzer.IPHandler.getIPAddress;

public class EnterIp extends AppCompatActivity {
    static ExtendedEditText IpPort;
    static String TeamName;
    static ExtendedEditText IpAddress;
    static String response;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_ip);

        IpPort = findViewById(R.id.e_ip_port);
        TeamName = PreferencesHandler.LoadPreference(EnterIp.this,Constants.TeamName);
        IpAddress = findViewById(R.id.e_ip_address);
        FancyButton Connect = findViewById(R.id.e_ip_connect);

        IpAddress.setText(PreferencesHandler.LoadPreference(EnterIp.this,Constants.IP_ADDRESS));
        IpPort.setText(PreferencesHandler.LoadPreference(EnterIp.this,Constants.IP_PORT));

        Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
//                   String response =  HttpPost(IpPort.getText().toString(),IpAddress.getText().toString(),TeamName);
                    new RetrieveFeedTask().execute();
//                    Log.d("EnterIP : Response", response);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

            }
        });
    }

    private String HttpPost(String IpPort,String IpAddress, String TeamName) throws IOException, JSONException {


        URL url = new URL("http://"+IpAddress+":"+IpPort+"/connect");

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        DateTime dateTime = new DateTime();

        // 2. build JSON object
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("ip",getIPAddress(EnterIp.this, true) );
        jsonObject.accumulate("username",  TeamName);
        jsonObject.accumulate("sent_time", dateTime.toString());

        // 3. add JSON content to POST request body
        setPostRequestContent(conn, jsonObject);
        Log.d("EnterIP : SentObj", jsonObject.toString());

        // 4. make POST request to the given URL
        conn.connect();

        BufferedReader bufferedReader = null;
        bufferedReader = new BufferedReader(new
                InputStreamReader(conn.getInputStream()));

        String result;

        result = bufferedReader.readLine();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        // 5. return response message
//        JSONObject jobj = new JSONObject(conn.getResponseMessage());
//        return conn.getResponseMessage()+"";
        return result;

    }

    private void setPostRequestContent(HttpURLConnection conn,
                                       JSONObject jsonObject) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        Log.i(MainActivity.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }

//    private String LoadPreference(Context context,String itemName){
//        //TODO: Convert To Constant >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>v
//        SharedPreferences sharedPref = getSharedPreferences("preferences", Context.MODE_PRIVATE);
//
//        String loadedString = sharedPref.getString(itemName, "");
//        return loadedString;
//
//    }


    class RetrieveFeedTask extends AsyncTask<String, Void, String > {

        private Exception exception;

        protected String doInBackground(String...strings) {
            try {
                response =  HttpPost(IpPort.getText().toString(),IpAddress.getText().toString(),TeamName);
                return response;
            } catch (Exception e) {
                this.exception = e;
                return null;
            } finally {

            }
        }

        protected void onPostExecute(String result) {
            // TODO: check this.exception
            // TODO: do something with the feed
            Log.d("Result : ", result);
            Log.d("EnterIP : Response", response);
            try {
                JSONObject obj = new JSONObject(response);
                String result_msg = obj.getString("result");
                JSONObject result_obj = new JSONObject(result_msg);
                String ip = result_obj.getString("ip");

                String return_time = result_obj.getString("return_time");
                String sent_time = result_obj.getString("sent_time");
                String status = result_obj.getString("status");
                String question_number = result_obj.getString("question_number");
                Log.d("ip : ", ip);
                Log.d("result_msg : ", result_msg);
                Log.d("return_time : ", return_time);
                Log.d("sent_time : ", sent_time);
                Log.d("status : ", status);
                Log.d("question_number : ", question_number);

                if (status.equals("002")){
                    Intent BuzzerActivityIntent = new Intent(EnterIp.this,BuzzerActivity.class);
                    BuzzerActivityIntent.putExtra(Constants.IP_ADDRESS,IpAddress.getText().toString());
                    BuzzerActivityIntent.putExtra(Constants.IP_PORT,IpPort.getText().toString());
                    BuzzerActivityIntent.putExtra(Constants.TeamName,TeamName);
                    BuzzerActivityIntent.putExtra(Constants.SEASON_NUMBER,"1");
                    BuzzerActivityIntent.putExtra(Constants.QUESTION_NUMBER,question_number);
                    startActivity(BuzzerActivityIntent);
                    PreferencesHandler.SavePreferences(EnterIp.this,Constants.IP_ADDRESS,IpAddress.getText().toString());
                    PreferencesHandler.SavePreferences(EnterIp.this,Constants.IP_PORT,IpPort.getText().toString());
                }



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



}
