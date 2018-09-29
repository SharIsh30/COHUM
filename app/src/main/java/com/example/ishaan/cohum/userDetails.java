package com.example.ishaan.cohum;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class userDetails extends AppCompatActivity {
    private EditText userName;
    private EditText phone;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        final DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.e("TAG", "userDetails called.");
        userName = findViewById(R.id.fname);
        phone = findViewById(R.id.phone);
        submit = findViewById(R.id.submitButton);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference database = dbref.child("users").child(user.getUid());
                database.child("name").setValue(userName.getText().toString());
                database.child("phone").setValue(phone.getText().toString());
                database.child("email").setValue(user.getEmail());
                startActivity(new Intent(userDetails.this, authenticateEmail.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("exit", "yes"));
                //finish();
            }
        });
    }
}

