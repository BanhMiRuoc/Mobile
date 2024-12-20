package com.example.milita;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ADD_USER = 10;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private CheckBox[] checkBoxes;
    private Button btnDelete, btnAdd, btnListStudent, btnHome, btnProfile, btnHistory;
    private CheckBox chkCheckAll;
    private FirebaseFirestore db;
    private ImageView logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnDelete = findViewById(R.id.btnDelete);
        btnHistory = findViewById(R.id.btnHistory);
        btnAdd = findViewById(R.id.btnAdd);
        btnListStudent = findViewById(R.id.btnListStudent);
        btnHome = findViewById(R.id.btnHome);
        chkCheckAll = findViewById(R.id.chkCheckAll);
        logout = findViewById(R.id.logout);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);
        userRecyclerView.setAdapter(userAdapter);

        // Lấy dữ liệu từ Firestore
        loadUserDataFromFirestore();

        logout.setOnClickListener(v -> {
            // Chuyển về màn hình đăng nhập (LoginActivity)
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // Kết thúc Activity hiện tại
            finish();
        });
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, LoginHistoryActivity.class);
                startActivity(intent);
            }
        });
        btnListStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, StudentManagementActivity.class);
                startActivity(intent);
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HomeActivity.this, "Currently on the home page", Toast.LENGTH_SHORT).show();
            }
        });

//        btnProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(HomeActivity.this, "You are Admin", Toast.LENGTH_SHORT).show();
//            }
//        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AddUserActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userAdapter.getSelectedItemsCount() > 0) {
                    showDeleteConfirmationDialog();
                } else {
                    Toast.makeText(HomeActivity.this, "Please select at least one user to delete.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        chkCheckAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userAdapter.selectAllItems(isChecked);
        });
    }

    private void loadUserDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getId(); // Lấy ID từ Firestore
                            String username = document.getString("username");
                            String role = document.getString("role");
                            String status = document.getString("status");
                            String profileImageBase64 = document.getString("profileImageBase64");

                            // Chuyển Base64 thành Bitmap
                            Bitmap profileImageBitmap = null;
                            if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                                byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                                profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            }

                            // Tạo đối tượng User và truyền Bitmap vào nếu có
                            User user = new User(username, role, status, profileImageBitmap);
                            user.setUserId(userId); // Lưu userId
                            userList.add(user);
                        }

                        // Kiểm tra nếu có dữ liệu trước khi cập nhật adapter
                        if (!userList.isEmpty()) {
                            userAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error fetching data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    // Hiển thị dialog xác nhận trước khi xóa
    private void showDeleteConfirmationDialog() {
        // Tạo một AlertDialog để xác nhận hành động xóa
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm delete")
                .setMessage("Are you sure to delete users?")
                .setCancelable(false)  // Không cho phép đóng dialog khi nhấn ngoài
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Nếu người dùng xác nhận xóa, gọi phương thức xóa các mục đã chọn và xóa trong Firestore
                        userAdapter.removeSelectedItems(db); // Truyền db vào
                        Toast.makeText(HomeActivity.this, "Các mục đã được xóa.", Toast.LENGTH_SHORT).show();
                        chkCheckAll.setChecked(false);
                    }
                })
                .setNegativeButton("Hủy", null)  // Nếu người dùng hủy, không làm gì
                .create();

        // Lấy ra các button của dialog
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                // Lấy các button từ dialog
                Button btnDelete = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);

                btnDelete.setTextColor(getResources().getColor(R.color.blue)); // Màu xanh (thêm màu xanh vào resource)
                btnCancel.setTextColor(getResources().getColor(R.color.red)); // Màu đỏ (thêm màu đỏ vào resource)
            }
        });

        // Hiển thị dialog
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadUserDataFromFirestore();
        }
    }

}