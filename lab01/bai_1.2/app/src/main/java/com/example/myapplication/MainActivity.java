package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private CheckBox rememberMeCheckBox;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        rememberMeCheckBox = findViewById(R.id.remember_me);


        // Handle "Remember me" checkbox
        rememberMeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Tài khoản của bạn sẽ được ghi nhớ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bạn sẽ cần phải đăng nhập trong các lần tiếp theo", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up login button click listener
        loginButton.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // Check if username or password is empty
            if (username.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên đăng nhập", Toast.LENGTH_SHORT).show();
                usernameEditText.requestFocus(); // Focus on username field
                return;
            } else if (password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                passwordEditText.requestFocus(); // Focus on password field
                return;
            }

            // Simple validation
            if (username.equals("admin") && password.equals("123456")) {
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();;
                finish();

            } else {
                Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                passwordEditText.requestFocus(); // Focus on password field
            }
        });
    }
}