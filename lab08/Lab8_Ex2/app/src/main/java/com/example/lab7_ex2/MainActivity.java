package com.example.lab7_ex2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MediaAdapter mediaAdapter;
    private Button btnDelete;
    private List<MediaItem> mediaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các thành phần giao diện
        recyclerView = findViewById(R.id.recyclerView);
        btnDelete = findViewById(R.id.btnDelete);
        mediaList = new ArrayList<>();

        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mediaAdapter = new MediaAdapter(mediaList, isSelected -> {
            btnDelete.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        });
        recyclerView.setAdapter(mediaAdapter);

        // Xử lý sự kiện xóa các mục đã chọn
        btnDelete.setOnClickListener(v -> deleteSelectedItems());

        // Kiểm tra quyền và tải dữ liệu nếu có quyền
        checkPermissionsAndLoadMedia();
    }

    private void checkPermissionsAndLoadMedia() {
        // Kiểm tra quyền truy cập các tệp phương tiện
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) {
            // Nếu quyền đã được cấp, tải dữ liệu
            loadMedia();
        } else {
            // Nếu chưa có quyền, yêu cầu quyền từ người dùng
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES); // hoặc sử dụng READ_MEDIA_VIDEO nếu cần
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Nếu quyền được cấp, tải dữ liệu
                    loadMedia();
                } else {
                    // Nếu quyền bị từ chối, kiểm tra xem quyền có bị từ chối vĩnh viễn không
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_MEDIA_IMAGES)) {
                        // Nếu quyền bị từ chối vĩnh viễn, yêu cầu người dùng cấp quyền qua cài đặt
                        Toast.makeText(this, "Permission denied. You can enable it from app settings.", Toast.LENGTH_LONG).show();
                    } else {
                        // Nếu quyền bị từ chối nhưng không vĩnh viễn, yêu cầu lại
                        Toast.makeText(this, "Permission denied. App cannot access media.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void loadMedia() {
        mediaList.clear();  // Làm sạch danh sách hiện tại
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED
        };
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE +
                " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                sortOrder)) {

            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);

                // Đọc dữ liệu và thêm vào danh sách
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    int mediaType = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE));
                    mediaList.add(new MediaItem(id, name, mediaType));
                }
                mediaAdapter.notifyDataSetChanged();  // Cập nhật RecyclerView
            }
        }
    }

    private void deleteSelectedItems() {
        List<MediaItem> selectedItems = mediaAdapter.getSelectedItems();
        for (MediaItem item : selectedItems) {
            // Xóa từng mục đã chọn
            getContentResolver().delete(MediaStore.Files.getContentUri("external"),
                    MediaStore.Files.FileColumns._ID + "=?",
                    new String[]{String.valueOf(item.getId())});
        }
        mediaList.removeAll(selectedItems);  // Xóa khỏi danh sách
        mediaAdapter.clearSelection();  // Xóa lựa chọn
        btnDelete.setVisibility(View.GONE);  // Ẩn nút Delete
        Toast.makeText(this, "Deleted " + selectedItems.size() + " items", Toast.LENGTH_SHORT).show();
    }
}
