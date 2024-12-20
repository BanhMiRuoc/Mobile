package com.example.lab03_4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private EditText nameEditText, phoneEditText, emailEditText, jobTitleEditText, addressEditText, websiteEditText;
    private ImageView avatarImageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Find views
        jobTitleEditText = findViewById(R.id.jobTitleEditText);
        addressEditText = findViewById(R.id.addressEditText);
        websiteEditText =  findViewById(R.id.websiteEditText);
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        avatarImageView = findViewById(R.id.avatarImageView);
        ImageButton cameraButton = findViewById(R.id.cameraButton);
        Button saveButton = findViewById(R.id.saveButton);

        // Get current profile info from MainActivity
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String phone = intent.getStringExtra("phone");
        String email = intent.getStringExtra("email");

        // Set current data into the EditText views
        nameEditText.setText(name);
        phoneEditText.setText(phone);
        emailEditText.setText(email);

        // Camera button to take a photo
        cameraButton.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        // Save button to return data to MainActivity
        saveButton.setOnClickListener(v -> {
            // Get the updated info from EditText views
            String updatedName = nameEditText.getText().toString();
            String updatedPhone = phoneEditText.getText().toString();
            String updatedEmail = emailEditText.getText().toString();

            // Send updated info back to MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("name", updatedName);
            resultIntent.putExtra("phone", updatedPhone);
            resultIntent.putExtra("email", updatedEmail);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    // Handle the result of the camera intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            assert extras != null;
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            avatarImageView.setImageBitmap(imageBitmap);
        }
    }
}
