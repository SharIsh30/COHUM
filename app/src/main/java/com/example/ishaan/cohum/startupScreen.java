package com.example.ishaan.cohum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class startupScreen extends AppCompatActivity {
    private View mLoadingView;
    private int mShortAnimationDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mLoadingView = findViewById(R.id.imageView);
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);

        final Handler handler = new Handler();
        final Handler handler1 = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mLoadingView.animate()
                        .alpha(0f)
                        .setDuration(mShortAnimationDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mLoadingView.setVisibility(View.GONE);
                            }
                        });
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(startupScreen.this, loginMainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }
                }, 550);
            }
        }, 1000);


    }

    public void submitFunc(View view) {
        Toast.makeText(this, "Submit successful", Toast.LENGTH_SHORT).show();
    }
}