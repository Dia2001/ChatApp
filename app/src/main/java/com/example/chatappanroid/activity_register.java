package com.example.chatappanroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatappanroid.Service.FirebaseNotificationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Objects;

public class  activity_register extends AppCompatActivity {
    EditText txtRegisUsername, txtRegisPassword, txtRegisEmail, txtReRegisPassword, txtRegisFullName, txtRegisPhone;
    Button btnRegister;
    CountryCodePicker ccp;
    String token;
    FirebaseAuth fAuth;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        txtRegisEmail = (EditText) findViewById(R.id.txtRegisEmail);
        txtRegisPassword = (EditText)findViewById(R.id.txtRegisPassword);
        txtReRegisPassword =(EditText) findViewById(R.id.txtReRegisPassword);
        txtRegisUsername =(EditText) findViewById(R.id.txtRegisUsername);
        txtRegisFullName = (EditText)findViewById(R.id.txtRegisFullName);
        txtRegisPhone =(EditText) findViewById(R.id.txtRegisPhone);
        ccp =(CountryCodePicker) findViewById(R.id.countryCodePicker);
        Toolbar toolbar =(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fAuth = FirebaseAuth.getInstance();
        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            String username = txtRegisUsername.getText().toString();
            String password = txtRegisPassword.getText().toString();
            String email = txtRegisEmail.getText().toString();
            String name = txtRegisFullName.getText().toString();
            String phone = txtRegisPhone.getText().toString();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(email) || TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(activity_register.this, "All fields must not be empty!!!", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(activity_register.this, "Password must be at least 6 characters!!!", Toast.LENGTH_SHORT).show();
            } else {
                String phoneNum = ccp.getSelectedCountryCodeWithPlus() + phone;
                register(username, password, email, name, phoneNum);
            }

        });
    }
    private void register(String username, String password, String email, String name, String phone) {
        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = fAuth.getCurrentUser();
                assert firebaseUser != null;
                String userID = firebaseUser.getUid();
                reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
                token = "";
                //set token
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task1 -> {
                            if (!task.isSuccessful()) {
                                Log.w("REGISTER_ACTIVITY", "Fetching FCM registration token failed", task.getException());
                                return;
                            }
                            // Get new FCM registration token
                            token = task1.getResult();
                            FirebaseNotificationService firebaseNotificationService = new FirebaseNotificationService();
                            assert token != null;
                            firebaseNotificationService.onNewToken(token);
                        });
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id", userID);
                hashMap.put("username", username);
                hashMap.put("imageURL", "default");
                hashMap.put("name", name);
                hashMap.put("email", email);
                hashMap.put("phone", phone);
                hashMap.put("status", "offline");
                hashMap.put("typing", "not");
                hashMap.put("token", token);
                reference.setValue(hashMap).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Intent intent = new Intent(activity_register.this, activity_login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Toast.makeText(activity_register.this, "Register successfully!!!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Log.d("ERROR", "");
                    }
                });
            } else {
                //Toast.makeText(RegisterActivity.this, "Register failed!!", Toast.LENGTH_SHORT).show();
                Log.w("TAG", "createUserWithEmail:failure", task.getException());
                Toast.makeText(activity_register.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}