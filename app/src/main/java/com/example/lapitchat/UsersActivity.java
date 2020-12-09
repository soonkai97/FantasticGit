package com.example.lapitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mUserList;
    private DatabaseReference mUserDatabase;
    private FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter;
    private StorageReference mImageStorage;

    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserList = findViewById(R.id.users_list);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));


        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mUserDatabase, Users.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int i, @NonNull Users users) {
                usersViewHolder.setName(users.getName());
                usersViewHolder.setUserStatus(users.getStatus());
                usersViewHolder.setUserImage(users.getImage());
            }
        };
        mUserList.setAdapter(firebaseRecyclerAdapter);
    }
@Override
    protected void onStart() {
    super.onStart();
    firebaseRecyclerAdapter.startListening();
}
        @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }
    public class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public UsersViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }
        public void setName(String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setUserStatus(String status) {
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setUserImage(String image) {
            final CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            mImageStorage = FirebaseStorage.getInstance().getReference();
            mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
            String current_uid = mCurrentUser.getUid();

            StorageReference profileImage  = mImageStorage.child("profile_images").child(current_uid + ".jpg");
            profileImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(userImageView);
                }
            });
        }
    }}
