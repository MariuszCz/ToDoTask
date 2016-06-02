package com.example.mariusz.todotask.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mariusz on 24/05/16.
 */
public class TasksDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 15;

    private static final String TABLE_CREATE =
            "CREATE TABLE " + DbConstants.TasksTable.TABLE_NAME + " (" +
                    DbConstants.TasksTable._ID + " INTEGER PRIMARY KEY,"+
                    DbConstants.TasksTable.COLUMN_NAME + " TEXT, " +
                    DbConstants.TasksTable.COLUMN_FACEBOOK_ID + " TEXT, " +
                    DbConstants.TasksTable.COLUMN_END_DATE + " INTEGER, " +
                    DbConstants.TasksTable.COLUMN_CREATED_AT + " INTEGER, " +
                    DbConstants.TasksTable.COLUMN_DESCRIPTION + " TEXT," +
                   // DbConstants.TasksTable.COLUMN_TIMESTAMP + " INTEGER," +
                  //  DbConstants.TasksTable.COLUMN_LAST_UPDATED + " INTEGER,"+
                    DbConstants.TasksTable.COLUMN_ISOPEN + " INTEGER, " +
                    DbConstants.TasksTable.COLUMN_DELETED+ " INTEGER);";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DbConstants.TasksTable.TABLE_NAME;

    public TasksDbHelper(Context context) {
        super(context, DbConstants.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

}
