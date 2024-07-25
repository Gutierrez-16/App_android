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

public class UserActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> listItems;
    Button buttonAddCourse, buttonLogout;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

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
                showUpdateDeleteDialog(position);
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logoutIntent = new Intent(UserActivity.this, LoginActivity.class);
                startActivity(logoutIntent);
                finish();
            }
        });
    }

    private void loadCourses() {
        listItems.clear();
        Cursor cursor = dbHelper.getAllCourses(userId);
        while (cursor.moveToNext()) {
            String courseName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_NAME));
            listItems.add(courseName);
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void showAddCourseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Course");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);
        EditText editCourseName = view.findViewById(R.id.edit_course_name);
        builder.setView(view);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String courseName = editCourseName.getText().toString();
                if (!courseName.isEmpty()) {
                    dbHelper.addCourse(courseName, userId);
                    loadCourses();
                } else {
                    Toast.makeText(UserActivity.this, "Course name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void showUpdateDeleteDialog(int position) {
        final String courseName = listItems.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update or Delete Course");

        String[] options = {"Update", "Delete"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) { // Update
                    showUpdateCourseDialog(courseName);
                } else if (which == 1) { // Delete
                    showDeleteCourseConfirmation(courseName);
                }
            }
        });

        builder.show();
    }

    private void showUpdateCourseDialog(final String oldCourseName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Course");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);
        EditText editCourseName = view.findViewById(R.id.edit_course_name);
        editCourseName.setText(oldCourseName);
        builder.setView(view);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newCourseName = editCourseName.getText().toString();
                if (!newCourseName.isEmpty()) {
                    Cursor cursor = dbHelper.getAllCourses(userId);
                    while (cursor.moveToNext()) {
                        int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                        String currentCourseName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_NAME));
                        if (currentCourseName.equals(oldCourseName)) {
                            dbHelper.updateCourse(courseId, newCourseName);
                            break;
                        }
                    }
                    cursor.close();
                    loadCourses();
                } else {
                    Toast.makeText(UserActivity.this, "Course name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void showDeleteCourseConfirmation(final String courseName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Course");
        builder.setMessage("Are you sure you want to delete the course \"" + courseName + "\"?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Cursor cursor = dbHelper.getAllCourses(userId);
                while (cursor.moveToNext()) {
                    int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String currentCourseName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_NAME));
                    if (currentCourseName.equals(courseName)) {
                        dbHelper.deleteCourse(courseId);
                        break;
                    }
                }
                cursor.close();
                loadCourses();
            }
        });

        builder.setNegativeButton("No", null);

        builder.show();
    }
}
