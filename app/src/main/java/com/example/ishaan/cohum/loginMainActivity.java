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

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class loginMainActivity extends AppCompatActivity implements View.OnClickListener {

    //defining view objects
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonSignup;
    private SignInButton googleBtn;
    private FirebaseUser user;
    private TextView textViewSignin;

    private ProgressDialog progressDialog;
    private static final String TAG = loginMainActivity.class.getSimpleName();

    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);

        //initializing firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //if getCurrentUser does not returns null
        if (firebaseAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(loginMainActivity.this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }

        //initializing views
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        textViewSignin = (TextView) findViewById(R.id.textViewSignin);
        googleBtn = (SignInButton) findViewById(R.id.googleBtn);

        buttonSignup = (Button) findViewById(R.id.buttonSignup);

        progressDialog = new ProgressDialog(this);

        //attaching listener to button
        buttonSignup.setOnClickListener(this);
        textViewSignin.setOnClickListener(this);
        googleBtn.setOnClickListener(this);
    }

    private void registerUser() {

        //getting email and password from edit texts
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

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if (task.isSuccessful()) {
                            user = firebaseAuth.getCurrentUser();
                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Email sent.");
                                                startActivity(new Intent(loginMainActivity.this, userDetails.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                            }
                                        }
                                    });

                        } else if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                editTextPassword.setError(getString(R.string.error_invalid_password));
                                editTextPassword.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                editTextEmail.setError(getString(R.string.error_invalid_email));
                                editTextEmail.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                editTextEmail.setError(getString(R.string.error_user_exists));
                                editTextEmail.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    public void onClick(View view) {

        if (view == buttonSignup) {
            registerUser();
        } else if (view == textViewSignin) {
            //open login activity when user taps on the already registered textview
            startActivity(new Intent(this, signinActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        } else if (view == googleBtn) {
            startActivity(new Intent(getApplicationContext(), googleSignin.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }
}