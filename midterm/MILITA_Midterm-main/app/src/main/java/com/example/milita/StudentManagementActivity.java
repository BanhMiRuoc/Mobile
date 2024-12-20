package com.example.milita;

import static android.os.Build.VERSION.SDK_INT;

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
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.opencsv.CSVWriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StudentManagementActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ADD_STUDENT = 10;
    private RecyclerView studentRecyclerView;
    private StudentAdapter studentAdapter;
    private List<Student> studentList;
    private CheckBox[] checkBoxes;
    private Button btnDelete, btnAdd, btnFilter, btnMore, btnListStudent, btnHome, btnProfile, btnLogout;
    private CheckBox chkCheckAll;
    private FirebaseFirestore db;
    private EditText search_input;
    private String userRole;
    private LinearLayout menu_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_management);

        db = FirebaseFirestore.getInstance();
        studentRecyclerView = findViewById(R.id.recyclerView);
        studentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnDelete = findViewById(R.id.btnDelete);
        btnAdd = findViewById(R.id.btnAdd);
        btnMore = findViewById(R.id.btnMore);
        btnListStudent = findViewById(R.id.btnListStudent);
        btnHome = findViewById(R.id.btnHome);
        btnLogout = findViewById(R.id.btnLogout);
        btnProfile = findViewById(R.id.btnProfile);
        chkCheckAll = findViewById(R.id.chkCheckAll);
        btnFilter = findViewById(R.id.btnFilter);
        search_input = findViewById(R.id.search_input);
        menu_layout = findViewById(R.id.menu_layout);
        String role = getIntent().getStringExtra("role");
        String currentUserEmail = getIntent().getStringExtra("currentUserEmail");

        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(this, studentList, role, currentUserEmail);
        studentRecyclerView.setAdapter(studentAdapter);
        //check quyền ng dùng
        if ("Employee".equals(role)) {
            // Ẩn các chức năng mà nhân viên không thể làm
            btnAdd.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            btnMore.setVisibility(View.GONE);
//            chkCheckAll.setVisibility(View.GONE);
            btnHome.setVisibility(View.GONE);
            Toast.makeText(this, "Employee role: View-only access", Toast.LENGTH_SHORT).show();
        } else if ("Manager".equals(role)) {
            menu_layout.setVisibility(View.GONE);
        }
        // Lấy dữ liệu từ Firestore
        loadStudentDataFromFirestore();

        btnListStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StudentManagementActivity.this, "Currently on the user page", Toast.LENGTH_SHORT).show();
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentManagementActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentManagementActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                // Kết thúc Activity hiện tại
                finish();
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentManagementActivity.this, EditUserActivity.class);
                intent.putExtra("role", role);
                intent.putExtra("currentUserEmail", currentUserEmail);
                startActivityForResult(intent, 21);
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentManagementActivity.this, AddStudentActivity.class);
                startActivityForResult(intent, 3);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (studentAdapter.getSelectedItemsCount() > 0) {
                    showDeleteConfirmationDialog();
                } else {
                    Toast.makeText(StudentManagementActivity.this, "Please select at least one student to delete.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        chkCheckAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            studentAdapter.selectAllItems(isChecked);
        });

        btnFilter.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(this, btnFilter);
            popupMenu.getMenuInflater().inflate(R.menu.filter_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.filter_name_asc) {
                    sortStudentListByName(true);
                } else if (id == R.id.filter_name_desc) {
                    sortStudentListByName(false);
                } else if (id == R.id.filter_id_asc) {
                    sortStudentListById(true);
                } else if (id == R.id.filter_id_desc) {
                    sortStudentListById(false);
                }
                return true;
            });

            popupMenu.show();
        });

        search_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase();
                filterStudentList(query);
            }
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
                        startActivityForResult(Intent.createChooser(intent,"Select File "),101);
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
                .setTitle("Choose type of export")
                .setMessage("Are you want to export Excel or CSV?")
                .setCancelable(false)  // Không cho phép đóng dialog khi nhấn ngoài
                .setPositiveButton("CSV", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        exportToCsvFromFirestore(StudentManagementActivity.this);
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

    private void loadStudentDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("students")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        studentList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String studentId = document.getString("id"); // Lấy ID từ Firestore
                            String name = document.getString("name");
                            String studentClass = document.getString("class");
                            String profileImageBase64 = document.getString("profileImageBase64");

                            // Chuyển Base64 thành Bitmap
                            Bitmap profileImageBitmap = null;
                            if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                                byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                                profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            }

                            // Tạo đối tượng student và truyền Bitmap vào nếu có
                            Student student = new Student(studentId, name, studentClass,  profileImageBitmap);
                            studentList.add(student);
                        }

                        // Kiểm tra nếu có dữ liệu trước khi cập nhật adapter
                        if (!studentList.isEmpty()) {
                            studentAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No student data found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error fetching data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    // Hiển thị dialog xác nhận trước khi xóa
    private void showDeleteConfirmationDialog() {
        // Tạo một AlertDialog để xác nhận hành động xóa
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm delete")
                .setMessage("Are you sure to delete students?")
                .setCancelable(false)  // Không cho phép đóng dialog khi nhấn ngoài
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Nếu người dùng xác nhận xóa, gọi phương thức xóa các mục đã chọn và xóa trong Firestore
                        studentAdapter.removeSelectedItems(db); // Truyền db vào
                        Toast.makeText(StudentManagementActivity.this, "Các mục đã được xóa.", Toast.LENGTH_SHORT).show();
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

    private void sortStudentListByName(boolean ascending) {
        if (ascending) {
            Collections.sort(studentList, (s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
        } else {
            Collections.sort(studentList, (s1, s2) -> s2.getName().compareToIgnoreCase(s1.getName()));
        }
        studentAdapter.notifyDataSetChanged();
    }

    private void sortStudentListById(boolean ascending) {
        if (ascending) {
            Collections.sort(studentList, Comparator.comparingInt(s -> Integer.parseInt(s.getId())));
        } else {
            Collections.sort(studentList, (s1, s2) -> Integer.parseInt(s2.getId()) - Integer.parseInt(s1.getId()));
        }
        studentAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3 && resultCode == RESULT_OK) {
            loadStudentDataFromFirestore();
        }
        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    String fileType = getContentResolver().getType(fileUri);
                    Toast.makeText(this, "tệp: " + fileType, Toast.LENGTH_SHORT).show();
                    // Kiểm tra MIME type của tệp
                    if ("text/comma-separated-values".equals(fileType)) {
                        importCsvFile(fileUri);
                        loadStudentDataFromFirestore();
                    } else if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(fileType)) {
                        importExcelFile(fileUri);
                        loadStudentDataFromFirestore();
                    } else {
                        Toast.makeText(this, "Vui lòng chọn tệp CSV hoặc Excel", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        if (requestCode == 21 && resultCode == RESULT_OK) {
            loadStudentDataFromFirestore();
        }
    }

    private void filterStudentList(String query) {
        List<Student> filteredList = new ArrayList<>();

        for (Student student : studentList) {
            if (student.getId().toLowerCase().contains(query) ||
                    student.getName().toLowerCase().contains(query) ||
                    student.getStudentClass().toLowerCase().contains(query)) {
                filteredList.add(student);
            }
        }

        studentAdapter.updateList(filteredList);
    }

    private void importCsvFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            List<Student> studentsToAdd = new ArrayList<>();
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 8) { // Đảm bảo đủ 8 thuộc tính
                    String name = data[0].trim();
                    String birthday = data[1].trim();
                    String email = data[2].trim();
                    String phone = data[3].trim();
                    String studentId = data[4].trim();
                    String studentClass = data[5].trim();
                    String faculty = data[6].trim();
                    String profileImageBase64 = data[7].trim();
                    Bitmap profileImageBitmap = decodeBase64ToBitmap(profileImageBase64);
                    studentsToAdd.add(new Student(studentId, name, birthday, email, phone, studentClass, faculty, profileImageBitmap));
                }
            }
            reader.close();
            // Tiến hành thêm sinh viên vào Firestore
            addStudentsToFirestore(studentsToAdd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importExcelFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            Workbook workbook = new XSSFWorkbook(inputStream); // Dùng Apache POI để đọc file Excel
            Sheet sheet = workbook.getSheetAt(0); // Lấy sheet đầu tiên
            Iterator<Row> rowIterator = sheet.iterator();
            List<Student> studentsToAdd = new ArrayList<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getRowNum() == 0) continue;
                Cell cell = row.getCell(0); // Giả sử cột đầu tiên là tên
                if (cell != null) {
                    String name = cell.getStringCellValue().trim();
                    String birthday = row.getCell(1).getStringCellValue().trim();
                    String email = row.getCell(2).getStringCellValue().trim();
                    String phone = row.getCell(3).getStringCellValue().trim();
                    String studentId = row.getCell(4).getStringCellValue().trim();
                    String studentClass = row.getCell(5).getStringCellValue().trim();
                    String faculty = row.getCell(6).getStringCellValue().trim();
                    String profileImageBase64 = row.getCell(7).getStringCellValue().trim();
                    Bitmap profileImageBitmap = decodeBase64ToBitmap(profileImageBase64);
                    studentsToAdd.add(new Student(studentId, name, birthday, email, phone, studentClass, faculty, profileImageBitmap));
                }
            }
            workbook.close();
            // Tiến hành thêm sinh viên vào Firestore
            addStudentsToFirestore(studentsToAdd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Bitmap decodeBase64ToBitmap(String base64) {
        if (base64 != null && !base64.isEmpty()) {
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        return null;
    }


    private void addStudentsToFirestore(List<Student> students) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (Student student : students) {
            // Chuyển đổi ảnh đại diện thành chuỗi Base64
            String profileImageBase64 = "";
            if (student.getProfileImage() != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                student.getProfileImage().compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                profileImageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            }

            // Tạo dữ liệu học viên dưới dạng Map để lưu vào Firestore
            Map<String, Object> studentData = new HashMap<>();
            studentData.put("id", student.getId());
            studentData.put("name", student.getName());
            studentData.put("birthday", student.getBirthday());
            studentData.put("email", student.getEmail());
            studentData.put("phone", student.getPhone());
            studentData.put("class", student.getStudentClass());
            studentData.put("faculty", student.getFaculty());
            studentData.put("profileImageBase64", profileImageBase64);

            // Thêm học viên vào bộ sưu tập "students"
            db.collection("students")
                    .add(studentData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Học viên đã được thêm vào Firestore", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi thêm học viên vào Firestore", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public void exportToCsvFromFirestore(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("students") // Thay đổi "students" nếu bạn dùng tên khác cho collection
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        List<Student> studentList = new ArrayList<>();

                        // Duyệt qua các tài liệu để tạo danh sách sinh viên
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String id = document.getString("id");
                            String name = document.getString("name");
                            String birthday = document.getString("birthday");
                            String studentClass = document.getString("class");
                            String faculty = document.getString("faculty");
                            String email = document.getString("email");
                            String phone = document.getString("phone");
                            String profileImageBase64 = document.getString("profileImageBase64");

                            Bitmap profileImageBitmap = null;
                            if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                                byte[] decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT);
                                profileImageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            }
                            // Thêm sinh viên vào danh sách
                            Student student = new Student(id, name, birthday, email, phone, studentClass, faculty, profileImageBitmap);
                            studentList.add(student);
                        }

                        // Sau khi có danh sách sinh viên, xuất ra file CSV
                        exportToCsv(studentList, context);
                    } else {
                        Toast.makeText(context, "Lỗi khi truy xuất dữ liệu từ Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void exportToCsv(List<Student> studentList, Context context) {
        File file = new File(context.getExternalFilesDir(null), "students.csv");

        try {
            FileWriter writer = new FileWriter(file);
            CSVWriter csvWriter = new CSVWriter(writer);

            // Viết tiêu đề
            String[] header = {"ID", "Name", "Birthday", "Email", "Phone", "Class", "Faculty", "Avatar"};
            csvWriter.writeNext(header);

            // Viết danh sách sinh viên
            for (Student student : studentList) {
                String[] studentData = {
                        student.getId(),
                        student.getName(),
                        student.getBirthday(),
                        student.getEmail(),
                        student.getPhone(),
                        student.getStudentClass(),
                        student.getFaculty(),
                        encodeImageToBase64(student.getProfileImage()),
                };
                csvWriter.writeNext(studentData);
            }

            csvWriter.close();
            Toast.makeText(context, "Xuất CSV thành công", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi khi xuất CSV", Toast.LENGTH_SHORT).show();
        }
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
}