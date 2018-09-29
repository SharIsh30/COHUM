package com.example.ishaan.cohum;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class deleteMember extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = deleteMember.class.getSimpleName();
    private EditText emailText;
    private Button deleteButton;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;


    public String getDeleteMemberID() {
        return deleteMemberId;
    }

    public void setDeleteMemberId(String deleteMemberId) {
        this.deleteMemberId = deleteMemberId;
    }

    String deleteMemberId;
    String currentUserId;


    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_user);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        emailText = findViewById(R.id.emailText);
        deleteButton = findViewById(R.id.deleteButton);

        emailText.setHint("Registered user's email address");
        emailText.setOnClickListener(this);
        deleteButton.setOnClickListener(this);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View view) {
        if (view.equals(findViewById(R.id.deleteButton))) {
            if (isEmailValid(emailText.getText().toString())) {
                searchMember();
            } else {
                Toast.makeText(this, "Not Valid", Toast.LENGTH_SHORT).show();
                emailText.setError("Please Enter an Email Address");
            }
        }
    }

    public void searchMember() {
        setCurrentUserId(mAuth.getCurrentUser().getUid());
        Log.d(TAG, "User Id: " + getCurrentUserId());

        final Query database = mDatabase.child("users").orderByChild("email").equalTo(emailText.getText().toString());
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(deleteMember.this,
                            "Member not found, please make sure if the user exists or try again.",
                            Toast.LENGTH_LONG).show();
                } else {
                    for (DataSnapshot userData : dataSnapshot.getChildren()) {
                        setDeleteMemberId(userData.getKey());
                        getMemberInformation();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Not found");
            }
        });
        Log.d(TAG, "Email: " + emailText.getText());
    }

    public void getMemberInformation() {

        DatabaseReference data = mDatabase.child("users").child(getDeleteMemberID());
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot info : dataSnapshot.getChildren()) {
                    if (info.getKey().equals("name")) {
                        mDatabase.child("geofence").child(mAuth.getCurrentUser().getUid()).child("geo_user").child(info.getValue().toString()).child("id").removeValue();
                        mDatabase.child("geofence").child(mAuth.getCurrentUser().getUid()).child("geo_user").child(info.getValue().toString()).child("enter").removeValue();
                    }
                    deleteMember();
                }
            }

                @Override
                public void onCancelled (DatabaseError databaseError){

                }
            });
        }

    public void deleteMember() {
        final DatabaseReference data = mDatabase.child("users").child(getCurrentUserId()).child("members").child(getDeleteMemberID());
        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    writeData(data);
                } else {
                    Toast.makeText(deleteMember.this,                            "User not found.",                            Toast.LENGTH_SHORT).show();
                    emailText.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void writeData(DatabaseReference data) {
        data.removeValue();

        Toast.makeText(deleteMember.this,
                "Member successfully deleted.",
                Toast.LENGTH_SHORT).show();


        startActivity(new Intent(deleteMember.this, MainActivity.class));

        finish();
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}

