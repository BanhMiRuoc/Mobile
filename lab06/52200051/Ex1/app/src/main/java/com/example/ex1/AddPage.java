package com.example.ex1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class AddPage extends AppCompatActivity {
    EditText edtName, edtMail, edtPhone;
    Button btnSave;
    TextView txtErrName, txtErrMail, txtErrPhone;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_page);
        txtErrName = findViewById(R.id.txtErrNameA);
        txtErrMail = findViewById(R.id.txtErrMailA);
        txtErrPhone = findViewById(R.id.txtErrPhoneA);

        edtName = findViewById(R.id.edtName);
        edtMail = findViewById(R.id.edtMail);
        edtPhone = findViewById(R.id.edtPhone);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack_AddPage);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = 0;
                if(edtName.getText().toString().equals("")){
                    txtErrName.setText("Name cannot be empty");
                    count++;
                } if (edtMail.getText().toString().equals("")) {
                    txtErrMail.setText("Mail cannot be empty");
                    count++;
                } else if (!edtMail.getText().toString().contains("@")) {
                    txtErrMail.setText("Missing '@'");
                    count++;
                }
                String phone = edtPhone.getText().toString().trim();
                if (phone.equals("")) {
                    txtErrPhone.setText("Phone cannot be empty");
                    count++;
                } else if (!phone.matches("\\d+")) {  // Kiểm tra xem có phải là chuỗi số không
                    txtErrPhone.setText("Phone must be digits only");
                    count++;
                } else if (phone.length() < 10 || edtPhone.length() > 15) {  // Kiểm tra độ dài của số điện thoại
                    txtErrPhone.setText("Phone must be between 10 to 15 digits");
                    count++;
                }
                if (count == 0){
                    Intent intent = new Intent();
                    intent.putExtra("name", edtName.getText().toString());
                    intent.putExtra("mail", edtMail.getText().toString());
                    intent.putExtra("phone", edtPhone.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }
}