package com.example.lab03_4;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText nameTextView, phoneTextView, emailTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        nameTextView = findViewById(R.id.nameEditText);
        phoneTextView = findViewById(R.id.phoneEditText);
        emailTextView = findViewById(R.id.emailEditText);

        // Set default profile info (you can fetch from database or shared preferences)
        nameTextView.setText("Edward Larry");
        phoneTextView.setText("(+84) 0909999999");
        emailTextView.setText("edward_larry@gmail.com");

        // Edit button logic
        ImageButton editButton = findViewById(R.id.fix_pen);
        editButton.setOnClickListener(v -> {
            // Navigate to EditProfileActivity
            Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
            // Send current profile data to EditProfileActivity
            intent.putExtra("name", nameTextView.getText().toString());
            intent.putExtra("phone", phoneTextView.getText().toString());
            intent.putExtra("email", emailTextView.getText().toString());
            startActivityForResult(intent, 1);
        });
    }

    // Handle data returned from EditProfileActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Update profile with new information
            String updatedName = data.getStringExtra("name");
            String updatedPhone = data.getStringExtra("phone");
            String updatedEmail = data.getStringExtra("email");

            nameTextView.setText(updatedName);
            phoneTextView.setText(updatedPhone);
            emailTextView.setText(updatedEmail);
        }
    }
}
