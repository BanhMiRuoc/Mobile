package com.example.ex2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    EventAdapter adapter;
    List<Event> eventList;
    List<Event> displayedList;
    boolean showAllEvents = true;
    Event eventToEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);

        eventList = new ArrayList<>();
        eventList.add(new Event("Sinh hoat chu nhiem", "C120", "09/03/2020", "04:43", false));
        eventList.add(new Event("Huong dan luan van", "C120", "09/03/2020", "04:43", true));

        // Khởi tạo displayedList với tất cả sự kiện
        displayedList = new ArrayList<>(eventList);

        adapter = new EventAdapter(this, displayedList);
        listView.setAdapter(adapter);

        registerForContextMenu(listView);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);

        MenuItem switchItem = menu.findItem(R.id.action_switch);
        Switch switchView = (Switch) switchItem.getActionView();

        // Đặt trạng thái ban đầu cho Switch
        switchView.setChecked(showAllEvents);

        // Xử lý sự kiện khi Switch thay đổi
        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showAllEvents = isChecked; // Cập nhật biến trạng thái
            updateEventList(showAllEvents); // Cập nhật danh sách sự kiện
        });
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Intent intent = new Intent(MainActivity.this, AddActivity.class);
            startActivityForResult(intent, 1);
            return true;
        } else if (id == R.id.action_remove) {
            showRemoveAllConfirmation();
            return true;
        } else if (id == R.id.action_about) {
            Toast.makeText(this, "đã chọn about menu", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(android.view.ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        menu.setHeaderTitle("Choose action:");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position; // Vị trí của item được nhấn giữ

        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Intent intent = new Intent(MainActivity.this, AddActivity.class);

            // Truyền dữ liệu sự kiện sang Activity edit
            eventToEdit = displayedList.get(position);
            intent.putExtra("eventName", eventToEdit.getName());
            intent.putExtra("eventPlace", eventToEdit.getPlace());
            intent.putExtra("eventDate", eventToEdit.getDate());
            intent.putExtra("eventTime", eventToEdit.getTime());
            startActivityForResult(intent, 1);
            return true;
        } else if (id == R.id.action_delete) {
            eventList.remove(displayedList.get(position)); // Xóa khỏi danh sách sự kiện chính
            updateEventList(showAllEvents); // Cập nhật danh sách hiển thị
            Toast.makeText(this, "Đã xóa sự kiện", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
    }



    private void showRemoveAllConfirmation() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure to remove all events?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    eventList.clear();
                    updateEventList(showAllEvents);
                })
                .setNegativeButton("No", null)
                .show();
    }


    private void updateEventList(boolean showAllEvents) {
        displayedList.clear(); // Xóa danh sách hiển thị cũ

        if (showAllEvents) {
            // Hiển thị tất cả sự kiện
            displayedList.addAll(eventList);
        } else {
            // Chỉ hiển thị các sự kiện đã được kích hoạt
            for (Event event : eventList) {
                if (event.isEnabled()) {
                    displayedList.add(event);
                }
            }
        }
        adapter.notifyDataSetChanged(); // Cập nhật adapter
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Event newEvent = (Event) data.getSerializableExtra("newEvent");
            Event updatedEvent = (Event) data.getSerializableExtra("updatedEvent");

            if (newEvent != null) {
                eventList.add(newEvent);  // Thêm sự kiện mới vào danh sách
            } else if (updatedEvent != null) {
                // Cập nhật sự kiện đã chỉnh sửa
                int position = eventList.indexOf(eventToEdit); // Sử dụng eventToEdit đã được lưu trong AddActivity
                if (position != -1) {
                    eventList.set(position, updatedEvent); // Cập nhật sự kiện trong danh sách chính
                }
            }

            updateEventList(showAllEvents);  // Cập nhật danh sách hiển thị
        }
    }

}