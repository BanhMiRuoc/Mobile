package com.example.milita;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentProfileActivity extends AppCompatActivity {
    String avatarUrl;
    Button btnSetting, btnBack, btnCertificate;
    CircleImageView avatar_user;
    TextView username, tv_name, tv_birthday, tv_email, tv_status, tv_phone, tv_studentId, tv_faculty;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSetting = findViewById(R.id.btnSetting);
        btnBack = findViewById(R.id.btnBack);
        avatar_user = findViewById(R.id.avatar_user);
        tv_birthday = findViewById(R.id.tv_birthday);
        tv_name = findViewById(R.id.tv_name);
        username = findViewById(R.id.username);
        tv_email = findViewById(R.id.tv_email);
        tv_status = findViewById(R.id.tv_status);
        tv_phone = findViewById(R.id.tv_phone);
        tv_studentId = findViewById(R.id.tv_studentId);
        tv_faculty = findViewById(R.id.tv_faculty);
        btnCertificate = findViewById(R.id.btnCertificate);

        db = FirebaseFirestore.getInstance();

        String userId = getIntent().getStringExtra("id");
        String role = getIntent().getStringExtra("role");
        String currentUserEmail = getIntent().getStringExtra("currentUserEmail");
        loadUserProfile(userId);

        if ("Employee".equals(role)) {
            // Ẩn các chức năng mà nhân viên không thể làm
            btnSetting.setVisibility(View.GONE);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnBack) {
                    Intent intent = new Intent(StudentProfileActivity.this, StudentManagementActivity.class);
                    intent.putExtra("role", role);
                    intent.putExtra("userId", userId);
                    intent.putExtra("currentUserEmail", currentUserEmail);
                    startActivity(intent);
                }
            }
        });
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnSetting) {
                    Intent intent = new Intent(StudentProfileActivity.this, EditStudentActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("role", role);
                    startActivityForResult(intent, 4);
                }
            }
        });

        btnCertificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnCertificate) {
                    Intent intent = new Intent(StudentProfileActivity.this, CertificateManagementActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("role", role);
                    intent.putExtra("studentId", tv_status.getText().toString());
                    intent.putExtra("studentName", tv_name.getText().toString());
                    intent.putExtra("currentUserEmail", currentUserEmail);
                    startActivityForResult(intent, 5);
                }
            }
        });
    }

    private void loadUserProfile(String userId) {
        db.collection("students")
                .whereEqualTo("id", userId) // Giả sử thuộc tính id là userId
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Lấy tài liệu đầu tiên tìm được
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        String birthday = documentSnapshot.getString("birthday");
                        String email = documentSnapshot.getString("email");
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phone");
                        String id = documentSnapshot.getString("id");
                        String studentClass = documentSnapshot.getString("class");
                        String faculty = documentSnapshot.getString("faculty");
                        String profileImageBase64 = documentSnapshot.getString("profileImageBase64");

                        username.setText(name);
                        tv_name.setText(name);
                        tv_birthday.setText(birthday);
                        tv_email.setText(email);
                        tv_phone.setText(phone);
                        tv_status.setText(id);
                        tv_studentId.setText(studentClass);
                        tv_faculty.setText(faculty);

                        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                            byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                            Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            avatar_user.setImageBitmap(profileImageBitmap);
                        }
                    } else {
                        Log.d("FirestoreData", "Không tìm thấy người dùng với id: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreData", "Lỗi khi lấy dữ liệu", e);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 4 && resultCode == RESULT_OK && data != null) {
            String userId = data.getStringExtra("userId");
            Toast.makeText(this, "ID là: " + userId, Toast.LENGTH_SHORT).show();
            loadUserProfile(userId);
        }
    }

}