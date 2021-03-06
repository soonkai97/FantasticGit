package com.example.lapitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> mMessageList;
    private DatabaseReference mUserDatabase;
    private Context mContext;
    public MessageAdapter(List<Message> mMessageList, Context mContext)
    {
        this.mMessageList = mMessageList;
        this.mContext = mContext;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(mContext).inflate(R.layout.message_single_layout, parent , false);
        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public ImageView messageImage,mapImage, fileImage;

        public MessageViewHolder(View view)
        {
            super(view);

            messageText = view.findViewById(R.id.message_text_layout);
            profileImage = view.findViewById(R.id.message_profile_layout);
            displayName = view.findViewById(R.id.name_text_layout);
            messageImage = view.findViewById(R.id.message_image_layout);
            mapImage = view.findViewById(R.id.message_map_layout);
            fileImage = view.findViewById(R.id.message_file_layout);
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {
        final Message c = mMessageList.get(i);
        String from_user = c.getFrom();
        String message_type = c.getType();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue().toString();
                String image = snapshot.child("thumb_image").getValue().toString();

                viewHolder.displayName.setText(name);

                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(message_type!=null && message_type.equals("text"))
        {
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageText.setVisibility(View.VISIBLE);
            viewHolder.messageImage.setVisibility(View.INVISIBLE);
            viewHolder.messageImage.setVisibility(View.GONE);
            viewHolder.mapImage.setVisibility(View.INVISIBLE);
            viewHolder.mapImage.setVisibility(View.GONE);
            viewHolder.fileImage.setVisibility(View.INVISIBLE);
            viewHolder.fileImage.setVisibility(View.GONE);

        }
        else if (message_type!=null && message_type.equals("image"))
        {
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            viewHolder.messageText.setVisibility(View.GONE);
            viewHolder.mapImage.setVisibility(View.INVISIBLE);
            viewHolder.mapImage.setVisibility(View.GONE);
            viewHolder.fileImage.setVisibility(View.INVISIBLE);
            viewHolder.fileImage.setVisibility(View.GONE);
            viewHolder.messageImage.setVisibility(View.VISIBLE);
            Picasso.get().load(c.getMessage()).placeholder(R.drawable.default_avatar).into(viewHolder.messageImage);

        }
        else if (message_type!=null && message_type.equals("location")){
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            viewHolder.messageText.setVisibility(View.GONE);
            viewHolder.messageImage.setVisibility(View.INVISIBLE);
            viewHolder.messageImage.setVisibility(View.GONE);
            viewHolder.fileImage.setVisibility(View.INVISIBLE);
            viewHolder.fileImage.setVisibility(View.GONE);
            viewHolder.mapImage.setVisibility(View.VISIBLE);
            viewHolder.mapImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri i = Uri.parse(String.valueOf("google.navigation:q=" +c.getMessage()));
                    Intent map = new Intent(Intent.ACTION_VIEW, i);
                    mContext.startActivity(map);
                }
            });

        }
        else if (message_type != null && message_type.equals("file")){
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            viewHolder.messageText.setVisibility(View.GONE);
            viewHolder.messageImage.setVisibility(View.INVISIBLE);
            viewHolder.messageImage.setVisibility(View.GONE);
            viewHolder.mapImage.setVisibility(View.INVISIBLE);
            viewHolder.mapImage.setVisibility(View.GONE);
            viewHolder.fileImage.setVisibility(View.VISIBLE);
            viewHolder.fileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent openintent = new Intent(Intent.ACTION_VIEW, Uri.parse(c.getMessage()));
                    viewHolder.fileImage.getContext().startActivity(openintent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}