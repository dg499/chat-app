package com.android.chatapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by AkshayeJH on 24/07/17.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {


    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mUserDatabasem;
    Friends friends;
    Context context;
    private StorageReference AudioStorage;
    private MediaPlayer mMediaPlayer;
    AudioCallbacks mAudioCallbacks;
    String audiopath = "";

    public MessageAdapter(Context context) {
        this.context = context;
    }

    public MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

        public TextView messageText, audioCurrentDurationAudio, audioTotalDurationAudio;
        public CircleImageView profileImage;
        public TextView displayName, time_text_layout;
        public ImageView messageImage;
        public LinearLayout audio_layout, retryUploadAudio, retryDownloadAudio;
        ImageView userAudioImage;
        ImageButton retry_upload_audio_button, cancelDownloadAudio, retryDownloadAudioButton, playBtnAudio, pauseBtnAudio;
        ProgressBar mProgressUploadAudio, mProgressUploadAudioInitial, mProgressDownloadAudio, mProgressDownloadAudioInitial;

        SeekBar audioSeekBar;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            displayName = (TextView) view.findViewById(R.id.name_text_layout);
            time_text_layout = (TextView) view.findViewById(R.id.time_text_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);

            //var for audio
            retryUploadAudio = (LinearLayout) view.findViewById(R.id.audio_layout);
            audio_layout = (LinearLayout) view.findViewById(R.id.retry_upload_audio);
            userAudioImage = (ImageView) view.findViewById(R.id.audio_user_image);
            retry_upload_audio_button = (ImageButton) view.findViewById(R.id.retry_upload_audio_button);
            cancelDownloadAudio = (ImageButton) view.findViewById(R.id.cancel_download_audio);
            retryDownloadAudio = (LinearLayout) view.findViewById(R.id.retry_download_audio);
            retryDownloadAudioButton = (ImageButton) view.findViewById(R.id.retry_download_audio_button);
            playBtnAudio = (ImageButton) view.findViewById(R.id.play_btn_audio);
            pauseBtnAudio = (ImageButton) view.findViewById(R.id.pause_btn_audio);
            audioSeekBar = (SeekBar) view.findViewById(R.id.audio_progress_bar);
            mProgressUploadAudio = (ProgressBar) view.findViewById(R.id.progress_bar_upload_audio);
            mProgressUploadAudioInitial = (ProgressBar) view.findViewById(R.id.progress_bar_upload_audio_init);
            mProgressDownloadAudio = (ProgressBar) view.findViewById(R.id.progress_bar_download_audio);
            mProgressDownloadAudioInitial = (ProgressBar) view.findViewById(R.id.progress_bar_download_audio_init);
            audioCurrentDurationAudio = (TextView) view.findViewById(R.id.audio_current_duration);
            audioTotalDurationAudio = (TextView) view.findViewById(R.id.audio_total_duration);

            cancelDownloadAudio.setOnClickListener(this);
            retryDownloadAudioButton.setOnClickListener(this);
          /*  cancelUploadAudio.setOnClickListener(this);
            retryUploadAudioButton.setOnClickListener(this);*/
            audioSeekBar.setOnSeekBarChangeListener(this);
            playBtnAudio.setOnClickListener(this);
            pauseBtnAudio.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.pause_btn_audio:
                    playBtnAudio.setVisibility(View.VISIBLE);
                    pauseBtnAudio.setVisibility(View.GONE);
                    pausePlayingAudio();
                    break;
                case R.id.play_btn_audio:
                    playBtnAudio.setVisibility(View.GONE);
                    pauseBtnAudio.setVisibility(View.VISIBLE);
                    stopPlayingAudio();
                    playingAudio();
                    break;
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }


    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        Messages c = mMessageList.get(i);
        Log.e("cMessages", c.toString() + "");
        Log.e("mMessageList", mMessageList + "");


        String from_user = c.getFrom();
        String message_type = c.getType();
        String datenew = c.getDate();

        //String date = Long.toString(datenew);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        // mUserDatabasem = FirebaseDatabase.getInstance().getReference().child("messages").child(from_user);
        AudioStorage = FirebaseStorage.getInstance().getReference();
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();


                viewHolder.displayName.setText(name);
                viewHolder.time_text_layout.setText(datenew);

                Picasso.with(viewHolder.profileImage.getContext()).load(image)
                        .placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (message_type.equals("text")) {

            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageImage.setVisibility(View.GONE);
            viewHolder.audio_layout.setVisibility(View.GONE);

        } else if (message_type.equals("image")) {

            viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.default_avatar).into(viewHolder.messageImage);

        } else {
            viewHolder.messageText.setVisibility(View.GONE);
            viewHolder.messageImage.setVisibility(View.GONE);
            viewHolder.audio_layout.setVisibility(View.VISIBLE);

            audiopath = c.getMessage();
            Log.e("audiopath", audiopath);
           /* StorageReference audioRefdown = AudioStorage.child(c.getMessage());
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
                   // Toast.makeText(context, "Downloded", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });*/


            // startdownload();

        }

    }


    void pausePlayingAudio() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                // updateAudioProgressBar();
                mAudioCallbacks.onPause();
            }
        }
    }

    /*
        void playingAudion(Messages messagesModel) {
          */
