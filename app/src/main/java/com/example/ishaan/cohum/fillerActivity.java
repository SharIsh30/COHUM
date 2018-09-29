package com.example.ishaan.cohum;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class fillerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filler_activity);
        Toast.makeText(this, "Module not ready  yet. Filler activity launched.", Toast.LENGTH_SHORT).show();
    }
}