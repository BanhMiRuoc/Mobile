package com.example.bai3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Button buttonSave;

    private EditText editTextName;
    private EditText editTextGender;
    private EditText editTextEmail;
    private EditText editTextAddress;
    private EditText editTextFavourite;
    private EditText editTextLove;
    private EditText editTextRating;
    private EditText editTextToeic;
    private EditText editTextNoti;

    private RadioButton genderFemale;
    private RadioButton genderMale;
    private RadioButton favouriteFemale;
    private RadioButton favouriteMale;
    private RadioButton favouriteBoth;
    private RadioGroup radioGroupGender;
    private RadioGroup radioGroupFavourite;

    private CheckBox football;
    private CheckBox swimming;
    private CheckBox jogging;

    private RatingBar star;
    private SeekBar point;
    private Switch notification;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize views
        buttonSave = findViewById(R.id.save_button);
        editTextName = findViewById(R.id.name);
        editTextGender = findViewById(R.id.gender);
        editTextEmail = findViewById(R.id.email);
        editTextAddress = findViewById(R.id.address);
        editTextFavourite = findViewById(R.id.favourite);
        editTextLove = findViewById(R.id.love);
        editTextRating = findViewById(R.id.rating_text);
        editTextToeic = findViewById(R.id.toeic_text);
        editTextNoti = findViewById(R.id.noti_text);
        genderFemale = findViewById(R.id.gender_female);
        genderMale = findViewById(R.id.gender_male);
        favouriteFemale = findViewById(R.id.favourite_female);
        favouriteMale = findViewById(R.id.favourite_male);
        favouriteBoth = findViewById(R.id.favourite_both);
        football = findViewById(R.id.football);
        swimming = findViewById(R.id.swimming);
        jogging = findViewById(R.id.jogging);
        star = findViewById(R.id.rating);
        point = findViewById(R.id.toeic);
        notification = findViewById(R.id.notification);
        radioGroupGender = findViewById(R.id.gender_choice);
        radioGroupFavourite = findViewById(R.id.favourite_choice);

        radioGroupGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.gender_female) {
                    editTextGender.setText("Nữ");
                }
                else if (checkedId == R.id.gender_male) {
                    editTextGender.setText("Nam");
                }
                else {
                    editTextGender.setText("");
                }
            }
        });
        radioGroupFavourite.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.favourite_female) {
                    editTextFavourite.setText("Nữ");
                }
                else if (checkedId == R.id.favourite_male) {
                    editTextFavourite.setText("Nam");
                }
                else if (checkedId == R.id.favourite_both){
                    editTextFavourite.setText("Cả hai");
                }
                else {
                    editTextFavourite.setText("");
                }
            }
        });
        star.setClickable(true);

        buttonSave.setOnClickListener(view -> {
//            public void onClick(View v) {

                String name = editTextName.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String address = editTextAddress.getText().toString().trim();
                int ratingBar = (int) star.getRating();
                int score = ((point.getProgress() + 1) * 5 + 5);

                if (name.isEmpty() || email.isEmpty() || address.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (radioGroupGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(MainActivity.this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (radioGroupFavourite.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(MainActivity.this, "Vui lòng chọn yêu thích", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!(football.isChecked() || swimming.isChecked() || jogging.isChecked())) {
                    Toast.makeText(MainActivity.this, "Vui lòng chọn sở thích", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ratingBar == 0) {
                    Toast.makeText(MainActivity.this, "Vui lòng chọn khả năng bơi", Toast.LENGTH_SHORT).show();
                }
                if (score < 5 || score > 990 || score % 5 != 0) {
                    Toast.makeText(MainActivity.this, "Điểm không hợp lệ, vui lòng nhập lại", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
                startActivity(intent);
//            }
        });
    }
}