package com.example.chatappanroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;


public class activity_login extends AppCompatActivity {
    EditText txtEmail, txtPassword;
    Button btnLogin;
    TextView reset_password;
    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtEmail = findViewById(R.id.txtLoginEmail);
        txtPassword = findViewById(R.id.txtLoginPassword);

        fAuth = FirebaseAuth.getInstance();

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            String email = txtEmail.getText().toString();
            String password = txtPassword.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(activity_login.this, "All fields must not be empty!!!", Toast.LENGTH_SHORT).show();
            } else {
                login(email, password);
            }
        });
        reset_password = findViewById(R.id.reset_password);
        reset_password.setOnClickListener(v -> {
            startActivity(new Intent(activity_login.this, ResetPasswordActivity.class));
            finish();
        });

    }
    private void login(String email, String password) {
        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity_login.this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(activity_login.this, "Login Successful!!!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity_login.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(activity_login.this, "Email or password is incorrect!!!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

