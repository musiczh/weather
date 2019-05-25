package com.example.a17280.whether.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a17280.whether.Interface.MyCityBack;
import com.example.a17280.whether.R;
import com.example.a17280.whether.adapter.MyCityAdapter;
import com.example.a17280.whether.db.DbHelper;
import com.example.a17280.whether.entity.City;
import com.example.a17280.whether.manager.NoBugLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MyCityActivity extends AppCompatActivity {
    private SQLiteDatabase db;
    private List<City> mListCity = new ArrayList<>();
    private MyCityAdapter adapter;
    public static Context context ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_city);


        //接受广播,刷新布局
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("REFRESH_MY_CITY");
        RefreshBroadcastReceiver refreshBroadcastReceiver = new RefreshBroadcastReceiver();
        registerReceiver(refreshBroadcastReceiver,intentFilter);
        context = MyCityActivity.this;

        //控件初始化
        TextView textView= (TextView) findViewById(R.id.text_view_my_city_add_city);
        ImageView imageView = (ImageView) findViewById(R.id.image_view_my_city_back);

        //设置监听
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter!=null ) {
                    if (adapter.getItemCount() < 10) {
                        Intent intent = new Intent(MyCityActivity.this, ChooseCityActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MyCityActivity.this, "最多只能添加9个城市", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Intent intent = new Intent(MyCityActivity.this, ChooseCityActivity.class);
                    startActivity(intent);
                }
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //获取或者建立数据库,并获得数据库里面的内容
        DbHelper dbHelper = new DbHelper(MyCityActivity.this,"CityList.db",null,1);
        db = dbHelper.getReadableDatabase();
        searchCity();

        //如果查询到数据就更新列表
        if (mListCity.size() != 0) {
            addAdapter();
        }


    }


    //查询数据库的内容
    public void searchCity() {
        mListCity.clear();
        Cursor cursor = db.query("City", null, null, null, null, null, null);
        //遍历媒体数据库
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                City city = new City();
                String cityName = cursor.getString(cursor.getColumnIndex("name"));
                String cityPath = cursor.getString(cursor.getColumnIndex("path"));
                city.setPath(cityPath);
                city.setName(cityName);
                mListCity.add(city);
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    //全局获取context
    public static Context getContextMyCityActivity(){
        return context;
    }

    //广播接收器,刷新布局
    class RefreshBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            searchCity();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }else {
                addAdapter();
            }
        }
    }

    //刷新数据，并刷新列表
    protected void addAdapter(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_my_city);
        NoBugLinearLayoutManager layoutManager = new NoBugLinearLayoutManager(MyCityActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MyCityAdapter(mListCity, new MyCityBack() {
            @Override
            public void onFinish(String name) {
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(MyCityActivity.this).edit();
                editor.putString("city_name",name);
                editor.apply();
                //发送广播
                Intent intent = new Intent("REFRESH");
                sendBroadcast(intent);
                finish();
            }

            @Override
            public void onLongFinish(int position ,View view) {
                adapter.del(view,position);
                String name = mListCity.get(position).getName();
                db.delete("City","name=?",new String[]{name});
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}
