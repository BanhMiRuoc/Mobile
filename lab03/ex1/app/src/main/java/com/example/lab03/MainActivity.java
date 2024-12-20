package com.example.lab03;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.lab03.MESSAGE";
    private EditText inputText;
    private Button btnLogin;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        inputText = findViewById(R.id.input_text);
        btnLogin = findViewById(R.id.login);
        textView = findViewById(R.id.view);

        // Set random followers and following count
        btnLogin.setOnClickListener(v -> {
            Intent intent;
            intent = new Intent(MainActivity.this, DisplayNextActivity.class);
            String email = inputText.getText().toString().trim();
            intent.putExtra(EXTRA_MESSAGE, email);
            startActivityForResult(intent, 1);
        });
    }
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1 && resultCode == RESULT_OK) {
                String name = data.getStringExtra("Name");
                textView.setText("Hẹn gặp lại");
                inputText.setText(name);
                btnLogin.setVisibility(View.GONE);
            }
        }
    }

