package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> listItems;
    Button buttonAddCourse, buttonLogout;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.list_view_courses);
        buttonAddCourse = findViewById(R.id.btn_add_course);
        buttonLogout = findViewById(R.id.btn_logout);

        listItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);

        Intent intent = getIntent();
        userId = intent.getIntExtra("USER_ID", -1);

        loadCourses();

        buttonAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCourseDialog();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showUpdateDeleteCourseDialog(position);
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadCourses() {
        listItems.clear();
        Cursor cursor = dbHelper.getAllCourses(userId);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String courseName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_NAME));
                    listItems.add(courseName);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddCourseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Course");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);
        final EditText editCourseName = view.findViewById(R.id.edit_course_name);

        builder.setView(view);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String courseName = editCourseName.getText().toString();
                if (!courseName.isEmpty()) {
                    dbHelper.addCourse(courseName, userId);
                    loadCourses();
                } else {
                    Toast.makeText(MainActivity.this, "Course name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showUpdateDeleteCourseDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update or Delete Course");

        final String oldCourseName = listItems.get(position);
        builder.setMessage("Would you like to update or delete this course?");
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showUpdateCourseDialog(position, oldCourseName);
            }
        });
        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Cursor cursor = dbHelper.getAllCourses(userId);
                if (cursor != null) {
                    if (cursor.moveToPosition(position)) {
                        int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                        dbHelper.deleteCourse(courseId);
                        loadCourses();
                    }
                    cursor.close();
                }
            }
        });
        builder.setNeutralButton("Cancel", null);
        builder.show();
    }

    private void showUpdateCourseDialog(final int position, final String oldCourseName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Course");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);
        final EditText editCourseName = view.findViewById(R.id.edit_course_name);
        editCourseName.setText(oldCourseName);

        builder.setView(view);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newCourseName = editCourseName.getText().toString();
                if (!newCourseName.isEmpty()) {
                    Cursor cursor = dbHelper.getAllCourses(userId);
                    if (cursor != null) {
                        if (cursor.moveToPosition(position)) {
                            int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                            dbHelper.updateCourse(courseId, newCourseName);
                            loadCourses();
                        }
                        cursor.close();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Course name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
