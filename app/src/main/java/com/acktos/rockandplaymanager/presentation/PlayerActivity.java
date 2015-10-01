package com.acktos.rockandplaymanager.presentation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.acktos.rockandplaymanager.R;
import com.acktos.rockandplaymanager.android.DateTimeUtils;
import com.acktos.rockandplaymanager.controllers.BaseController;
import com.acktos.rockandplaymanager.controllers.TracksController;
import com.acktos.rockandplaymanager.models.JoinedTrack;
import com.acktos.rockandplaymanager.models.PlayerTrack;
import com.acktos.rockandplaymanager.models.Track;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity
        implements PlayerNotificationCallback,ConnectionStateCallback,View.OnClickListener {

    //Attributes
    String spotifyAccessToken;
    String sessionId;
    List<JoinedTrack> joinedTracks;
    String playerState;
    String trackState;
    JoinedTrack currentJoinedTrack=null;
    PlayerProgressTask playerProgressTask;
    Track trackInfo;
    long startTrackTimestamp;

    //Components
    private Player mPlayer;
    Firebase mFirebaseRef;
    private TracksController tracksController;

    //Android Utils
    SharedPreferences mPrefs;// Handle to SharedPreferences for this APP
    SharedPreferences.Editor mEditor;// Handle to a SharedPreferences editor

    //UI References
    ImageView imgPlayButton;
    ImageView imgNextButton;
    ImageView imgPrevButton;
    ImageView imgAlbum;
    TextView txtTrackProgressTime;
    TextView txtTrackDuration;
    ProgressBar progressbarTrack;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //Initialize attributes
        joinedTracks=new ArrayList<>();
        playerState= PlayerTrack.STATE_PLAYER_STOP;

        //Initialize Android Utils
        mPrefs = getSharedPreferences(BaseController.SHARED_PREFERENCES, Context.MODE_PRIVATE);// Open Shared Preferences
        mEditor = mPrefs.edit();// Get an editor

        if (mPrefs.contains(BaseController.SHARED_SPOTIFY_ACCESS_TOKEN)) {
            spotifyAccessToken= mPrefs.getString(BaseController.SHARED_SPOTIFY_ACCESS_TOKEN, "");
        }

        //Initialize UI
        imgPlayButton=(ImageView) findViewById(R.id.img_play_button);
        imgNextButton=(ImageView) findViewById(R.id.img_next_button);
        imgPrevButton=(ImageView) findViewById(R.id.img_prev_button);
        progressbarTrack=(ProgressBar) findViewById(R.id.progress_track);
        imgAlbum=(ImageView) findViewById(R.id.img_album);
        txtTrackDuration=(TextView) findViewById(R.id.txt_track_time);
        txtTrackProgressTime=(TextView) findViewById(R.id.txt_progress_time);
        actionBar=getSupportActionBar();


        // Setup components
        tracksController=new TracksController();
        setupSpotifyPlayer();
        setupFirebase();
        setupPlayListListener();


        // Set onclick listeners
        imgPlayButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()){

            case R.id.img_play_button:
                startPlaying();
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        playerState=PlayerTrack.STATE_PLAYER_STOP;
        updateFirebasePlayerState();
        super.onDestroy();
    }


    /* *************************************
     *              FIREBASE               *
     ***************************************/

    private void setupFirebase(){

        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase(BaseController.FIREBASE_URL);//Create the Firebase ref

    }

    private void setupPlayListListener(){

        sessionId="0"; //TODO: get session ID

        Firebase tracksRef = new Firebase(
                BaseController.FIREBASE_URL+BaseController.TABLE_SESSIONS+"/"+sessionId+"/"+BaseController.TABLE_TRACKS);

        tracksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(BaseController.TAG, dataSnapshot.getValue().toString());

                for (DataSnapshot trackSnapshot : dataSnapshot.getChildren()) {

                    //Log.i(BaseController.TAG, "track_id:" + trackSnapshot.child(Track.KEY_TRACK_ID).getValue().toString());
                    joinedTracks.add(trackSnapshot.getValue(JoinedTrack.class));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

                Log.e(BaseController.TAG, firebaseError.getMessage());
            }
        });

    }


    /* *************************************
     *              SPOTIFY                *
     ***************************************/

    private void setupSpotifyPlayer(){

        Config playerConfig = new Config(this, spotifyAccessToken, BaseController.CLIENT_ID);
        mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                mPlayer.addConnectionStateCallback(PlayerActivity.this);
                mPlayer.addPlayerNotificationCallback(PlayerActivity.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(BaseController.TAG, "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    private JoinedTrack findNextTrackToPlay(){

        JoinedTrack joinedTrack=null;

        if(joinedTracks.size()>0){

            for (JoinedTrack itemTrack: joinedTracks){

                if(itemTrack.getState().equals(JoinedTrack.STATE_APPROVED)){
                    return itemTrack;
                }
            }
        }
        return joinedTrack;
    }

    private void updatePlayNowView(JoinedTrack joinedTrack){

        tracksController.getSpotifyTrack(joinedTrack.getTrackId(), new Response.Listener<Track>() {

            @Override
            public void onResponse(Track track) {
                if (track != null) {

                    //set global var with track info
                    trackInfo=track;
                    //Log.i(BaseController.TAG,"track image:"+track.getImage());
                    //Log.i(BaseController.TAG,"track name:"+track.getName());
                    //Log.i(BaseController.TAG,"track artist:"+track.getArtist());

                    Picasso.with(PlayerActivity.this)
                            .load(track.getImage())
                            .placeholder(R.drawable.bad_album)
                            .into(imgAlbum);

                    actionBar.setTitle(track.getName());
                    actionBar.setSubtitle(track.getArtist());
                    updateFirebasePlayerState();

                } else {
                    Log.i(BaseController.TAG, "get track from spotify is empty");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i(BaseController.TAG, "error try retrieving track info");
            }
        });

    }

    /**
     * Main method for handling the flow of play.
     */
    private void startPlaying(){

        playerState="play";
        currentJoinedTrack=findNextTrackToPlay();

        if(currentJoinedTrack!=null){

            updatePlayNowView(currentJoinedTrack); // set image and titles to UI views

            if(mPlayer!=null) {

                Log.i(BaseController.TAG, "spotify:track:" + currentJoinedTrack.getTrackId());
                mPlayer.play("spotify:track:" + currentJoinedTrack.getTrackId());


            }else{
                Log.i(BaseController.TAG,"player isn´t ready");
            }

        }else{
            playerState=PlayerTrack.STATE_PLAYER_STOP;
            updateFirebasePlayerState();
            Log.i(BaseController.TAG,"No songs to play yet");
        }

    }

    /**
     * Initialize and start progress bar with track duration
     */
    private void startProgressTrack(){


        mPlayer.getPlayerState(new PlayerStateCallback() {
            @Override
            public void onPlayerState(PlayerState playerState) {
                int duration=playerState.durationInMs/1000;
                int position=playerState.positionInMs/1000;

                playerProgressTask=new PlayerProgressTask(duration,position);
                playerProgressTask.execute();
            }
        });
    }

    /**
     * this method saves current player info to firebase "player_state" object
     */
    private void updateFirebasePlayerState(){

        Firebase playerTrackRef = new Firebase(
                BaseController.FIREBASE_URL+BaseController.TABLE_SESSIONS+"/"+sessionId+"/"+BaseController.TABLE_PLAYER_STATE);

        if(trackInfo!=null){
            PlayerTrack playerTrack=new PlayerTrack(
                    trackInfo.getName(),
                    trackInfo.getArtist(),
                    trackInfo.getThumb(),
                    trackInfo.getDuration(),
                    this.playerState,
                    Long.toString(startTrackTimestamp));

            playerTrackRef.setValue(playerTrack, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        Log.i(BaseController.TAG,"Player track could not be saved. " + firebaseError.getMessage());
                    } else {
                        Log.i(BaseController.TAG, "Player track updated successfully.");
                    }
                }
            });
        }else{
            Log.e(BaseController.TAG, "Track info is not available to update player state");
        }


    }


    private void clearCurrentJoinedTrack(){

        int position=joinedTracks.indexOf(currentJoinedTrack);
        Log.i(BaseController.TAG,"position current track:"+position);

        currentJoinedTrack.setState("completed");// TODO: get state from Player Model
        joinedTracks.add(position, currentJoinedTrack);

    }



    @Override
    public void onLoggedIn() {
        Log.d(BaseController.TAG, "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d(BaseController.TAG, "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d(BaseController.TAG, "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d(BaseController.TAG, "Received connection message: " + message);
    }


    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d(BaseController.TAG, "Playback event received: " + eventType.name());
        switch (eventType) {

            case BECAME_ACTIVE:
                trackState= PlayerTrack.STATE_TRACK_BECOME_ACTIVE;
                //Log.i(BaseController.TAG,"set BECOME ACTIVE state");
                break;

            case PLAY:
                startTrackTimestamp=System.currentTimeMillis();
                Log.i(BaseController.TAG,"start track timestamp:"+startTrackTimestamp);
                startProgressTrack();
                updateFirebasePlayerState();
                break;

            case TRACK_START:
               //Log.i(BaseController.TAG,"set START state");
                trackState= PlayerTrack.STATE_TRACK_START;
                break;

            case TRACK_END:
                //Log.i(BaseController.TAG,"set END state");
                trackState= PlayerTrack.STATE_TRACK_END;
                clearCurrentJoinedTrack();
                break;

            case TRACK_CHANGED:
                //Log.i(BaseController.TAG,"set TRACK CHANGED state");
                if(trackState.equals(PlayerTrack.STATE_TRACK_END)){
                    startPlaying();
                }
                break;

            case END_OF_CONTEXT:
                //Log.i(BaseController.TAG,"set END OF CONTEXT state");
                trackState= PlayerTrack.STATE_TRACK_END_OF_CONTEXT;
                break;

            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d(BaseController.TAG, "Playback error received: " + errorType.name());
        switch (errorType) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    public class PlayerProgressTask extends AsyncTask<Void,Integer,Void> {

        private int duration;
        private int position;

        public PlayerProgressTask(int duration,int position){
            this.duration=duration;
            this.position=position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressbarTrack.setMax(duration);
            txtTrackDuration.setText(DateTimeUtils.secondsToMinutes(duration));
        }

        @Override
        protected Void doInBackground(Void... params) {

             for(int i=position; i<=duration;i++){

                 try {
                     publishProgress(i);
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }

            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            //Log.i(BaseController.TAG, "track progress:" + progress[0]);
            progressbarTrack.setProgress(progress[0]);
            txtTrackProgressTime.setText(DateTimeUtils.secondsToMinutes(progress[0]));

        }
    }

}
