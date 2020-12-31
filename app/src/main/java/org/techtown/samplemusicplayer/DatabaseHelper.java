package org.techtown.samplemusicplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static String databaseName = "FAVORITE_LIST";

    public static int VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, databaseName, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists FAVORITE_LIST_TABLE("
                + "_id integer PRIMARY KEY autoincrement, "
                + " title text)";
        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion > 1){
            db.execSQL("DROP TABLE IF EXISTS FAVORITE_LIST_TABLE");
        }
    }
}
