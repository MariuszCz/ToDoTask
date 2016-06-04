package com.example.mariusz.todotask.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mariusz.todotask.Models.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by mariusz on 24/05/16.
 */
public class TaskDatabase implements TasksProvider {
    private TasksDbHelper dbHelper;
    private String facebookId;

    public TaskDatabase(Context context, String facebookId) {
        this.dbHelper = new TasksDbHelper(context);
        this.facebookId = facebookId;
    }

    public TaskDatabase(Context context) {
        this.dbHelper = new TasksDbHelper(context);
    }

    public long addTaskAndReturnItsId(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = createAndReturnContentValuesForTask(task);
        long id = db.insert(DbConstants.TasksTable.TABLE_NAME, null, values);
        task.setId(id);
        return id;
    }

    public void addTaskFromServer(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = createAndReturnContentValuesForTaskFromServer(task);
        db.insert(DbConstants.TasksTable.TABLE_NAME, null, values);
    }

    private ContentValues createAndReturnContentValuesForTask(Task task) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.TasksTable.COLUMN_NAME, task.getName());
        values.put(DbConstants.TasksTable.COLUMN_FACEBOOK_ID, task.getFacebookId());
        values.put(DbConstants.TasksTable.COLUMN_END_DATE, task.getEndDate() != null? task.getEndDate().getTime():0);
        values.put(DbConstants.TasksTable.COLUMN_CREATED_AT, task.getCreatedAt().getTime());
        values.put(DbConstants.TasksTable.COLUMN_DESCRIPTION, task.getDescription() != null ? task.getDescription() : "");
        values.put(DbConstants.TasksTable.COLUMN_ISOPEN, task.isOpen() ? 1 : 0);
        return values;
    }

    private ContentValues createAndReturnContentValuesForTaskFromServer(Task task) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.TasksTable.COLUMN_TASK_ID, task.getTaskId());
        values.put(DbConstants.TasksTable.COLUMN_NAME, task.getName());
        values.put(DbConstants.TasksTable.COLUMN_FACEBOOK_ID, task.getFacebookId());
        values.put(DbConstants.TasksTable.COLUMN_END_DATE, task.getEndDate() != null? task.getEndDate().getTime():0);
        values.put(DbConstants.TasksTable.COLUMN_CREATED_AT, task.getCreatedAt().getTime());
        values.put(DbConstants.TasksTable.COLUMN_DESCRIPTION, task.getDescription() != null ? task.getDescription() : "");
        values.put(DbConstants.TasksTable.COLUMN_ISOPEN, task.isOpen() ? 1 : 0);
        return values;
    }


    private Task createAndReturnTaskFromCursor(Cursor cursor) {
        Task task = new Task();
        task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.TasksTable._ID)));
        task.setTaskId(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_TASK_ID)));
        task.setFacebookId(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_FACEBOOK_ID)));
        task.setName(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_NAME)));
        task.setEndDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_END_DATE))));
        task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_DESCRIPTION)));
        task.setCreatedAt(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_CREATED_AT))));
        boolean isOpen = cursor.getInt(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_ISOPEN)) != 0 ? true : false;
        task.setOpen(isOpen);
        return task;
    }

    public Task getTaskByPosition(int position) {
        Task task;
        Cursor cursor = getDetailsCursorByPosition(position);
        task = createAndReturnTaskFromCursor(cursor);
        return task;
    }

    private Cursor getDetailsCursorByPosition(int position) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] tables = DbConstants.TasksTable.allTables;
        Cursor cursor = db.query(DbConstants.TasksTable.TABLE_NAME, tables, null, null, null, null, null);
     //   cursor = moveToPositionWorkingWithFacebook(position, cursor);
        cursor.moveToPosition(position);
        return cursor;
    }

/*
    private Cursor moveToPositionWorkingWithFacebook(int position, Cursor cursor) {
        cursor.moveToFirst();
        int it = -1;
        while (true) {
            if (cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_FACEBOOK_ID)).equals(facebookId)) {
                it++;
            }   if(it == position) {
                return cursor;
            }
            cursor.moveToNext();
        }
    }*/

    public void deleteTaskByItsId(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String select = DbConstants.TasksTable._ID + "=?";
        String[] selectionArgs = { Long.toString(id) };
        db.delete(DbConstants.TasksTable.TABLE_NAME, select, selectionArgs);
    }

    public void deleteAllTasks() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DbConstants.TasksTable.TABLE_NAME, null, null);
    }

    public long getTaskIdByPosition(int position) {
        Cursor cursor = getDetailsCursorByPosition(position);
        return cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.TasksTable._ID));
    }

    public void updateTask(Task task, long id) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = createAndReturnContentValuesForTask(task);
            String selection = DbConstants.TasksTable._ID + "=?";
            String[] selectionArgs = {Long.toString(id)};
            db.update(DbConstants.TasksTable.TABLE_NAME, values, selection, selectionArgs);
    }


    @Override
    public int getTasksNumber() {
        SQLiteDatabase db =  dbHelper.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, DbConstants.TasksTable.TABLE_NAME, null, null);
    }

}
