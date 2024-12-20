package com.example.milita;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditStudentActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private FirebaseStorage storage;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    Button btnCapture, btnSave, btnBack;
    TextView username;
    CircleImageView profile_image;
    EditText et_username, et_birthday, et_email, et_phone, et_status, et_class, et_faculty;
    private String selectedDate = "";
    private int selectedYear, selectedMonth, selectedDay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_student);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnCapture = findViewById(R.id.btnCapture);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        username = findViewById(R.id.username);
        profile_image = findViewById(R.id.profile_image);
        et_username = findViewById(R.id.et_username);
        et_birthday = findViewById(R.id.et_birthday);
        et_email = findViewById(R.id.et_email);
        et_phone = findViewById(R.id.et_phone);
        et_status = findViewById(R.id.et_status);
        et_class = findViewById(R.id.et_class);
        et_faculty = findViewById(R.id.et_faculty);
        db = FirebaseFirestore.getInstance();

        String userId = getIntent().getStringExtra("userId");
        String role = getIntent().getStringExtra("role");
        if ("Employee".equals(role)) {
            // Ẩn các chức năng mà nhân viên không thể làm
            et_username.setEnabled(false);
            et_birthday.setEnabled(false);
            et_email.setEnabled(false);
            et_phone.setEnabled(false);
            et_status.setEnabled(false);
            et_class.setEnabled(false);
            et_faculty.setEnabled(false);
        }
        loadUserProfile(userId);

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        et_birthday.setOnClickListener(v -> {
            showDateDialog();
        });

        btnSave.setOnClickListener(v -> {
            profile_image.setDrawingCacheEnabled(true); // Bật cache
            profile_image.buildDrawingCache();
            Bitmap profileBitmap = profile_image.getDrawingCache();
            UpdateUserData(userId, profileBitmap);
        });

        btnCapture.setOnClickListener(v -> showImagePickerDialog());

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void showImagePickerDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    } else if (which == 1) {
                        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhotoIntent, REQUEST_PICK_IMAGE);
                    }
                })
                .show();
    }

    private void showDateDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = dayOfMonth;
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            et_birthday.setText(selectedDate);
        }, selectedYear, selectedMonth, selectedDay);
        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getData() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                selectedImageUri = getImageUri(imageBitmap);
                profile_image.setImageURI(selectedImageUri);
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null && data.getData() != null) {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    profile_image.setImageURI(selectedImageUri);
                } else {
                    Toast.makeText(this, "Lỗi khi chọn ảnh. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }            }
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
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
                        et_username.setText(name);
                        et_birthday.setText(birthday);
                        et_email.setText(email);
                        et_phone.setText(phone);
                        et_status.setText(id);
                        et_class.setText(studentClass);
                        et_faculty.setText(faculty);

                        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                            byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                            Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            profile_image.setImageBitmap(profileImageBitmap);
                        }
                    } else {
                        Log.d("FirestoreData", "Không tìm thấy người dùng với id: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreData", "Lỗi khi lấy dữ liệu", e);
                });
    }

    private void UpdateUserData(String userId, Bitmap profileBitmap) {
        String username = et_username.getText().toString();
        String birthday = et_birthday.getText().toString();
        String email = et_email.getText().toString();
        String phone = et_phone.getText().toString();
        String id = et_status.getText().toString();
        String studentClass = et_class.getText().toString();
        String faculty = et_faculty.getText().toString();

        if (username.isEmpty()) {
            et_username.setError("Please enter username");
            et_username.requestFocus();
            return;
        }
        if (birthday.isEmpty()) {
            Toast.makeText(this, "Please enter birthday", Toast.LENGTH_SHORT).show();
            et_birthday.requestFocus();
            return;
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date birthDate = sdf.parse(birthday);
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

                calendar.setTime(birthDate);
                int birthYear = calendar.get(Calendar.YEAR);
                int birthMonth = calendar.get(Calendar.MONTH);
                int birthDay = calendar.get(Calendar.DAY_OF_MONTH);

                int age = currentYear - birthYear;

                if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
                    age--;
                }

                if (age <= 5) {
                    Toast.makeText(this, "Age must be greater than 5", Toast.LENGTH_SHORT).show();
                    et_birthday.requestFocus();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error birthday", Toast.LENGTH_SHORT).show();
                et_birthday.requestFocus();
                return;
            }
        }
        if (email.isEmpty()) {
            et_email.setError("Please enter email");
            et_email.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            et_phone.setError("Please enter phone number");
            et_phone.requestFocus();
            return;
        } else if (!phone.matches("\\d+")) {
            et_phone.setError("Phone number must be only number");
            et_phone.requestFocus();
            return;
        } else if (phone.length() != 10) {
            et_phone.setError("Phone number must be 10 digits");
            et_phone.requestFocus();
            return;
        }

        if (id.isEmpty()) {
            et_status.setError("Please enter studentId");
            et_status.requestFocus();
            return;
        } else if (!id.matches("\\d+")) { // Kiểm tra nếu không phải là số
            et_status.setError("StudentId must contain only numbers");
            et_status.requestFocus();
            return;
        }

        if (studentClass.isEmpty()) {
            et_class.setError("Please enter studentClass");
            et_class.requestFocus();
            return;
        }
        if (faculty.isEmpty()) {
            et_faculty.setError("Please enter email");
            et_faculty.requestFocus();
            return;
        }
        if (profileBitmap == null) {
            Toast.makeText(this, "Please capture or select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển đổi ảnh đại diện thành chuỗi Base64
        String profileImageBase64 = encodeImageToBase64(profileBitmap);

        // Tạo map để lưu trữ các dữ liệu cần cập nhật
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", username);
        userData.put("birthday", birthday);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("id", id);
        userData.put("class", studentClass);
        userData.put("faculty", faculty);
        if (profileImageBase64 != null) {
            userData.put("profileImageBase64", profileImageBase64);
        }

        // Tìm tài liệu dựa trên trường `id` có giá trị là `userId`
        db.collection("students")
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Lấy tài liệu đầu tiên tìm được
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        // Cập nhật dữ liệu vào Firestore
                        documentSnapshot.getReference().update(userData)
                                .addOnSuccessListener(aVoid -> {
                                    // Thông báo cập nhật thành công
                                    Toast.makeText(this, "Student data updated successfully!", Toast.LENGTH_SHORT).show();
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("userId", id);
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Thông báo lỗi khi cập nhật
                                    Toast.makeText(this, "Error updating user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Không tìm thấy người dùng với id: " + userId, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tìm kiếm người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    public String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream); // Bạn có thể giảm chất lượng nếu muốn
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}