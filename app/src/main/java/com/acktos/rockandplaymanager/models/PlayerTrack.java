package com.acktos.rockandplaymanager.models;

/**
 * Created by Acktos on 9/23/15.
 */
public class PlayerTrack extends BaseModel {

    private String trackName;
    private String artistName;
    private String thumbAlbum;
    private String duration;
    private String state;
    private String message;
    private String startTime;
    private String pauseTime;
    private String endTime;


    public static final String STATE_PLAYER_PLAY="play";
    public static final String STATE_PLAYER_PAUSE="pause";
    public static final String STATE_PLAYER_STOP="stop";


    public static final String STATE_TRACK_START="track_start";
    public static final String STATE_TRACK_END="track_end";
    public static final String STATE_TRACK_BECOME_ACTIVE="track_become_active";
    public static final String STATE_TRACK_END_OF_CONTEXT="track_end_of_context";

    public PlayerTrack(){}

    public PlayerTrack(String trackName, String artistName,String thumbAlbum, String duration, String state, String startTime){

        this.trackName=trackName;
        this.artistName=artistName;
        this.thumbAlbum=thumbAlbum;
        this.duration=duration;
        this.state=state;
        this.startTime=startTime;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getPauseTime() {
        return pauseTime;
    }

    public void setPauseTime(String pauseTime) {
        this.pauseTime = pauseTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getThumbAlbum() {
        return thumbAlbum;
    }

    public void setThumbAlbum(String thumbAlbum) {
        this.thumbAlbum = thumbAlbum;
    }
}
