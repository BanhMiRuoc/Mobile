package com.example.ex2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddActivity extends AppCompatActivity {
    EditText txtName, txtPlace, txtDate, txtTime;
    TextView tvName, tvPlace, tvDate, tvTime;
    TextView tvErrorName, tvErrorPlace, tvErrorDate, tvErrorTime;
    String[] places = {"C201", "C202", "C203", "C204"};
    String selectedDate = "";
    String selectedTime = "";
    boolean isEditMode; // Biến xác định chế độ chỉnh sửa
    Event eventToEdit;

    // Lưu ngày và thời gian đã chọn trước đó
    int selectedYear, selectedMonth, selectedDay;
    int selectedHour, selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);

        txtName = findViewById(R.id.txtName);
        txtPlace = findViewById(R.id.txtPlace);
        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);
        tvName = findViewById(R.id.tvName);
        tvPlace = findViewById(R.id.tvPlace);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);

        Intent intent = getIntent();
        String eventName = intent.getStringExtra("eventName");
        String eventPlace = intent.getStringExtra("eventPlace");
        String eventDate = intent.getStringExtra("eventDate");
        String eventTime = intent.getStringExtra("eventTime");

        if (eventName != null) {
            txtName.setText(eventName);
            txtPlace.setText(eventPlace);
            txtDate.setText(eventDate);
            txtTime.setText(eventTime);
            isEditMode = true; // Chuyển sang chế độ chỉnh sửa
            eventToEdit = new Event(eventName, eventPlace, eventDate, eventTime, true); // Tạo đối tượng sự kiện
        }

        if (eventName != null) {
            txtName.setText(eventName);
        }
        if (eventPlace != null) {
            txtPlace.setText(eventPlace);
        }
        if (eventDate != null) {
            txtDate.setText(eventDate);
        }
        if (eventTime != null) {
            txtTime.setText(eventTime);
        }

        tvName.setVisibility(View.GONE);
        tvPlace.setVisibility(View.GONE);
        tvDate.setVisibility(View.GONE);
        tvTime.setVisibility(View.GONE);

        tvErrorName = findViewById(R.id.tvErrorName);
        tvErrorPlace = findViewById(R.id.tvErrorPlace);
        tvErrorDate = findViewById(R.id.tvErrorDate);
        tvErrorTime = findViewById(R.id.tvErrorTime);

        tvErrorName.setVisibility(View.GONE);
        tvErrorPlace.setVisibility(View.GONE);
        tvErrorDate.setVisibility(View.GONE);
        tvErrorTime.setVisibility(View.GONE);

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
        selectedMinute = calendar.get(Calendar.MINUTE);

        txtName.setOnFocusChangeListener((v, hasFocus) -> {
            tvName.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
        });

        txtPlace.setOnClickListener(v -> {
            tvPlace.setVisibility(View.VISIBLE);
            showPlaceDialog();
        });

        txtDate.setOnClickListener(v -> {
            tvDate.setVisibility(View.VISIBLE);
            showDateDialog();
        });

        txtTime.setOnClickListener(v -> {
            tvTime.setVisibility(View.VISIBLE);
            showTimeDialog();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            validateAndSaveEvent(); // Gọi hàm validate và lưu sự kiện
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPlaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn địa điểm")
                .setItems(places, (dialog, which) -> {
                    String selectedPlace = places[which];
                    txtPlace.setText(selectedPlace);
                });
        builder.create().show();
    }

    private void showDateDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = dayOfMonth;
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            txtDate.setText(selectedDate);
        }, selectedYear, selectedMonth, selectedDay);
        datePickerDialog.show();
    }

    private void showTimeDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
            selectedTime = hourOfDay + ":" + String.format("%02d", minute);
            txtTime.setText(selectedTime);
        }, selectedHour, selectedMinute, true);
        timePickerDialog.show();
    }

    private void validateAndSaveEvent() {
        String name = txtName.getText().toString().trim();
        String place = txtPlace.getText().toString().trim();
        String date = txtDate.getText().toString().trim();
        String time = txtTime.getText().toString().trim();

        // Biến để kiểm tra xem có lỗi hay không
        boolean hasError = false;

        // Kiểm tra trường tên sự kiện
        if (name.isEmpty()) {
            tvErrorName.setVisibility(View.VISIBLE);
            tvErrorName.setText("Please enter event name");
            txtName.requestFocus();
            hasError = true; // Đánh dấu là có lỗi
        } else {
            tvErrorName.setVisibility(View.GONE); // Ẩn thông báo lỗi khi có giá trị hợp lệ
        }

        // Nếu có lỗi ở trường name, không kiểm tra các trường khác
        if (!hasError) {
            // Kiểm tra địa điểm
            if (place.isEmpty()) {
                tvErrorPlace.setVisibility(View.VISIBLE);
                tvErrorPlace.setText("Please select a place");
                txtPlace.requestFocus();
                hasError = true; // Đánh dấu là có lỗi
            } else {
                tvErrorPlace.setVisibility(View.GONE); // Ẩn thông báo lỗi khi có giá trị hợp lệ
            }
        }

        // Nếu không có lỗi ở trường name và place
        if (!hasError) {
            // Kiểm tra ngày
            if (date.isEmpty()) {
                tvErrorDate.setVisibility(View.VISIBLE);
                tvErrorDate.setText("Please select a date");
                txtDate.requestFocus();
                hasError = true; // Đánh dấu là có lỗi
            } else {
                tvErrorDate.setVisibility(View.GONE); // Ẩn thông báo lỗi khi có giá trị hợp lệ
            }
        }

        // Nếu không có lỗi ở trường name, place và date
        if (!hasError) {
            // Kiểm tra giờ
            if (time.isEmpty()) {
                tvErrorTime.setVisibility(View.VISIBLE);
                tvErrorTime.setText("Please select a time");
                txtTime.requestFocus();
                hasError = true; // Đánh dấu là có lỗi
            } else {
                tvErrorTime.setVisibility(View.GONE); // Ẩn thông báo lỗi khi có giá trị hợp lệ
            }
        }

        if (!hasError) {
            // Nếu là chế độ chỉnh sửa, cập nhật thông tin của sự kiện hiện có
            if (isEditMode) {
                eventToEdit.setName(txtName.getText().toString().trim());
                eventToEdit.setPlace(txtPlace.getText().toString().trim());
                eventToEdit.setDate(txtDate.getText().toString().trim());
                eventToEdit.setTime(txtTime.getText().toString().trim());

                // Gửi sự kiện đã chỉnh sửa về MainActivity
                Intent intent = new Intent();
                intent.putExtra("updatedEvent", eventToEdit);
                setResult(RESULT_OK, intent);
            } else {
                // Nếu là chế độ thêm mới, tạo sự kiện mới
                Event newEvent = new Event(txtName.getText().toString().trim(),
                        txtPlace.getText().toString().trim(),
                        txtDate.getText().toString().trim(),
                        txtTime.getText().toString().trim(), true);

                // Gửi sự kiện mới về MainActivity
                Intent intent = new Intent();
                intent.putExtra("newEvent", newEvent);
                setResult(RESULT_OK, intent);
            }

            // Quay lại màn hình chính
            finish();
        }
    }


}
