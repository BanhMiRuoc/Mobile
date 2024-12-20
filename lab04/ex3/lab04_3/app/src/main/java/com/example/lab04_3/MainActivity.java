package com.example.lab04_3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private Button removeAllButton, removeSelectedButton;
    private CustomAdapter adapter;
    private ArrayList<ItemModel> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        removeAllButton = findViewById(R.id.remove_all);
        removeSelectedButton = findViewById(R.id.remove_selected);

        itemList = new ArrayList<>();
        itemList.add(new ItemModel("Apple", false));
        itemList.add(new ItemModel("Samsung", false));
        itemList.add(new ItemModel("Nokia", false));
        itemList.add(new ItemModel("Oppo", false));

        adapter = new CustomAdapter(this, itemList);
        listView.setAdapter(adapter);

        removeAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemList.clear();
                adapter.notifyDataSetChanged();
            }
        });

        removeSelectedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ItemModel> itemsToRemove = new ArrayList<>();
                for (ItemModel item : itemList) {
                    if (item.isSelected()) {
                        itemsToRemove.add(item);
                    }
                }
                itemList.removeAll(itemsToRemove);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
