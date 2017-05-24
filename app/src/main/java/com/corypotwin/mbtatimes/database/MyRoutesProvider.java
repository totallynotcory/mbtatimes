package com.corypotwin.mbtatimes.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import android.content.ContentProvider;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by ctpotwin on 2/8/17.
 */
public class MyRoutesProvider extends ContentProvider {

    DBHelper dbHelper;

    public static final String CONTENT_AUTHORITY = "com.corypotwin.mbtatimes.userroutes";
    static final String URL = "content://" + CONTENT_AUTHORITY + "/routes";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    public static final String ID = "id";
    public static final String COLUMN_STOP = "stop";
    public static final String COLUMN_ROUTE = "route";
    public static final String COLUMN_MODE = "mode";
    public static final String COLUMN_DIRECTION = "direction";
    public static final String COLUMN_DIRECTION_NAME = "direction_name";

    // integer values used in content URI
    static final int ROUTES = 1;
    static final int ROUTES_ID = 2;

    // projection map for a query
    private static HashMap<String, String> RouteMap;

    // maps content URI "patterns" to the integer values that were set above
    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, "routes", ROUTES);
        uriMatcher.addURI(CONTENT_AUTHORITY, "routes/#", ROUTES_ID);
    }

    // database declarations
    private SQLiteDatabase database;
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

    public MyRoutesProvider(){

    }

    // class that creates and manages the provider's database
    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DBHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ". Old data will be destroyed");
            db.execSQL("DROP TABLE IF EXISTS " +  TABLE_NAME);
            onCreate(db);
        }

    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new DBHelper(context);
        // permissions to be writable
        database = dbHelper.getWritableDatabase();

        if(database == null)
            return false;
        else
            return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        // the TABLE_NAME to query on
        queryBuilder.setTables(TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            // maps all database column names
            case ROUTES:
                queryBuilder.setProjectionMap(RouteMap);
                break;
            case ROUTES_ID:
                queryBuilder.appendWhere( ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder == ""){
            // No sorting-> sort on names by default
            sortOrder = COLUMN_STOP;
        }
        Cursor cursor = queryBuilder.query(database, projection, selection,
                selectionArgs, null, null, sortOrder);
        /**
         * register to watch a content URI for changes
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long row = database.insert(TABLE_NAME, "", values);

        // If record is added successfully
        if(row > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, row);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        } else {
            try {
                throw new SQLException("Fail to add a new record into " + uri);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case ROUTES:
                count = database.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case ROUTES_ID:
                count = database.update(TABLE_NAME, values, ID +
                        " = " + uri.getLastPathSegment() +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case ROUTES:
                // delete all the records of the table
                count = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case ROUTES_ID:
                String id = uri.getLastPathSegment();	//gets the id
                count = database.delete( TABLE_NAME, ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;


    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            // Get all routes
            case ROUTES:
                return "vnd.android.cursor.dir/vnd.corypotwin.user_routes";
            // Get a particular route
            case ROUTES_ID:
                return "vnd.android.cursor.item/vnd.corypotwin.user_routes";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

}