package com.acktos.rockandplaymanager.presentation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.acktos.rockandplaymanager.R;
import com.acktos.rockandplaymanager.controllers.BaseController;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;

public class LoginActivity extends AppCompatActivity implements ConnectionStateCallback, View.OnClickListener{


    private static final int SPOTIFY_REQUEST_CODE = 1325;


    //Components
    private AuthenticationRequest authenticationRequest;

    //UI references
    private ImageView imgSpotifyLogin;
    private TextView txtLoginStatus;

    //Android Utils
    SharedPreferences mPrefs;// Handle to SharedPreferences for this APP
    SharedPreferences.Editor mEditor;// Handle to a SharedPreferences editor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialize UI
        imgSpotifyLogin=(ImageView) findViewById(R.id.img_spotify_login);
        txtLoginStatus=(TextView) findViewById(R.id.txt_login_status);

        //setup spotify listeners
        setupSpotifyLogin();

        //initialize Android Utils
        mPrefs = getSharedPreferences(BaseController.SHARED_PREFERENCES, Context.MODE_PRIVATE);// Open Shared Preferences
        mEditor = mPrefs.edit();// Get an editor

        //Set clicks listener to UI
        imgSpotifyLogin.setOnClickListener(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == SPOTIFY_REQUEST_CODE) {

            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {

                Log.i(BaseController.TAG, "spotify access token:" + response.getAccessToken());

                mEditor.putString(BaseController.SHARED_SPOTIFY_ACCESS_TOKEN, response.getAccessToken());
                mEditor.commit();

                Intent i=new Intent(LoginActivity.this, PlayerActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();

                /*usersController.getSpotifyUser(response.getAccessToken(), new Response.Listener<User>() {
                    @Override
                    public void onResponse(User user) {

                        if(user!=null){

                            signUpFirebaseUser(user);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });*/

            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.img_spotify_login:

                AuthenticationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, authenticationRequest);
                break;

            default:
                break;
        }
    }

    private void setupSpotifyLogin() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(BaseController.CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                BaseController.REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        authenticationRequest = builder.build();
    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

}
