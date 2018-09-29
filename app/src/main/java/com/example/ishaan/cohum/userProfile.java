package com.example.ishaan.cohum;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class userProfile extends AppCompatActivity {

    private List<String> mChildNames = new ArrayList<>();
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    ListView lv;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        user = FirebaseAuth.getInstance().getCurrentUser();

        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading.");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.show();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        lv = (ListView) findViewById(R.id.lv);

        final Query parentReference = mDatabase.child("users").child(user.getUid()).child("members").orderByChild("name");
        parentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 1;
                String s1 = "", s2 = "", s3 = "";
                for (DataSnapshot childId : dataSnapshot.getChildren()) {
                    for (DataSnapshot snap : childId.getChildren()) {
                        if (snap.getKey().equals("name")) {
                            s1 = ("Name: " + snap.getValue().toString());
                        }
                        if (snap.getKey().equals("email")) {
                            s2 = ("\nEmail: " + snap.getValue().toString());
                        }
                        if (snap.getKey().equals("phone")) {
                            s3 = ("\nPhone: " + snap.getValue().toString());
                        }
                    }
                    mChildNames.add("\n" + s1 + s2 + s3 + "\n");
                }
                final Handler handler = new Handler();
                final Handler handler1 = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {

                        handler1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showAdapter();
                            }
                        }, 550);
                    }
                }, 1000);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(userProfile.this, "Failed to load child",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAdapter() {
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mChildNames);
        //lv.setAdapter(adapter);
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mChildNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                row.setBackgroundColor(Color.WHITE); // some color
                return row;
            }
        });
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }
}
