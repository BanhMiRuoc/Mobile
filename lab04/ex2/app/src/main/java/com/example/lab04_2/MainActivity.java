package com.example.lab04_2;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<String> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = generateRandomItems();
        itemAdapter = new ItemAdapter(itemList, this);
        recyclerView.setAdapter(itemAdapter);
    }

    private List<String> generateRandomItems() {
        List<String> items = new ArrayList<>();
        Random random = new Random();
        int itemCount = random.nextInt(20) + 1; // Random number of items between 1 and 20

        for (int i = 1; i <= itemCount; i++) {
            items.add("Item " + i);
        }

        return items;
    }
}
