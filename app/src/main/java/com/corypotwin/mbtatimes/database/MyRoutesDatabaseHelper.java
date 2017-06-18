package com.corypotwin.mbtatimes.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ctpotwin on 2/8/17.
 */

public class MyRoutesDatabaseHelper extends SQLiteOpenHelper {

        public MyRoutesDatabaseHelper(Context context) {
            super(context, MyRoutesContract.DATABASE_NAME, null, MyRoutesContract.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(MyRoutesContract.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(MyRoutesDatabaseHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ". Old data will be destroyed");
            db.execSQL("DROP TABLE IF EXISTS " +  MyRoutesContract.TABLE_NAME);
            onCreate(db);
        }

    }
