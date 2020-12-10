package com.example.lapitchat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Message{

    private String message, type;
    private long time;

    public Message (String message, long time, String type)
    {
        this.message = message;
        this.time = time;
        this.type = type;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message= message;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time= time;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type= type;
    }


    /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
    }*/
}