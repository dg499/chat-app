package com.android.chatapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;


public class EditUsernameActivity extends AppCompatActivity {
    @BindView(R.id.cancelStatus)
    TextView cancelStatusBtn;
    @BindView(R.id.OkStatus)
    TextView OkStatusBtn;
    @BindView(R.id.StatusWrapper)
    EditText StatusWrapper;
    @BindView(R.id.emoticonBtn)
    ImageView emoticonBtn;

    private String oldName;
    private ProgressDialog mProgress;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;


    // private EditProfilePresenter mEditProfilePresenter ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_status);
        ButterKnife.bind(this);
        initializerView();
        // mEditProfilePresenter = new EditProfilePresenter(this, true);
        //  mEditProfilePresenter.onCreate();
        if (getIntent().getExtras() != null) {

            oldName = getIntent().getStringExtra("displayname");

        }
        StatusWrapper.setText(oldName);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);


    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        emoticonBtn.setVisibility(View.GONE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_edit_name);
        cancelStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        OkStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    mProgress = new ProgressDialog(EditUsernameActivity.this);
                    mProgress.setTitle("Saving Changes");
                    mProgress.setMessage("Please wait while we save the changes");
                    mProgress.show();

                    String newUsername = StatusWrapper.getText().toString().trim();

                    mStatusDatabase.child("name").setValue(newUsername).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                mProgress.dismiss();
                                finish();

                            } else {

                                Toast.makeText(getApplicationContext(), "There was some error in saving Changes.", Toast.LENGTH_LONG).show();

                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
