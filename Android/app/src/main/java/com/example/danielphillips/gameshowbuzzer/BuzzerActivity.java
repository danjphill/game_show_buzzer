package com.example.danielphillips.gameshowbuzzer;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;

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

import mehdi.sakout.fancybuttons.FancyButton;

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
    TextView RingTimeText;
    RelativeLayout Layout;
    FancyButton Reset;
    MediaPlayer mp;
    Boolean BellRang;
    Boolean Ready = false;
    SpinKitView loadingBar;
    int Tries = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buzzer_activity);

       SeasonText = findViewById(R.id.buzzer_season);
       QuestionText = findViewById(R.id.buzzer_question);
        StatusText = findViewById(R.id.buzzer_status_text);
        TeamNameText = findViewById(R.id.buzzer_team_name);
        Reset = findViewById(R.id.buzzer_reset);
        Layout = findViewById(R.id.buzzer_layout);
        RingTimeText = findViewById(R.id.buzzer_ring_time);
        loadingBar = findViewById(R.id.spin_kit);

        Intent intent = getIntent();
        TeamName = intent.getStringExtra(Constants.TeamName);
        IP_PORT = intent.getStringExtra(Constants.IP_PORT);
        IP_ADDRESS = intent.getStringExtra(Constants.IP_ADDRESS);
        String Season_number = intent.getStringExtra(Constants.SEASON_NUMBER);
        String Question_number = intent.getStringExtra(Constants.QUESTION_NUMBER);
        BellRang = false;

         mp = MediaPlayer.create(this, R.raw.chime);

        SeasonText.setText(Season_number);
        QuestionText.setText(Question_number);
        TeamNameText.setText(TeamName);

        Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Type = "ring_buzzer";
                if (!BellRang) {
                    BellRang = true;
                    new RetrieveFeedTask().execute();
                }
            }
        });

        Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Type = "i_am_ready";
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
        jsonObject.accumulate("ip",getIPAddress(BuzzerActivity.this, true) );
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
    private void CheckIfEveryoneIsReady(){
        Type = "is_everyone_ready";
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
            try {
                Log.d("Result : ", result);
                Log.d("EnterIP : Response", response);
                try {

                    JSONObject obj = new JSONObject(response);
                    String result_full = obj.getString("result");
                    JSONObject result_obj = new JSONObject(result_full);
                    String ip = result_obj.getString("ip");
                    String result_msg = result_obj.getString("result");

                    String return_time = result_obj.getString("return_time");
                    String sent_time = result_obj.getString("sent_time");
                    String status = result_obj.getString("status");
                    String question_number = "0";
                    if (status.equals("010") && Type.equals("is_everyone_ready")) {
                        question_number = result_obj.getString("question_number");
                    }


                    Log.d("ip : ", ip);
                    Log.d("result_msg : ", result_msg);
                    Log.d("return_time : ", return_time);
                    Log.d("sent_time : ", sent_time);
                    Log.d("status : ", status);


                    if (status.equals("004") && Type.equals("ring_buzzer")) {
                        Ready = false;
                        mp.start();
                        GetWinner();
                    } else if (status.equals("006") && Type.equals("get_winner")) {
                        String winning_ip = result_obj.getString("winning_ip");
                        if (winning_ip.equals(IPHandler.getIPAddress(BuzzerActivity.this, true))) {
                            Layout.setBackgroundColor(Color.GREEN);
                            StatusText.setText("WINNER!!!");
                            RingTimeText.setText(return_time.split(" ")[1]);
                            RingTimeText.setVisibility(View.VISIBLE);
                        } else {
                            Layout.setBackgroundColor(Color.RED);
                            StatusText.setText("BETTER LUCK NEXT TIME");
                        }
                        Reset.setText("NEXT QUESTION");
                        Reset.setVisibility(View.VISIBLE);

                    } else if (status.equals("008") && Type.equals("i_am_ready")) {
                        String CurrentQuestion = QuestionText.getText().toString();
//                QuestionText.setVisibility(View.GONE);
                        Layout.setBackgroundColor(Color.LTGRAY);
                        Reset.setVisibility(View.GONE);
                        RingTimeText.setVisibility(View.GONE);
                        CheckIfEveryoneIsReady();
                    } else if (status.equals("009") && Type.equals("is_everyone_ready")) {
                        StatusText.setText(result_msg);
                        loadingBar.setVisibility(View.VISIBLE);
                        Ready = false;
                        try {
                            Tries += 1;
                            if (Tries < 50) {
                                Thread.sleep(1000);
                                CheckIfEveryoneIsReady();
                            } else if (Tries < 100) {
                                Thread.sleep(3000);
                                CheckIfEveryoneIsReady();
                            } else if (Tries < 200) {
                                Thread.sleep(5000);
                                CheckIfEveryoneIsReady();
                            }
                        } catch (InterruptedException e) {

                        }
                    } else if (status.equals("010") && Type.equals("is_everyone_ready")) {
                        Ready = true;
                        BellRang = false;
                        StatusText.setText("READY!!");
                        loadingBar.setVisibility(View.GONE);
                        Tries = 0;
                        QuestionText.setText(question_number);
                        QuestionText.setVisibility(View.VISIBLE);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (java.lang.NullPointerException e) {
                Toast.makeText(BuzzerActivity.this, "Check Network Connection", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        }

}
