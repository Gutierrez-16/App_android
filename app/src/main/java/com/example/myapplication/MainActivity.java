package com.example.myapplication;


import android.content.DialogInterface;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.list_view);  // Asegúrate de que esto coincide con el ID en el XML
        Button btnAdd = findViewById(R.id.btn_add);

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
        View subView = inflater.inflate(R.layout.dialog_add_update, null);

        final EditText nameField = subView.findViewById(R.id.edit_name);
        final EditText ageField = subView.findViewById(R.id.edit_age);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Student");
        builder.setView(subView);
        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameField.getText().toString();
                int age = Integer.parseInt(ageField.getText().toString());
                dbHelper.addStudent(name, age);
                loadData();
                Toast.makeText(MainActivity.this, "Student Added", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void showUpdateDeleteDialog(final int position) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View subView = inflater.inflate(R.layout.dialog_add_update, null);

        final EditText nameField = subView.findViewById(R.id.edit_name);
        final EditText ageField = subView.findViewById(R.id.edit_age);

        final String[] parts = listItems.get(position).split(": ");
        final int id = Integer.parseInt(parts[0]);
        final String[] details = parts[1].split(" - ");
        nameField.setText(details[0]);
        ageField.setText(details[1]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update/Delete Student");
        builder.setView(subView);
        builder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameField.getText().toString();
                int age = Integer.parseInt(ageField.getText().toString());
                dbHelper.updateStudent(id, name, age);
                loadData();
                Toast.makeText(MainActivity.this, "Student Updated", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.deleteStudent(id);
                loadData();
                Toast.makeText(MainActivity.this, "Student Deleted", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}