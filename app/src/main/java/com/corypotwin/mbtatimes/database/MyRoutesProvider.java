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

import static com.corypotwin.mbtatimes.database.MyRoutesContract.COLUMN_STOP;
import static com.corypotwin.mbtatimes.database.MyRoutesContract.CONTENT_AUTHORITY;
import static com.corypotwin.mbtatimes.database.MyRoutesContract.TABLE_NAME;

/**
 * Created by ctpotwin on 2/8/17.
 */
public class MyRoutesProvider extends ContentProvider {

    MyRoutesDatabaseHelper dbHelper;

    // integer values used in content URI
    static final int ROUTES = 1;
    static final int ROUTES_ID = 2;

    // projection map for a query
    private static HashMap<String, String> RouteMap;

    // maps content URI "patterns" to the integer values that were set above
    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MyRoutesContract.CONTENT_AUTHORITY, "routes", ROUTES);
        uriMatcher.addURI(MyRoutesContract.CONTENT_AUTHORITY, "routes/#", ROUTES_ID);
    }

    // database declaration
    private SQLiteDatabase database;

    public MyRoutesProvider(){

    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new MyRoutesDatabaseHelper(context);
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
        queryBuilder.setTables(MyRoutesContract.TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            // maps all database column names
            case ROUTES:
                queryBuilder.setProjectionMap(RouteMap);
                break;
            case ROUTES_ID:
                queryBuilder.appendWhere( MyRoutesContract.ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder == ""){
            // No sorting-> sort on names by default
            sortOrder = MyRoutesContract.COLUMN_STOP;
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
        long row = database.insert(MyRoutesContract.TABLE_NAME, "", values);

        // If record is added successfully
        if(row > 0) {
            Uri newUri = ContentUris.withAppendedId(MyRoutesContract.CONTENT_URI, row);
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
                count = database.update(MyRoutesContract.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ROUTES_ID:
                count = database.update(MyRoutesContract.TABLE_NAME, values, MyRoutesContract.ID +
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
                count = database.delete(MyRoutesContract.TABLE_NAME, selection, selectionArgs);
                break;
            case ROUTES_ID:
                String id = uri.getLastPathSegment();	//gets the id
                count = database.delete( MyRoutesContract.TABLE_NAME, MyRoutesContract.ID +  " = " + id +
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