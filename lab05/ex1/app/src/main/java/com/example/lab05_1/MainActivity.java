package com.example.lab05_1;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private CustomAdapter adapter;
    private List<Item> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        itemList = new ArrayList<>();

        // Thêm dữ liệu vào danh sách
        itemList.add(new Item("Apple", R.drawable.phone, false));
        itemList.add(new Item("Samsung", R.drawable.phone, false));
        itemList.add(new Item("Nokia", R.drawable.phone, false));
        itemList.add(new Item("Oppo", R.drawable.phone, false));

        adapter = new CustomAdapter(this, itemList);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.check_all) {
            checkAllItems(true);
            return true;
        } else if (id == R.id.uncheck_all) {
            checkAllItems(false);
            return true;
        } else if (id == R.id.delete_selected) {
            confirmDeleteSelected();
            return true;
        } else if (id == R.id.delete_all) {
            confirmDeleteAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void checkAllItems(boolean check) {
        for (Item item : itemList) {
            item.setChecked(check);
        }
        adapter.notifyDataSetChanged();
    }

    private void deleteSelectedItems() {
        List<Item> toRemove = new ArrayList<>();
        for (Item item : itemList) {
            if (item.isChecked()) {
                toRemove.add(item);
            }
        }
        itemList.removeAll(toRemove);
        adapter.notifyDataSetChanged();
    }

    private void deleteAllItems() {
        itemList.clear();
        adapter.notifyDataSetChanged();
    }

    private void confirmDeleteSelected() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Selected Items")
                .setMessage("Are you sure you want to delete the selected items?")
                .setPositiveButton("Yes", (dialog, which) -> deleteSelectedItems())
                .setNegativeButton("No", null)
                .show();
    }

    private void confirmDeleteAll() {
        new AlertDialog.Builder(this)
                .setTitle("Delete All Items")
                .setMessage("Are you sure you want to delete all items?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAllItems())
                .setNegativeButton("No", null)
                .show();
    }
}
