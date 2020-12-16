package com.example.lapitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneVerifyActivity extends AppCompatActivity {

    private Button SendOtp, VerifyOtp;
    private EditText GetPhoneNumber, GetOtp;
    FirebaseAuth mAuth;
    private String CodeSent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verify);
        SendOtp = findViewById(R.id.sentOtp);
        VerifyOtp = findViewById(R.id.verifyOtp);
        GetPhoneNumber = findViewById(R.id.getPhoneNumber);
        GetOtp = findViewById(R.id.getOtp);
        mAuth = FirebaseAuth.getInstance();
        SendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });

        VerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyOtp();
            }
        });
    }

    private void verifyOtp() {
        String code = GetOtp.getText().toString().trim();
        if (code.isEmpty()) {
            GetOtp.setError("Enter OTP");
            GetOtp.requestFocus();
            return;
        } else {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(CodeSent, code);
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(PhoneVerifyActivity.this,"Your Phone Number is Verified",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PhoneVerifyActivity.this,LoginActivity.class);
                            startActivity(intent);

                        } else {
                           if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                               Toast.makeText(PhoneVerifyActivity.this,"Your Phone Number is Failed Verified",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void sendVerificationCode() {
        String Phone = GetPhoneNumber.getText().toString().trim();
        if (Phone.isEmpty()) {
            GetPhoneNumber.setError("Please Enter Phone Number");
            GetPhoneNumber.requestFocus();
            return;
        } else if (Phone.length() < 9 || Phone.length() > 10) {
            GetPhoneNumber.setError("Please Enter Correct Number");
            GetPhoneNumber.requestFocus();
            return;
        } else {

            String phoneNumber = "+6" + Phone;
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phoneNumber)       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(this)                 // Activity (for callback binding)
                            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);

        }
    }

        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(PhoneVerifyActivity.this,"Sent",Toast.LENGTH_LONG).show();
                CodeSent = s;
            }
        };

}