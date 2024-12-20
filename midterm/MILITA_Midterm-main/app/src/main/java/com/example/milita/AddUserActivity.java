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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddUserActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private Uri selectedImageUri;
    private EditText txtUserName, txtBirthday, txtEmail, txtPhone;
    private FirebaseFirestore db;
    private Button btnSave, btnCapture, btnBack;
    private CircleImageView profileImageView;
    private RadioGroup radioGroup;
    private RadioButton rBtnNormal, rBtnLocked;
    private String selectedOption = "", selectedRole = "";
    private Spinner spinnerRole;
    private String selectedDate = "";
    private int selectedYear, selectedMonth, selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_user);

        db = FirebaseFirestore.getInstance();
        txtUserName = findViewById(R.id.txtUserName);
        txtBirthday = findViewById(R.id.txtBirthday);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        profileImageView = findViewById(R.id.profile_image);
        btnCapture = findViewById(R.id.btnCapture);
        radioGroup = findViewById(R.id.radioGroup);
        rBtnNormal = findViewById(R.id.rBtnNormal);
        rBtnLocked = findViewById(R.id.rBtnLocked);

        spinnerRole = findViewById(R.id.spinnerRole);

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        txtBirthday.setOnClickListener(v -> {
            showDateDialog();
        });

        btnSave.setOnClickListener(v -> {
            selectedRole = spinnerRole.getSelectedItem().toString();

            profileImageView.setDrawingCacheEnabled(true); // Bật cache
            profileImageView.buildDrawingCache();
            Bitmap profileBitmap = profileImageView.getDrawingCache();
            saveUserData(profileBitmap);
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnCapture.setOnClickListener(v -> showImagePickerDialog());

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rBtnNormal) {
                selectedOption = "Normal";
            } else if (checkedId == R.id.rBtnLocked) {
                selectedOption = "Locked";
            } else {
                selectedOption = "";
            }
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

    private void saveUserData(Bitmap profileImageBitmap) {
        String username = txtUserName.getText().toString();
        String birthday = txtBirthday.getText().toString();
        String email = txtEmail.getText().toString();
        String phone = txtPhone.getText().toString();
        String status = selectedOption;
        String profileImageBase64 = encodeImageToBase64(profileImageBitmap);

        // Kiểm tra các trường đầu vào
        if (username.isEmpty()) {
            txtUserName.setError("Please enter username");
            txtUserName.requestFocus();
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

        if (status.isEmpty()) {
            Toast.makeText(this, "Please choose status user", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedRole.isEmpty()) {
            Toast.makeText(this, "Please choose role user", Toast.LENGTH_SHORT).show();
            return;
        }
        if (profileImageBitmap == null) {
            Toast.makeText(this, "Please choose avatar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem email đã tồn tại trong Firestore chưa
        checkEmailExists(email, exists -> {
            if (exists) {
                // Nếu email đã tồn tại, không lưu thông tin và thông báo cho người dùng
                txtEmail.setError("Email already exists");
                txtEmail.requestFocus();
            } else {
                // Nếu email chưa tồn tại, tiến hành lưu dữ liệu người dùng
                String password = email.split("@")[0]; // Tạo mật khẩu từ phần trước dấu @ của email
                String documentId = UUID.randomUUID().toString();

                // Thông tin tài khoản cho bảng accounts
                Map<String, Object> accountData = new HashMap<>();
                accountData.put("email", email);
                accountData.put("password", password);
                accountData.put("role", selectedRole);

                // Thông tin người dùng cho bảng users
                Map<String, Object> userData = new HashMap<>();
                userData.put("username", username);
                userData.put("birthday", birthday);
                userData.put("email", email);
                userData.put("phone", phone);
                userData.put("status", status);
                userData.put("role", selectedRole);
                userData.put("profileImageBase64", profileImageBase64);

                // Lưu thông tin vào bảng accounts
                db.collection("accounts").document(documentId).set(accountData)
                        .addOnSuccessListener(aVoid -> {
                            // Sau khi lưu tài khoản thành công, lưu tiếp thông tin người dùng với cùng documentId
                            db.collection("users").document(documentId).set(userData)
                                    .addOnSuccessListener(aVoid1 -> {
                                        Toast.makeText(this, "User and account created successfully!", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error saving account data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void checkEmailExists(String email, OnEmailCheckListener listener) {
        db.collection("accounts")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        listener.onCheckCompleted(true); // Email đã tồn tại
                    } else {
                        listener.onCheckCompleted(false); // Email chưa tồn tại
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Error checking email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    listener.onCheckCompleted(false); // Nếu có lỗi, coi như email không hợp lệ
                });
    }

    public interface OnEmailCheckListener {
        void onCheckCompleted(boolean isEmailExists);
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