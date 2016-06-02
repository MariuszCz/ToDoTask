package com.example.mariusz.todotask.database;

import android.provider.BaseColumns;
/**
 * Created by mariusz on 24/05/16.
 */
public class DbConstants {


    public static final String DATABASE_NAME = "tasks_db";

    public static abstract class TasksTable implements BaseColumns {
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_FACEBOOK_ID = "facebook_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_END_DATE = "end_date";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_ISOPEN = "is_open";
        public static final String COLUMN_DELETED = "deleted";


        public static final String[] allTables = {DbConstants.TasksTable._ID, TasksTable.COLUMN_FACEBOOK_ID,
                DbConstants.TasksTable.COLUMN_NAME,
                DbConstants.TasksTable.COLUMN_END_DATE, DbConstants.TasksTable.COLUMN_DESCRIPTION,
                DbConstants.TasksTable.COLUMN_CREATED_AT, DbConstants.TasksTable.COLUMN_ISOPEN,
                DbConstants.TasksTable.COLUMN_DELETED};
    }
}
