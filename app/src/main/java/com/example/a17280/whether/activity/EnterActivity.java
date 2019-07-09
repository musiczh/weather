package com.example.a17280.whether.activity;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.a17280.whether.Interface.HttpCallBackListener;
import com.example.a17280.whether.R;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.example.a17280.whether.util.InternetUtil.sendHttpURL;

public class EnterActivity extends AppCompatActivity {
    private OverBroadcastReceiver overBroadcastReceiver; //广播接收器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        //接受广播,结束活动
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("OVER");
        overBroadcastReceiver = new OverBroadcastReceiver();
        registerReceiver(overBroadcastReceiver,intentFilter);

        //申请权限
        if (Build.VERSION.SDK_INT >= 16){
            ActivityCompat.requestPermissions(EnterActivity.this,new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }


        //给进入按钮设置监听
        Button button = findViewById(R.id.button_enter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshNew();
                Intent intent = new Intent(EnterActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });




    }//-------------onCreate----------


    //广播接收类，退出活动
    class OverBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            finish();
        }
    }

    //获取经纬度
    private String getLocation(Context context){
        double latitude = 0.0;
        double longitude = 0.0;

        LocationManager locationManager = (LocationManager) context .getSystemService(Context.LOCATION_SERVICE);
        if(ContextCompat.checkSelfPermission(EnterActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            return "no";
        }else {

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {//从gps获取经纬度

                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {//当GPS信号弱没获取到位置的时候又从网络获取

                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                        Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location1 != null) {
                            latitude = location1.getLatitude();
                            longitude = location1.getLongitude();
                        }
                    }else{
                        return "no";
                    }
                }


            } else {    //从网络获取经纬度

                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                    Location location2 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location2 != null) {
                        latitude = location2.getLatitude();
                        longitude = location2.getLongitude();
                    }

                }else{
                    return "no";
                }
            }

        }
        return latitude + ":" + longitude;
    }
    LocationListener locationListener = new LocationListener() {

        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {

        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {

        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
        }
    };
    //定位并初始化天气
    protected void refreshNew(){
        String location = getLocation(EnterActivity.this);
        if(location.equals("no")) {
            Toast.makeText(EnterActivity.this,"定位失败",Toast.LENGTH_SHORT).show();
        }else{
            String data = location;
            String address = "https://api.seniverse.com/v3/location/search.json?key=SMhxMxzEERc3eGJ4s&limit=100&q=" + data;
            sendHttpURL(address, new HttpCallBackListener() {
                @Override
                public void onFinish(String response) {
                    parseCity(response);
                }

                @Override
                public void onError(Exception e) {
                   e.printStackTrace();
                }
            });
        }
    }

    //解析城市信息JSON数据并存储在键中
    protected void parseCity(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
            SharedPreferences.Editor editor = PreferenceManager.
                    getDefaultSharedPreferences(EnterActivity.this).edit();
            String name = jsonObject1.getString("name");
            editor.putString("city_name", name);
            editor.apply();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //关闭广播
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(overBroadcastReceiver);
    }
}
