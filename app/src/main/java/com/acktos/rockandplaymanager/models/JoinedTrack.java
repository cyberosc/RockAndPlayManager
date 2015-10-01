package com.acktos.rockandplaymanager.models;

/**
 * Created by Acktos on 9/22/15.
 */
public class JoinedTrack {


    private String id;
    private String trackId;
    private String state;
    private String order;
    private String artistName;
    private String duration;
    private String thumbAlbum;
    private String trackName;
    private String userId;
    private String userName;


    public static final String STATE_APPROVED="approved";
    public static final String STATE_PENDING_APPROVAL="pending_approval";
    public static final String STATE_PLAYING="playing";
    public static final String STATE_COMPLETED="completed";

    public JoinedTrack(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
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

    public String getThumbAlbum() {
        return thumbAlbum;
    }

    public void setThumbAlbum(String thumbAlbum) {
        this.thumbAlbum = thumbAlbum;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
