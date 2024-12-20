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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    String avatarUrl;
    Button btnSetting, btnBack;
    CircleImageView avatar_user;
    TextView username, tv_name, tv_birthday, tv_email, tv_status, tv_phone;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
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

        db = FirebaseFirestore.getInstance();

        String userId = getIntent().getStringExtra("userId");

        loadUserProfile(userId);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnBack) {
                    Intent intent = new Intent(UserProfileActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId() == R.id.btnSetting) {
                    Intent intent = new Intent(UserProfileActivity.this, EditUserActivity.class);
                    intent.putExtra("userId", userId);
                    startActivityForResult(intent, 2);
                }
            }
        });
    }

    private void loadUserProfile(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String birthday = documentSnapshot.getString("birthday");
                        String email = documentSnapshot.getString("email");
                        String name = documentSnapshot.getString("username");
                        String phone = documentSnapshot.getString("phone");
                        String status = documentSnapshot.getString("status");
                        String profileImageBase64 = documentSnapshot.getString("profileImageBase64");

                        username.setText(name);
                        tv_name.setText(name);
                        tv_birthday.setText(birthday);
                        tv_email.setText(email);
                        tv_phone.setText(phone);
                        tv_status.setText(status);

                        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                            byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                            Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            avatar_user.setImageBitmap(profileImageBitmap);
                        }
                    } else {
                        Log.d("FirestoreData", "Document không tồn tại.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreData", "Lỗi khi lấy dữ liệu", e);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            String userId = getIntent().getStringExtra("userId");
            loadUserProfile(userId);
        }
    }
}