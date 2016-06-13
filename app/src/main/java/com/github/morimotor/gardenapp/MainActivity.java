package com.github.morimotor.gardenapp;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.felipecsl.gifimageview.library.GifImageView;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.nispok.snackbar.Snackbar;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static String TAG = MainActivity.class.getSimpleName();
    Activity activity = this;

    BluetoothSPP bt;
    boolean connectFlag = false;

    Button topButton;
    GifImageView bottleButton1;
    GifImageView bottleButton2;
    GifImageView bottleButton3;

    boolean isTappedTopBotton = false;
    boolean isUpBottle1 = false;
    boolean isUpBottle2 = false;
    boolean isUpBottle3 = false;

    boolean isMoving = false;

    int gifDuration  = 1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Subscriberとして登録する
        BusHolder.get().register(activity);

        // widget関係
        topButton = (Button) findViewById(R.id.topButton);
        bottleButton1 = (GifImageView) findViewById(R.id.bottleButton1);
        bottleButton2 = (GifImageView) findViewById(R.id.bottleButton2);
        bottleButton3 = (GifImageView) findViewById(R.id.bottleButton3);

        topButton.setOnClickListener(this);
        bottleButton1.setOnClickListener(this);
        bottleButton2.setOnClickListener(this);
        bottleButton3.setOnClickListener(this);

        gifAnimation(bottleButton1, gifDuration, "rwineb.gif");
        gifAnimation(bottleButton2, gifDuration, "rwiner.gif");
        gifAnimation(bottleButton3, gifDuration, "rwiney.gif");



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        new SecondaryDrawerItem().withName("管理画面"),
                        new SecondaryDrawerItem().withName("接続")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D

                        if (position == 0) {
                            Intent intent = new Intent(activity, LoginActivity.class);
                            startActivity(intent);

                        }

                        if(position == 1){
                            if(bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                                bt.disconnect();
                            } else {
                                Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                            }
                        }

                        return false;
                    }
                })
                .build();





        bt = new BluetoothSPP(this);
        bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);


        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Log.d(TAG, "onDataReceived : recv:" + message);

            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                Log.d(TAG, "onDeviceDisconnected : disconnected");
                connectFlag = false;
            }

            public void onDeviceConnectionFailed() {
                Log.d(TAG, "onDeviceDisconnected : conection failed");

            }

            public void onDeviceConnected(String name, String address) {
                Log.d(TAG, "onDeviceConnected : device conected");
                connectFlag = true;

            }
        });




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bt.stopService();


        // Subscriberの登録を解除する
        BusHolder.get().unregister(activity);

    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);

                Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null)
            if(data.getExtras() != null){
                Log.d(TAG, "onActivityResult : " + data.getExtras().getString(BluetoothState.EXTRA_DEVICE_ADDRESS));

                if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
                    if(resultCode == Activity.RESULT_OK)
                        Log.d(TAG, "onActivityResult : connect");
                    bt.connect(data);

                } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
                    if(resultCode == Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult : setup");

                        bt.setupService();
                        //bt.startService(BluetoothState.DEVICE_OTHER);
                    } else {
                        Log.d(TAG, "onActivityResult : buu");

                        Toast.makeText(getApplicationContext()
                                , "Bluetooth was not enabled."
                                , Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

            }



    }

    @Subscribe
    public void subscribe(ButtonClickEvent event) throws JSONException {
        Log.d(TAG, "event bus:" + event.message);

        JSONObject json = new JSONObject(event.message);
        String device_id = json.getString("‘device_id’");
        Boolean red = json.getBoolean("‘red’");
        Boolean green = json.getBoolean("‘green’");
        Boolean blue = json.getBoolean("‘blue’");
        Boolean tape = json.getBoolean("’tape’");

        Log.d(TAG, "[device_id]:" + device_id + " [red]:" + red + " [green]:" + green + " [blue]:" + blue + " [tape]:" + tape);

        if(green)bottleButton1.setVisibility(View.VISIBLE);
        else bottleButton1.setVisibility(View.GONE);
        if(red)bottleButton2.setVisibility(View.VISIBLE);
        else bottleButton2.setVisibility(View.GONE);
        if(blue)bottleButton3.setVisibility(View.VISIBLE);
        else bottleButton3.setVisibility(View.GONE);

    }


    public void animationUp(View v){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat( v, "translationY", v.getTranslationY(), -800.0f );
        objectAnimator.setDuration(gifDuration);
        objectAnimator.start();
    }
    public void animationDown(View v){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat( v, "translationY", v.getTranslationY(), 0.0f );
        objectAnimator.setDuration(gifDuration);
        objectAnimator.start();
    }

    @Override
    public void onClick(View v) {

        if(isMoving)return;

        if(!connectFlag){
            Snackbar.with(activity)
                    .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                    .text("Status : Not connect")
                    .show(activity);
            return;
        }

        switch (v.getId()){

            case R.id.topButton:
                if (!isTappedTopBotton){
                    topButton.setBackgroundColor(Color.YELLOW);
                    isTappedTopBotton = true;
                    bt.send("g", true);
                }
                else {
                    topButton.setBackgroundColor(Color.GRAY);
                    isTappedTopBotton = false;
                    bt.send("h", true);
                }
                break;


            case R.id.bottleButton1:
                if(!isUpBottle1){
                    animationUp(bottleButton1);
                    animationDown(bottleButton2);
                    animationDown(bottleButton3);

                    if(isUpBottle2)gifAnimation(bottleButton2, gifDuration, "rwiner.gif");
                    if(isUpBottle3)gifAnimation(bottleButton3, gifDuration, "rwiney.gif");

                    isUpBottle1 = true;
                    isUpBottle2 = false;
                    isUpBottle3 = false;

                    gifAnimation(bottleButton1, gifDuration, "wineb.gif");
                    isMoving = true;
                    bt.send("a", true);

                } else{
                    animationDown(bottleButton1);
                    gifAnimation(bottleButton1, gifDuration, "rwineb.gif");
                    isMoving = true;
                    isUpBottle1 = false;
                    bt.send("d", true);
                }
                break;

            case R.id.bottleButton2:
                if(!isUpBottle2){
                    animationDown(bottleButton1);
                    animationUp(bottleButton2);
                    animationDown(bottleButton3);

                    if(isUpBottle1)gifAnimation(bottleButton1, gifDuration, "rwineb.gif");
                    if(isUpBottle3)gifAnimation(bottleButton3, gifDuration, "rwiney.gif");

                    isUpBottle1 = false;
                    isUpBottle2 = true;
                    isUpBottle3 = false;

                    gifAnimation(bottleButton2, gifDuration, "winer.gif");
                    isMoving = true;
                    bt.send("b", true);

                } else{
                    animationDown(bottleButton2);
                    gifAnimation(bottleButton2, gifDuration, "rwiner.gif");
                    isMoving = true;
                    isUpBottle2 = false;

                    bt.send("e", true);
                }
                break;

            case R.id.bottleButton3:
                if(!isUpBottle3){
                    animationDown(bottleButton1);
                    animationDown(bottleButton2);
                    animationUp(bottleButton3);

                    if(isUpBottle1)gifAnimation(bottleButton1, gifDuration, "rwineb.gif");
                    if(isUpBottle2)gifAnimation(bottleButton2, gifDuration, "rwiner.gif");

                    isUpBottle1 = false;
                    isUpBottle2 = false;
                    isUpBottle3 = true;

                    gifAnimation(bottleButton3, gifDuration, "winey.gif");
                    isMoving = true;
                    bt.send("c", true);

                }
                else{
                    animationDown(bottleButton3);
                    gifAnimation(bottleButton3, gifDuration, "rwiney.gif");
                    isMoving = true;
                    isUpBottle3 = false;

                    bt.send("f", true);
                }
                break;

        }
    }

    void gifAnimation(final GifImageView v, int duration, String filename){
        try {
            InputStream is = getAssets().open(filename);
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            is.close();

            v.setBytes(bytes);
            v.startAnimation();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    v.stopAnimation();
                    isMoving = false;
                }
            }, duration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
