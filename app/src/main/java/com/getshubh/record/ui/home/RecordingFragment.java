package com.getshubh.record.ui.home;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.getshubh.record.MainActivity;
import com.getshubh.record.R;
import com.getshubh.record.TransparentPermissionActivity;
import com.getshubh.record.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class RecordingFragment extends Fragment {

    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    private static final int CHANNEL_ID_STOP_NOTIFICATION = 102;
    private static final int CHANNEL_ID_START_NOTIFICATION = 101;
    private static final String LOG_TAG = "AudioRecordering";
    private static final int REQUEST_CODE = 0;
    private static String mFileCompletePath = null;
    private static String mFileName;
    private final String TAG = RecordingFragment.class.getSimpleName();
    TextView tvSS, tvStart, tvStop, tvPLay, tvStopPlay;
    TextView tvVoiceRecText, tvVoiceRecDot;
    private ImageView startBtn, stopBtn, playBtn, stopPlayBtn;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private int counter;
    private CountDownTimer countDownTimer;
    private MediaPlayer mMediaPlayer;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;

    private String recorderTitle;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_record_audio, container, false);

        startBtn = root.findViewById(R.id.btnRecord);
        stopBtn = root.findViewById(R.id.btnStop);
        playBtn = root.findViewById(R.id.btnPlay);
        stopPlayBtn = root.findViewById(R.id.btnStopPlay);
        //pauseBtn = root.findViewById(R.id.btn_pause_resume);
        tvSS = root.findViewById(R.id.tv_timer_ss);
        tvVoiceRecText = root.findViewById(R.id.tv_voice_rec_text);
        tvVoiceRecDot = root.findViewById(R.id.tv_voice_rec_dot);
        tvStart = root.findViewById(R.id.tv_start);
        tvStop = root.findViewById(R.id.tv_stop);
        tvPLay = root.findViewById(R.id.tv_play);
        tvStopPlay = root.findViewById(R.id.tv_stop_play);

        stopBtn.setEnabled(false);
        playBtn.setEnabled(false);
        stopPlayBtn.setEnabled(false);


        startBtn.setTag(R.drawable.ic_start_blue);
        stopBtn.setTag(R.drawable.ic_stop_grey);
        playBtn.setTag(R.drawable.ic_play_grey);
        stopPlayBtn.setTag(R.drawable.ic_stop_grey);
        //pauseBtn.setTag(R.drawable.ic_pause);


        stopBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_disable_grey));
        startBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        playBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_disable_grey));
        stopPlayBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_disable_grey));


        tvStart.setTextColor(getResources().getColor(R.color.colorPrimary));
        tvStop.setTextColor(getResources().getColor(R.color.color_disable_grey));
        tvPLay.setTextColor(getResources().getColor(R.color.color_disable_grey));
        tvStopPlay.setTextColor(getResources().getColor(R.color.color_disable_grey));
        recorderTitle = getResources().getString(R.string.voice_recorder_title);

        if (mRecorder != null) {
            startBtnUIupdate();
        }
        initListener();
        return root;
    }

    private void initListener() {
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.getTag().equals(R.drawable.ic_start_blue))
                    startButtonCLicked();
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag().equals(R.drawable.ic_stop_blue))
                    stopButtonClicked();
            }
        });
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag().equals(R.drawable.ic_play_blue))
                    playButtonClicked();
            }
        });
        stopPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag().equals(R.drawable.ic_stop_blue))
                    stopPlayButtonClicked();
            }
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setToolbar(getContext().getString(R.string.title_home));
    }

    private void stopPlayButtonClicked() {

        stopMediaPlayer();
        mPlayer.release();
        stopTimer();
        mPlayer = null;

        stopPlayBtnUIUpdate();

        Toast.makeText(getActivity(), "Audio Stopped", Toast.LENGTH_SHORT).show();

    }

    private void stopPlayBtnUIUpdate() {
        recorderTitle = getResources().getString(R.string.app_name);
        tvVoiceRecText.setText(recorderTitle);
        stopBtn.setEnabled(false);
        startBtn.setEnabled(true);
        playBtn.setEnabled(true);
        stopPlayBtn.setEnabled(false);


        startBtn.setTag(R.drawable.ic_start_blue);
        stopBtn.setTag(R.drawable.ic_stop_grey);
        playBtn.setTag(R.drawable.ic_play_blue);
        stopPlayBtn.setTag(R.drawable.ic_stop_grey);


        stopBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_disable_grey));
        startBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        playBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        stopPlayBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_disable_grey));


        tvStart.setTextColor(getResources().getColor(R.color.colorPrimary));
        tvStop.setTextColor(getResources().getColor(R.color.color_disable_grey));
        tvPLay.setTextColor(getResources().getColor(R.color.colorPrimary));
        tvStopPlay.setTextColor(getResources().getColor(R.color.color_disable_grey));

    }

    private void playButtonClicked() {
        playBatteryFullSound();

        playBtnUIUpdate();
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileCompletePath);
            mPlayer.prepare();
            mPlayer.start();
            showTimer();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlayButtonClicked();
                }
            });
            Toast.makeText(getActivity(), "Playing Recorded Audio", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void playBtnUIUpdate() {
        recorderTitle = getResources().getString(R.string.play_voice_recorder_title) + " " + mFileName;
        tvVoiceRecText.setText(recorderTitle);

        stopBtn.setEnabled(false);
        startBtn.setEnabled(true);
        playBtn.setEnabled(false);
        stopPlayBtn.setEnabled(true);


        startBtn.setTag(R.drawable.ic_start_grey);
        stopBtn.setTag(R.drawable.ic_stop_grey);
        playBtn.setTag(R.drawable.ic_play_grey);
        stopPlayBtn.setTag(R.drawable.ic_stop_blue);


        stopBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_disable_grey));
        startBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_disable_grey));
        playBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_disable_grey));
        stopPlayBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary));


        tvStart.setTextColor(getResources().getColor(R.color.color_disable_grey));
        tvStop.setTextColor(getResources().getColor(R.color.color_disable_grey));
        tvPLay.setTextColor(getResources().getColor(R.color.color_disable_grey));
        tvStopPlay.setTextColor(getResources().getColor(R.color.colorPrimary));

    }

    private void stopButtonClicked() {

        stopButtonUIUpdate();
        stopTimer();
        stopMediaPlayer();
        stopRecording();
        stopNotification();
//        Toast.makeText(getActivity(), "Recording Stopped", Toast.LENGTH_LONG).show();
    }

    private void stopButtonUIUpdate() {
        recorderTitle = getResources().getString(R.string.app_name);
        tvVoiceRecText.setText(recorderTitle);

        stopBtn.setEnabled(false);
        startBtn.setEnabled(true);
        playBtn.setEnabled(true);
        stopPlayBtn.setEnabled(true);

        startBtn.setTag(R.drawable.ic_start_blue);
        stopBtn.setTag(R.drawable.ic_stop_grey);
        playBtn.setTag(R.drawable.ic_play_blue);
        stopPlayBtn.setTag(R.drawable.ic_stop_grey);

        stopBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_disable_grey));
        startBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        playBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        stopPlayBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_disable_grey));


        tvStart.setTextColor(getResources().getColor(R.color.colorPrimary));
        tvStop.setTextColor(getResources().getColor(R.color.color_disable_grey));
        tvPLay.setTextColor(getResources().getColor(R.color.colorPrimary));
        tvStopPlay.setTextColor(getResources().getColor(R.color.color_disable_grey));

    }

    private void startButtonCLicked() {
        if (checkPermissions()) {
            playBatteryFullSound();
            recordingStarted();
        } else {
            requestPermissions();
        }
    }

    private void playBatteryFullSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(getActivity(), notification);
            final AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null && audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                //mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private void stopMediaPlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer = null;
        }
    }

    private void recordingStarted() {
        recorderTitle = getResources().getString(R.string.start_voice_recorder_title);
        tvVoiceRecText.setText(recorderTitle);
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Audio");
        if (!file.exists()) {
            file.mkdirs();

        }
        mFileName = Utils.getInstance().getFileNameFormat() + ".mp3";
        mFileCompletePath = file.getAbsolutePath() + File.separator + mFileName;


        startBtnUIupdate();

        showNotification();
        startRecording();


        showTimer();
        Toast.makeText(getActivity(), "Recording Started", Toast.LENGTH_SHORT).show();
    }

    private void startBtnUIupdate() {
        stopBtn.setEnabled(true);
        startBtn.setEnabled(false);
        playBtn.setEnabled(false);
        stopPlayBtn.setEnabled(false);


        startBtn.setTag(R.drawable.ic_start_grey);
        stopBtn.setTag(R.drawable.ic_stop_blue);
        playBtn.setTag(R.drawable.ic_play_grey);
        stopPlayBtn.setTag(R.drawable.ic_stop_grey);

        stopBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        startBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_disable_grey));
        playBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_disable_grey));
        stopPlayBtn.setColorFilter(ContextCompat.getColor(getActivity(), R.color.color_disable_grey));


        tvStart.setTextColor(getResources().getColor(R.color.color_disable_grey));
        tvStop.setTextColor(getResources().getColor(R.color.colorPrimary));
        tvPLay.setTextColor(getResources().getColor(R.color.color_disable_grey));
        tvStopPlay.setTextColor(getResources().getColor(R.color.color_disable_grey));

    }


    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            //playSound();
            playBatteryFullSound();
            saveValues();
            showRecordedNotification();
        }
    }

    private void showRecordedNotification() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_voice_white)
                .setContentTitle(mFileName)
                //.setContentText(mFileName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getActivity());

