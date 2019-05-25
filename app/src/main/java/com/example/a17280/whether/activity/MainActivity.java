package com.example.a17280.whether.activity;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.a17280.whether.R.drawable.car_washing_64_white_img;
import static com.example.a17280.whether.R.drawable.clothes_64_white_img;
import static com.example.a17280.whether.R.drawable.flu_64_white_img;
import static com.example.a17280.whether.R.drawable.sport_64_white_img;
import static com.example.a17280.whether.R.drawable.travel_64_white_img;
import static com.example.a17280.whether.R.drawable.uv_64_white_img;
import static com.example.a17280.whether.util.InternetUtil.sendHttpURL;

import com.example.a17280.whether.Interface.HttpCallBackListener;
import com.example.a17280.whether.R;
import com.example.a17280.whether.adapter.LifeAdapter;
import com.example.a17280.whether.adapter.WhetherAdapter;
import com.example.a17280.whether.entity.Life;
import com.example.a17280.whether.entity.Whether;
import com.example.a17280.whether.manager.NoBugLinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.a17280.whether.R.drawable.sun;
import static com.example.a17280.whether.util.UiUtil.chooseWhetherIcon;

public class MainActivity extends AppCompatActivity {


    private List<Whether> whetherList = new ArrayList<>();
    private List<Life> lifeList = new ArrayList<>();
    private WhetherAdapter whetherAdapter;
    private LifeAdapter lifeAdapter;
    private int icon;
    protected TextView textViewTemperature;
    protected TextView textViewWhether;
    protected TextView textViewCity;
    protected ImageView imageViewWhether;
    protected String nowWhether;
    protected String nowTemperature;
    protected String nowWhetherCode;
    protected String nowCity;
    protected ProgressDialog progressDialog ;
    private SharedPreferences preferences ;
    private NetBroadcastReceiver netBroadcastReceiver;  //广播接收器
    private RefreshBroadcastReceiver refreshBroadcastReceiver;//广播接收器
    private SwipeRefreshLayout swipeRefreshLayout;//下拉刷新


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置标题栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("简易天气预报");
        setSupportActionBar(toolbar);





