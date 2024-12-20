package com.example.ex1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class UpdatePage extends AppCompatActivity {
    EditText edtName, edtMail, edtPhone, edtId;
    ImageView btnBack;
    Button btnSave;
    TextView txtErrId, txtErrName, txtErrMail, txtErrPhone;
    Integer id = 0;
    String name, mail, phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_page);
        Intent intent = getIntent();

        if (intent != null){
            id = intent.getIntExtra("edit_id", 0);
            name = intent.getStringExtra("edit_name");
            mail = intent.getStringExtra("edit_mail");
            phone = intent.getStringExtra("edit_phone");
        }
        txtErrId = findViewById(R.id.txtErrIdU);
        txtErrName = findViewById(R.id.txtErrNameU);
        txtErrMail = findViewById(R.id.txtErrMailU);
        txtErrPhone = findViewById(R.id.txtErrPhoneU);

        edtId = findViewById(R.id.edtIdU);
        edtName = findViewById(R.id.edtNameU);
        edtMail = findViewById(R.id.edtMailU);
        edtPhone = findViewById(R.id.edtPhoneU);

        btnBack = findViewById(R.id.imgBackU);
        btnSave = findViewById(R.id.btnSaveU);

        edtId.setEnabled(false);

        edtId.setText(id.toString());
        edtName.setText(name);
        edtMail.setText(mail);
        edtPhone.setText(phone);

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
                }else if (!edtMail.getText().toString().contains("@")) {
                    txtErrMail.setText("Missing '@'");
                    count++;
                } if (edtPhone.getText().toString().equals("")) {
                    txtErrPhone.setText("Phone cannot be empty");
                    count++;
                }
                if (count == 0){
                    Intent intent = new Intent();
                    intent.putExtra("idU", id.toString());
                    intent.putExtra("nameU", edtName.getText().toString());
                    intent.putExtra("mailU", edtMail.getText().toString());
                    intent.putExtra("phoneU", edtPhone.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }


            }
        });
    }
}