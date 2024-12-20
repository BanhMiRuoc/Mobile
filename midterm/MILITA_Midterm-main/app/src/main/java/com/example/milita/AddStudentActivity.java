package com.example.milita;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddStudentActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private Uri selectedImageUri;
    private EditText txtStudentName, txtBirthday, txtEmail, txtPhone, txtStudentId, txtClass, txtFaculty;
    private FirebaseFirestore db;
    private Button btnSave, btnCapture, btnBack;
    private CircleImageView profileImageView;
    private String selectedDate = "";
    private int selectedYear, selectedMonth, selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_student);

        db = FirebaseFirestore.getInstance();
        txtStudentName = findViewById(R.id.txtStudentName);
        txtBirthday = findViewById(R.id.txtBirthday);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtStudentId = findViewById(R.id.txtStudentId);
        txtClass = findViewById(R.id.txtClass);
        txtFaculty = findViewById(R.id.txtFaculty);
        btnSave = findViewById(R.id.btnSave);
        profileImageView = findViewById(R.id.profile_image);
        btnCapture = findViewById(R.id.btnCapture);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSave.setOnClickListener(v -> {
            profileImageView.setDrawingCacheEnabled(true); // Bật cache
            profileImageView.buildDrawingCache();
            Bitmap profileBitmap = profileImageView.getDrawingCache();
            savestudentData(profileBitmap);
        });

        btnCapture.setOnClickListener(v -> showImagePickerDialog());

        txtBirthday.setOnClickListener(v -> {
            showDateDialog();
        });

    }

    private void showDateDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = dayOfMonth;
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            txtBirthday.setText(selectedDate);
        }, selectedYear, selectedMonth, selectedDay);
        datePickerDialog.show();
    }

    private void savestudentData(Bitmap profileImageBitmap) {
        String studentName = txtStudentName.getText().toString();
        String birthday = txtBirthday.getText().toString();
        String email = txtEmail.getText().toString();
        String phone = txtPhone.getText().toString();
        String studentId = txtStudentId.getText().toString();
        String studentClass = txtClass.getText().toString();
        String faculty = txtFaculty.getText().toString();

        if (studentName.isEmpty()) {
            txtStudentName.setError("Please enter username");
            txtStudentName.requestFocus();
            return;
        }
        if (birthday.isEmpty()) {
            Toast.makeText(this, "Please enter birthday", Toast.LENGTH_SHORT).show();
            txtBirthday.requestFocus();
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
                    txtBirthday.requestFocus();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error birthday", Toast.LENGTH_SHORT).show();
                txtBirthday.requestFocus();
                return;
            }
        }
        if (email.isEmpty()) {
            txtEmail.setError("Please enter email");
            txtEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            txtPhone.setError("Please enter phone number");
            txtPhone.requestFocus();
            return;
        } else if (!phone.matches("\\d+")) {
            txtPhone.setError("Phone number must be only number");
            txtPhone.requestFocus();
            return;
        } else if (phone.length() != 10) {
            txtPhone.setError("Phone number must be 10 digits");
            txtPhone.requestFocus();
            return;
        }

        if (studentId.isEmpty()) {
            txtStudentId.setError("Please enter studentId");
            txtStudentId.requestFocus();
            return;
        } else if (!studentId.matches("\\d+")) { // Kiểm tra nếu không phải là số
            txtStudentId.setError("StudentId must contain only numbers");
            txtStudentId.requestFocus();
            return;
        }

        if (studentClass.isEmpty()) {
            txtClass.setError("Please enter studentClass");
            txtClass.requestFocus();
            return;
        }
        if (faculty.isEmpty()) {
            txtFaculty.setError("Please enter email");
            txtFaculty.requestFocus();
            return;
        }
        if (profileImageBitmap == null) {
            Toast.makeText(this, "Please capture or select a profile image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển đổi ảnh đại diện thành chuỗi Base64
        String profileImageBase64 = encodeImageToBase64(profileImageBitmap);

        // Tạo đối tượng student
        Map<String, Object> student = new HashMap<>();
        student.put("name", studentName);
        student.put("birthday", birthday);
        student.put("email", email);
        student.put("phone", phone);
        student.put("id", studentId);
        student.put("class", studentClass);
        student.put("faculty", faculty);
        student.put("profileImageBase64", profileImageBase64); // Lưu chuỗi Base64 của ảnh đại diện

        // Thêm dữ liệu vào Firestore
        db.collection("students").add(student)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "student saved successfully!", Toast.LENGTH_SHORT).show();

                    // Sau khi lưu thành công, quay lại trang Home
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent); // Trả kết quả về Home activity
                    finish(); // Quay lại màn hình Home
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    public String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream); // Bạn có thể giảm chất lượng nếu muốn
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getData() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                selectedImageUri = getImageUri(imageBitmap);
                profileImageView.setImageURI(selectedImageUri);
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null && data.getData() != null) {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    profileImageView.setImageURI(selectedImageUri);
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
}