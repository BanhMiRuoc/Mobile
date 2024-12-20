package com.example.lab03;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;



public class DisplayNextActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.lab03.MESSAGE";

    private TextView textView;
    private EditText inputText;
    private Button btnLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.view);
        inputText = findViewById(R.id.input_text);
        btnLogin  = findViewById(R.id.login);

        Intent intent = getIntent();
        String email = intent.getStringExtra(com.example.lab03.MainActivity_2.EXTRA_MESSAGE);
        String text = "Xin chào, " + email + ". Vui lòng nhập tên";
        String hint = "Nhập tên";
        String btn = "LƯU VÀ THOÁT";
        textView.setText(text);
        inputText.setHint(hint);
        btnLogin.setText(btn);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = inputText.getText().toString().trim();
                Intent intent = new Intent();
                intent.putExtra("Name", name);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
