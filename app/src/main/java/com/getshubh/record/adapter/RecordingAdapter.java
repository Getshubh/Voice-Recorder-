package com.getshubh.record.adapter;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.getshubh.record.R;
import com.getshubh.record.model.Recording;
import com.getshubh.record.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder> {

    private  ArrayList<Recording> recordingArrayList;
    private final Context context;
    private final Handler mHandler = new Handler();
    OnDeleteClick onDeleteClick;
    ViewHolder holder;
    private MediaPlayer mPlayer;
    private boolean isPlaying = false;
    private int last_index = -1;
    private CountDownTimer countDownTimer;
    private int counter;
    private String recordingUri;
    private int lastProgress = 0;
    private int pauseLenth;
    private int position;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            seekUpdate(holder, position);
        }
    };

    public RecordingAdapter(Context context, ArrayList<Recording> recordingArrayList) {
        this.context = context;
        this.recordingArrayList = recordingArrayList;
        onDeleteClick = (OnDeleteClick) context;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.recording_item_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.setIsRecyclable(false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
        this.holder = holder;
        setUpData(holder, position);
        getItemId(position);
    }

    @Override
    public int getItemCount() {
        return recordingArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        Log.d("RecordingAdapter", "getItemId : " + position);
        return super.getItemId(position);

    }

    @Override
    public int getItemViewType(int position) {
        Log.d("RecordingAdapter", "getItemViewType : " + position);
        return super.getItemViewType(position);
    }

    private void setUpData(ViewHolder holder, int position) {
        this.position = position;
        Recording recording = recordingArrayList.get(position);
        holder.tvFileName.setText(recording.getFileName());
        holder.tvRecordingDuration.setText("Duration : " + recording.getAudioFileLength() + " sec");


        if (recording.isPlaying()) {
            showSeekbar(holder, position, recording);
        } else {
            hideseekbar(holder, position);
        }


        manageSeekBar(holder);

        holder.imageViewPlay.setOnClickListener(view -> {


            try {
                Log.d("RecordingAdapter", "position " + position);
                recordingUri = recording.getUri();
                if (isPlaying) {
                    stopPlaying();

                    if (position == last_index) {
                        Log.d("RecordingAdapter", "stopPlaying first loop " + position);
                        recording.setPlaying(false);
                        stopPlaying();
                        notifyItemChanged(position);
                    } else {
                        Log.d("RecordingAdapter", "stopPlaying second loop " + position);
                        markAllPaused(position);
                        recording.setPlaying(true);

                        startPlaying(recording, position);
                        last_index = position;
                        notifyItemChanged(position);
                    }
                } else {
                    //hideDelete(holder, true);
                    Log.d("RecordingAdapter", "startPlying : " + position);
                    holder.ivPause.setImageResource(R.drawable.ic_pause);
                    startPlaying(recording, position);
                    recording.setPlaying(true);
                    holder.seekBar.setMax(mPlayer.getDuration());
                    Log.d("isPlayin", "False");
                    notifyItemChanged(position);
                    last_index = position;
                }
            } catch (Exception e) {
                Log.d("RecordingAdapter", "Exception : " + e.toString());
            }
        });

        holder.ivDelete.setOnClickListener(view -> {

            final Dialog dialog = new Dialog(context);

            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.requestFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog);

            ImageView ivOk = dialog.findViewById(R.id.iv_ok);
            ImageView ivCancel = dialog.findViewById(R.id.iv_cancel);

            dialog.show();

            ivOk.setOnClickListener(view12 -> {
                // int position = getAdapterPosition();
                //Recording recording = recordingArrayList.get(position);

                recordingUri = recording.getUri();
                File fdelete = new File(recordingUri);
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        System.out.println("file Deleted :" + recordingUri);
                    } else {
                        System.out.println("file not Deleted :" + recordingUri);
                    }
                    refreshList();
                    if (dialog != null)
                        dialog.dismiss();

                }
            });

            ivCancel.setOnClickListener(view1 -> dialog.dismiss());


        });

        holder.ivPause.setOnClickListener(view -> {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.pause();
                pauseLenth = mPlayer.getCurrentPosition();
                holder.ivPause.setImageResource(R.drawable.ic_resume);
            } else {
                if (mPlayer != null) {
                    mPlayer.start();
                    mPlayer.seekTo(pauseLenth);
                    holder.ivPause.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        holder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.getInstance().shareAudioFile(Environment.getExternalStorageDirectory() + "/Audio" + File.separator + recording.getFileName(), context);
            }
        });
    }

    private void showSeekbar(ViewHolder holder, int position, Recording recording) {
       // Glide.with(context).load(R.drawable.ic_speak_block_white).into(holder.imageViewPlay);
        holder.imageViewPlay.setImageResource(R.drawable.ic_speak_block_white);
        TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
        holder.seekBar.setVisibility(View.VISIBLE);
        holder.rlProgress.setVisibility(View.VISIBLE);
        holder.tvEndTime.setText(recording.getAudioFileLength());
        holder.tvRecordingDuration.setVisibility(View.GONE);
        // hideDelete(holder,true);
        Log.d("RecordingAdapter", "position : " + position);
        seekUpdate(holder, position);
    }

    private void hideseekbar(ViewHolder holder, int position) {
        //Glide.with(context).load(R.drawable.ic_voice_white).into(holder.imageViewPlay);
            holder.imageViewPlay.setImageResource(R.drawable.ic_voice_white);
        TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
        holder.seekBar.setVisibility(View.GONE);
        holder.rlProgress.setVisibility(View.GONE);
        holder.ivPause.setImageResource(R.drawable.ic_pause);
        holder.tvRecordingDuration.setVisibility(View.VISIBLE);
    }

    public void manageSeekBar(ViewHolder holder) {
        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (mPlayer != null && fromUser) {
                    mPlayer.seekTo(progress);
                    Log.d("RecordingAdapter", "onProgressChanged by User");
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

    private void markAllPaused(int position) {

        for (int i = 0; i < recordingArrayList.size(); i++) {
            if (i != position) {
                recordingArrayList.get(i).setPlaying(false);
                recordingArrayList.set(i, recordingArrayList.get(i));
                hideseekbar(holder, position);
                notifyItemChanged(position);

            }
        }

    }

    private void seekUpdate(ViewHolder holder, int position) {
        Log.d("RecordingAdapter", "seekUpdate");
        if (mPlayer != null) {
            int mCurrentPosition = mPlayer.getCurrentPosition();
            holder.seekBar.setMax(mPlayer.getDuration());
            holder.seekBar.setProgress(mCurrentPosition);
            lastProgress = mCurrentPosition;
            holder.tvStartTime.setText(Utils.getInstance().milliSecondsToTimer(mCurrentPosition));
        }

        mHandler.postDelayed(runnable, 100);
    }

    private void stopPlaying() {
        try {
            if (mPlayer != null)
                mPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPlayer = null;
        isPlaying = false;
    }

    private void startPlaying(final Recording audio, final int position) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(recordingUri);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("LOG_TAG", "prepare() failed");
        }
        //showing the pause button
        holder.seekBar.setMax(mPlayer.getDuration());
        isPlaying = true;

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //hideDelete(holder,false);
                audio.setPlaying(false);
                notifyItemChanged(position);
                isPlaying = false;

            }
        });
    }

    public void stopPlayingAudio(boolean isScrolling) {

        if (isPlaying) {
            stopPlaying();
            hideseekbar(holder, position);
            recordingArrayList.get(position).setPlaying(false);
            recordingArrayList.set(position, recordingArrayList.get(position));
            notifyItemChanged(position);
            Toast.makeText(context, isScrolling ? context.getString(R.string.recording_stopped_message) : context.getString(R.string.fragment_destroy_message), Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshList() {
        onDeleteClick.onItemRefresh();
    }

    public interface OnDeleteClick {
        void onItemRefresh();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout rlProgress;
        ImageView imageViewPlay, ivDelete, ivPause, ivShare;
        SeekBar seekBar;
        TextView tvFileName, tvEndTime, tvRecordingDuration, tvStartTime;


        @SuppressWarnings("LambdaCanBeReplacedWithAnonymous")
        public ViewHolder(View itemView) {
            super(itemView);

            imageViewPlay = itemView.findViewById(R.id.iv_play);
            seekBar = itemView.findViewById(R.id.seekBar);
            tvFileName = itemView.findViewById(R.id.tv_recording_name);
            ivDelete = itemView.findViewById(R.id.ic_delete);
            ivPause = itemView.findViewById(R.id.ic_pause);
            ivShare = itemView.findViewById(R.id.ic_share);
            tvEndTime = itemView.findViewById(R.id.tv_end_time);
            tvStartTime = itemView.findViewById(R.id.tv_start_time);
            tvRecordingDuration = itemView.findViewById(R.id.tv_recording_duration);
            rlProgress = itemView.findViewById(R.id.rl_progress);


        }


    }

    public void updateList(ArrayList<Recording> recordingArrayList){
        this.recordingArrayList = recordingArrayList;
        notifyDataSetChanged();
    }
}

