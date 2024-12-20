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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import de.hdodenhof.circleimageview.CircleImageView;

public class CertificateInfomationActivity extends AppCompatActivity {
    String avatarUrl;
    Button btnSetting, btnBack;
    CircleImageView avatar_user;
    TextView username, tv_name, tv_session, tv_organization, tv_school, tv_des;
    private FirebaseFirestore db;
    String studentId, cerId, studentName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_certificate_infomation);

        btnSetting = findViewById(R.id.btnSetting);
        btnBack = findViewById(R.id.btnBack);
        avatar_user = findViewById(R.id.avatar_user);
        tv_session = findViewById(R.id.tv_session);
        tv_name = findViewById(R.id.tv_name);
        username = findViewById(R.id.username);
        tv_organization = findViewById(R.id.tv_organization);
        tv_school = findViewById(R.id.tv_school);
        tv_des = findViewById(R.id.tv_des);

        db = FirebaseFirestore.getInstance();

        String role = getIntent().getStringExtra("role");
        studentId = getIntent().getStringExtra("studentId");
        cerId = getIntent().getStringExtra("id");
        studentName = getIntent().getStringExtra("studentName");
        String currentUserEmail = getIntent().getStringExtra("currentUserEmail");


        loadCertificateById();

        if ("Employee".equals(role)) {
            // Ẩn các chức năng mà nhân viên không thể làm
            btnSetting.setVisibility(View.GONE);
        }
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnBack) {
                    Intent intent = new Intent();
                    intent.putExtra("role", role);
                    intent.putExtra("studentId", studentId);
                    intent.putExtra("currentUserEmail", currentUserEmail);
                    intent.putExtra("studentName", studentName);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnSetting) {
                    Intent intent = new Intent(CertificateInfomationActivity.this, EditCertificateActivity.class);
                    intent.putExtra("id", cerId);
                    intent.putExtra("role", role);
                    intent.putExtra("studentId", studentId);
                    startActivityForResult(intent, 2);
                }
            }
        });
    }

    private void loadCertificateById() {
        db.collection("students")
                .whereEqualTo("id", studentId) // Tìm student theo thuộc tính studentId
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot studentDocument : queryDocumentSnapshots) {
                            // Tìm subcollection "certificates" và chỉ lấy tài liệu với ID cerId
                            studentDocument.getReference().collection("certificates").document(cerId)
                                    .get()
                                    .addOnSuccessListener(document -> {
                                        if (document.exists()) {
                                            String cername = document.getString("cername");
                                            String session = document.getString("session");
                                            String organization = document.getString("organization");
                                            String des = document.getString("des");
                                            String school = document.getString("school");
                                            String profileImageBase64 = document.getString("profileImageBase64");

                                            // Gán dữ liệu vào các TextView và ImageView
                                            username.setText(cername);
                                            tv_name.setText(cername);
                                            tv_session.setText(session);
                                            tv_organization.setText(organization);
                                            tv_des.setText(des);
                                            tv_school.setText(school);

                                            // Hiển thị ảnh đại diện nếu có
                                            if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                                                byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                                                Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                                avatar_user.setImageBitmap(profileImageBitmap);
                                            }
                                        } else {
                                            Log.d("CertificateData", "Không tìm thấy certificate với ID: " + cerId);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("CertificateData", "Lỗi khi lấy dữ liệu certificate", e);
                                    });
                        }
                    } else {
                        Log.d("CertificateData", "Không tìm thấy sinh viên với studentId: " + studentId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CertificateData", "Lỗi khi lấy dữ liệu sinh viên", e);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            loadCertificateById();
        }
    }
}