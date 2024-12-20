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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
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

public class EditUserActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private FirebaseStorage storage;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private Button btnCapture, btnSave, btnBack;
    private TextView username;
    private CircleImageView profile_image;
    private EditText et_username, et_birthday, et_email, et_phone;
    private RadioGroup radioGroup;
    private RadioButton rBtnNormal, rBtnLocked;
    private String selectedOption, userId;
    private String selectedDate = "";
    private int selectedYear, selectedMonth, selectedDay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user);
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
        radioGroup = findViewById(R.id.radioGroup);
        rBtnNormal = findViewById(R.id.rBtnNormal);
        rBtnLocked = findViewById(R.id.rBtnLocked);
        db = FirebaseFirestore.getInstance();

        userId = getIntent().getStringExtra("userId");
        String currentUserEmail = getIntent().getStringExtra("currentUserEmail");
        String role = getIntent().getStringExtra("role");


        if ("Employee".equals(role)) {
            et_username.setEnabled(false);
            et_birthday.setEnabled(false);
            et_email.setEnabled(false);
            et_phone.setEnabled(false);
            rBtnNormal.setEnabled(false);
            rBtnLocked.setEnabled(false);
            loadUserProfileWithEmail(currentUserEmail);
        } else {
            loadUserProfile(userId);
        }

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        et_birthday.setOnClickListener(v -> {
            showDateDialog();
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSave.setOnClickListener(v -> {
            profile_image.setDrawingCacheEnabled(true); // Bật cache
            profile_image.buildDrawingCache();
            Bitmap profileBitmap = profile_image.getDrawingCache();
            UpdateUserData(userId, profileBitmap);
        });

        btnCapture.setOnClickListener(v -> showImagePickerDialog());

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Determine the selected option
            if (checkedId == R.id.rBtnNormal) {
                selectedOption = "Normal";
            } else if (checkedId == R.id.rBtnLocked) {
                selectedOption = "Locked";
            } else {
                selectedOption = "";
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
                }
            }
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
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
                        et_username.setText(name);
                        et_birthday.setText(birthday);
                        et_email.setText(email);
                        et_phone.setText(phone);

                        if ("Normal".equals(status)) {
                            rBtnNormal.setChecked(true);
                            selectedOption = "Normal"; // Update selectedOption
                        } else if ("Locked".equals(status)) {
                            rBtnLocked.setChecked(true);
                            selectedOption = "Locked"; // Update selectedOption
                        }

                        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                            byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                            Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            profile_image.setImageBitmap(profileImageBitmap);
                        }
                    } else {
                        Log.d("FirestoreData", "Document không tồn tại.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreData", "Lỗi khi lấy dữ liệu", e);
                });
    }

    private void loadUserProfileWithEmail(String userEmail) {
        db.collection("users")
                .whereEqualTo("email", userEmail)  // Truy vấn theo email
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Chỉ lấy tài liệu đầu tiên (nếu có nhiều tài liệu trùng với email)
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        userId = documentSnapshot.getId();
                        String birthday = documentSnapshot.getString("birthday");
                        String email = documentSnapshot.getString("email");
                        String name = documentSnapshot.getString("username");
                        String phone = documentSnapshot.getString("phone");
                        String status = documentSnapshot.getString("status");
                        String profileImageBase64 = documentSnapshot.getString("profileImageBase64");

                        // Cập nhật các trường trong UI
                        username.setText(name);
                        et_username.setText(name);
                        et_birthday.setText(birthday);
                        et_email.setText(email);
                        et_phone.setText(phone);

                        // Cập nhật trạng thái người dùng
                        if ("Normal".equals(status)) {
                            rBtnNormal.setChecked(true);
                            selectedOption = "Normal"; // Cập nhật lựa chọn
                        } else if ("Locked".equals(status)) {
                            rBtnLocked.setChecked(true);
                            selectedOption = "Locked"; // Cập nhật lựa chọn
                        }

                        // Xử lý hình ảnh profile (nếu có)
                        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                            byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                            Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            profile_image.setImageBitmap(profileImageBitmap);
                        }
                    } else {
                        Log.d("FirestoreData", "Không tìm thấy tài liệu với email này.");
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
        String status = selectedOption;

        // Input validations
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

        if (status.isEmpty()) {
            Toast.makeText(this, "Please choose status user", Toast.LENGTH_SHORT).show();
            return;
        }
        if (profileBitmap == null) {
            Toast.makeText(this, "Please choose role user", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển đổi ảnh đại diện thành chuỗi Base64
        String profileImageBase64 = encodeImageToBase64(profileBitmap);

        // Check if the email already exists in the Firestore database
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Email already exists, check if it's not the current user
                        boolean emailExistsForAnotherUser = false;
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (!documentSnapshot.getId().equals(userId)) {
                                emailExistsForAnotherUser = true;
                                break;
                            }
                        }

                        if (emailExistsForAnotherUser) {
                            et_email.setError("Email already exists");
                            et_email.requestFocus();
                            return;
                        }
                    }

                    // If email is valid, proceed with updating the user data
                    DocumentReference userRef = db.collection("users").document(userId);

                    Map<String, Object> userData = new HashMap<>();
                    userData.put("username", username);
                    userData.put("birthday", birthday);
                    userData.put("email", email);
                    userData.put("phone", phone);
                    userData.put("status", status);
                    if (profileImageBase64 != null) {
                        userData.put("profileImageBase64", profileImageBase64);
                    }

                    // Update user data in Firestore
                    userRef.update(userData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "User data updated successfully!", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("userId", userId);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error updating user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking email existence: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    public String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream); // Bạn có thể giảm chất lượng nếu muốn
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}