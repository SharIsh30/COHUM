package com.example.ishaan.cohum;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class signinActivity extends AppCompatActivity implements View.OnClickListener {

    //defining views
    private Button buttonSignIn;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignup;

    //firebase auth object
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;
    private static final String TAG = signinActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        //getting firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //if the objects getcurrentuser method is not null
        //means user is already logged in
        if (firebaseAuth.getCurrentUser() != null) {
            if (intent.hasExtra("unauthentic")) {
                Toast.makeText(this, "Verify Email first", Toast.LENGTH_SHORT).show();
            } else {
                //close this activity
                finish();
                startActivity(new Intent(getApplicationContext(), authenticateEmail.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                //opening profile activity
            }
        }
            //initializing views
            editTextEmail = (EditText) findViewById(R.id.editTextEmail);
            editTextPassword = (EditText) findViewById(R.id.editTextPassword);
            buttonSignIn = (Button) findViewById(R.id.buttonSignin);
            textViewSignup = (TextView) findViewById(R.id.textViewSignUp);

            progressDialog = new ProgressDialog(this);

            //attaching click listener
            buttonSignIn.setOnClickListener(this);
            textViewSignup.setOnClickListener(this);
        }

    //method for user login

    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();


        //checking if email and passwords are empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return;
        }

        //if the email and password are not empty
        //displaying a progress dialog

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();

        //logging in the user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        //if the task is successfull
                        if (task.isSuccessful()) {
                            //start the profile activity
                            finish();
                            startActivity(new Intent(getApplicationContext(), authenticateEmail.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        } else if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                editTextPassword.setError(getString(R.string.error_incorrect_password));
                                editTextPassword.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    }
                });

    }

    @Override
    public void onClick(View view) {
        if (view == buttonSignIn) {
            userLogin();
        } else if (view == textViewSignup) {
            finish();
            startActivity(new Intent(this, loginMainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }
}