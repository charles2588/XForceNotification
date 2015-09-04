package com.aminnovent.xforcenotification;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by charles on 4/21/2015.
 */
public class GetXforceJsonData extends GetRawData {

    private String LOG_TAG = GetXforceJsonData.class.getSimpleName();



    private List<Photo> mPhotos;
    private Uri mDestinationUri;
    private String mauthToken;

    public GetXforceJsonData() {
        super(null, null); //set mRawUrl to null
        mPhotos = new ArrayList<Photo>();
        mauthToken = null;
    }

    // Get XforceAuthToken
    public String getXforceAuthToken(){
        String token=null;
        createAndUpdateUri("auth/anonymousToken");
        DownloadJsonData downloadJsonData = new DownloadJsonData();
        downloadJsonData.execute(mDestinationUri.toString(),null);
        //Log.v(LOG_TAG, "returned data :- " + getmData());
        return token;
    }

    public void startSearch(String term)
    {
        List<String> results =new ArrayList<String>();
        createAndUpdateUri("vulnerabilities/fulltext?q="+term);
        DownloadJsonData downloadJsonData = new DownloadJsonData();
        downloadJsonData.execute(mDestinationUri.toString(), mauthToken);
    }

    public List<String> processSearch()
    {
        List<String> results =new ArrayList<String>();
        try{
            String s,total_rows;
            s = getmData();
            Object json = new JSONTokener(s).nextValue();
            if (json instanceof JSONObject) {
                //you have an object
                total_rows  = ((JSONObject) json).get("total_rows").toString();

            }
            else if (json instanceof JSONArray) {


                //you have an array
            }
            //Log.v(LOG_TAG, "returned version data :- " + getmData());

            //mauthToken = jsonObject.get("token").toString();
            //Log.v(LOG_TAG, "returned version :- " + mauthToken);
        }catch(JSONException e){}
        return results;
    }

    public String processAuthToken(){

        try{
            JSONObject jsonObject = new JSONObject(getmData());
            mauthToken = jsonObject.get("token").toString();
            Log.v(LOG_TAG, "returned data :- " + mauthToken);
        }catch(JSONException e){}
        //String str = getXforceVersion();
        startSearch("java");
        return mauthToken;
    }

    // Get XforceVersion
    public String getXforceVersion(){
        String token=null;
        createAndUpdateUri("version");
        DownloadJsonData downloadJsonData = new DownloadJsonData();
        downloadJsonData.execute(mDestinationUri.toString(), mauthToken);
        return token;
    }

    public String processXforceVersion(){
        try{
            String s;
            JSONObject jsonObject = new JSONObject(getmData());
            //Log.v(LOG_TAG, "returned version data :- " + getmData());
            s = jsonObject.get("build").toString();
            //mauthToken = jsonObject.get("token").toString();
            //Log.v(LOG_TAG, "returned version :- " + mauthToken);
        }catch(JSONException e){}
        return mauthToken;
    }




    public void execute(){
        super.setmRawUrl(mDestinationUri.toString());
        DownloadJsonData downloadJsonData = new DownloadJsonData();
        Log.v(LOG_TAG,"BUILT URI :- " +mDestinationUri.toString());
        downloadJsonData.execute(mDestinationUri.toString());
    }

    public boolean createAndUpdateUri(String Param){
        final String XFORCE_API_BASE_URL = "https://xforce-api.mybluemix.net:443";//"https://api.xforce.ibmcloud.com";

        final String XFORCE_API_AUTH_PARM = Param;//"/auth/anonymousToken";
        /*
        * example response {"token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyVW5pcXVlSUQiOiJhZGNiNTMwYy0wZjQ3LTQ2ZDctYWU0NC0xYzMyM2IyYTgyYTUiLCJ0eXBlIjoiYW5vbnltb3VzIiwiaWF0IjoxNDQxMDQwMjc4LCJleHAiOjE0NDEyOTk0Nzh9.F11m7uFLUdRpzKOz0GcMETI5ljIgSlDsRu2LntKkb30"}
        * */

        //final String XFORCE_API_VERSION_PARM ="/version";
        /*
        * {
             "build": "7658a",
             "created": "2015-08-30T22:07:16.000Z"
          }
        * */

        //Xforce Auth
        mDestinationUri = Uri.parse(XFORCE_API_BASE_URL).buildUpon()
                .appendEncodedPath(XFORCE_API_AUTH_PARM)
                .build();

        //Xforce version
        /*mDestinationUri = Uri.parse(XFORCE_API_BASE_URL).buildUpon()
                .appendEncodedPath(XFORCE_API_VERSION_PARM)
                .build();
        */
        return mDestinationUri != null;
    }

    public void processResult() {
        if(getmDownloadStatus()!= DownloadStatus.OK){
            Log.e(LOG_TAG,"Error Downloading Raw File");
            return;
        }

        final String FLICKR_ITEMS = "items";
        final String FLICKR_TITLE = "title";
        final String FLICKR_MEDIA = "media";
        final String FLICKR_PHOTO_URL = "m";
        final String FLICKR_AUTHOR = "author";
        final String FLICKR_AUTHOR_ID = "author_id";
        final String FLICKR_LINK = "link";
        final String FLICKR_TAGS = "tags";

        //process json

        try {
            JSONObject jsonData = new JSONObject(getmData());
            JSONArray itemsArray = jsonData.getJSONArray(FLICKR_ITEMS);
            for(int i=0;i<itemsArray.length();i++){
                JSONObject jsonPhoto = itemsArray.getJSONObject(i);
                String title = jsonPhoto.getString(FLICKR_TITLE);
                String author = jsonPhoto.getString(FLICKR_AUTHOR);
                String authorid = jsonPhoto.getString(FLICKR_AUTHOR_ID);
                //String link = jsonPhoto.getString(FLICKR_LINK);
                String tags = jsonPhoto.getString(FLICKR_TAGS);

                JSONObject jsonMedia = jsonPhoto.getJSONObject(FLICKR_MEDIA);
                String photoUrl = jsonMedia.getString(FLICKR_PHOTO_URL);

                //replace the photo URL to make big image
                String link = photoUrl.replaceFirst("_m","_b");

                Photo photoObject = new Photo(title,author,authorid,link,tags, photoUrl);

                this.mPhotos.add(photoObject);
            }

            for(Photo singlePhoto:mPhotos){
                Log.d(LOG_TAG,singlePhoto.toString());
            }


        } catch(JSONException e){
            e.printStackTrace();
            Log.e(LOG_TAG,"Error Processing JSON Data",e);

        }
    }

    public List<Photo> getPhotos() {
        return mPhotos;
    }


    public class DownloadJsonData extends DownloadRawData{

        @Override
        protected void onPostExecute(String webData) {
            super.onPostExecute(webData);
            Log.v(LOG_TAG, "data :- " + webData.toString());
            if(mauthToken==null)
                processAuthToken();
            else{
                processSearch();
            }
                //processXforceVersion();

        }



        @Override
        protected String doInBackground(String... params) {
          //  String[] par = {mDestinationUri.toString(),mauthToken};
            return super.doInBackground(params);
        }
    }
}
