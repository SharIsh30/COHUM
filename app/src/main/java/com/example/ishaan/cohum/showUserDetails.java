package com.example.ishaan.cohum;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class showUserDetails extends AppCompatActivity {

    private EditText name, email, number;
    private ImageView img;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private ProgressDialog progressDialog;
    private String nameString, phoneString, emailString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading.");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.show();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        name = (EditText) findViewById(R.id.u_name);
        email = (EditText) findViewById(R.id.u_email);
        number = (EditText) findViewById(R.id.u_phone);
        img = (ImageView) findViewById(R.id.editPicImgView);
        mDatabase.child("users").child(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals("name")) {
                    nameString = dataSnapshot.getValue().toString();
                    name.setText(nameString);
                }
                if (dataSnapshot.getKey().equals("email")) {
                    emailString = dataSnapshot.getValue().toString();
                    email.setText(emailString);
                }
                if (dataSnapshot.getKey().equals("phone")) {
                    phoneString = dataSnapshot.getValue().toString();
                    number.setText(phoneString);
                }
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Picasso.with(this).load(user.getPhotoUrl()).into(img);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }

    public void submit(View view) {
        final String newUserEmail = email.getText().toString().trim();
        String newUserName = name.getText().toString().trim();
        String newMobileNumber = number.getText().toString().trim();
        Toast.makeText(this, nameString + " " + emailString + " " + phoneString, Toast.LENGTH_SHORT).show();
        final AuthCredential credential = EmailAuthProvider
                .getCredential(emailString, "abc2018");

        if (!(newUserEmail.equals(emailString))) {
            //Toast.makeText(this, "Email changed", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("After changing your primary email address, you will have to authenticate your email and login again.\nContinue?");
            alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mDatabase.child("users").child(user.getUid()).child("email").setValue(newUserEmail);
                            // Prompt the user to re-provide their sign-in credentials
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d("TAG", "User re-authenticated.");
                                        }
                                    });

                            user.updateEmail(newUserEmail)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("TAG", "User email address updated.");
                                                user.sendEmailVerification()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d("TAG", "Email sent.");
                                                                    startActivity(new Intent(showUserDetails.this, loginMainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                                }
                                                            }
                                                        });
                                            }
                                            else{
                                                Toast.makeText(showUserDetails.this, "Try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
    }

    public void changePassword(View view) {
        Toast.makeText(this, "changePassword called", Toast.LENGTH_SHORT).show();
    }
}
