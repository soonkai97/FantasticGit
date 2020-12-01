package com.example.lapitchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button mRegBtn;
    private Button mAccountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mRegBtn = (Button) findViewById(R.id.button);
        mAccountBtn = (Button) findViewById(R.id.button2);

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent reg_intent =new Intent (StartActivity.this, RegisterActivity.class);
                startActivity(reg_intent);

            }
        });

        mAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent acc_intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(acc_intent);
            }
        });
    }
}