/*  if (messagesModel.getSenderID() == PreferenceManager.getID(mActivity)) {
            updateAudioProgressBar();
            String AudioDataSource;
            if (mMediaPlayer != null) {
                try {

                    if (FilesManager.isFileAudiosSentExists(mActivity, FilesManager.getAudio(messagesModel.getAudioFile()))) {
                        AudioDataSource = FilesManager.getFileAudiosSentPath(mActivity, messagesModel.getAudioFile());
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mMediaPlayer.setDataSource(AudioDataSource);
                        mMediaPlayer.prepare();
                        mMediaPlayer.setOnPreparedListener(MediaPlayer::start);

                    } else {

                        AudioDataSource = EndPoints.MESSAGE_AUDIO_URL + messagesModel.getAudioFile();
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mMediaPlayer.setDataSource(AudioDataSource);
                        mMediaPlayer.prepareAsync();
                        mMediaPlayer.setOnPreparedListener(MediaPlayer::start);

                    }

                } catch (IllegalArgumentException | IllegalStateException | IOException e) {
                    e.printStackTrace();
                }

                mMediaPlayer.start();
                audioTotalDurationAudio.setVisibility(View.GONE);
                audioCurrentDurationAudio.setVisibility(View.VISIBLE);

            }
        } else {*//*

            updateAudioProgressBar();
            String AudioDataSource;
            if (mMediaPlayer != null) {

                try {
                    if (FilesManager.isFileAudioExists(mActivity, FilesManager.getAudio(messagesModel.getAudioFile()))) {
                        AudioDataSource = FilesManager.getFileAudioPath(mActivity, messagesModel.getAudioFile());
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mMediaPlayer.setDataSource(AudioDataSource);
                        mMediaPlayer.prepare();
                        mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
                    } else {
                        AudioDataSource = EndPoints.MESSAGE_AUDIO_URL + messagesModel.getAudioFile();
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mMediaPlayer.setDataSource(AudioDataSource);
                        mMediaPlayer.prepareAsync();
                        mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
                    }

                } catch (Exception e) {
                    AppHelper.LogCat("IOException audio recipient " + e.getMessage());
                }


                mMediaPlayer.start();
                audioTotalDurationAudio.setVisibility(View.GONE);
                audioCurrentDurationAudio.setVisibility(View.VISIBLE);

            }
   //     }


    }
*/
    public void playingAudio() {
        try {
            MediaPlayer m = new MediaPlayer();

            try {
                m.setDataSource(audiopath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                m.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            m.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }
    //set up MediaPlayer
       /* MediaPlayer mp = new MediaPlayer();

        try {
            mp.setDataSource(path + File.separator );
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /*  void updateAudioProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            try {
                if (mMediaPlayer.isPlaying()) {
                    long totalDuration = mMediaPlayer.getDuration();
                    long currentDuration = mMediaPlayer.getCurrentPosition();
                    audioCurrentDurationAudio.setText(UtilsTime.getFileTime(currentDuration));
                    int progress = (int) UtilsTime.getProgressPercentage(currentDuration, totalDuration);
                    mAudioCallbacks.onUpdate(progress);
                    mHandler.postDelayed(this, 100);
                }
            } catch (Exception e) {
                AppHelper.LogCat("Exception mUpdateTimeTask " + e.getMessage());
            }

        }
    };*/
    void stopPlayingAudio() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                updateAudioProgressBar();
                mMediaPlayer.stop();
                mMediaPlayer.reset();
              /*  audioSeekBar.setProgress(0);
                audioCurrentDurationAudio.setVisibility(View.GONE);
                audioTotalDurationAudio.setVisibility(View.VISIBLE);
                playBtnAudio.setVisibility(View.VISIBLE);
                pauseBtnAudio.setVisibility(View.GONE);*/


            }

        }

    }

    private void updateAudioProgressBar() {
    }


    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


}
