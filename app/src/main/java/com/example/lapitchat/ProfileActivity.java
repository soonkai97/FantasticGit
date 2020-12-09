package com.example.lapitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private FirebaseUser mCurrent_user;
    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mProfileImage = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.profile_displayName);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriendsCount = findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = findViewById(R.id.profile_send_req_button);

        mCurrent_state = "not_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User data");
        mProgressDialog.setMessage("Please wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();



        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                mProgressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrent_state.equals("not_friends"))
                {
                    mFriendRequestDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                mFriendRequestDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type").setValue("recieved").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ProfileActivity.this, " Request Sent Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else{
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}