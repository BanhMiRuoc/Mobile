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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LoginHistoryActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ADD_loginHistory = 10;
    private RecyclerView loginRecyclerView;
    private UserAdapterInLoginHistory loginHistoryAdapter;
    private List<LoginHistory> loginHistoryList;
    private Button btnListStudent, btnHome;
    private FirebaseFirestore db;
    private ImageView logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_history);

        db = FirebaseFirestore.getInstance();
        loginRecyclerView = findViewById(R.id.loginRecyclerView);
        loginRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        logout = findViewById(R.id.logout);
        btnListStudent = findViewById(R.id.btnListStudent);
        btnHome = findViewById(R.id.btnHome);


        loginHistoryList = new ArrayList<>();
        loginHistoryAdapter = new UserAdapterInLoginHistory(this, loginHistoryList);
        loginRecyclerView.setAdapter(loginHistoryAdapter);

        // Lấy dữ liệu từ Firestore
        loadloginHistoryDataFromFirestore();

        logout.setOnClickListener(v -> {
            // Chuyển về màn hình đăng nhập (LoginActivity)
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // Kết thúc Activity hiện tại
            finish();
        });

        btnListStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginHistoryActivity.this, StudentManagementActivity.class);
                startActivity(intent);
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginHistoryActivity.this, "Currently on the home page", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadloginHistoryDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("LoginHistory")
                .orderBy("loginTime", Query.Direction.DESCENDING) // Sắp xếp theo thời gian giảm dần
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loginHistoryList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String loginHistoryId = document.getId(); // Lấy ID từ Firestore
                            String account = document.getString("email");
                            String name = document.getString("username");
                            String time = document.getString("loginTime");
                            String profileImageBase64 = document.getString("profileImageBase64");

                            // Chuyển Base64 thành Bitmap
                            Bitmap profileImageBitmap = null;
                            if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                                byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                                profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            }

                            // Tạo đối tượng loginHistory và truyền Bitmap vào nếu có
                            LoginHistory loginHistory = new LoginHistory(account, name, time, profileImageBitmap);
                            loginHistoryList.add(loginHistory);
                        }

                        // Kiểm tra nếu có dữ liệu trước khi cập nhật adapter
                        if (!loginHistoryList.isEmpty()) {
                            loginHistoryAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No loginHistory data found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error fetching data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}