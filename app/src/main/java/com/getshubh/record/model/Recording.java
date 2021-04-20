package com.getshubh.record.model;

public class Recording {

    String Uri, fileName, audioFileLength;
    boolean isPlaying = false;


    public Recording(String uri, String fileName, boolean isPlaying, String audioFileLength) {
        Uri = uri;
        this.fileName = fileName;
        this.isPlaying = isPlaying;
        this.audioFileLength = audioFileLength;
    }

    public String getUri() {
        return Uri;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }

    public String getAudioFileLength() {
        return audioFileLength;
    }

    public void setAudioFileLength(String audioFileLength) {
        this.audioFileLength = audioFileLength;
    }
}