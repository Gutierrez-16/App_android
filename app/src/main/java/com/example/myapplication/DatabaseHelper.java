package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "application.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_COURSES = "courses";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_COURSE_NAME = "course_name";
    public static final String COLUMN_USER_ID = "user_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(CREATE_TABLE_USERS);

        String CREATE_TABLE_COURSES = "CREATE TABLE " + TABLE_COURSES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_COURSE_NAME + " TEXT NOT NULL, "
                + COLUMN_USER_ID + " INTEGER, "
                + "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_TABLE_COURSES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public long addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        return db.insert(TABLE_USERS, null, values);
    }

    public Cursor getUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null,
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);
    }

    public long addCourse(String courseName, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COURSE_NAME, courseName);
        values.put(COLUMN_USER_ID, userId);
        return db.insert(TABLE_COURSES, null, values);
    }

    public Cursor getAllCourses(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_COURSES, null,
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null);
    }

    public int updateCourse(int id, String courseName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COURSE_NAME, courseName);
        return db.update(TABLE_COURSES, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deleteCourse(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COURSES, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