// notificationId is a unique int for each notification that you must define
        notificationManagerCompat.notify(CHANNEL_ID_STOP_NOTIFICATION, builder.build());
    }

    private void saveValues() {
        Toast.makeText(getActivity(), "Audio saved at " + mFileCompletePath, 10).show();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DATA, mFileCompletePath);
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
        values.put(MediaStore.Audio.Media.TITLE, "Audio");
        //store audio recorder file in the external content uri
        getActivity().getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        mRecorder.setOutputFile(mFileCompletePath);
        try {
            mRecorder.prepare();
        } catch (Exception e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mRecorder.start();

    }

    private void stopTimer() {
        resetText();
        counter = 0;
        if (countDownTimer != null)
            countDownTimer.cancel();
    }

    private void showTimer() {
        countDownTimer = new CountDownTimer(5000000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvSS.setText(getTimeFormat(counter));
                counter++;

                if (recorderTitle != null) {
                    if (recorderTitle.contains(getResources().getString(R.string.start_voice_recorder_title)) || recorderTitle.contains(getResources().getString(R.string.play_voice_recorder_title))) {
                        tvVoiceRecDot.setVisibility(View.VISIBLE);
                        String dotValue = tvVoiceRecDot.getText().toString();
                        if (dotValue.equals(" . . .")) {
                            dotValue = dotValue.replaceAll(" . . .", "");
                        }
                        dotValue = dotValue + " .";

                        tvVoiceRecDot.setText(dotValue);
                    }
                }
            }

            @Override
            public void onFinish() {
                resetText();

            }
        }.start();
    }

    private void resetText() {
        tvSS.setText("00:00:00");
        tvVoiceRecDot.setVisibility(View.GONE);
        tvVoiceRecDot.setText("");
    }

    private String getTimeFormat(int sec) {
        Date d = new Date(sec * 1000L);
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss"); // HH for 0-23
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String time = df.format(d);
        return time;
    }


    private void stopNotification() {
        ((NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(101);
    }


    public boolean checkPermissions() {
        int result = ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getActivity(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {

        Intent intent = new Intent(getActivity(), TransparentPermissionActivity.class);
        startActivity(intent);
        /*  ActivityCompat.requestPermissions(AudioRecorderActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);*/
    }


    private void showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), "CHANNEL_ID")
                    .setSmallIcon(R.drawable.ic_voice_white)
                    .setContentTitle("Recording Started")
//                    .setContentText(description)
                    .setColor(getActivity().getColor(R.color.colorPrimary))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setShowWhen(true)
                    .setWhen(System.currentTimeMillis())
                    .setUsesChronometer(true)
                    .setOngoing(true);


            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getActivity());

// notificationId is a unique int for each notification that you must define
            notificationManagerCompat.notify(CHANNEL_ID_START_NOTIFICATION, builder.build());


        }

    }

    private void playSound() {
        try {
            MediaPlayer player = MediaPlayer.create(getContext(), R.raw.beep);
            player.start();
        } catch (Exception e) {
            Log.i(TAG, e.toString());

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null)
            stopButtonClicked();
    }

/*
    public class ButtonAnimationListener implements Animation.AnimationListener{

        private View view;
        public ButtonAnimationListener(View view){
            this.view = view;
        }


        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            switch (view.getId()){
                case R.id.btnRecord:
                    if (view.getTag().equals(R.drawable.ic_start_blue))
                        startButtonCLicked();
                    break;
                case R.id.btnStop:
                    if (view.getTag().equals(R.drawable.ic_stop_blue))
                        stopButtonClicked();
                    break;
                case R.id.btnPlay:
                    if (view.getTag().equals(R.drawable.ic_play_blue))
                        playButtonClicked();
                    break;
                case R.id.btnStopPlay:
                    if (view.getTag().equals(R.drawable.ic_stop_blue))
                        stopPlayButtonClicked();
                    break;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }*/
}