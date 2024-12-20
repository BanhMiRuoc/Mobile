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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddCertificateActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private Uri selectedImageUri;
    private EditText et_cername, et_name, et_studentId, et_session, et_origanization, et_des, et_school ;
    private FirebaseFirestore db;
    private Button btnSave, btnCapture, btnBack;
    private CircleImageView profileImageView;
    private String selectedDate = "";
    private int selectedYear, selectedMonth, selectedDay;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_certificate);

        db = FirebaseFirestore.getInstance();
        et_cername = findViewById(R.id.et_cername);
        et_name = findViewById(R.id.et_name);
        et_studentId = findViewById(R.id.et_studentId);
        et_session = findViewById(R.id.et_session);
        et_origanization = findViewById(R.id.et_origanization);
        et_des = findViewById(R.id.et_des);
        et_school = findViewById(R.id.et_school);

        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        profileImageView = findViewById(R.id.profile_image);
        btnCapture = findViewById(R.id.btnCapture);

        studentId = getIntent().getStringExtra("studentId");

        loadStudentInfo(studentId);

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        et_session.setOnClickListener(v -> {
            showDateDialog();
        });

        btnSave.setOnClickListener(v -> {
            profileImageView.setDrawingCacheEnabled(true); // Bật cache
            profileImageView.buildDrawingCache();
            Bitmap profileBitmap = profileImageView.getDrawingCache();
            saveCertificateData(profileBitmap);
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnCapture.setOnClickListener(v -> showImagePickerDialog());
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

    private void saveCertificateData(Bitmap profileImageBitmap) {
        String cername = et_cername.getText().toString();
        String session = et_session.getText().toString();
        String organization = et_origanization.getText().toString();
        String des = et_des.getText().toString();
        String school = et_school.getText().toString();

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
            et_origanization.setError("Please enter organization");
            et_origanization.requestFocus();
            return;
        }

        if (des.isEmpty()) {
            et_des.setError("Please enter description");
            et_des.requestFocus();
            return;
        }

        if (school.isEmpty()) {
            et_school.setError("Please enter school");
            et_school.requestFocus();
            return;
        }

        // Kiểm tra nếu ảnh chưa được chọn
        if (profileImageBitmap == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }
        String profileImageBase64 = encodeImageToBase64(profileImageBitmap);

        Map<String, Object> certificate = new HashMap<>();
        certificate.put("cername", cername);
        certificate.put("session", session);
        certificate.put("organization", organization);
        certificate.put("des", des);
        certificate.put("school", school);
        certificate.put("dateCreated", new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        certificate.put("profileImageBase64", profileImageBase64);

        // Tìm tài liệu sinh viên dựa trên studentId
        db.collection("students")
                .whereEqualTo("id", studentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Thêm chứng chỉ vào subcollection "certificates" của tài liệu sinh viên
                        db.collection("students")
                                .document(documentId)
                                .collection("certificates")
                                .add(certificate)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Lưu chứng chỉ thành công!", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi khi lưu chứng chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Không tìm thấy sinh viên với studentId này:" + studentId, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tìm sinh viên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void loadStudentInfo(String studentId) {
        db.collection("students")
                .whereEqualTo("id", studentId) // Giả sử thuộc tính id là userId
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Lấy tài liệu đầu tiên tìm được
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        String name = documentSnapshot.getString("name");
                        String profileImageBase64 = documentSnapshot.getString("profileImageBase64");


                        et_name.setText(name);
                        et_studentId.setText(studentId);

                        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                            byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                            Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            profileImageView.setImageBitmap(profileImageBitmap);
                        }
                    } else {
                        Log.d("FirestoreData", "Không tìm thấy người dùng với id: " + studentId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreData", "Lỗi khi lấy dữ liệu", e);
                });
    }
}