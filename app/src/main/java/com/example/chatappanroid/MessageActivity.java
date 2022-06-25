package com.example.chatappanroid;

import android.Manifest;
import android.animation.Animator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatappanroid.Adapter.MessageAdapter;
import com.example.chatappanroid.ModelApp.Chat;
import com.example.chatappanroid.ModelApp.User;
import com.example.chatappanroid.Service.Constants;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

// Hiệu ứng rung
@RequiresApi(api = Build.VERSION_CODES.O)
public class MessageActivity extends AppCompatActivity {
    //header
    CircleImageView profile_image;
    TextView username;

    FirebaseUser firebaseUser;
    DatabaseReference dReference, iReference;
    StorageReference sReference;

    Intent intent;

    //body
    ImageButton btn_send, btn_info, btn_send_file;
    LottieAnimationView typingAnimation;
    EditText txt_send;
    RelativeLayout input_layout;
    ImageView ic_camera, ic_gallery, ic_micro;
    LinearLayout input_text_layout;
    MediaRecorder mediaRecorder;
//Khi chúng tôi sử dụng, ValueEventListenerchúng tôi
// sẽ nhận được toàn bộ bộ sưu tập, nhưng trong trường hợp có bất kỳ thay đổi nào,
// chẳng hạn như một người dùng thay đổi nội dung của bài đăng, chúng tôi sẽ phải tải lại toàn bộ dữ liệu, có nghĩa là sẽ có nhiều byte gửi qua chuyển mạng đắt tiền.
// Một vấn đề khác là khi chúng ta sử dụng đa công cụ, và đây là ví dụ:
    ValueEventListener seenListener;
    MessageAdapter messageAdapter;
    List<Chat> chatList;

    private Uri imageUri;
    private Uri audioUri;
    private String extension;

    RecyclerView recyclerView;

    String userID;
    String chatID;

    StorageTask<UploadTask.TaskSnapshot> uploadImageTask, uploadAudioTask;
    private ArrayList<String> selectedImages;
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        recyclerView = findViewById(R.id.recycler_messages);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        txt_send = findViewById(R.id.txt_message);
        btn_info = findViewById(R.id.btn_info);
        btn_send = findViewById(R.id.btn_send);
        btn_send_file = findViewById(R.id.btn_send_file);
        input_layout = findViewById(R.id.input_layout);
        ic_camera = findViewById(R.id.ic_camera);
        ic_gallery = findViewById(R.id.ic_gallery);
        ic_micro = findViewById(R.id.ic_micro);
        input_text_layout = findViewById(R.id.input_text_layout);

        intent = getIntent();
        if (intent.hasExtra("chatID")) {
            chatID = intent.getStringExtra("chatID");
            userID = chatID;
       }
        //else {
//            userID = intent.getStringExtra("userID");
//            chatID = userID;
//        }

        // get receiver
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        dReference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        dReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.drawable.ic_baseline_person_24);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
                readMessage(firebaseUser.getUid(), userID, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        // info of user
        btn_info.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(MessageActivity.this, android.R.style.Theme_Black_NoTitleBar);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(R.layout.dialog_infomation);

            ImageView dialog_image = dialog.findViewById(R.id.profile_image);
            TextView dialog_username = dialog.findViewById(R.id.username);
            TextView dialog_name = dialog.findViewById(R.id.txtDesNameValue);
            TextView dialog_email = dialog.findViewById(R.id.txtDesEmailValue);
            TextView dialog_phone = dialog.findViewById(R.id.txtDesPhoneValue);
            ImageButton dialog_back = dialog.findViewById(R.id.btn_dialog_back);

            //information of userID
            iReference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
            iReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    dialog_username.setText(user.getUsername());
                    dialog_name.setText(user.getName());
                    dialog_phone.setText(user.getPhone());
                    dialog_email.setText(user.getEmail());
                    if (user.getImageURL() != null && user.getImageURL().equals("default")) {
                        dialog_image.setImageResource(R.drawable.ic_baseline_person_24);
                    } else {
                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(dialog_image);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
            dialog_back.setOnClickListener(v1 -> dialog.dismiss());
            dialog.show();
        });
        //send massage
        btn_send.setOnClickListener(v -> {
            String msg = txt_send.getText().toString();
            if (!msg.equals("")) {
                sendMessage(firebaseUser.getUid(), userID, msg, "text");
            } else {
                Toast.makeText(MessageActivity.this, "Empty message!!", Toast.LENGTH_SHORT).show();
            }
            txt_send.setText("");
        });
        seenMessage(userID);
    }
    private void status(String status) {
        dReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> mapStatus = new HashMap<>();
        mapStatus.put("status", status);
        dReference.updateChildren(mapStatus);
    }
    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (seenListener != null) {
            dReference.removeEventListener(seenListener);
        }
        status("offline");
    }
    private void readMessage(String currentID, String userID, String imageURL) {
        chatList = new ArrayList<>();
        dReference = FirebaseDatabase.getInstance().getReference("Chats");
        dReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getSender() != null && chat.getReceiver() != null) {
                        if ((chat.getReceiver().equals(currentID) && chat.getSender().equals(userID)) ||
                                (chat.getReceiver().equals(userID) && chat.getSender().equals(currentID))) {
                            chatList.add(chat);
                        }
                    }
                }
                messageAdapter = new MessageAdapter(MessageActivity.this, chatList, imageURL);
                messageAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendMessage(String sender, String receiver, String message, String type) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("seen", false);

        LocalDateTime current = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        String formatted = current.format(formatter);

        hashMap.put("timeSend", formatted);
        hashMap.put("timeSeen", "");
        hashMap.put("type", type);

        reference.child("Chats").push().setValue(hashMap);
        //add user to chat fragment
        DatabaseReference chatReference1 = FirebaseDatabase.getInstance().getReference("ChatsList").child(firebaseUser.getUid()).child(userID);
        DatabaseReference chatReference2 = FirebaseDatabase.getInstance().getReference("ChatsList").child(userID).child(firebaseUser.getUid());
        chatReference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child("id").exists()) {
                    chatReference1.child("id").setValue(userID);
                    chatReference1.child("lastMessageDate").setValue(formatted);
                    chatReference2.child("id").setValue(firebaseUser.getUid());
                    chatReference2.child("lastMessageDate").setValue(formatted);
                } else {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("lastMessageDate", formatted);
                    chatReference1.updateChildren(hashMap);
                    chatReference2.updateChildren(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void seenMessage(final String userID) {
        dReference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = dReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver() != null && chat.getSender() != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userID)) {
                            if (!chat.isSeen()) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("seen", true);
                                LocalDateTime current = LocalDateTime.now();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                                String formatted = current.format(formatter);
                                hashMap.put("timeSeen", formatted);
                                //Được sử dụng để có được một tham chiếu đến vị trí nguồn cho ảnh chụp nhanh này.
                                //https://firebase.google.com/docs/reference/android/com/google/firebase/database/DataSnapshot
                                dataSnapshot.getRef().updateChildren(hashMap);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
