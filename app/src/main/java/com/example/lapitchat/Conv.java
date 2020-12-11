package com.example.lapitchat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Conv{

    public long timestamp;

    public Conv()
    {

    }
    public Conv (long timestamp)
    {
        this.timestamp = timestamp;
    }

    public void setTimestamp (long timestamp)
    {
        this.timestamp = timestamp;
    }

    public long getTimestamp()
    {
        return timestamp;
    }


}