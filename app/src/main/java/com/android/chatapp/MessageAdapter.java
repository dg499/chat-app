package com.android.chatapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    Friends friends;
    Context context;
    AudioCallbacks mAudioCallbacks;
    String audiopath = "";
    Messages msgsList;
    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mUserDatabasem;
    private StorageReference AudioStorage;
    private MediaPlayer mPlayer;
    private boolean isPlaying = false;
    private int lastProgress = 0;
    private Handler mHandler = new Handler();
    private int last_index = -1;
    public MessageAdapter(Context context, List<Messages> mMessageList) {
        this.context = context;
        this.mMessageList = mMessageList;
    }
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }
    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(MessageViewHolder viewHolder, int i) {
        Messages msgsList = mMessageList.get(i);
        Log.e("msgsList", msgsList + "");
        String from_user = msgsList.getFrom();
        String message_type = msgsList.getType();
        String datenew = msgsList.getDate();
        //String date = Long.toString(datenew);
        final String[] name = new String[1];
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        // mUserDatabasem = FirebaseDatabase.getInstance().getReference().child("messages").child(from_user);
        AudioStorage = FirebaseStorage.getInstance().getReference();
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name[0] = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();
                viewHolder.displayName.setText(name[0]);
                viewHolder.time_text_layout.setText(datenew);
                Picasso.with(viewHolder.profileImage.getContext()).load(image)
                        .placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        Log.e("message_type", message_type + "");
        if (message_type.equals("text")) {
            viewHolder.messageText.setText(msgsList.getMessage());
            viewHolder.messageImage.setVisibility(View.GONE);
            viewHolder.audio_layout.setVisibility(View.GONE);
        } else if (message_type.equals("image")) {
            viewHolder.messageText.setVisibility(View.GONE);
            viewHolder.audio_layout.setVisibility(View.GONE);
            Picasso.with(viewHolder.profileImage.getContext()).load(msgsList.getMessage())
                    .placeholder(R.drawable.default_avatar).into(viewHolder.messageImage);
        } else if (message_type.equals("audio")) {
            viewHolder.messageText.setVisibility(View.GONE);
            viewHolder.messageImage.setVisibility(View.GONE);
            viewHolder.audio_layout.setVisibility(View.VISIBLE);
        }
        setUpData(viewHolder, i);
        viewHolder.messageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageDialog(msgsList.getMessage(), name[0]);
            }
        });
    }
    private void setUpData(MessageViewHolder holder, int position) {
        Messages msgsList = mMessageList.get(position);
        if (msgsList.isPlaying()) {
            holder.pauseBtnAudio.setVisibility(View.VISIBLE);
            holder.playBtnAudio.setVisibility(View.GONE);
            //  holder.imageViewPlay.setImageResource(R.drawable.ic_pause);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            holder.audioSeekBar.setVisibility(View.VISIBLE);
            holder.seekUpdation(holder);
        } else {
            holder.playBtnAudio.setVisibility(View.VISIBLE);
            holder.pauseBtnAudio.setVisibility(View.GONE);
            // holder.imageViewPlay.setImageResource(R.drawable.ic_play);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            holder.audioSeekBar.setVisibility(View.VISIBLE);
        }
        holder.manageSeekBar(holder);
    }
    private void showImageDialog(String img1, String name) {
        final Dialog dialog1 = new Dialog(context);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setContentView(R.layout.dialog_branding_img);
        TextView title = (TextView) dialog1.findViewById(R.id.name_tv);
        ImageView img11 = (ImageView) dialog1.findViewById(R.id.img1);
        title.setText(name);
        if (img1.isEmpty()) {
            img11.setImageResource(R.drawable.default_avatar);
        } else {
            Picasso.with(context).load(img1)
                    .placeholder(R.drawable.default_avatar).into(img11);
        }
        TextView ok = (TextView) dialog1.findViewById(R.id.btn_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog1.dismiss();
            }
        });
        dialog1.show();
    }
    public void playingAudio(String audiopath) {
        Messages messages = new Messages();
        Log.e("messages", messages + "");
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
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText, audioCurrentDurationAudio, audioTotalDurationAudio;
        public CircleImageView profileImage;
        public TextView displayName, time_text_layout;
        public ImageView messageImage;
        public LinearLayout audio_layout, retryUploadAudio, retryDownloadAudio;
        ImageView userAudioImage;
        ImageButton playBtnAudio, pauseBtnAudio;
        MessageViewHolder viewHolder;
        SeekBar audioSeekBar;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                seekUpdation(viewHolder);
            }
        };
        public MessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            displayName = (TextView) view.findViewById(R.id.name_text_layout);
            time_text_layout = (TextView) view.findViewById(R.id.time_text_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);
            audio_layout = (LinearLayout) view.findViewById(R.id.audio_layout);
            userAudioImage = (ImageView) view.findViewById(R.id.audio_user_image);
            playBtnAudio = (ImageButton) view.findViewById(R.id.play_btn_audio);
            pauseBtnAudio = (ImageButton) view.findViewById(R.id.pause_btn_audio);
            audioSeekBar = (SeekBar) view.findViewById(R.id.audio_progress_bar);
            audioCurrentDurationAudio = (TextView) view.findViewById(R.id.audio_current_duration);
            audioTotalDurationAudio = (TextView) view.findViewById(R.id.audio_total_duration);
            playBtnAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Messages recording = mMessageList.get(position);
                    audiopath = recording.getMessage();
                    if (isPlaying) {
                        stopPlaying();
                        if (position == last_index) {
                            recording.setPlaying(false);
                            stopPlaying();
                            notifyItemChanged(position);
                        } else {
                            markAllPaused();
                            recording.setPlaying(true);
                            notifyItemChanged(position);
                            startPlaying(recording, position);
                            last_index = position;
                        }
                    } else {
                        startPlaying(recording, position);
                        recording.setPlaying(true);
                        audioSeekBar.setMax(mPlayer.getDuration());
                        Log.d("isPlayin", "False");
                        notifyItemChanged(position);
                        last_index = position;
                    }
                }
            });
            pauseBtnAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
        private void markAllPaused() {
            for (int i = 0; i < mMessageList.size(); i++) {
                mMessageList.get(i).setPlaying(false);
                mMessageList.set(i, mMessageList.get(i));
            }
            notifyDataSetChanged();
        }
        public void manageSeekBar(MessageViewHolder holder) {
            holder.audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mPlayer != null && fromUser) {
                        mPlayer.seekTo(progress);
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }
        private void seekUpdation(MessageViewHolder holder) {
            this.viewHolder = holder;
            if (mPlayer != null) {
                int mCurrentPosition = mPlayer.getCurrentPosition();
                holder.audioSeekBar.setMax(mPlayer.getDuration());
                holder.audioSeekBar.setProgress(mCurrentPosition);
                lastProgress = mCurrentPosition;
            }
            mHandler.postDelayed(runnable, 100);
        }
        private void stopPlaying() {
            try {
                mPlayer.release();
                viewHolder.audioSeekBar.setProgress(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mPlayer = null;
            isPlaying = false;
        }
        private void startPlaying(final Messages audio, final int position) {
            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(audiopath);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e("LOG_TAG", "prepare() failed");
            }
            //showing the pause button
            audioSeekBar.setMax(mPlayer.getDuration());
            isPlaying = true;
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    audio.setPlaying(false);
                    notifyItemChanged(position);
                }
            });
        }
    }
}
