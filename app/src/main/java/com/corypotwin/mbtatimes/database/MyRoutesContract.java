package com.corypotwin.mbtatimes.database;

import android.net.Uri;


/**
 * Created by ctpotwin on 3/28/17.
 */

public final class MyRoutesContract {

    private MyRoutesContract(){}

    public static final String CONTENT_AUTHORITY = "com.corypotwin.mbtatimes.userroutes";
    static final String URL = "content://" + CONTENT_AUTHORITY + "/routes";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    public static final String ID = "id";
    public static final String COLUMN_STOP = "stop";
    public static final String COLUMN_ROUTE = "route";
    public static final String COLUMN_MODE = "mode";
    public static final String COLUMN_DIRECTION = "direction";
    public static final String COLUMN_DIRECTION_NAME = "direction_name";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "mbtatimes_user_routes";
    public static final String TABLE_NAME = "user_routes";
    static final String CREATE_TABLE =
            " CREATE TABLE " + TABLE_NAME + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_STOP + " TEXT NOT NULL, " +
                    COLUMN_ROUTE + " TEXT NOT NULL, " +
                    COLUMN_MODE + " TEXT NOT NULL, " +
                    COLUMN_DIRECTION + " TEXT NOT NULL);";
}
