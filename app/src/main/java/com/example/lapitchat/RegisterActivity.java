package com.example.lapitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextInputLayout mDisplayName,mEmail, mPassword;
    private Button mCreateBtn;
    private Toolbar mtoolbar;
    private DatabaseReference mDatabase;

    //Progress Dialog
    private ProgressDialog mRegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mtoolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);

        //Firebase Auth 
        mAuth = FirebaseAuth.getInstance();

        mDisplayName = (TextInputLayout) findViewById(R.id.reg_displayname);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mCreateBtn = (Button) findViewById(R.id.reg_createbutton);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){

                   mRegProgress.setTitle("Registering User");
                   mRegProgress.setMessage("Please wait while we create your account");
                   mRegProgress.setCanceledOnTouchOutside(false);
                   mRegProgress.show();

                    register_user(display_name,email,password);


                }

              }
        });
    }

    private void register_user(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();
                            //Pointing to root directory which is the Fantastic git directory with the child name of users with the child of userid
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", display_name);
                            userMap.put("status", "Hi there! I am using Fantastic Git App.");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");

                            mDatabase.setValue(userMap);

                            mRegProgress.dismiss();
                            Intent mainIntent = new Intent(RegisterActivity.this, PhoneVerifyActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        } else {

                            mRegProgress.hide();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}