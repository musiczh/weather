package com.example.a17280.whether.view;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.example.a17280.whether.R;
import com.example.a17280.whether.service.WidgetService;

/**
 * 桌面小控件
 */
public class WhetherWidget extends AppWidgetProvider {
    private static String mWhether;
    private static String mTemperature;
    private static String mLocation;
    private static int mImageResource;
    private String AA = "android.appwidget.action.APPWIDGET_UPDATE";

    //接受广播存储广播中携带的数据
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action.equals(AA)) {
            mWhether = intent.getStringExtra("whether");
            mTemperature = intent.getStringExtra("temperature");
            mLocation = intent.getStringExtra("location");
            mImageResource = intent.getIntExtra("image_resource", R.drawable.img_0);
        }
        super.onReceive(context, intent);


    }

    //内部类更新UI
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        //CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
       RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.whether_widget);
        //remoteViews.setTextViewText(R.id.text_view_location_widget, widgetText);
        remoteViews.setImageViewResource(R.id.image_view_widget, mImageResource);
        remoteViews.setTextViewText(R.id.text_view_widget_whether, mWhether);
        remoteViews.setTextViewText(R.id.text_view_widget_temperature, mTemperature+"℃");
        remoteViews.setTextViewText(R.id.text_view_location_widget, mLocation);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }


    //更新的时候使用的方法，更新数据
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        Intent intent1 = new Intent (context, WidgetService.class);
        context.startService(intent1);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
            updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Intent intent1 = new Intent (context, WidgetService.class);
        context.startService(intent1);
    }

    //最后桌面小控件被删除的时候关闭服务
    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Intent intent = new Intent(context, WidgetService.class);
        context.stopService(intent);
    }
}

