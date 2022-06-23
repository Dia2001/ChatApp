package com.example.chatappanroid.Service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.HashMap;
import java.util.Map;

public class FirebaseNotificationService extends FirebaseMessagingService {
    private String userID = FirebaseAuth.getInstance().getUid();
    public void onNewToken(String s) {
        sendRegistrationToServer(s);
        Log.d("TOKEN_IN_SERVICE", s);
        super.onNewToken(s);
    }
    private void sendRegistrationToServer(String token) {
        if (userID != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange( DataSnapshot snapshot) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("token", token);
                    reference.updateChildren(map);
                }
                @Override
                public void onCancelled( DatabaseError error) {

                }
            });
        }


    }
}
