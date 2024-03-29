package com.example.chatappanroid.Adapter;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
//import com.example.mychatandroid.GlideApp;
import com.example.chatappanroid.ModelApp.Chat;
import com.example.chatappanroid.R;
//import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    final private Context mContext;
    final private List<Chat> chatList;
    final private String imageURL;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext, List<Chat> chatList, String imageURL) {
        this.mContext = mContext;
        this.chatList = chatList;
        this.imageURL = imageURL;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_LEFT) {
            // người gửi
            view = LayoutInflater.from(mContext).inflate(R.layout.chats_item_left, parent, false);
        } else {
            // Người nhận
            view = LayoutInflater.from(mContext).inflate(R.layout.chats_item_right, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        if (chat.getType() != null) {
            switch (chat.getType()) {
                case "text":
                    holder.show_message.setText(chat.getMessage());
                    holder.show_message.setVisibility(View.VISIBLE);
                    holder.show_message_image.setVisibility(View.GONE);
                    holder.show_message_video.setVisibility(View.GONE);
                    holder.show_message_audio.setVisibility(View.GONE);
                    break;
            }
        }
        if (imageURL.equals("default")) {
            holder.profile_image.setImageResource(R.drawable.ic_baseline_person_24);
        } else {
            Glide.with(mContext).load(imageURL).into(holder.profile_image);
        }

        //check for last message and show the time delivering or seen
        if (position == chatList.size() - 1) {
            if (chat.isSeen()) {
                String string = "Seen at";
                holder.txt_seen.setText(string);
                holder.txt_seen.setVisibility(View.VISIBLE);
                if (!chat.getTimeSeen().equals("")) {
                    holder.txt_time_seen.setText(chat.getTimeSeen());
                    holder.txt_time_seen.setVisibility(View.VISIBLE);
                }
            } else {
                String string = "Delivered at";
                holder.txt_seen.setText(string);
                holder.txt_seen.setVisibility(View.VISIBLE);
                if (!chat.getTimeSend().equals("")) {
                    holder.txt_time_seen.setText(chat.getTimeSend());
                    holder.txt_time_seen.setVisibility(View.VISIBLE);
                }
            }
        }

        //popup menu
        holder.show_message.setOnLongClickListener(v -> {
            showMenu(holder.show_message, chat.getTimeSend());
            return false;
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message, txt_seen, txt_time_seen;
        public ImageView profile_image, show_message_image;
        public ImageButton btn_play_message;
        public LinearLayout show_message_audio;
        public VideoView show_message_video;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            txt_time_seen = itemView.findViewById(R.id.txt_time_seen);
            show_message_image = itemView.findViewById(R.id.show_message_image);
            btn_play_message = itemView.findViewById(R.id.btn_play_message);
            show_message_audio = itemView.findViewById(R.id.show_message_audio);
            show_message_video = (VideoView) itemView.findViewById(R.id.show_message_video);
        }

    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        Chat chat = chatList.get(position);
        if (chat.getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    private void showMenu(TextView show_message, String timeSend) {
        PopupMenu popupMenu = new PopupMenu(mContext, show_message);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.delete_message:
                    deleteMessage(timeSend);
                    break;
                case R.id.info_message:
                    infoMessage(timeSend);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }
    private void infoMessage(String timeSend) {
        DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Chats");
        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getTimeSend().equals(timeSend)) {
                        Toast.makeText(mContext, "Time send: "+ timeSend, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deleteMessage(String timeSend) {
        DatabaseReference deleteChatReference = FirebaseDatabase.getInstance().getReference();
        Query query1 = deleteChatReference.child("Chats");
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat != null && chat.getSender() != null && chat.getReceiver() != null) {
                        if (chat.getTimeSend().equals(timeSend)) {
                            dataSnapshot.getRef().removeValue();
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