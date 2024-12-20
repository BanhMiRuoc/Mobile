package com.example.ex1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.ex1.Adapter.FileAdapter;
import com.example.ex1.Model.File;
import com.example.ex1.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity{
    RecyclerView recyclerView;
    Button btnDown;
    EditText edtTxtLink;

    List<File> fileList = new ArrayList<>();
    public static FileAdapter fileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rcv);
        edtTxtLink = findViewById(R.id.edtTxtLink);
        btnDown = findViewById(R.id.btnDown);

        fileAdapter = new FileAdapter(MainActivity.this, fileList);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(fileAdapter);

        btnDown.setOnClickListener(view -> {
            String fileName = edtTxtLink.getText().toString();
            double fileCapacity = new Random().nextDouble();
            File downloadFile = new File(fileName, fileCapacity, 1, 0);
            fileList.add(downloadFile);
            fileAdapter.notifyDataSetChanged();


        });

    }

}