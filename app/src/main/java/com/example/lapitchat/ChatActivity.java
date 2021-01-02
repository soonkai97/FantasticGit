package com.example.lapitchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private Toolbar mChatToolbar;
    private DatabaseReference mRoofRef;
    private TextView mTitleView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private ImageButton mChatAddBtn, mChatSendBtn;
    private EditText mChatMessageView;
    private RecyclerView mMessagesList;
    private final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private static final int TOTAL_ITEMS_TO_LOAD =10;
    private int mCurrentPage = 1;
    private SwipeRefreshLayout mRefreshLayout;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStorage;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    Uri image_rui = null;
    private static final int FILE_CODE = 438;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRoofRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatUser= getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");

        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        //-----------Custom Action Bar Items -----------

        mTitleView = findViewById(R.id.custom_bar_title);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);
        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);

        mAdapter = new MessageAdapter(messageList,ChatActivity.this);

        mMessagesList = findViewById(R.id.messages_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        loadMessages();

        mTitleView.setText(userName);

        mRoofRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String image = snapshot.child("image").getValue().toString();
                Picasso.get().load(image).into(mProfileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mRoofRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser))
                {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("timestamp",ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" +mCurrentUserId + "/"+ mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId,chatAddMap);

                    mRoofRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference ref) {

                            if (databaseError!=null)
                            {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }
        });

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] items = {"Camera", "Gallery", "Files", "Location", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (items[which].equals("Camera")) {
                            ContentValues cv = new ContentValues();
                            image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                            Intent cameraintent = new Intent();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
                            startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);


                        } else if (items[which].equals("Gallery")) {
                            Intent galleryIntent = new Intent();
                            galleryIntent.setType("image/*");
                            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_PICK);
                        } else if (items[which].equals("Files")){
                            Intent fileIntent = new Intent();
                            fileIntent.setAction(Intent.ACTION_GET_CONTENT);
                            fileIntent.setType("application/pdf");
                            startActivityForResult(fileIntent.createChooser(fileIntent,"Select File to Upload"),FILE_CODE);
                        }

                        else if (items[which].equals("Location")) {
                            GpsTracker gt = new GpsTracker(getApplicationContext());
                            Location l = gt.getLocation();
                            if (l == null) {
                                Log.d("state","allow acess");
                            } else {
                                double lat = l.getLatitude();
                                double lon = l.getLongitude();

                                DatabaseReference user_message_push = mRoofRef.child("message").child(mCurrentUserId).child(mChatUser).push();
                                String push_id = user_message_push.getKey();

                                String current_user_ref = "message/" + mCurrentUserId + "/" +mChatUser;
                                String chat_user_ref = "message/" + mChatUser + "/" +mCurrentUserId;
                                Map messageMap = new HashMap();

                                messageMap.put("message", String.valueOf(lat)+","+String.valueOf(lon) );
                                messageMap.put("time",ServerValue.TIMESTAMP);
                                messageMap.put("type", "location");
                                messageMap.put("from",mCurrentUserId);

                                Map messageUserMap = new HashMap();
                                messageUserMap.put(current_user_ref+ "/" + push_id, messageMap);
                                messageUserMap.put(chat_user_ref + "/" + push_id,messageMap);

                                mChatMessageView.setText("");

                                mRoofRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if (error != null){
                                            Log.d("CHAT_LOG", error.getMessage().toString());
                                        }
                                    }
                                });
                            }
                        }
                        else {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
            }
        });
    }

    private void sendCameraMessage() throws IOException {
        final ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
        progressDialog.setTitle("sending image from camera");
        progressDialog.show();

        DatabaseReference user_message_push = mRoofRef.child("message").child(mCurrentUserId).child(mChatUser).push();
        final String push_id = user_message_push.getKey();
        String fileNameAndPath = "message_images/"+ push_id + ".jpg";
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_rui);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Task<Uri>uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                String downloadURI = uriTask.getResult().toString();
                if (uriTask.isSuccessful())
                {

                    final String current_user_ref = "message/" + mCurrentUserId + "/" + mChatUser;
                    final String chat_user_ref = "message/" + mChatUser + "/" + mCurrentUserId;

                    Map messageMap = new HashMap();
                    messageMap.put("message",downloadURI);
                    messageMap.put("type","image");
                    messageMap.put("time",ServerValue.TIMESTAMP);
                    messageMap.put("from",mCurrentUserId);

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref+ "/" + push_id, messageMap);
                    messageUserMap.put(chat_user_ref + "/" + push_id,messageMap);

                    mChatMessageView.setText("");

                    mRoofRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null){
                                Log.d("CHAT_LOG", error.getMessage().toString());
                            }
                        }
                    });
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }


    private void loadMoreMessages(){
        DatabaseReference messageRef = mRoofRef.child("message").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                String messageKey = snapshot.getKey();
                if (!mPrevKey.equals(messageKey)){
                    messageList.add(itemPos++,message);
                }
                else {
                    mPrevKey = mLastKey;
                }
                if (itemPos == 1){

                    mLastKey = messageKey;
                }

                Log.d("TOTALKEYS","Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);
                mAdapter.notifyDataSetChanged();

                //mMessagesList.scrollToPosition(messageList.size() - 1);
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void loadMessages() {
        DatabaseReference messageRef = mRoofRef.child("message").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Message message = snapshot.getValue(Message.class);
                itemPos++;
                if (itemPos == 1){
                    String messageKey = snapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }
                messageList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messageList.size() - 1);
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage() {

        String message = mChatMessageView.getText().toString();

        if(!TextUtils.isEmpty(message)){

            String current_user_ref = "message/" + mCurrentUserId + "/" +mChatUser;
            String chat_user_ref = "message/" + mChatUser + "/" +mCurrentUserId;

            DatabaseReference user_message_push = mRoofRef.child("message").child(mCurrentUserId).child(mChatUser).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref+ "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id,messageMap);

            mChatMessageView.setText("");

            mRoofRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    if (error != null){
                        Log.d("CHAT_LOG", error.getMessage().toString());
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            final String current_user_ref = "message/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "message/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRoofRef.child("message").child(mCurrentUserId).child(mChatUser).push();
            final String push_id = user_message_push.getKey();

            StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){
                       // final String download_url = task.getResult().getStorage().getDownloadUrl().toString();
                        final Task <Uri> firebaseUri = task.getResult().getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String download_url = uri.toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message",download_url);
                        messageMap.put("type","image");
                        messageMap.put("time",ServerValue.TIMESTAMP);
                        messageMap.put("from",mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref+ "/" + push_id,messageMap);
                        messageUserMap.put(chat_user_ref+ "/" + push_id,messageMap);

                        mChatMessageView.setText("");

                        mRoofRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error != null) {

                                    Log.d ("CHAT_LOG",error.getMessage().toString());

                                }
                            }
                        });
                            }
                        });
                    }
                }
            });

        }
        else if (requestCode == IMAGE_PICK_CAMERA_CODE)
        {
            try {
                sendCameraMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == FILE_CODE && resultCode == RESULT_OK){
            Uri fileUri = data.getData();

            final String current_user_ref = "message/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "message/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRoofRef.child("message").child(mCurrentUserId).child(mChatUser).push();
            final String push_id = user_message_push.getKey();

            StorageReference filepath = mImageStorage.child("message_files").child(push_id + ".pdf");

            filepath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){
                        // final String download_url = task.getResult().getStorage().getDownloadUrl().toString();
                        final Task <Uri> firebaseUri = task.getResult().getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String download_url = uri.toString();

                                Map messageMap = new HashMap();
                                messageMap.put("message",download_url);
                                messageMap.put("type","file");
                                messageMap.put("time",ServerValue.TIMESTAMP);
                                messageMap.put("from",mCurrentUserId);

                                Map messageUserMap = new HashMap();
                                messageUserMap.put(current_user_ref+ "/" + push_id,messageMap);
                                messageUserMap.put(chat_user_ref+ "/" + push_id,messageMap);

                                mChatMessageView.setText("");

                                mRoofRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if (error != null) {

                                            Log.d ("CHAT_LOG",error.getMessage().toString());

                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            });

        }
    }
}