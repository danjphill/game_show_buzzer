package com.example.danielphillips.gameshowbuzzer;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.danielphillips.gameshowbuzzer.IPHandler.getIPAddress;

public class BuzzerActivity extends AppCompatActivity {
    static String IP_PORT;
    static String TeamName;
    static String IP_ADDRESS;
    static String response;
    static String Type;
    TextView SeasonText;
    TextView QuestionText;
    TextView StatusText ;
    TextView TeamNameText;
    RelativeLayout Layout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buzzer_activity);

       SeasonText = findViewById(R.id.buzzer_season);
       QuestionText = findViewById(R.id.buzzer_question);
        StatusText = findViewById(R.id.buzzer_status_text);
        TeamNameText = findViewById(R.id.buzzer_team_name);
        Layout = findViewById(R.id.buzzer_layout);

        Intent intent = getIntent();
        TeamName = intent.getStringExtra(Constants.TeamName);
        IP_PORT = intent.getStringExtra(Constants.IP_PORT);
        IP_ADDRESS = intent.getStringExtra(Constants.IP_ADDRESS);
        String Season_number = intent.getStringExtra(Constants.SEASON_NUMBER);
        String Question_number = intent.getStringExtra(Constants.QUESTION_NUMBER);

        SeasonText.setText(Season_number);
        QuestionText.setText(Question_number);
        TeamNameText.setText(TeamName);

        Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Type = "ring_buzzer";
                new RetrieveFeedTask().execute();
            }
        });

    }

    private String HttpPost(String IpPort,String IpAddress, String Type, String TeamName) throws IOException, JSONException {


        URL url = new URL("http://"+IpAddress+":"+IpPort+"/"+Type);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        DateTime dateTime = new DateTime();
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd/yy HH:mm:ss.SSSSSS");

        // 2. build JSON object
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("ip",getIPAddress(true) );
        jsonObject.accumulate("username",  TeamName);
        jsonObject.accumulate("sent_time", dateTime.toString("MM/dd/yy HH:mm:ss.SSSSSS"));
        jsonObject.accumulate("question", QuestionText.getText().toString() );
        jsonObject.accumulate("touch_time",dateTime.toString("MM/dd/yy HH:mm:ss.SSSSSS"));
        jsonObject.accumulate("session", SeasonText.getText().toString());

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

    private void GetWinner(){
        Type = "get_winner";
        new RetrieveFeedTask().execute();
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, String > {

        private Exception exception;

        protected String doInBackground(String...strings) {
            try {
                response =  HttpPost(IP_PORT,IP_ADDRESS,Type,TeamName);
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


            Log.d("ip : ", ip);
            Log.d("result_msg : ", result_msg);
            Log.d("return_time : ", return_time);
            Log.d("sent_time : ", sent_time);
            Log.d("status : ", status);


            if (status.equals("004") && Type.equals("ring_buzzer")){
                GetWinner();
            }else if (status.equals("006") && Type.equals("get_winner")){
                String winning_ip = result_obj.getString("winning_ip");
                if (winning_ip.equals(IPHandler.getIPAddress(true))){
                    Layout.setBackgroundColor(Color.GREEN);
                    StatusText.setText("WINER!!!");
                }else{
                    Layout.setBackgroundColor(Color.RED);
                    StatusText.setText("BETTER LUCK NEXT TIME");
                }
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

}
