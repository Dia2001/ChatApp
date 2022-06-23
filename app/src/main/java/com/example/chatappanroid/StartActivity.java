package com.example.chatappanroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {
    Button login, register;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        login.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, activity_login.class);
            startActivity(intent);
        });
        register.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, activity_register.class);
            startActivity(intent);
        });
    }
}
