package com.example.lab03_2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    private EditText editTextUrl;
    private Button openBrowserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUrl = findViewById(R.id.input);
        openBrowserButton = findViewById(R.id.btn);

        openBrowserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = editTextUrl.getText().toString();
                Intent intent = new Intent(MainActivity.this, chooseBrowser.class);
                intent.putExtra("URL", url);
                startActivity(intent);
            }
        });
    }
}
