package com.aminnovent.xforcenotification;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by charles on 4/20/2015.
 */

enum DownloadStatus {IDLE, PROCESSING, NOT_INITIALISED, FAILED_OR_EMPTY, OK}

public class GetRawData {

    private String LOG_TAG = GetRawData.class.getSimpleName();

    public void setmRawUrl(String mRawUrl) {
        this.mRawUrl = mRawUrl;
    }

    private String mRawUrl;
    private String mauthToken;
    private String mData;
    private DownloadStatus mDownloadStatus;

    public GetRawData(String mRawUrl,String mauthToken) {
        this.mRawUrl = mRawUrl;
        this.mauthToken = mauthToken;
        this.mDownloadStatus = DownloadStatus.IDLE;
    }

    public void reset() {
        this.mDownloadStatus = DownloadStatus.IDLE;
        this.mRawUrl = null;
        this.mData = null;
    }


    public String getmData() {
        return mData;
    }

    public DownloadStatus getmDownloadStatus() {
        return mDownloadStatus;
    }






    public void execute(){
        this.mDownloadStatus = DownloadStatus.PROCESSING;
        DownloadRawData downloadRawData = new DownloadRawData();
        downloadRawData.execute(mRawUrl,mauthToken);
    }

    public class DownloadRawData extends AsyncTask<String, Void, String> {



        @Override
        protected void onPostExecute(String webData) {
            // TODO
            mData = webData;
            Log.v(LOG_TAG, "Data returned was: " +mData);
            if (mData == null) {
                if (mRawUrl == null) {
                    mDownloadStatus = DownloadStatus.NOT_INITIALISED;
                } else {
                    mDownloadStatus = DownloadStatus.FAILED_OR_EMPTY;
                }
            } else {

                mDownloadStatus = DownloadStatus.OK;
            }
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            if (params == null)
                return null;


            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                if(params[1]!=null){String token = params[1].toString();
                    urlConnection.setRequestProperty("Authorization","Bearer "+token);
                }
                /*String token = params[1].toString();
                if(token != null){
                    Log.e("token passed:-", token);
                }*/

                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream is = urlConnection.getInputStream();

                if (is == null){
                    return null;
                }

                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                return buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "GetRawData Error", e);
                return  null;
            } finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                if(reader != null) {
                    try {
                        reader.close();
                    } catch(final IOException e) {
                        Log.e(LOG_TAG,"Error closing stream", e);

                    }
                }

            }
        }
    }


}
