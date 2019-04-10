package com.android.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;
    TextView signup;

    private TextView mLogin_btn;

    private ProgressDialog mLoginProgress;

    private FirebaseAuth mAuth;

    private DatabaseReference mUserDatabase;

    String email = "", password = "";
    EditText email_et, password_et;
    TextView toolbar_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );

        mAuth = FirebaseAuth.getInstance();
        setTitle( "" );

        mLoginProgress = new ProgressDialog( this );
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child( "Users" );


        email_et = (EditText) findViewById( R.id.email_et );
        password_et = (EditText) findViewById( R.id.password_et );
        mLoginEmail = (TextInputLayout) findViewById( R.id.login_email );
        mLoginPassword = (TextInputLayout) findViewById( R.id.login_password );
        mLogin_btn = (TextView) findViewById( R.id.login_btn );
        signup = (TextView) findViewById( R.id.sign_up_tv );
        email = email_et.getText().toString();
        password = password_et.getText().toString();

        email_et.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                if (email_et.length() > 0) {
                    email = mLoginEmail.getEditText().getText().toString();
                    mLoginEmail.setErrorEnabled( false );
                } else {
                    mLoginEmail.setErrorEnabled( true );
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        } );
        password_et.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                if (password_et.length() > 0) {
                    password = mLoginPassword.getEditText().getText().toString();
                    mLoginPassword.setErrorEnabled( false );
                } else {
                    mLoginPassword.setErrorEnabled( true );
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        } );


        mLogin_btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

             /*   email = email_et.getText().toString();
                password = password_et.getText().toString(); */

                email = mLoginEmail.getEditText().getText().toString();
                password = mLoginPassword.getEditText().getText().toString();

                if (email.isEmpty()) {
                    mLoginEmail.setError( "Email is required!" );
                    mLoginEmail.requestFocus();

                } else if (password.isEmpty()) {
                    mLoginPassword.setError( "Password is required!" );
                    mLoginPassword.requestFocus();

                } else {

                    mLoginProgress.setTitle( "Logging In" );
                    mLoginProgress.setMessage( "Please wait while we check your credentials." );
                    mLoginProgress.setCanceledOnTouchOutside( false );
                    mLoginProgress.show();

                    loginUser( email, password );
                }
            }
        } );

        signup.setOnClickListener( new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

             /*   if (isNull( email )) {
                    mLoginEmail.setError( "Username is required!" );
                    mLoginEmail.requestFocus();
                    return;
                } else if (isNull( password )) {
                    mLoginPassword.setError( "Password is required!" );
                    mLoginPassword.requestFocus();
                    return;
                } else {*/


                AppHelper.LaunchActivity( LoginActivity.this, RegisterActivity.class );

                // }
            }
        } );


    }

    private void validateEditText(Editable s) {
        if (!TextUtils.isEmpty( s )) {
            email_et.setError( null );
        } else {
            email_et.setError( getString( R.string.ui_no_password_toast ) );
        }
    }

    private void loginUser(String email, String password) {


        mAuth.signInWithEmailAndPassword( email, password ).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    mLoginProgress.dismiss();

                    String current_user_id = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mUserDatabase.child( current_user_id ).child( "device_token" ).setValue( deviceToken ).addOnSuccessListener( new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText( LoginActivity.this, "Login Successful!!...", Toast.LENGTH_SHORT).show();

                            Intent mainIntent = new Intent( LoginActivity.this, DashboardActivity.class );
                            mainIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                            startActivity( mainIntent );
                            finish();
                            overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_left );

                        }
                    } );


                } else {

                    mLoginProgress.hide();

                    String task_result = task.getException().getMessage().toString();
                    showDialog( "Error : " + task_result );

                    //  Toast.makeText( LoginActivity.this, "Error : " + task_result, Toast.LENGTH_LONG ).show();

                }

            }
        } );

    }

    private void showDialog(String msg) {
        android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder( this );
        adb.setMessage( msg );
        adb.setPositiveButton( getResources().getString( R.string.okay ), null );
        adb.show();
    }
}
