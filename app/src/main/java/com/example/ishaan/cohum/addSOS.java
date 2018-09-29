package com.example.ishaan.cohum;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
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

public class addSOS extends AppCompatActivity {
    private List<String> mChildNames = new ArrayList<>();
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    ListView lv;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.add_user_sos);
        user = FirebaseAuth.getInstance().getCurrentUser();

        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading.");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.show();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        lv = (ListView) findViewById(R.id.lvSOS);

        final Query parentReference = mDatabase.child("sos").child(user.getUid()).child("members");
        parentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.getValue() == null)) {
                    Toast.makeText(addSOS.this, "Add user to send SOS message.", Toast.LENGTH_SHORT).show();
                } else {
                    String s2 = "", s3 = "";
                    for (DataSnapshot childName : dataSnapshot.getChildren()) {
                        s2 = ("\nName: " + childName.getKey());
                        for (DataSnapshot snap : childName.getChildren()) {
                            if (snap.getKey().equals("phone")) {
                                s3 = ("\nPhone: " + snap.getValue().toString() + "\n");
                            }
                        }
                        if (s3 == "") {

                        } else {
                            mChildNames.add(s2 + s3);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(addSOS.this, "Failed to load child",
                        Toast.LENGTH_SHORT).show();
            }
        });
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
        if (progressDialog.isShowing() && mChildNames != null) {
            progressDialog.dismiss();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }

    public void addSOSUser(View view) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.add_geo_user, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        final EditText name = promptsView.findViewById(R.id.sos_user);
        final EditText number = promptsView.findViewById(R.id.sos_number);
        final DatabaseReference parentReference = mDatabase.child("sos").child(user.getUid()).child("members");
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                parentReference.child(name.getText().toString()).child("phone").setValue(number.getText().toString());
                                mChildNames.clear();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void deleteSOSUser(View view) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.delete_geo_user, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        final EditText name = promptsView.findViewById(R.id.sos_del_user);
        final DatabaseReference parentReference = mDatabase.child("sos").child(user.getUid()).child("members");
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (name.getText().toString().isEmpty()) {
                                    Toast.makeText(addSOS.this, "Name can not be blank.", Toast.LENGTH_SHORT).show();
                                } else {
                                    displayAlert2(parentReference, name.getText().toString());
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void displayAlert2(final DatabaseReference parentReference, final String userName) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Do you want to delete " + userName + "?");
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        parentReference.child(userName).removeValue();
                        mChildNames.clear();
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