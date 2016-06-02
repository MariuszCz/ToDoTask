package com.example.mariusz.todotask.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

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

    private ContentValues createAndReturnContentValuesForTask(Task task) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.TasksTable.COLUMN_NAME, task.getName());
        values.put(DbConstants.TasksTable.COLUMN_FACEBOOK_ID, task.getFacebookId());
        values.put(DbConstants.TasksTable.COLUMN_END_DATE, task.getEndDate() != null? task.getEndDate().getTime():0);
        values.put(DbConstants.TasksTable.COLUMN_CREATED_AT, task.getCreatedAt().getTime());
        values.put(DbConstants.TasksTable.COLUMN_DESCRIPTION, task.getDescription() != null ? task.getDescription() : "");
        values.put(DbConstants.TasksTable.COLUMN_ISOPEN, task.isOpen() ? 1 : 0);
        values.put(DbConstants.TasksTable.COLUMN_DELETED, task.isDeleted());
        return values;
    }

    public Task getTaskById(String id) {
        Cursor taskCursor = getDetailsCursor(id);
        Task task = createAndReturnTaskFromCursor(taskCursor);
        return task;
    }

    private Cursor getDetailsCursor(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] tables = DbConstants.TasksTable.allTables;
        String where = DbConstants.TasksTable._ID+" = ?";
        String whereFacebook = DbConstants.TasksTable.COLUMN_FACEBOOK_ID+" = "+ facebookId;
        String[] selectionArgs = { String.valueOf(id) };
        Cursor cursor = db.query(DbConstants.TasksTable.TABLE_NAME, tables, whereFacebook, selectionArgs, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    private Task createAndReturnTaskFromCursor(Cursor cursor) {
        Task task = new Task();
        task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.TasksTable._ID)));
        task.setFacebookId(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_FACEBOOK_ID)));
        task.setName(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_NAME)));
        task.setEndDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_END_DATE))));
        task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_DESCRIPTION)));
        task.setCreatedAt(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_CREATED_AT))));
        boolean isOpen = cursor.getInt(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_ISOPEN)) != 0 ? true : false;
        task.setOpen(isOpen);
        task.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_DELETED)) != 0);
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
        cursor = moveToPositionWorkingWithDeleted(position, cursor);
        return cursor;
    }

    private Cursor moveToPositionWorkingWithDeleted(int position, Cursor cursor) {
        cursor.moveToFirst();
        //We don't know if first element is deleted or not
        int currentPosition = -1;

        while (true) {
            if (cursor.getInt(cursor.getColumnIndexOrThrow(DbConstants.TasksTable.COLUMN_DELETED)) == 0) {
                currentPosition++;
            }
            if (currentPosition == position) {
                return cursor;
            }
            cursor.moveToNext();
        }
    }

    public void deleteTaskByItsId(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.TasksTable.COLUMN_DELETED, 1);
       // values.put(DbConstants.TasksTable.COLUMN_LAST_UPDATED, new Date().getTime());
        String select = DbConstants.TasksTable._ID + "=?";
        String[] selectionArgs = { Long.toString(id) };
        db.update(DbConstants.TasksTable.TABLE_NAME, values, select, selectionArgs);
    }

    public void deleteAllTasks() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbConstants.TasksTable.COLUMN_DELETED, 1);
        db.update(DbConstants.TasksTable.TABLE_NAME, values, null, null);
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

 /*   public Task findTaskByTimestamp(long timestamp) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DbConstants.TasksTable.COLUMN_TIMESTAMP + "=?";
        String[] selectionArgs = { Long.toString(timestamp)};
        Cursor query = db.query(DbConstants.TasksTable.TABLE_NAME, DbConstants.TasksTable.allTables,
                selection, selectionArgs, null, null, null);
        if (!query.moveToFirst()) {
            return null;
        }

        Task task = createAndReturnTaskFromCursor(query);
        return task;
    }*/

    @Override
    public int getTasksNumber() {
        SQLiteDatabase db =  dbHelper.getReadableDatabase();
        String selection = DbConstants.TasksTable.COLUMN_DELETED + "=?";
        String[] selectionArgs = { Integer.toString(0) };
        return (int) DatabaseUtils.queryNumEntries(db, DbConstants.TasksTable.TABLE_NAME, selection, selectionArgs);
    }

    public ArrayList<Task> getAllDeletedTasks() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<Task> tasks = new ArrayList<>();
        String selection = DbConstants.TasksTable.COLUMN_DELETED + "=?";
        String[] selectionArgs = { Long.toString(1)};
        Cursor query = db.query(DbConstants.TasksTable.TABLE_NAME, DbConstants.TasksTable.allTables,
                selection, selectionArgs, null, null, null);

        for (int i=0; i<query.getCount();i++) {
            query.moveToPosition(i);
            tasks.add(createAndReturnTaskFromCursor(query));
        }
        return tasks;
    }
}
