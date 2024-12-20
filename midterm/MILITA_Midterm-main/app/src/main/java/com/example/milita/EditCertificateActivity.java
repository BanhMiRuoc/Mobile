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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditCertificateActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private Button btnCapture, btnSave, btnBack;
    private TextView username;
    private CircleImageView profile_image;
    private EditText et_cername, et_session, et_organization, et_school, et_description;
    private String selectedDate = "";
    private int selectedYear, selectedMonth, selectedDay;
    private String studentId, cerId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_certificate);
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
        et_cername = findViewById(R.id.et_cername);
        et_session = findViewById(R.id.et_session);
        et_organization = findViewById(R.id.et_organization);
        et_school = findViewById(R.id.et_school);
        et_description = findViewById(R.id.et_description);
        db = FirebaseFirestore.getInstance();

        String role = getIntent().getStringExtra("role");
        studentId = getIntent().getStringExtra("studentId");
        cerId = getIntent().getStringExtra("id");

        loadCertificateById();

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        et_session.setOnClickListener(v -> {
            showDateDialog();
        });

        btnSave.setOnClickListener(v -> {
            profile_image.setDrawingCacheEnabled(true); // Bật cache
            profile_image.buildDrawingCache();
            Bitmap profileBitmap = profile_image.getDrawingCache();
            UpdateCertificateData(profileBitmap);
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
            et_session.setText(selectedDate);
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
                                            et_cername.setText(cername);
                                            et_session.setText(session);
                                            et_organization.setText(organization);
                                            et_description.setText(des);
                                            et_school.setText(school);

                                            // Hiển thị ảnh đại diện nếu có
                                            if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                                                byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                                                Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                                profile_image.setImageBitmap(profileImageBitmap);
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


    private void UpdateCertificateData(Bitmap profileBitmap) {
        String cername = et_cername.getText().toString();
        String session = et_session.getText().toString();
        String organization = et_organization.getText().toString();
        String school = et_school.getText().toString();
        String description = et_description.getText().toString();

        if (cername.isEmpty()) {
            et_cername.setError("Please enter cername");
            et_cername.requestFocus();
            return;
        }

        if (session.isEmpty()) {
            Toast.makeText(this, "Please select a birthday", Toast.LENGTH_SHORT).show();
            et_session.requestFocus();
            return;
        }

        if (organization.isEmpty()) {
            et_organization.setError("Please enter organization");
            et_organization.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            et_description.setError("Please enter description");
            et_description.requestFocus();
            return;
        }

        if (school.isEmpty()) {
            et_school.setError("Please enter school");
            et_school.requestFocus();
            return;
        }

        // Kiểm tra nếu ảnh chưa được chọn
        if (profileBitmap == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert the profile image to a Base64 string
        String profileImageBase64 = encodeImageToBase64(profileBitmap);

        // Create a map to store the data to be updated
        Map<String, Object> certificateData = new HashMap<>();
        certificateData.put("cername", cername);
        certificateData.put("session", session);
        certificateData.put("organization", organization);
        certificateData.put("school", school);
        certificateData.put("description", description);
        if (profileImageBase64 != null) {
            certificateData.put("profileImageBase64", profileImageBase64);
        }

        // Find the student document by studentId and update the certificate subcollection
        db.collection("students")
                .whereEqualTo("id", studentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot studentDocument = queryDocumentSnapshots.getDocuments().get(0);

                        // Update the specific certificate document in the "certificates" subcollection
                        studentDocument.getReference().collection("certificates").document(cerId)
                                .update(certificateData)
                                .addOnSuccessListener(aVoid -> {
                                    // Show success message
                                    Toast.makeText(this, "Certificate data updated successfully!", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Show error message
                                    Toast.makeText(this, "Error updating certificate data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "No student found with id: " + studentId, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error finding student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream); // Bạn có thể giảm chất lượng nếu muốn
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}