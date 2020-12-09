package com.example.lapitchat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {
    private TextView mDisplayID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        String user_id = getIntent().getStringExtra("user_id");
        mDisplayID = findViewById(R.id.profile_displayName);
        mDisplayID.setText(user_id);
    }
}