        //初始化控件
        progressDialog =  new ProgressDialog(this);
        textViewTemperature = (TextView)findViewById(R.id.text_view_temp);
        textViewWhether = (TextView) findViewById(R.id.text_view_whether);
        textViewCity = (TextView) findViewById(R.id.text_view_city);
        imageViewWhether = (ImageView) findViewById(R.id.image_view_whether);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view_whether);
        RecyclerView recyclerView1 = (RecyclerView) findViewById(R.id.recycler_view_life);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_location);

        //设置监听
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MyCityActivity.class);
                startActivity(intent);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWhether();
            }
        });




        //接受广播提示没有网络
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        netBroadcastReceiver = new NetBroadcastReceiver();
        registerReceiver(netBroadcastReceiver,intentFilter);
        //接受广播刷新数据
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction("REFRESH");
        refreshBroadcastReceiver = new RefreshBroadcastReceiver();
        registerReceiver(refreshBroadcastReceiver,intentFilter1);
        //发送广播
        Intent intent = new Intent("OVER");
        sendBroadcast(intent);


        //判断是否已经选择了城市，否则打开另一个活动选择城市
        if (preferences.getString("city_name",null) == null) {
            refreshNew();
        }else{
            refreshWhether();
        }


        //---------recyclerView
        add();
       NoBugLinearLayoutManager layoutManager = new NoBugLinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        whetherAdapter = new WhetherAdapter(whetherList);
        recyclerView.setAdapter(whetherAdapter);

        add1();
        NoBugLinearLayoutManager layoutManager1 = new NoBugLinearLayoutManager(this);
        layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView1.setLayoutManager(layoutManager1);
        recyclerView1.setNestedScrollingEnabled(false);
        lifeAdapter = new LifeAdapter(lifeList);
        recyclerView1.setAdapter(lifeAdapter);








    }//--------------------------------onCreate-----------------------------------------------------


    //刷新天气
    protected void refreshWhether(){


        String nowWhetherUrl =
                "https://api.seniverse.com/v3/weather/now.json?key=SvgmH3LiKYKouNOB-&location=";
        String dailyWhetherUrl =
                "https://api.seniverse.com/v3/weather/daily.json?key=SvgmH3LiKYKouNOB-&location=";
        String lifeWhetherUrl =
                "https://api.seniverse.com/v3/life/suggestion.json?key=SvgmH3LiKYKouNOB-&location=";


       // Toast.makeText(MainActivity.this,"开始刷新天气",Toast.LENGTH_SHORT).show();
        String address = nowWhetherUrl+preferences.getString("city_name","");
            sendHttpURL(address, new HttpCallBackListener() {
                @Override
                public void onFinish(String response) {
                    parseNowJSON(response);

                }
                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    toastErrorUrl();
                }
            });

        String address1 = dailyWhetherUrl+preferences.getString("city_name","");
        sendHttpURL(address1, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parseDailyWhether(response);

            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();

                toastErrorUrl();
            }
        });

        String address2 = lifeWhetherUrl+preferences.getString("city_name","");
        sendHttpURL(address2, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parseLife(response);
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                toastErrorUrl();

            }
        });



    }
    //定位并初始化天气
    protected void refreshNew(){
        String location = getLocation(MainActivity.this);
        if(location.equals("no")) {
            Toast.makeText(MainActivity.this,"定位失败",Toast.LENGTH_SHORT).show();
        }else{
            String address = "https://api.seniverse.com/v3/location/search.json?key=SMhxMxzEERc3eGJ4s&limit=100&q=" + location;
            sendHttpURL(address, new HttpCallBackListener() {
                @Override
                public void onFinish(String response) {
                    parseCity(response);
                    refreshWhether();
                }

                @Override
                public void onError(Exception e) {
                    toastErrorUrl();
                }
            });
        }
    }


    // 给顶部标题栏按钮设置响应事件
    //先加载菜单布局文件
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    //再给标题栏按钮设置响应事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share_item:
                shareText();
                break;
            case R.id.add_item:
                Intent intent = new Intent(MainActivity.this,MyCityActivity.class);
                startActivity(intent);
                break;
            default:
        }
        return true;
    }


    //-展示的模拟列表-
    private void add(){
        for (int i = 0;i<=15;i++){
            Whether whether = new Whether();
            whether.setImageView_whether(sun);
            whether.setString_whether("多云");
            whetherList.add(whether);
        }
    }
    private void add1(){
        for (int i = 0;i<=6;i++){
            Life life = new Life();
            life.setImageView_life(clothes_64_white_img);
            life.setString_model_life("null");
            life.setString_life("null");
            lifeList.add(life);
        }
    }



    //---回到主线程刷新布局-
    public void refreshNowLayout(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String string = nowTemperature+"℃";
                textViewWhether.setText(nowWhether);
                textViewTemperature.setText(string);
                textViewCity.setText(nowCity);
                imageViewWhether.setImageResource(icon);
                swipeRefreshLayout.setRefreshing(false);


            }
        });
    }
    public void refreshDailyLayout(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               whetherAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

            }
        });
    }
    public void refreshLifeLayout(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lifeAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                progressDialog.dismiss();
            }
        });
    }

    //解析天气实况的JSON数据
    protected void parseNowJSON(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
            JSONObject now = jsonObject1.getJSONObject("now");
            JSONObject location = jsonObject1.getJSONObject("location");
            nowWhether = now.getString("text");
            nowTemperature = now.getString("temperature");
            nowWhetherCode = now.getString("code");
            nowCity = location.getString("name");
            String code = now.getString("code");
            icon = chooseWhetherIcon(code);
            Intent intent1 = new Intent("com.example.a17280.whether.widget");
            intent1.putExtra("whether",nowWhether);
            intent1.putExtra("temperature",nowTemperature);
            intent1.putExtra("location",nowCity);
            intent1.putExtra("imageResource",icon);
            sendBroadcast(intent1);
            //刷新布局
            refreshNowLayout();
        }catch (Exception e){
            toastErrorUnknow();
            e.printStackTrace();
        }
    }
    //解析天气预报的JSON数据存储在whetherList中
    protected void parseDailyWhether(String data){
        try {
            whetherList.clear();
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
            JSONArray jsonArrayDaily = jsonObject1.getJSONArray("daily");
            for (int i = 0; i < jsonArrayDaily.length(); i++) {
                JSONObject object = jsonArrayDaily.getJSONObject(i);
                Whether whether = new Whether();
                String code = object.getString("code_day");
                int icon = chooseWhetherIcon(code);
                String temp = object.getString("high") + "℃/" + object.getString("low") + "℃";
                whether.setString_whether(object.getString("text_day"));
                whether.setString_day(object.getString("date"));
                whether.setImageView_whether(icon);
                whether.setString_tempareture(temp);
                whetherList.add(whether);
                //刷新布局
                refreshDailyLayout();
            }
        } catch (Exception e){
            toastErrorUnknow();
            e.printStackTrace();
        }
    }
    //解析生活指数JSON数据存储在lifeList中
    protected void parseLife(String data){
        lifeList.clear();
        try {
            JSONObject jsonObject = new JSONObject(data);
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                JSONObject jsonObject0 = jsonArray.getJSONObject(0);
                JSONObject jsonObject1 = jsonObject0.getJSONObject("suggestion");


                JSONObject dressing = jsonObject1.getJSONObject("dressing");
                JSONObject flu = jsonObject1.getJSONObject("flu");
                JSONObject sport = jsonObject1.getJSONObject("sport");
                JSONObject uv = jsonObject1.getJSONObject("uv");
                JSONObject car_washing = jsonObject1.getJSONObject("car_washing");
                JSONObject travel = jsonObject1.getJSONObject("travel");

                Life life1 = new Life();
                Life life2 = new Life();
                Life life3 = new Life();
                Life life4 = new Life();
                Life life5 = new Life();
                Life life6 = new Life();

                life1.setImageView_life(clothes_64_white_img);
                life1.setString_life(dressing.getString("brief"));
                life1.setString_model_life("穿衣");

                life2.setImageView_life(flu_64_white_img);
                life2.setString_life(flu.getString("brief"));
                life2.setString_model_life("感冒");

                life3.setImageView_life(sport_64_white_img);
                life3.setString_life(sport.getString("brief"));
                life3.setString_model_life("运动");

                life4.setImageView_life(uv_64_white_img);
                life4.setString_life(uv.getString("brief"));
                life4.setString_model_life("紫外线");

                life5.setImageView_life(car_washing_64_white_img);
                life5.setString_life(car_washing.getString("brief"));
                life5.setString_model_life("洗车");

                life6.setImageView_life(travel_64_white_img);
                life6.setString_life(travel.getString("brief"));
                life6.setString_model_life("旅游");

                lifeList.add(life1);
                lifeList.add(life2);
                lifeList.add(life3);
                lifeList.add(life4);
                lifeList.add(life5);
                lifeList.add(life6);

            //刷新布局
                refreshLifeLayout();

        }catch (Exception e) {
            toastErrorUnknow();
            e.printStackTrace();
        }
    }
    //解析城市信息JSON数据并存储在键中
    protected void parseCity(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(MainActivity.this).edit();
                String name = jsonObject1.getString("name");
                editor.putString("city_name", name);
                editor.apply();

        }catch (Exception e){
            toastErrorUnknow();
            e.printStackTrace();
        }
    }


    //广播接收器类，判断有无网络,弹出指示
    class NetBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context , Intent intent){
            netAvailable();
        }
    }
    //刷新的广播类
    class RefreshBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            progressDialog.setTitle("正在玩命加载中...");
            progressDialog.setMessage("稍安勿躁，淡定...");
            progressDialog.show();
            refreshWhether();
        }
    }


    //回到主线程弹出访问服务器失败的Toast
    public void toastErrorUrl(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"访问服务器失败，请稍后再试",Toast.LENGTH_SHORT).show();
            }
        });
    }
    //判断是否有网络弹出Toast
    protected void netAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isAvailable()) {
            refreshWhether();
        }else{
                Toast.makeText(MainActivity.this,"网络已断开",Toast.LENGTH_SHORT).show();
            }
        }
    //未知访问错误
    public void toastErrorUnknow(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"服务器出现错误，请稍后再试",Toast.LENGTH_SHORT).show();
            }
        });
    }




    //获取经纬度
    private String getLocation(Context context){
        double latitude = 0.0;
        double longitude = 0.0;

        LocationManager locationManager = (LocationManager) context .getSystemService(Context.LOCATION_SERVICE);
        if(ContextCompat.checkSelfPermission(MainActivity.this,
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


    //分享功能
    public void shareText() {
        String message = "【天气快报】\n"+"今天是"+whetherList.get(0).getString_day()+nowCity
                +"的天气是"+whetherList.get(0).getString_whether()+"，明天的天气是"
                +whetherList.get(1).getString_whether()+",后天的天气是"
                + whetherList.get(2).getString_whether()+"。祝您生活愉快！";
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        shareIntent.setType("text/plain");
        //设置分享列表的标题，并且每次都显示分享列表
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }


    //重写返回键方法
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    //关闭广播
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(refreshBroadcastReceiver);
        unregisterReceiver(netBroadcastReceiver);
    }




}//----------------------------activity-------------------------------------------------------------
