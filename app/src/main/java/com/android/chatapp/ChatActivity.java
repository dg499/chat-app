package com.android.chatapp;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.chatapp.animations.ViewAudioProxy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;


public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;
    private ProgressDialog mProgressDialog;
    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private String FileAudioPath = null;
    private ImageButton mChatAddBtn;
    Uri uriAudio;
    private ImageButton mChatSendBtn, chat_audio_btn;
    private EditText mChatMessageView;
    private String Duration = "0";
    int AUDIO_FROM_GALLERY = 6;
    int AUDIO_FROM_MICK = 5;
    String audio_path = null;
    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;
    @BindView(R.id.tvPath)
    TextView tvPath;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private static final int GALLERY_PICK = 1;
    private Animator.AnimatorListener mAnimatorListenerOpen, mAnimatorListenerClose;

    // Storage Firebase
    private StorageReference mImageStorage, AudioStorage;
    public static final int UPLOAD_PICTURE_REQUEST_CODE = 0x001;
    public static final int UPLOAD_VIDEO_REQUEST_CODE = 0x002;
    public static final int SELECT_MESSAGES_CAMERA = 0x007;
    public static final int UPLOAD_AUDIO_REQUEST_CODE = 0x0003;
    public static final int UPLOAD_DOCUMENT_REQUEST_CODE = 0x004;
    public static final int SELECT_PROFILE_PICTURE = 0x005;
    public static final int SELECT_PROFILE_CAMERA = 0x006;
    //for audio
    @BindView(R.id.recording_time_text)
    TextView recordTimeText;
    @BindView(R.id.record_panel)
    View recordPanel;
    @BindView(R.id.slide_text_container)
    View slideTextContainer;
    @BindView(R.id.slideToCancelText)
    TextView slideToCancelText;
    @BindView(R.id.items_container)
    LinearLayout items_container;
    @BindView(R.id.attach_camera)
    LinearLayout attachCamera;
    @BindView(R.id.attach_image)
    LinearLayout attachImage;
    @BindView(R.id.attach_audio)
    LinearLayout attachAudio;
    @BindView(R.id.attach_document)
    LinearLayout attachDocument;
    @BindView(R.id.attach_video)
    LinearLayout attachVideo;
    @BindView(R.id.attach_record_video)
    LinearLayout attachRecordVideo;
    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;
    @BindView(R.id.btn_close_images)
    Button btn_close_images;
    final int MIN_INTERVAL_TIME = 2000;
    long mStartTime;
    private boolean isOpen;

    private MediaRecorder mMediaRecorder = null;
    private float startedDraggingX = -1;
    private float distCanMove = convertToDp(80);
    private long startTime = 0L;
    private Timer recordTimer;


    //New Solution
    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";
    String formattedDate = "";
    String currentDate = "";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        setTitle("");

        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");
        String userimg = getIntent().getStringExtra("user_image");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        try {
            currentDate = DateFormat.getDateTimeInstance().format(new Date());
          /*  Date today = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            currentDate = format.format(today);*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ---- Custom Action bar Items ----

        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);

        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        chat_audio_btn = (ImageButton) findViewById(R.id.chat_audio_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);

        mAdapter = new MessageAdapter(messagesList);
        Picasso.with(ChatActivity.this).load(userimg).placeholder(R.drawable.default_avatar).into(mProfileImage);

        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        //  mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);
    /*    Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
         formattedDate = df.format( c.getTime() );*/
        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference();
        AudioStorage = FirebaseStorage.getInstance().getReference();

        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);

        loadMessages();


        mTitleView.setText(userName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                if (online.equals("true")) {

                    mLastSeenView.setText("Online");

                } else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    mLastSeenView.setText(lastSeenTime);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();

            }
        });
        btn_close_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isOpen) {
                    isOpen = false;
                    animateItems(false);
                }
                items_container.setVisibility(View.GONE);

            }
        });
        slideToCancelText.setText(R.string.slide_to_cancel_audio);


        mChatMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String message = mChatMessageView.getText().toString();

                if (message.length() == 0) {
                    items_container.setVisibility(View.GONE);
                    chat_audio_btn.setVisibility(View.VISIBLE);
                    mChatSendBtn.setVisibility(View.GONE);
                    mChatAddBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
                String message = mChatMessageView.getText().toString();
                if (message.length() > 0) {
                    items_container.setVisibility(View.GONE);
                    chat_audio_btn.setVisibility(View.GONE);
                    mChatSendBtn.setVisibility(View.VISIBLE);
                    mChatAddBtn.setVisibility(View.GONE);
                    //  sendMessage();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                String message = mChatMessageView.getText().toString();
                if (message.length() == 0) {
                    items_container.setVisibility(View.GONE);
                    chat_audio_btn.setVisibility(View.VISIBLE);
                    mChatSendBtn.setVisibility(View.GONE);
                    mChatAddBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //items_container.setVisibility(View.VISIBLE);
                // linearLayout.setVisibility(View.GONE);
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });
        mAnimatorListenerOpen = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                items_container.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        };

        mAnimatorListenerClose = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                items_container.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        };

        items_container.setOnClickListener(view -> {
            if (isOpen) {
                isOpen = false;
                animateItems(false);

            }
        });

        chat_audio_btn.setOnTouchListener((view, motionEvent) -> {
            setDraggingAnimation(motionEvent, view);
            return true;
        });


        attachCamera.setOnClickListener(view -> launchAttachCamera());
        attachImage.setOnClickListener(view -> launchImageChooser());
        attachAudio.setOnClickListener(view -> launchAudioChooser());
    }

    private void launchAttachCamera() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }
        items_container.setVisibility(View.GONE);

        if (PermissionHandler.checkPermission(this, Manifest.permission.CAMERA)) {
            AppHelper.LogCat("camera permission already granted.");

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            startActivityForResult(cameraIntent, SELECT_MESSAGES_CAMERA);

        } else {
            AppHelper.LogCat("Please request camera  permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.CAMERA);
        }

    }

    /**
     * method to launch the image chooser
     */
    private void launchImageChooser() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }
        items_container.setVisibility(View.GONE);
        if (PermissionHandler.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read data permission already granted.");

            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(galleryIntent, "Choose An image"), GALLERY_PICK);
        } else {
            AppHelper.LogCat("Please request Read data permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

    private void animateItems(boolean opened) {
        float startRadius = 0.0f;
        float endRadius = Math.max(items_container.getWidth(), items_container.getHeight());
        if (opened) {
            int cy = items_container.getLeft();
            int dx = items_container.getBottom();
            SupportAnimator supportAnimator = ViewAnimationUtils.createCircularReveal(items_container, cy, dx, startRadius, endRadius);
            supportAnimator.setInterpolator(new AccelerateInterpolator());
            supportAnimator.setDuration(400);
            supportAnimator.addListener((SupportAnimator.AnimatorListener) mAnimatorListenerOpen);
            supportAnimator.start();
        } else {
            int cy = items_container.getLeft();
            int dx = items_container.getBottom();
            SupportAnimator supportAnimator2 = ViewAnimationUtils.createCircularReveal(items_container, cy, dx, endRadius, startRadius);
            supportAnimator2.setInterpolator(new DecelerateInterpolator());
            supportAnimator2.setDuration(400);
            supportAnimator2.addListener((SupportAnimator.AnimatorListener) mAnimatorListenerClose);
            supportAnimator2.start();
        }
    }


    /**
     * method to launch audio chooser
     */
    private void launchAudioChooser() {
        if (isOpen) {
            isOpen = false;
            animateItems(false);
        }
        items_container.setVisibility(View.GONE);
        if (PermissionHandler.checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AppHelper.LogCat("Read data permission already granted.");
           /* Intent intent = new Intent();
            intent.setType("audio/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Choose an audio"),
                    UPLOAD_AUDIO_REQUEST_CODE);*/

            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            //  intent.setType("audio/*");
            startActivityForResult(intent, UPLOAD_AUDIO_REQUEST_CODE);
        } else {
            AppHelper.LogCat("Please request Read data permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }


    public void browseAudio() {
        String[] img_options = {"Audio List", "Record"};
        AlertDialog.Builder bldr = new AlertDialog.Builder(ChatActivity.this);
        ArrayAdapter<String> adap = new ArrayAdapter<>(ChatActivity.this, R.layout.li_alr_d_tv, img_options);
        bldr.setAdapter(adap, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent int_audio_gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
//                    Intent int_audio_gallery = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(int_audio_gallery, AUDIO_FROM_GALLERY);
                }
                if (which == 1) {
                    Intent int_audio_camera = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                    startActivityForResult(int_audio_camera, AUDIO_FROM_MICK);
                }
            }
        });
        bldr.show();
    }


    private int convertToDp(float value) {
        return (int) Math.ceil(1 * value);
    }

    private boolean setDraggingAnimation(MotionEvent motionEvent, View view) {

        mChatSendBtn.setVisibility(View.GONE);
        recordPanel.setVisibility(View.VISIBLE);
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideTextContainer.getLayoutParams();
            params.leftMargin = convertToDp(30);
            slideTextContainer.setLayoutParams(params);
            ViewAudioProxy.setAlpha(slideTextContainer, 1);
            startedDraggingX = -1;
            mStartTime = System.currentTimeMillis();
            startRecording();
            chat_audio_btn.getParent().requestDisallowInterceptTouchEvent(true);
            recordPanel.setVisibility(View.VISIBLE);
            //mChatAddBtn.setVisibility(View.GONE);
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
            startedDraggingX = -1;
            recordPanel.setVisibility(View.GONE);
            mChatSendBtn.setVisibility(View.VISIBLE);

            long intervalTime = System.currentTimeMillis() - mStartTime;
            if (intervalTime < MIN_INTERVAL_TIME) {

                //  messageWrapper.setError(getString(R.string.hold_to_record));
                try {
                    if (FilesManager.isFileRecordExists(FileAudioPath)) {
                        boolean deleted = FilesManager.getFileRecord(FileAudioPath).delete();
                        if (deleted)
                            FileAudioPath = null;
                    }
                } catch (Exception e) {
                    AppHelper.LogCat("Exception record path file  MessagesPopupActivity");
                }
            } else {

                sendMessageaudio();
                FileAudioPath = null;

            }
            stopRecording();
        } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            float x = motionEvent.getX();
            if (x < -distCanMove) {
                AppHelper.LogCat("here we will delete  the file ");
                try {
                    if (FilesManager.isFileRecordExists(FileAudioPath)) {
                        boolean deleted = FilesManager.getFileRecord(FileAudioPath).delete();
                        if (deleted)
                            FileAudioPath = null;
                    }


                } catch (Exception e) {
                    AppHelper.LogCat("Exception exist record  " + e.getMessage());
                }
                FileAudioPath = null;
                stopRecording();
            }
            x = x + ViewAudioProxy.getX(chat_audio_btn);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideTextContainer
                    .getLayoutParams();
            if (startedDraggingX != -1) {
                float dist = (x - startedDraggingX);
                params.leftMargin = convertToDp(30) + (int) dist;
                slideTextContainer.setLayoutParams(params);
                float alpha = 1.0f + dist / distCanMove;
                if (alpha > 1) {
                    alpha = 1;
                } else if (alpha < 0) {
                    alpha = 0;
                }
                ViewAudioProxy.setAlpha(slideTextContainer, alpha);
            }
            if (x <= ViewAudioProxy.getX(slideTextContainer) + slideTextContainer.getWidth()
                    + convertToDp(30)) {
                if (startedDraggingX == -1) {
                    startedDraggingX = x;
                    distCanMove = (recordPanel.getMeasuredWidth()
                            - slideTextContainer.getMeasuredWidth() - convertToDp(48)) / 2.0f;
                    if (distCanMove <= 0) {
                        distCanMove = convertToDp(80);
                    } else if (distCanMove > convertToDp(80)) {
                        distCanMove = convertToDp(80);
                    }
                }
            }
            if (params.leftMargin > convertToDp(30)) {
                params.leftMargin = convertToDp(30);
                slideTextContainer.setLayoutParams(params);
                ViewAudioProxy.setAlpha(slideTextContainer, 1);
                startedDraggingX = -1;
            }
        }

        view.onTouchEvent(motionEvent);
        return true;
    }

    private void startRecording() {

        if (PermissionHandler.checkPermission(this, Manifest.permission.RECORD_AUDIO)) {
            AppHelper.LogCat("Record audio permission already granted.");
        } else {
            AppHelper.LogCat("Please request Record audio permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.RECORD_AUDIO);
        }

        if (PermissionHandler.checkPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
            AppHelper.LogCat("Record audio permission already granted.");
        } else {
            AppHelper.LogCat("Please request Record audio permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS);
        }


        if (PermissionHandler.checkPermission(this, Manifest.permission.VIBRATE)) {
            AppHelper.LogCat("Vibrate permission already granted.");
        } else {
            AppHelper.LogCat("Please request Vibrate permission.");
            PermissionHandler.requestPermission(this, Manifest.permission.VIBRATE);
        }
        try {
            startRecordingAudio();
            startTime = SystemClock.uptimeMillis();
            recordTimer = new Timer();
            UpdaterTimerTask updaterTimerTask = new UpdaterTimerTask();
            recordTimer.schedule(updaterTimerTask, 1000, 1000);
            vibrate();
        } catch (Exception e) {
            AppHelper.LogCat("IOException start audio " + e.getMessage());
        }


    }

    /**
     * method to initialize the audio for start recording
     *
     * @throws IOException
     */
    @SuppressLint("SetTextI18n")
    private void startRecordingAudio() throws IOException {
        stopRecordingAudio();
        FileAudioPath = FilesManager.getFileRecordPath(this);
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(FileAudioPath);
        mMediaRecorder.setOnErrorListener(errorListener);
        mMediaRecorder.setOnInfoListener(infoListener);
        mMediaRecorder.prepare();
        mMediaRecorder.start();

    }

    private MediaRecorder.OnErrorListener errorListener = (mr, what, extra) -> AppHelper.LogCat("Error: " + what + ", " + extra);

    private MediaRecorder.OnInfoListener infoListener = (mr, what, extra) -> AppHelper.LogCat("Warning: " + what + ", " + extra);

    private void stopRecordingAudio() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
                FileAudioPath = null;
            }
        } catch (Exception e) {
            AppHelper.LogCat("Exception stop recording " + e.getMessage());
        }

    }


    /**
     * method to stop recording auido
     */
    @SuppressLint("SetTextI18n")
    private void stopRecording() {
        if (recordTimer != null) {
            recordTimer.cancel();
        }
        if (recordTimeText.getText().toString().equals("00:00")) {
            return;
        }
        recordTimeText.setText("00:00");
        vibrate();
        recordPanel.setVisibility(View.GONE);
        mChatSendBtn.setVisibility(View.GONE);
        stopRecordingAudio();


    }

    @SuppressLint("MissingPermission")
    private void vibrate() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class UpdaterTimerTask extends TimerTask {

        @Override
        public void run() {
            long timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            long timeSwapBuff = 0L;
            long updatedTime = timeSwapBuff + timeInMilliseconds;
            Duration = String.valueOf(updatedTime);
            final String recordTime = AppHelper.getFileTime(updatedTime);
            runOnUiThread(() -> {
                try {
                    if (recordTimeText != null) {
                        recordTimeText.setText(recordTime);
                    }

                } catch (Exception e) {
                    AppHelper.LogCat("Exception record MessagesPopupActivity");
                }

            });
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_MESSAGES_CAMERA && resultCode == RESULT_OK) {

            mProgressDialog = new ProgressDialog(ChatActivity.this);
            mProgressDialog.setTitle("Send Image...");
            mProgressDialog.setMessage("Please wait while we upload and process the image.");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            //  Uri imageUri = data.getData();

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Uri imageUri = getImageUri(ChatActivity.this, photo);
            //  gstFile = encodeToBase64(photo);

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");

            Log.e("imageUri", imageUri + "");
            Log.e("filepath", filepath + "");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {
                        mProgressDialog.dismiss();

                        String download_url = task.getResult().getDownloadUrl().toString();

                        // formattedDate have current date/time

                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("date", currentDate);
                        messageMap.put("from", mCurrentUserId);
                        Log.e("messageMapimage", messageMap + "");
                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");
                        Log.e("messageUserMapimage", messageUserMap + "");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if (databaseError != null) {

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });


                    }

                }
            });

        }
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            mProgressDialog = new ProgressDialog(ChatActivity.this);
            mProgressDialog.setTitle("Send Image...");
            mProgressDialog.setMessage("Please wait while we upload and process the image.");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");
            Log.e("imageUri", imageUri + "");
            Log.e("filepath", filepath + "");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {
                        mProgressDialog.dismiss();

                        String download_url = task.getResult().getDownloadUrl().toString();

                        // formattedDate have current date/time

                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("date", currentDate);
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Log.e("messageMapgalimage", messageMap + "");

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");
                        Log.e("messageUsergalMapimage", messageUserMap + "");
                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if (databaseError != null) {

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });


                    }

                }
            });

        }

        if (requestCode == UPLOAD_AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {

            mProgressDialog = new ProgressDialog(ChatActivity.this);
            mProgressDialog.setTitle("Send Audio...");
            mProgressDialog.setMessage("Please wait while we upload and process the Audio.");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();


            try {
              /*  FileAudioPath =
                        Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/myaudio.3gp";*/
                FileAudioPath = FilesManager.getPath(getApplicationContext(), data.getData());
                MediaPlayer mp = MediaPlayer.create(this, Uri.parse(FileAudioPath));
                int duration = mp.getDuration();
                Duration = String.valueOf(duration);
                mp.release();
                uriAudio = Uri.fromFile(new File(FileAudioPath).getAbsoluteFile());
                Log.e("uriAudio", uriAudio + "");

              /*  FileAudioPath = FilesManager.getPath(getApplicationContext(), data.getData());
                MediaPlayer mp = MediaPlayer.create(this, Uri.parse(FileAudioPath));
                int duration = mp.getDuration();
                Duration = String.valueOf(duration);
                mp.release();
                sendMessage();*/
            } catch (Exception e) {
                AppHelper.LogCat(" Exception " + e.getMessage());
                return;
            }

          /*  if (FileAudioPath != null)
                messageGroup.put("audio", FileAudioPath);
            else
                messageGroup.put("audio", "null");*/
            //        StorageReference filepath = AudioStorage.child("audio").child(push_id);
            final StorageReference audioRef = AudioStorage.child("audio").child(push_id + " audio/mp3");

            audioRef.putFile(uriAudio).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {
                        mProgressDialog.dismiss();
                        Log.e("suc", "suc");
                        Toast.makeText(ChatActivity.this, "Suc", Toast.LENGTH_SHORT).show();
                        @SuppressWarnings("VisibleForTests")
                        String audioUrl = task.getResult().getDownloadUrl().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message", audioUrl);
                        messageMap.put("seen", false);
                        messageMap.put("type", "audio");
                        messageMap.put("date", currentDate);
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        //mChatMessageView.setText("");
                        Log.e("audioUrl", audioUrl);
                        Log.e("messageUserMapaudi", messageUserMap + "");
                        Log.e("messageMap_ad", messageMap + "");
                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if (databaseError != null) {

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });

                    } else {
                        Log.e("suc", "fail");
                    }

                }
            });

        }
        if (requestCode == AUDIO_FROM_GALLERY && resultCode == RESULT_OK && data != null) {
            String[] projection = {MediaStore.Audio.Media.DATA};
            Cursor cursor = getContentResolver().query(data.getData(), projection, null, null, null);
            int cursor_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            audio_path = cursor.getString(cursor_index);
            tvPath.setText(audio_path);
            try {
                MediaPlayer mp = new MediaPlayer();
                mp.setDataSource(audio_path);
                mp.prepare();
                mp.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestCode == AUDIO_FROM_MICK && resultCode == RESULT_OK && data != null) {
            String[] projection = {MediaStore.Audio.Media.DATA};
            Cursor cursor = getContentResolver().query(data.getData(), projection, null, null, null);
            int cursor_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            audio_path = cursor.getString(cursor_index);
            tvPath.setText(audio_path);
            try {
                MediaPlayer mp = new MediaPlayer();
                mp.setDataSource(audio_path);
                mp.prepare();
                mp.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                Log.e("message", message + "");
                Log.e("messageKey", messageKey + "");

                if (!mPrevKey.equals(messageKey)) {

                    messagesList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }


                if (itemPos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mAdapter.notifyDataSetChanged();

                // mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(10, 0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if (itemPos == 1) {

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);

                //  mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {


        String message = mChatMessageView.getText().toString();

        if (!TextUtils.isEmpty(message)) {

            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("date", currentDate);
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);
            Log.e("messageMap", messageMap + "");
            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageView.setText("");

            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            Log.e("messageUserMap", messageUserMap + "");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    }

                }
            });

        }

    }


    private void sendMessageaudio() {
        mProgressDialog = new ProgressDialog(ChatActivity.this);
        mProgressDialog.setTitle("Send Audio...");
        mProgressDialog.setMessage("Please wait while we upload and process the Audio.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        if (FileAudioPath != null) {

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();

            uriAudio = Uri.fromFile(new File(FileAudioPath).getAbsoluteFile());

            final StorageReference audioRef = AudioStorage.child("audio").child(push_id + " audio/mp3");

            Log.e("audioRef", audioRef + "");
            Log.e("uriAudio", uriAudio + "");

            audioRef.putFile(uriAudio).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    mProgressDialog.dismiss();
                    if (task.isSuccessful()) {

                        String audioUrl = task.getResult().getDownloadUrl().toString();

                        Log.e("audioUrl", audioUrl);

                        Map messageMap = new HashMap();

                       /* StorageReference audioRefdown = AudioStorage.child(task.getResult().toString());

                        Log.e("audioRefdown", audioRefdown + "");
                        File localFile = null;
                        try {
                            localFile = File.createTempFile("Audio", "mp3");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        audioRefdown.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(ChatActivity.this, "Downloded", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {

                            }
                        });*/
                        messageMap.put("message", audioUrl);
                        messageMap.put("seen", false);
                        messageMap.put("type", "audio");
                        messageMap.put("date", currentDate);
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");
                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if (databaseError != null) {

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });

                    } else {
                        Toast.makeText(ChatActivity.this, "fail", Toast.LENGTH_SHORT).show();

                    }

                }
            });
        }

    }

}