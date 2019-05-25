package com.example.a17280.whether.service;
/**
 * 桌面小控件定时刷新的后台服务逻辑
 * 每次刷新访问数据并传给桌面控件
 * 2019/05/15
 *
 */

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.example.a17280.whether.Interface.HttpCallBackListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import static com.example.a17280.whether.util.InternetUtil.sendHttpURL;
import static com.example.a17280.whether.util.UiUtil.chooseWhetherIcon;

public class WidgetService extends Service {
   // private static RemoteViews remoteViews;
    protected SharedPreferences preferences;  //获取已储存的城市
    //四个天气实况的数据
    private String mWhether;
    private String mTemperature;
    private String mLocation;
    private int mImageResource;

    public WidgetService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        preferences = PreferenceManager.getDefaultSharedPreferences(WidgetService. this);
                        if (preferences.getString("city_name",null) != null) {
                            refreshWhether();
                        }
                    }
                }).start();
            }
        },0,60*60*1000);
    }

    //在这里设置定时刷新
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //刷新要执行的逻辑
       /*
        //获取alarm对象
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //刷新的间隔时间
        int time = 3*60*60*1000;
        //要传入的时间参数，开机到现在的时间+间隔时间
        long triggerAtTime = SystemClock.elapsedRealtime()+time;
        //要执行的intent
        Intent intent1 = new Intent(this,WidgetService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this,0,intent1,0);
        //设置计时器
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
        */
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //获取天气数据，并把数据存在intent中发送广播给桌面小控件
    private void refreshWhether(){
        String nowWhetherUrl =
                "https://api.seniverse.com/v3/weather/now.json?key=SMhxMxzEERc3eGJ4s&location=";

        String address = nowWhetherUrl+preferences.getString("city_name","");
        sendHttpURL(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                parseNowJSON(response);
                upState();

            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
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
            mWhether = now.getString("text");
            mTemperature = now.getString("temperature");
            mLocation = location.getString("name");
            String code = now.getString("code");
            mImageResource = chooseWhetherIcon(code);
            //刷新布局
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //把获取到的数据发送广播，在refreshWhether方法中被调用
    protected void upState(){
        preferences = PreferenceManager.getDefaultSharedPreferences(WidgetService.this);
        //判断是否已经选择了城市，否则打开另一个活动选择城市
        if (preferences.getString("city_name", null) != null) {
            refreshWhether();
            Intent intent1 = new Intent();
            intent1.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            intent1.putExtra("whether",mWhether);
            intent1.putExtra("temperature",mTemperature);
            intent1.putExtra("location",mLocation);
            intent1.putExtra("image_resource",mImageResource);
            getApplicationContext().sendBroadcast(intent1);
        }

    }

    /*private void up(){

        AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
        ComponentName cn =new ComponentName(getApplicationContext(),WhetherWidget.class);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.whether_widget);
        //remoteViews.setTextViewText(R.id.text_view_location_widget, widgetText);
        remoteViews.setImageViewResource(R.id.image_view_widget, mImageResource);
        remoteViews.setTextViewText(R.id.text_view_widget_whether, mWhether);
        remoteViews.setTextViewText(R.id.text_view_widget_temperature, mTemperature);
        remoteViews.setTextViewText(R.id.text_view_location_widget, mLocation);

        manager.updateAppWidget(cn,remoteViews);
    }*/


}
