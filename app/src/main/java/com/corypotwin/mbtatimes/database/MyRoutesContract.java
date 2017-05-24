package com.corypotwin.mbtatimes.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.sql.SQLException;
import java.util.HashMap;

import static com.google.android.gms.security.ProviderInstaller.PROVIDER_NAME;
import static java.security.AccessController.getContext;

/**
 * Created by ctpotwin on 3/28/17.
 */

public class MyRoutesContract {

//    DBHelper dbHelper;
//
//    public static final String CONTENT_AUTHORITY = "com.corypotwin.mbtatimes.userroutes";
//    static final String URL = "content://" + CONTENT_AUTHORITY + "/routes";
//    public static final Uri CONTENT_URI = Uri.parse(URL);
//
//
//
//    public static final String ID = "_id";
//    public static final String COLUMN_STOP = "stop";
//    public static final String COLUMN_ROUTE = "route";
//    public static final String COLUMN_MODE = "mode";
//    public static final String COLUMN_DIRECTION = "direction";
//
//    // integer values used in content URI
//    static final int ROUTES = 1;
//    static final int ROUTES_ID = 2;
//
//    // projection map for a query
//    private static HashMap<String, String> RouteMap;
//
//    // maps content URI "patterns" to the integer values that were set above
//    static final UriMatcher uriMatcher;
//    static{
//        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
//        uriMatcher.addURI(CONTENT_AUTHORITY, "routes", ROUTES);
//        uriMatcher.addURI(CONTENT_AUTHORITY, "routes/#", ROUTES_ID);
//    }
//
//    // database declarations
//    private SQLiteDatabase database;
//    public static final int DATABASE_VERSION = 1;
//    public static final String DATABASE_NAME = "mbtatimes_user_routes"
//    public static final String TABLE_NAME = "user_routes";
//    static final String CREATE_TABLE =
//            " CREATE TABLE " + TABLE_NAME +
//                    " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    " name TEXT NOT NULL, " +
//                    " birthday TEXT NOT NULL);";
//
//    @Override
//    public boolean onCreate() {
//        Context context = getContext();
//        MyRoutesDatabaseHelper dbHelper = new MyRoutesDatabaseHelper(context);
//        database = new MyRoutesDatabaseHelper(getContext());
//        db = database.getWritableDatabase();
//        return (db == null)? false:true;
//    }
//
//    @Nullable
//    @Override
//    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
//
//        SQLiteDatabase sqlDB = database.getWritableDatabase();
//
//        long rowID = sqlDB.insert(	MyRoutesContract.TABLE_NAME, "", values);
//
//        if (rowID > 0) {
//            Uri _uri = ContentUris.withAppendedId(MyRoutesContract.TABLE_FOR_INSERT, rowID);
//            getContext().getContentResolver().notifyChange(_uri, null);
//            return _uri;
//        }
//
//        try {
//            throw new SQLException("Failed to add a record into " + uri);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @Nullable
//    @Override
//    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
//                        @Nullable String selection, @Nullable String[] selectionArgs,
//                        @Nullable String sortOrder) {
//        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
//        qb.setTables(MyRoutesContract.TABLE_NAME);
//
//        switch (uriMatcher.match(uri)) {
//            case ROUTE_ID:
//                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
//                break;
//
//            case ROUTE_NAME:
//                qb.appendWhere( MyRoutesContract.ID + "=" + uri.getPathSegments().get(1));
//                break;
//
//            default:
//        }
//
//        Cursor c = qb.query(db,	projection,	selection,
//                selectionArgs,null, null, sortOrder);
//        /**
//         * register to watch a content URI for changes
//         */
//        c.setNotificationUri(getContext().getContentResolver(), uri);
//        return c;
//    }
//
//    @Nullable
//    @Override
//    public String getType(@NonNull Uri uri) {
//        switch (uriMatcher.match(uri)){
//            /**
//             * Get all student records
//             */
//            case ROUTE_ID:
//                return "vnd.android.cursor.item/vnd.corypotwin.routes";
//            /**
//             * Get a particular student
//             */
//            case ROUTE_NAME:
//                return "vnd.android.cursor.dir/vnd.corypotwin.routes";
//            default:
//                throw new IllegalArgumentException("Unsupported URI: " + uri);
//        }
//    }
//
//    @Override
//    public int delete(@NonNull Uri uri, @Nullable String selection,
//                      @Nullable String[] selectionArgs) {
//        int count = 0;
//        switch (uriMatcher.match(uri)){
//            case ROUTE_ID:
//                String id = uri.getPathSegments().get(1);
//                count = db.delete(MyRoutesContract.TABLE_NAME, MyRoutesContract.ID +  " = " + id +
//                                (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
//                        selectionArgs);
//                break;
//
//            case ROUTE_NAME:
//                count = db.delete(MyRoutesContract.TABLE_NAME, selection, selectionArgs);
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown URI " + uri);
//        }
//
//        getContext().getContentResolver().notifyChange(uri, null);
//        return count;
//    }
//
//    @Override
//    public int update(@NonNull Uri uri, @Nullable ContentValues values,
//                      @Nullable String selection, @Nullable String[] selectionArgs) {
//        int count = 0;
//        switch (uriMatcher.match(uri)) {
//            case ROUTE_ID:
//                count = db.update(MyRoutesContract.TABLE_NAME, values,
//                        MyRoutesContract.ID + " = " + uri.getPathSegments().get(1) +
//                                (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
//                break;
//
//            case ROUTE_NAME:
//                count = db.update(MyRoutesContract.TABLE_NAME, values, selection, selectionArgs);
//
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown URI " + uri );
//        }
//
//        getContext().getContentResolver().notifyChange(uri, null);
//        return count;
//    }
}
