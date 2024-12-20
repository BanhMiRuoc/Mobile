package com.example.sender;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo intent để gửi broadcast
                Intent intent = new Intent("com.example.sender.ACTION_SEND");
                intent.putExtra("message", "Hello from Ainz!");
                sendBroadcast(intent, "com.example.receiver.PERMISSION_SEND");

            }
        });
    }
}
