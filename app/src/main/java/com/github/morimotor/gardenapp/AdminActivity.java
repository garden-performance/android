package com.github.morimotor.gardenapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class AdminActivity extends AppCompatActivity {

    static String TAG = AdminActivity.class.getSimpleName();
    Button button;
    Activity activity = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("管理画面");

        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // WebAPI からjson取得
                AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {

                        String jsonStr = "";

                        OkHttpClient client = new OkHttpClient();

                        Request request = new Request.Builder()
                                //.url("host/func/" + dev_num)
                                .url("http://www.yahoo.co.jp")
                                .get()
                                .build();


                        try {
                            Response response = client.newCall(request).execute();


                            Log.d(TAG, "" + response.body().string());


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        jsonStr = "{"  +
                                "  ‘device_id’: 10," +
                                "  ‘red’: True," +
                                "  ‘green’: False," +
                                "  ‘blue’: False," +
                                "  ’tape’: False" +
                                "}";




                        return jsonStr;
                    }

                    @Override
                    protected void onPostExecute(String json) {

                        // Mainactivityへ通知
                        BusHolder.get().post(new ButtonClickEvent(json));
                        Log.d(TAG, "" + json);
                    }
                };
                task.execute();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
