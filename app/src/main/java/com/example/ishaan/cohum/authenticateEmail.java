package com.example.ishaan.cohum;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class authenticateEmail extends FragmentActivity {
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate_email);
        Log.e("TAG", "authenticateEmail called.");
        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading.");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.show();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.reload();
        Intent intent = getIntent();
        if (intent.hasExtra("goog")) {
            Log.e("TAG", "authenticateEmail goog called.");
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!(dataSnapshot.exists())) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        startActivity(new Intent(authenticateEmail.this, userDetails.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    } else {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        notGoogSignin(user);
                    }
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        //Toast.makeText(this, user.getDisplayName(), Toast.LENGTH_SHORT).show();//TestToast//AWESOME NAME THO
        else if (intent.hasExtra("exit")) {
            Log.e("TAG", "authenticateEmail exit called.");
            notGoogSignin(user);
        } else {
            verifyEmail(user);
        }
    }

    private void verifyEmail(FirebaseUser user) {
        user.reload();
        Log.e("TAG", "verifyEmail func called.");
        if (user.isEmailVerified()) {
            Log.e("TAG", "email verified.");
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            finish();
            //Toast.makeText(this, "Email Verified", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        } else {
            Toast.makeText(this, "Verify email first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(authenticateEmail.this, signinActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("unauthentic", 0));
        }
    }

    private void notGoogSignin(FirebaseUser user) {
        user.reload();
        Log.e("TAG", "notGoogSignin called.");
        if (user.isEmailVerified()) {
            Log.e("TAG", "email verified.");
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            finish();
            //Toast.makeText(this, "Email Verified", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        } else {
            //finish();

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            user.reload();
            finish();

            Toast.makeText(this, "Verify email first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(authenticateEmail.this, signinActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("unauthentic", 0));
        }
    }
}
