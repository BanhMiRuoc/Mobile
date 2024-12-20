package com.example.milita;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.opencsv.CSVWriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CertificateManagementActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ADD_certificate = 10;
    private RecyclerView certificateRecyclerView;
    private CertificateAdapter certificateAdapter;
    private List<Certificate> certificateList;
    private CheckBox[] checkBoxes;
    private Button btnDelete, btnAdd, btnMore, btnBack, btnListStudent, btnHome, btnProfile;
    private CheckBox chkCheckAll;
    private FirebaseFirestore db;
    private String studentId = "";
    private LinearLayout menu_layout;
    private String studentName = "";
    private String currentUserEmail = "";
    private TextView stuName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_certificate_management);

        db = FirebaseFirestore.getInstance();
        certificateRecyclerView = findViewById(R.id.recyclerView);
        certificateRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnDelete = findViewById(R.id.btnDelete);
        btnAdd = findViewById(R.id.btnAdd);
        btnMore = findViewById(R.id.btnMore);
        btnBack = findViewById(R.id.btnBack);
        btnListStudent = findViewById(R.id.btnListStudent);
        btnHome = findViewById(R.id.btnHome);
        btnProfile = findViewById(R.id.btnProfile);
        chkCheckAll = findViewById(R.id.chkCheckAll);
        menu_layout = findViewById(R.id.menu_layout);
        stuName = findViewById(R.id.stuName);
        String role = getIntent().getStringExtra("role");
        studentId = getIntent().getStringExtra("userId");
        currentUserEmail = getIntent().getStringExtra("currentUserEmail");
        studentName = getIntent().getStringExtra("studentName");

        certificateList = new ArrayList<>();
        certificateAdapter = new CertificateAdapter(this,this, certificateList, role, studentId, currentUserEmail);
        certificateRecyclerView.setAdapter(certificateAdapter);
        //check quyền ng dùng
        if ("Employee".equals(role)) {
            // Ẩn các chức năng mà nhân viên không thể làm
            btnAdd.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
//            chkCheckAll.setVisibility(View.GONE);
            btnHome.setVisibility(View.GONE);
            btnMore.setVisibility(View.GONE);
            Toast.makeText(this, "Employee role: View-only access", Toast.LENGTH_SHORT).show();
        } else if ("Manager".equals(role)) {
            btnHome.setVisibility(View.GONE);
            btnProfile.setVisibility(View.GONE);
        }
        // Lấy dữ liệu từ Firestore
        loadCertificateDataFromFirestore();

        btnListStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CertificateManagementActivity.this, StudentManagementActivity.class);
                intent.putExtra("role", role);
                intent.putExtra("currentUserEmail", currentUserEmail);
                startActivity(intent);
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CertificateManagementActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CertificateManagementActivity.this, EditUserActivity.class);
                intent.putExtra("role", role);
                intent.putExtra("currentUserEmail", currentUserEmail);
                startActivityForResult(intent, 20);
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CertificateManagementActivity.this, AddCertificateActivity.class);
                intent.putExtra("studentId", studentId);

                startActivityForResult(intent, 6);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (certificateAdapter.getSelectedItemsCount() > 0) {
                    showDeleteConfirmationDialog();
                } else {
                    Toast.makeText(CertificateManagementActivity.this, "Please select at least one student to delete.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        chkCheckAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            certificateAdapter.selectAllItems(isChecked);
        });

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu();
            }
        });

    }

    private void showPopupMenu() {
        // Tạo PopupMenu
        PopupMenu popupMenu = new PopupMenu(this, btnMore);
        popupMenu.getMenuInflater().inflate(R.menu.options_file_menu, popupMenu.getMenu());

        // Xử lý sự kiện khi chọn một mục trong menu
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_import) {
                if(SDK_INT >= Build.VERSION_CODES.R)
                {
                    if(Environment.isExternalStorageManager()){
                        //choosing csv file
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE,true);
                        startActivityForResult(Intent.createChooser(intent,"Select File "),102);
                    }
                    else{
                        //getting permission from user
                        Intent intent=new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri=Uri.fromParts("package",getPackageName(),null);
                        startActivity(intent);
                    }
                }

            } else if (id == R.id.action_export) {
                Toast.makeText(this, "Export được chọn", Toast.LENGTH_SHORT).show();
                showDialogExport();
            }
            return true;
        });

        // Hiển thị menu
        popupMenu.show();
    }

    private void showDialogExport() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm export list certificate")
                .setMessage("Are you sure to export list certificate?")
                .setCancelable(false)  // Không cho phép đóng dialog khi nhấn ngoài
                .setPositiveButton("Export", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        exportStudentCertificatesToCsv(CertificateManagementActivity.this);
                    }
                })
                .setNegativeButton("Hủy", null)  // Nếu người dùng hủy, không làm gì
                .create();

        // Lấy ra các button của dialog
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                // Lấy các button từ dialog
                Button btnDelete = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);

                btnDelete.setTextColor(getResources().getColor(R.color.blue)); // Màu xanh (thêm màu xanh vào resource)
                btnCancel.setTextColor(getResources().getColor(R.color.red)); // Màu đỏ (thêm màu đỏ vào resource)
            }
        });

        // Hiển thị dialog
        dialog.show();
    }

    private void loadCertificateDataFromFirestore() {
        stuName.setText(studentName);
        // Tìm tài liệu sinh viên dựa trên studentId
        db.collection("students")
                .whereEqualTo("id", studentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Lấy documentId của sinh viên đầu tiên tìm thấy
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Lấy subcollection certificates của tài liệu sinh viên
                        db.collection("students")
                                .document(documentId)
                                .collection("certificates")
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        certificateList.clear();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String id = document.getId(); // Lấy ID từ Firestore
                                            String name = document.getString("cername");
                                            String session = document.getString("session");
                                            String date = document.getString("dateCreated");
                                            String profileImageBase64 = document.getString("profileImageBase64");

                                            // Chuyển Base64 thành Bitmap
                                            Bitmap profileImageBitmap = null;
                                            if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                                                byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                                                profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                            }

                                            // Tạo đối tượng certificate và truyền Bitmap vào nếu có
                                            Certificate certificate = new Certificate(name, session, date, profileImageBitmap);
                                            certificate.setId(id);
                                            certificateList.add(certificate);
                                        }

                                        // Kiểm tra nếu có dữ liệu trước khi cập nhật adapter
                                        if (!certificateList.isEmpty()) {
                                            certificateAdapter.notifyDataSetChanged();
                                        } else {
                                            Toast.makeText(this, "Không có dữ liệu certificate nào.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(this, "Lỗi khi lấy dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Không tìm thấy sinh viên với studentId: " + studentId, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tìm sinh viên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    // Hiển thị dialog xác nhận trước khi xóa
    private void showDeleteConfirmationDialog() {
        // Tạo một AlertDialog để xác nhận hành động xóa
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm delete")
                .setMessage("Are you sure to delete certificates?")
                .setCancelable(false)  // Không cho phép đóng dialog khi nhấn ngoài
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Nếu người dùng xác nhận xóa, gọi phương thức xóa các mục đã chọn và xóa trong Firestore
                        certificateAdapter.removeSelectedItems(db, studentId); // Truyền db vào
                        chkCheckAll.setChecked(false);
                    }
                })
                .setNegativeButton("Hủy", null)  // Nếu người dùng hủy, không làm gì
                .create();

        // Lấy ra các button của dialog
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                // Lấy các button từ dialog
                Button btnDelete = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);

                btnDelete.setTextColor(getResources().getColor(R.color.blue)); // Màu xanh (thêm màu xanh vào resource)
                btnCancel.setTextColor(getResources().getColor(R.color.red)); // Màu đỏ (thêm màu đỏ vào resource)
            }
        });

        // Hiển thị dialog
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 6 && resultCode == RESULT_OK) {
            loadCertificateDataFromFirestore();
        }

        if (requestCode == 102 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    String fileType = getContentResolver().getType(fileUri);
                    Toast.makeText(this, "tệp: " + fileType, Toast.LENGTH_SHORT).show();
                    // Kiểm tra MIME type của tệp
                    if ("text/comma-separated-values".equals(fileType)) {
                        importCsvFile(fileUri);
                        loadCertificateDataFromFirestore();
                    } else if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(fileType)) {
                        importExcelFile(fileUri);
                        loadCertificateDataFromFirestore();
                    } else {
                        Toast.makeText(this, "Vui lòng chọn tệp CSV hoặc Excel", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        if (requestCode == 20 && resultCode == RESULT_OK) {
            studentId = getIntent().getStringExtra("studentId");
            currentUserEmail = getIntent().getStringExtra("currentUserEmail");
            studentName = getIntent().getStringExtra("studentName");
            loadCertificateDataFromFirestore();
        }
    }

    public void exportStudentCertificatesToCsv(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Truy xuất thông tin sinh viên từ Firestore
        db.collection("students")
                .whereEqualTo("id", studentId)
                .get()
                .addOnCompleteListener(studentTask -> {
                    if (studentTask.isSuccessful() && !studentTask.getResult().isEmpty()) {
                        DocumentSnapshot studentDoc = studentTask.getResult().getDocuments().get(0);

                        // Lấy thông tin sinh viên
                        String id = studentDoc.getString("id");
                        String name = studentDoc.getString("name");
                        String birthday = studentDoc.getString("birthday");
                        String studentClass = studentDoc.getString("class");
                        String faculty = studentDoc.getString("faculty");
                        String email = studentDoc.getString("email");
                        String phone = studentDoc.getString("phone");

                        // Lấy danh sách chứng chỉ của sinh viên
                        studentDoc.getReference().collection("certificates")
                                .get()
                                .addOnCompleteListener(certTask -> {
                                    if (certTask.isSuccessful()) {
                                        List<Certificate> certificateList = new ArrayList<>();
                                        for (QueryDocumentSnapshot certDoc : certTask.getResult()) {
                                            String certName = certDoc.getString("name");
                                            String certDate = certDoc.getString("dateCreated");
                                            String certDes = certDoc.getString("des");
                                            String certOrganization = certDoc.getString("organization");
                                            String certSchool = certDoc.getString("school");
                                            String certSession = certDoc.getString("session");
                                            String profileImageBase64 = certDoc.getString("profileImageBase64");

                                            Bitmap profileImageBitmap = null;
                                            if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                                                byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                                                profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                            }
                                            Certificate certificate = new Certificate(certName, certDate, certDes, certOrganization, certSchool, certSession, profileImageBitmap);
                                            certificateList.add(certificate);
                                        }

                                        // Export data to CSV
                                        exportToCsv(context, id, name, birthday, studentClass, faculty, email, phone, certificateList);
                                    } else {
                                        Toast.makeText(context, "Lỗi khi truy xuất chứng chỉ từ Firestore", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(context, "Lỗi khi truy xuất thông tin sinh viên từ Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void exportToCsv(Context context, String id, String name, String birthday, String studentClass, String faculty, String email, String phone, List<Certificate> certificateList) {
        File file = new File(context.getExternalFilesDir(null), "student_certificates.csv");

        try {
            FileWriter writer = new FileWriter(file);
            CSVWriter csvWriter = new CSVWriter(writer);

            // Write student information header
            String[] studentHeader = {"Student ID", "Name", "Birthday", "Class", "Faculty", "Email", "Phone"};
            csvWriter.writeNext(studentHeader);

            // Write student information row
            String[] studentData = {id, name, birthday, studentClass, faculty, email, phone};
            csvWriter.writeNext(studentData);

            // Write certificate information header
            String[] certHeader = {"Certificate Name", "Date Created", "Description", "Organization", "School", "Session"};
            csvWriter.writeNext(certHeader);

            // Write certificate details
            for (Certificate certificate : certificateList) {
                String[] certData = {
                        certificate.getName(),
                        certificate.getDateCreated(),
                        certificate.getDes(),
                        certificate.getOrganization(),
                        certificate.getSchool(),
                        certificate.getSession(),
                        encodeImageToBase64(certificate.getProfileImage()),
                };
                csvWriter.writeNext(certData);
            }

            csvWriter.close();
            Toast.makeText(context, "CSV export successful", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error exporting CSV", Toast.LENGTH_SHORT).show();
        }
    }

    private void importCsvFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            List<Certificate> certificatesToAdd = new ArrayList<>();
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 7) { // Đảm bảo đủ 7 thuộc tính
                    String name = data[0].trim();
                    String date = data[1].trim();
                    String des = data[2].trim();
                    String organization = data[3].trim();
                    String school = data[4].trim();
                    String session = data[5].trim();
                    String profileImageBase64 = data[6].trim();
                    Bitmap profileImageBitmap = decodeBase64ToBitmap(profileImageBase64);
                    certificatesToAdd.add(new Certificate(name, date, des, organization, school, session, profileImageBitmap));
                }
            }
            reader.close();
            // Tiến hành thêm sinh viên vào Firestore
            addCertificatesToFirestore(certificatesToAdd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importExcelFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            Workbook workbook = new XSSFWorkbook(inputStream); // Sử dụng Apache POI để đọc file Excel
            Sheet sheet = workbook.getSheetAt(0); // Lấy sheet đầu tiên
            Iterator<Row> rowIterator = sheet.iterator();
            List<Certificate> certificatesToAdd = new ArrayList<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getRowNum() == 0) continue; // Bỏ qua hàng tiêu đề
                Cell cell = row.getCell(0); // Giả sử cột đầu tiên là tên chứng chỉ
                if (cell != null) {
                    String name = cell.getStringCellValue().trim();
                    String dateCreated = row.getCell(1).getStringCellValue().trim();
                    String des = row.getCell(2).getStringCellValue().trim();
                    String organization = row.getCell(3).getStringCellValue().trim();
                    String school = row.getCell(4).getStringCellValue().trim();
                    String session = row.getCell(5).getStringCellValue().trim();
                    String profileImageBase64 = row.getCell(6).getStringCellValue().trim();

                    // Decode Base64 to Bitmap if profile image is available
                    Bitmap profileImageBitmap = null;
                    if (!profileImageBase64.isEmpty()) {
                        profileImageBitmap = decodeBase64ToBitmap(profileImageBase64);
                    }

                    // Tạo đối tượng Certificate và thêm vào danh sách
                    certificatesToAdd.add(new Certificate(name, dateCreated, des, organization, school, session, profileImageBitmap));
                }
            }
            workbook.close();

            // Thêm danh sách chứng chỉ vào Firestore
            addCertificatesToFirestore(certificatesToAdd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void addCertificatesToFirestore(List<Certificate> certificatesToAdd) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query Firestore to find the student document based on the studentId attribute
        db.collection("students")
                .whereEqualTo("id", studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Get the student document reference
                        DocumentSnapshot studentDoc = task.getResult().getDocuments().get(0);
                        for (Certificate certificate : certificatesToAdd) {
                            // Convert the profile image to a Base64 string for storage
                            String profileImageBase64 = encodeImageToBase64(certificate.getProfileImage());

                            // Prepare the certificate data
                            Map<String, Object> certData = new HashMap<>();
                            certData.put("cername", certificate.getName());
                            certData.put("dateCreated", certificate.getDateCreated());
                            certData.put("des", certificate.getDes());
                            certData.put("organization", certificate.getOrganization());
                            certData.put("school", certificate.getSchool());
                            certData.put("session", certificate.getSession());
                            certData.put("profileImageBase64", profileImageBase64);

                            // Add the certificate to the student's certificates sub-collection
                            studentDoc.getReference().collection("certificates").add(certData)
                                    .addOnSuccessListener(docRef -> {
                                        Log.d("Firestore", "Certificate added with ID: " + docRef.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("Firestore", "Error adding certificate", e);
                                    });
                        }
                    } else {
                        Log.w("Firestore", "Student not found or task failed.");
                    }
                });
    }

    public String encodeImageToBase64(Bitmap bitmap) {
        if (bitmap == null) {
            return ""; // Trả về chuỗi rỗng nếu không có ảnh
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream); // Bạn có thể giảm chất lượng nếu muốn
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private Bitmap decodeBase64ToBitmap(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}