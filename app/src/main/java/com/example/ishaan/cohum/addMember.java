package com.example.ishaan.cohum;

import android.content.Intent;
import android.service.autofill.Dataset;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class addMember extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = addMember.class.getSimpleName();
    private EditText emailText;
    private Button addButton;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;


    public String getAddMemberId() {
        return addMemberId;
    }

    public void setAddMemberId(String addMemberId) {
        this.addMemberId = addMemberId;
    }

    String addMemberId;
    String memberName;
    String memberPhone;
    String memberEmail;
    String currentUserId;


    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }


    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberPhone() {
        return memberPhone;
    }

    public void setMemberPhone(String memberPhone) {
        this.memberPhone = memberPhone;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        emailText = findViewById(R.id.emailText);
        addButton = findViewById(R.id.addButton);

        emailText.setHint("Registered user's email address");
        emailText.setOnClickListener(this);
        addButton.setOnClickListener(this);

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View view) {
        if (view.equals(findViewById(R.id.addButton))) {
            if (isEmailValid(emailText.getText().toString())) {
                searchMember();
            } else {
                Toast.makeText(this, "Not Vaid", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(addMember.this,
                            "Member not found, please make sure if the user exists or try again.",
                            Toast.LENGTH_LONG).show();
                } else {


                    for (DataSnapshot userData : dataSnapshot.getChildren()) {
                        setAddMemberId(userData.getKey());
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

        DatabaseReference data = mDatabase.child("users").child(getAddMemberId());
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot info : dataSnapshot.getChildren()) {
                    if (info.getKey().equals("name")) {
                        mDatabase.child("geofence").child(mAuth.getCurrentUser().getUid()).child("geo_user").child(info.getValue().toString()).child("id").setValue(getAddMemberId());
                        mDatabase.child("geofence").child(mAuth.getCurrentUser().getUid()).child("geo_user").child(info.getValue().toString()).child("enter").setValue("default");
                        setMemberName(info.getValue().toString());
                    }
                    if (info.getKey().equals("email")) {
                        setMemberEmail(info.getValue().toString());
                    }
                    if (info.getKey().equals("phone")) {
                        setMemberPhone(info.getValue().toString());
                    }
                }
                addMember();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addMember() {
        final DatabaseReference data = mDatabase.child("users").child(getCurrentUserId()).child("members");
        data.child(getAddMemberId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(addMember.this,
                            "User already added as member.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    writeData(data);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void writeData(DatabaseReference data) {
        data.child(getAddMemberId()).child("name").setValue(getMemberName());
        data.child(getAddMemberId()).child("phone").setValue(getMemberPhone());
        data.child(getAddMemberId()).child("email").setValue(getMemberEmail());

        Toast.makeText(addMember.this,
                "Member successfully added.",
                Toast.LENGTH_SHORT).show();


        startActivity(new Intent(addMember.this, MainActivity.class));

        finish();
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}

