package com.android.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    EditText email_et, password_et, displayname_et;
    String display_name = "", email = "", password = "";
    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private TextView mCreateBtn;
    private Toolbar mToolbar;
    private DatabaseReference mDatabase;
    //ProgressDialog
    private ProgressDialog mRegProgress;
    //Firebase Auth
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mRegProgress = new ProgressDialog(this);
        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_app_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("Register");
        // Android Fields
        displayname_et = (EditText) findViewById(R.id.displayname_et);
        email_et = (EditText) findViewById(R.id.email_et);
        password_et = (EditText) findViewById(R.id.password_et);
        mDisplayName = (TextInputLayout) findViewById(R.id.register_display_name);
        mEmail = (TextInputLayout) findViewById(R.id.register_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mCreateBtn = (TextView) findViewById(R.id.reg_create_btn);
        displayname_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (displayname_et.length() > 0) {
                    display_name = mDisplayName.getEditText().getText().toString();
                    mDisplayName.setErrorEnabled(false);
                } else {
                    mDisplayName.setErrorEnabled(true);
                }
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        email_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (email_et.length() > 0) {
                    email = mEmail.getEditText().getText().toString();
                    mEmail.setErrorEnabled(false);
                } else {
                    mEmail.setErrorEnabled(true);
                }
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        password_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (password_et.length() > 0) {
                    password = mPassword.getEditText().getText().toString();
                    mPassword.setErrorEnabled(false);
                } else {
                    mPassword.setErrorEnabled(true);
                }
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                display_name = mDisplayName.getEditText().getText().toString();
                email = mEmail.getEditText().getText().toString();
                password = mPassword.getEditText().getText().toString();
                if (display_name.isEmpty()) {
                    mDisplayName.setError("Name is required!");
                    mDisplayName.requestFocus();
                } else if (email.isEmpty()) {
                    mEmail.setError("Email is required!");
                    mEmail.requestFocus();
                } else if (password.isEmpty()) {
                    mPassword.setError("Password is required!");
                    mPassword.requestFocus();
                } else {
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_user(display_name, email, password);
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            }
        }
        return false;
    }
    private void register_user(final String display_name, final String email, String password) {
        Log.e("display_name", display_name + "");
        Log.e("email", email + "");
        Log.e("password", password + "");
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    String device_token = FirebaseInstanceId.getInstance().getToken();
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", display_name);
                    userMap.put("status", "Hi there I'm using  Chat App.");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");
                    userMap.put("device_token", device_token);
                    userMap.put("email", email);
                    Log.e("userMap", userMap + "");
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mRegProgress.dismiss();
                                mRegProgress.hide();
                                Toast.makeText(RegisterActivity.this, "Registrion Successful!! please login...", Toast.LENGTH_SHORT).show();
                                Intent mainIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        }
                    });
                } else {
                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this, "Cannot Sign in. Please check the form and try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
