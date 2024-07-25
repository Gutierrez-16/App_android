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
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.list_view);
        Button btnAdd = findViewById(R.id.btn_add);

        // Obtener el userId del Intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("USER_ID", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        listItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);

        loadData();

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showUpdateDeleteDialog(position);
            }
        });
    }

    private void loadData() {
        listItems.clear();
        Cursor cursor = dbHelper.getAllStudents();
        if (cursor.moveToFirst()) {
            do {
                listItems.add(cursor.getInt(0) + ": " + cursor.getString(1) + " - " + cursor.getInt(2));
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void showAddDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View subView = inflater.inflate(R.layout.dialog_add_student, null);
        final EditText edtName = subView.findViewById(R.id.edt_name);
        final EditText edtAge = subView.findViewById(R.id.edt_age);

        new AlertDialog.Builder(this)
                .setTitle("Add Student")
                .setView(subView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = edtName.getText().toString();
                        int age = Integer.parseInt(edtAge.getText().toString());
                        dbHelper.addStudent(name, age);
                        loadData();
                        Toast.makeText(MainActivity.this, "Student Added", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showUpdateDeleteDialog(final int position) {
        final String selectedItem = listItems.get(position);
        final int studentId = Integer.parseInt(selectedItem.split(":")[0]);

        LayoutInflater inflater = LayoutInflater.from(this);
        View subView = inflater.inflate(R.layout.dialog_update_student, null);
        final EditText edtName = subView.findViewById(R.id.edt_name);
        final EditText edtAge = subView.findViewById(R.id.edt_age);

        String[] parts = selectedItem.split(": ")[1].split(" - ");
        edtName.setText(parts[0]);
        edtAge.setText(parts[1]);

        new AlertDialog.Builder(this)
                .setTitle("Update/Delete Student")
                .setView(subView)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = edtName.getText().toString();
                        int age = Integer.parseInt(edtAge.getText().toString());
                        dbHelper.updateStudent(studentId, name, age);
                        loadData();
                        Toast.makeText(MainActivity.this, "Student Updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteStudent(studentId);
                        loadData();
                        Toast.makeText(MainActivity.this, "Student Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Cancel", null)
                .show();
    }
}
