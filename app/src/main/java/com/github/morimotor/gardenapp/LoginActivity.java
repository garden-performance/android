package com.github.morimotor.gardenapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.glomadrian.codeinputlib.CodeInput;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {

    CodeInput codeInput;

    Timer mTimer    = new Timer(true);            //onClickメソッドでインスタンス生成
    Handler mHandler = new Handler();   //UI Threadへのpost用ハンドラ

    Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("ログイン");

        codeInput = (CodeInput)findViewById(R.id.codeInput);


        mTimer.schedule( new TimerTask(){
            @Override
            public void run() {
                // mHandlerを通じてUI Threadへ処理をキューイング
                mHandler.post( new Runnable() {
                    public void run() {

                        String code = Arrays.toString(codeInput.getCode());
                        //Log.d("login", code + code.equals("[8, 9, 8, 9]"));


                        if(code.equals("[8, 9, 8, 9]")){
                            Intent intent = new Intent(activity, AdminActivity.class);
                            startActivity(intent);
                            mTimer.cancel();
                            finish();
                        }
                    }
                });
            }
        }, 200, 200);
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
