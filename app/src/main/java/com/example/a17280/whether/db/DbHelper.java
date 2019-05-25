package com.example.a17280.whether.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 17280 on 2019/5/14.
 *
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String CREATE_BOOK = "create table City(" +
            "id integer primary key autoincrement," +
            "name text,"+
            "path text)";

    String CREATE_BOOK_F = "create table History(" +
            "id integer primary key autoincrement," +
            "input text)";




    private Context mContext;

    public DbHelper(Context context, String name,
                    SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_BOOK_F);
        db.execSQL(CREATE_BOOK);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
