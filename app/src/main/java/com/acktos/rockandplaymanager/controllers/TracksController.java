package com.acktos.rockandplaymanager.controllers;

import android.util.Log;

import com.acktos.rockandplaymanager.android.AppController;
import com.acktos.rockandplaymanager.models.BaseModel;
import com.acktos.rockandplaymanager.models.Track;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Acktos on 9/22/15.
 */
public class TracksController extends BaseController{

    //Constants
    public static final String TAG_GET_TRACK_SPOTIFY="search_track_spotify";

    public void getSpotifyTrack(String trackId, final Response.Listener<Track> responseListener, final Response.ErrorListener errorListener){

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(

                Request.Method.GET,
                BaseController.API.GET_SPOTIFY_TRACK.getUrl()+trackId,
                null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d(TAG, "spotify get track:" + response.toString());


                        responseListener.onResponse(getTrackFromSpotifyObject(response));
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.e(TAG, "spotify get track error:" + error.getMessage());
                        error.printStackTrace();
                        errorListener.onErrorResponse(error);
                    }
                });


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, TAG_GET_TRACK_SPOTIFY);

    }

    /**
     * Transform json string object from spotify search api to models.Track Object
     */
    private Track getTrackFromSpotifyObject(JSONObject itemObject){

        Track track=null;

        try {

            String name=itemObject.getString(Track.KEY_SPOTIFY_NAME);
            String id=itemObject.getString(BaseModel.KEY_ID);
            String duration=itemObject.getString(Track.KEY_SPOTIFY_DURATION);

            JSONObject jsonAlbum=itemObject.getJSONObject(Track.KEY_SPOTIFY_ALBUM);
            JSONArray jsonArrayArtists=itemObject.getJSONArray(Track.KEY_SPOTIFY_ARTIST);
            String album=jsonAlbum.getString(Track.KEY_SPOTIFY_NAME);
            String thumb=jsonAlbum.getJSONArray(Track.KEY_SPOTIFY_IMAGE).getJSONObject(2).getString(Track.KEY_SPOTIFY_URL);
            String image=jsonAlbum.getJSONArray(Track.KEY_SPOTIFY_IMAGE).getJSONObject(0).getString(Track.KEY_SPOTIFY_URL);
            String artist=jsonArrayArtists.getJSONObject(0).getString(Track.KEY_SPOTIFY_NAME);

            track =new Track(id,name,artist,album,thumb,image,duration);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return track;

    }
}
