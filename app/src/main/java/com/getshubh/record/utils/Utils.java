package com.getshubh.record.utils;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.getshubh.record.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    private static Utils utils = null;

    private Utils() {

    }

    public static Utils getInstance() {
        if (utils == null)
            utils = new Utils();
        return utils;
    }

    public String getAudioFileLength(String path, boolean stringFormat, Context context) {

        Uri uri = Uri.parse(path);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        if (uri != null) {
            mmr.setDataSource(context, uri);
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (duration != null) {
                int millSecond = Integer.parseInt(duration);

                if (millSecond < 0)
                    return String.valueOf(0); // if some error then we say duration is zero

                if (!stringFormat) return String.valueOf(millSecond);

                int hours, minutes, seconds = millSecond / 1000;

                hours = (seconds / 3600);
                minutes = (seconds / 60) % 60;
                seconds = seconds % 60;

                StringBuilder stringBuilder = new StringBuilder();
                if (hours > 0 && hours < 10) stringBuilder.append("0").append(hours).append(":");
                else if (hours > 0) stringBuilder.append(hours).append(":");

                if (minutes < 10) stringBuilder.append("0").append(minutes).append(":");
                else stringBuilder.append(minutes).append(":");

                if (seconds < 10) stringBuilder.append("0").append(seconds);
                else stringBuilder.append(seconds);

                return stringBuilder.toString();
            } else
                return "";
        } else
            return "";
    }

    public String getFilePath() {

        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Audio" + File.separator;
    }

    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public String getFileNameFormat() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yy, hh.mm.ss a");
        return formatter.format(date);
    }


    public void shareAudioFile(String filepath, Context context) {

        try {
//        Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", new File(filepath));
            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", new File(filepath));
            //  Uri uri = Uri.fromFile(new File(filepath));
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/*");
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(Intent.createChooser(share, "Share Audio"));
        }catch (IllegalArgumentException i){
            Toast.makeText(context, context.getString(R.string.permission_denied_error), Toast.LENGTH_SHORT).show();
        }
    }
}
