package com.corypotwin.mbtatimes.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.corypotwin.mbtatimes.database.MyRoutesProvider.CREATE_TABLE;
import static com.corypotwin.mbtatimes.database.MyRoutesProvider.DATABASE_NAME;
import static com.corypotwin.mbtatimes.database.MyRoutesProvider.DATABASE_VERSION;
import static com.corypotwin.mbtatimes.database.MyRoutesProvider.TABLE_NAME;

/**
 * Created by ctpotwin on 2/8/17.
 */

public final class MyRoutesDatabaseHelper extends SQLiteOpenHelper {


    public MyRoutesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MyRoutesDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ". Old data will be destroyed");
        db.execSQL("DROP TABLE IF EXISTS " +  TABLE_NAME);
        onCreate(db);
    }
}