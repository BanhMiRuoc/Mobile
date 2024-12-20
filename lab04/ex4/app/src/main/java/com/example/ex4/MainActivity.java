package com.example.ex4;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    GridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get RecyclerView reference
        recyclerView = findViewById(R.id.recyclerView);

        // Generate random number of items between 10 and 100
        int randomItemCount = new Random().nextInt(91) + 10;

        // Set GridLayoutManager with 3 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);  // Set LayoutManager programmatically

        // Initialize adapter and set it to RecyclerView
        gridAdapter = new GridAdapter(randomItemCount, this);
        recyclerView.setAdapter(gridAdapter);
    }
}
