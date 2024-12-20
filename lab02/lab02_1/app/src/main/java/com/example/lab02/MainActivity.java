package com.example.lab02;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private CheckBox rememberMe;
    private Button resetPW;
    private Button signIn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        EditText username = findViewById(R.id.username_edittext);
        EditText password = findViewById(R.id.pw_edittext);
        Button signIn = findViewById(R.id.sign_in_btn);
        Button resetPW = findViewById(R.id.reset_pw_btn);

        signIn.setOnClickListener(v -> {
            String username_s = username.getText().toString().trim();
            String password_s = password.getText().toString().trim();

            if (username_s.isEmpty() || password_s.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập username hoặc password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isPasswordValid(password_s)) {
                Toast.makeText(MainActivity.this, "Mật khẩu không đúng yêu cầu", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username_s.equals("admin") && password_s.equals("Admin1234")) {
                Toast.makeText(MainActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Tên đăng nhập hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
            }
        });

        resetPW.setOnClickListener(v -> {
            String username1 = username.getText().toString().trim();

            if (username1.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập username", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username1.equals("admin")) {
                Toast.makeText(MainActivity.this, "Reset mật khẩu thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Tên đăng nhập không chính xác", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private boolean isPasswordValid(String password) {
        if (password.length() < 6) {
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            }
        }

        return hasUpperCase && hasLowerCase;
    }
}
