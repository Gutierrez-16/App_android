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
    ListView listViewCourses, listViewUsers;
    ArrayAdapter<String> coursesAdapter, usersAdapter;
    ArrayList<String> coursesList, usersList;
    Button buttonAddCourse, buttonLogout, buttonViewUsers;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        listViewCourses = findViewById(R.id.list_view_courses);
        listViewUsers = findViewById(R.id.list_view_users);
        buttonAddCourse = findViewById(R.id.btn_add_course);
        buttonLogout = findViewById(R.id.btn_logout);
        buttonViewUsers = findViewById(R.id.btn_view_users);

        coursesList = new ArrayList<>();
        usersList = new ArrayList<>();
        coursesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, coursesList);
        usersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usersList);
        listViewCourses.setAdapter(coursesAdapter);
        listViewUsers.setAdapter(usersAdapter);

        Intent intent = getIntent();
        userId = intent.getIntExtra("USER_ID", -1);

        loadCourses();

        buttonAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCourseDialog();
            }
        });

        buttonViewUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUsers();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(logoutIntent);
                finish();
            }
        });

        listViewCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showUpdateDeleteCourseDialog(position);
            }
        });

        listViewUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showUpdateDeleteUserDialog(position);
            }
        });
    }

    private void loadCourses() {
        coursesList.clear();
        Cursor cursor = dbHelper.getAllCourses(userId);
        while (cursor.moveToNext()) {
            String courseName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE_NAME));
            String creatorName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)); // Asumiendo que se agrega el nombre de usuario en el cursor
            coursesList.add(courseName + " (Created by: " + creatorName + ")");
        }
        cursor.close();
        coursesAdapter.notifyDataSetChanged();
    }

    private void loadUsers() {
        usersList.clear();
        Cursor cursor = dbHelper.getAllUsers();
        while (cursor.moveToNext()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
            usersList.add(username);
        }
        cursor.close();
        usersAdapter.notifyDataSetChanged();
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
                    Toast.makeText(MainActivity.this, "Course name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void showUpdateDeleteCourseDialog(int position) {
        final String courseName = coursesList.get(position).split(" \\(Created by: ")[0]; // Para obtener solo el nombre del curso

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
                    Toast.makeText(MainActivity.this, "Course name cannot be empty", Toast.LENGTH_SHORT).show();
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

    private void showUpdateDeleteUserDialog(int position) {
        final String username = usersList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update or Delete User");

        String[] options = {"Update", "Delete"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) { // Update
                    showUpdateUserDialog(username);
                } else if (which == 1) { // Delete
                    showDeleteUserConfirmation(username);
                }
            }
        });

        builder.show();
    }

    private void showUpdateUserDialog(final String oldUsername) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update User");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_update_user, null);
        EditText editUsername = view.findViewById(R.id.edit_username);
        EditText editPassword = view.findViewById(R.id.edit_password);
        editUsername.setText(oldUsername);
        builder.setView(view);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newUsername = editUsername.getText().toString();
                String newPassword = editPassword.getText().toString();
                if (!newUsername.isEmpty() && !newPassword.isEmpty()) {
                    Cursor cursor = dbHelper.getAllUsers();
                    while (cursor.moveToNext()) {
                        int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                        String currentUsername = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
                        if (currentUsername.equals(oldUsername)) {
                            dbHelper.updateUser(userId, newUsername, newPassword);
                            break;
                        }
                    }
                    cursor.close();
                    loadUsers();
                } else {
                    Toast.makeText(MainActivity.this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void showDeleteUserConfirmation(final String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete User");
        builder.setMessage("Are you sure you want to delete the user \"" + username + "\"?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Cursor cursor = dbHelper.getAllUsers();
                while (cursor.moveToNext()) {
                    int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String currentUsername = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
                    if (currentUsername.equals(username)) {
                        dbHelper.deleteUser(userId);
                        break;
                    }
                }
                cursor.close();
                loadUsers();
            }
        });

        builder.setNegativeButton("No", null);

        builder.show();
    }
}
