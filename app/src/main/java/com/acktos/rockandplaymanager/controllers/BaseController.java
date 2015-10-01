package com.acktos.rockandplaymanager.controllers;

/**
 * Created by Acktos on 8/11/15.
 */
public class BaseController {

    public static final String TAG="rockandplay-debug";
    public static final String SHARED_PREFERENCES="rockandplay-shared";
    public static final String SHARED_SPOTIFY_ACCESS_TOKEN="spotify_access_token";

    // Spotify Credentials
    public static final String CLIENT_ID = "3dfc10d1cf314ce89bc992c8ac864052";
    public static final String REDIRECT_URI = "coffeplay-protocol://callback";

    //FireBase Credentials
    public static final String FIREBASE_URL="https://coffe-play.firebaseio.com/";
    public static final String TABLE_USERS="users";
    public static final String TABLE_SESSIONS="sessions";
    public static final String TABLE_TRACKS="tracks";
    public static final String TABLE_PLAYER_STATE="playerState";



    // Api endpoints

    public enum API{

        GET_SPOTIFY_USER("https://api.spotify.com/v1/me"),
        GET_SPOTIFY_TRACK("https://api.spotify.com/v1/tracks/");

        private final String url;

        API (String uri){
            url=uri;
        }

        public String getUrl(){
            return url;
        }
    }
}
