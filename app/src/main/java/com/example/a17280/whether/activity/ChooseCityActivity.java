package com.example.a17280.whether.activity;
/**
 * 选择城市的界面
 */

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.a17280.whether.Interface.HttpCallBackListener;
import com.example.a17280.whether.R;
import com.example.a17280.whether.adapter.CityAdapter;
import com.example.a17280.whether.db.DbHelper;
import com.example.a17280.whether.entity.City;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import static com.example.a17280.whether.activity.MyCityActivity.getContextMyCityActivity;
import static com.example.a17280.whether.util.InternetUtil.sendHttpURL;

public class ChooseCityActivity extends AppCompatActivity {
    //网址
    private String CITY_URL = "https://api.seniverse.com/v3/location/search.json?"+
            "key=SvgmH3LiKYKouNOB-&limit=100&q=";
    //控件变量
    private EditText editText;
    private List<City> cityList = new ArrayList<>();
    private CityAdapter adapter;
    private ListView listView;
    private TextView locationTextView;
    private ProgressDialog progressDialog;

    //定位到的城市
    private String locationCityName;
    private String locationCityPath;

    private OverBroadcastReceiver overBroadcastReceiver; //广播接收器
    private NetBroadcastReceiver netBroadcastReceiver;  //广播接收器

    private SQLiteDatabase db;                                  //数据库
    private ContentValues values = new ContentValues();         //在表中增加数据的载体



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_city);


        //获取或者建立数据库,并获得数据库里面的内容
        DbHelper dbHelper = new DbHelper(ChooseCityActivity.this,"CityList.db",null,1);
        db = dbHelper.getReadableDatabase();


        //接受广播,结束活动
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("OVER");
        overBroadcastReceiver = new OverBroadcastReceiver();
        registerReceiver(overBroadcastReceiver,intentFilter);
        //接受广播提示没有网络
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        netBroadcastReceiver = new NetBroadcastReceiver();
        registerReceiver(netBroadcastReceiver,intentFilter1);


        //控件的初始化
        listView = (ListView) findViewById(R.id.list_view_fragment_city);
        editText = (EditText) findViewById(R.id.edit_text_sort);
        Button button = (Button) findViewById(R.id.button_fragment_sort);
        locationTextView = (TextView) findViewById(R.id.text_view_choose_activity_location);
        adapter = new CityAdapter(ChooseCityActivity.this,R.layout.city_item,cityList);
        listView.setAdapter(adapter);


        //定位，并把定位到的城市显示出来
        String location = getLocation(ChooseCityActivity.this);
        if(location.equals("no")) {
            Toast.makeText(ChooseCityActivity.this,"定位失败",Toast.LENGTH_SHORT).show();
        }else{
            String address = "https://api.seniverse.com/v3/location/search.json?key=SvgmH3LiKYKouNOB-&limit=100&q=" + location;
            sendHttpURL(address, new HttpCallBackListener() {
                @Override
                public void onFinish(String response) {
                    parseCity(response);
                    refreshLocation();
                }
                @Override
                public void onError(Exception e) {
                    toastErrorUrl();
                }
            });
        }


        //给搜索按钮设置监听，按下进行搜索
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String s = editText.getText().toString();
                String address = CITY_URL  + s;
                sendHttpCity(address);
            }
        });
        //给textView定位的城市设置监听
        locationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check(locationCityName)){
                    Toast.makeText(getContextMyCityActivity(),"该城市已存在",Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    values.put("name", locationCityName);
                    values.put("path", locationCityPath);
                    db.insert("City", null, values);
                    values.clear();
                    Intent intent = new Intent("REFRESH_MY_CITY");
                    sendBroadcast(intent);
                    finish();
                }
            }
        });


        //给listView子项设置监听，按下存储选中的城市
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(check(cityList.get(position).getName())){
                    Toast.makeText(getContextMyCityActivity(),"该城市已存在",Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    String name = cityList.get(position).getName();
                    String path = cityList.get(position).getPath();
                    values.put("name", name);
                    values.put("path", path);
                    db.insert("City", null, values);
                    values.clear();
                    Intent intent = new Intent("REFRESH_MY_CITY");
                    sendBroadcast(intent);
                    finish();
                }
            }
        });

        //给输入框设置监听
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")){
                    listView.setVisibility(View.INVISIBLE);
                }else{
                    listView.setVisibility(View.VISIBLE);
                }
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String s = editText.getText().toString();
                String address = CITY_URL  + s;
                sendHttpCity(address);
                return false;
            }
        });







    }//-----------------onCreate----------------------------------------------


    //解析返回的城市数据，装到集合中
    protected void parseJSON(String data){
        try {
            cityList.clear();
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                City city = new City();
                city.setId(jsonObject1.getString("id"));
                city.setName(jsonObject1.getString("name"));
                city.setPath(jsonObject1.getString("path"));
                cityList.add(city);

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //解析定位城市信息JSON数据
    protected void  parseCity(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
            locationCityName = jsonObject1.getString("name");
            locationCityPath = jsonObject1.getString("path");
        }catch (Exception e){
            toastErrorUrl();
            e.printStackTrace();
        }
    }

    //回到主线程刷新布局
    public void refreshList(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(cityList.size() != 0) {
                    adapter.notifyDataSetChanged();
                    listView.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(ChooseCityActivity.this,"找不到相关城市，请重新输入",Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });
    }
    public void refreshLocation(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                locationTextView.setText(locationCityName);
            }
        });
    }
    public void toastErrorUrl(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ChooseCityActivity.this,"服务器忙，请稍后再试",Toast.LENGTH_SHORT).show();
                if(progressDialog!=null){
                    progressDialog.dismiss();
                }
            }
        });
    }

    //访问服务器获取城市数据
    protected void sendHttpCity(String address){
        progressDialog = new ProgressDialog(ChooseCityActivity.this);
        progressDialog.setTitle("正在玩命加载中");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(true);
        progressDialog.show();
        sendHttpURL(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parseJSON(response);
                refreshList();
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                toastErrorUrl();
            }
        });
    }


    //广播接收器,改变下栏的显示的歌曲名字以及播放图标,监听网络变化
    class OverBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            finish();
        }
    }
    class NetBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context , Intent intent){
            netAvailable();
            }
        }


    //判断是否有网络
    protected void netAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (!(networkInfo != null && networkInfo.isAvailable())) {
            Toast.makeText(ChooseCityActivity.this, "网络已断开", Toast.LENGTH_SHORT).show();
        }
    }

    //获取经纬度
    private String getLocation(Context context){
        double latitude = 0.0;
        double longitude = 0.0;

        LocationManager locationManager = (LocationManager) context .getSystemService(Context.LOCATION_SERVICE);
        if(ContextCompat.checkSelfPermission(ChooseCityActivity.this,
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

    //判断数据库中是否有重复的数据
    private boolean check(String name){
        Cursor cursor = db.query("City",null,null,null,null,null,null);
        int check = 0;
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                String mName = cursor.getString(cursor.getColumnIndex("name"));
                if(mName.equals(name)) {
                    check = 1;
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        return check == 1;
    }




    //关闭广播
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(overBroadcastReceiver);
        unregisterReceiver(netBroadcastReceiver);
    }

}
