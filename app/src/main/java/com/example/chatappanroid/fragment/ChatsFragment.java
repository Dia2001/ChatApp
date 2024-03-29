package com.example.chatappanroid.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatappanroid.Adapter.UsersAdapter;
import com.example.chatappanroid.ModelApp.ChatsList;
import com.example.chatappanroid.ModelApp.User;
import com.example.chatappanroid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatsFragment extends Fragment{
    private RecyclerView recyclerView;
    EditText search;
    private UsersAdapter usersAdapter;
    private List<User> userList;
    FirebaseUser fUser;
    DatabaseReference reference;
    private  List<ChatsList> sortedUserList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        sortedUserList = new ArrayList<>();
        sortedUserList.clear();

        //get list user on contact
        reference = FirebaseDatabase.getInstance().getReference("ChatsList").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (userList != null) {
                    userList.clear();
                    sortedUserList.clear();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ChatsList chatsList = dataSnapshot.getValue(ChatsList.class);
                    sortedUserList.add(chatsList);
                }
                if (sortedUserList.size() > 1) {
                    //https://viblo.asia/p/java-collections-sap-xep-collections-naQZRgvdlvx
                    Collections.sort(sortedUserList, (o1, o2) -> {
                        if (o1.getLastMessageDate() != null && o2.getLastMessageDate() != null) {
                            String[] str1 = o1.getLastMessageDate().split("\\s");
                            String[] str2 = o2.getLastMessageDate().split("\\s");
                            if (o1.getLastMessageDate() == null || o2.getLastMessageDate() == null)
                                return 0;
                            if (str2[1].compareTo(str1[1]) == 0){
                                return str2[0].compareTo(str1[0]);
                            }
                            return str2[1].compareTo(str1[1]);
                        } else {
                            return 0;
                        }
                    });
                }
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //search user
        search = view.findViewById(R.id.search_message);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }
    private void search(String s) {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("b",fUser+"");
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (userList != null) {
                    userList.clear();
                }
                Log.d("a",snapshot+"");
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getId() != null) {

                        assert fUser != null;
                        for (ChatsList chatsList : sortedUserList) {
                            if (user.getUsername().contains(s)) {
                                if (!user.getId().equals((fUser.getUid())) && user.getId().equals(chatsList.getId()) ) {
                                    userList.add(user);
                                }
                            }
                        }
                    }
                }
                usersAdapter = new UsersAdapter(getContext(), userList, true);
                recyclerView.setAdapter(usersAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void readChats() {
        userList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (ChatsList chatsList : sortedUserList) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        assert user != null;
                        if (user.getId()!=null) {
                            if (user.getId().equals(chatsList.getId())) {
                                userList.add(user);
                            }
                        }
                    }
                }
                usersAdapter = new UsersAdapter(getContext(), userList, true, getFragmentManager());
                recyclerView.setAdapter(usersAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
