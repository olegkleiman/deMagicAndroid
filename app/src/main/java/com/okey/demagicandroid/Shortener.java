package com.okey.demagicandroid;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.projectoxford.face.contract.SimilarPersistedFace;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class Shortener extends AsyncTask<UUID, Void, URI> {

    private final String TAG = "deMagic:Shortener";
    private final IShortener mCallback;
    private final JSONObject mUserData;
    private final String mShortenerURL = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=AIzaSyB8Ubt-p_d0QvWZe4XzM7a4RMqoZlDvvY0";
    private final String customDomainURL = "https://demagic.page.link";
    private final String longLinkURL = "https://demagicticket.azurewebsites.net/dist/index.html";

    Shortener(JSONObject userData, IShortener callback) {
        mUserData = userData;
        mCallback = callback;
    }

    @Override
    protected URI doInBackground(UUID... faceIds) {

        String faceId = faceIds[0].toString();

        try {

            final URL dLinkURL = new URL(mShortenerURL);

            HttpsURLConnection client = (HttpsURLConnection)dLinkURL.openConnection();

            client.setRequestMethod("POST");
            client.setRequestProperty("Content-Type", "application/json");
            client.setDoInput(true);

            JSONObject payload = new JSONObject();
            payload.put("longDynamicLink",
                    customDomainURL.concat("?link=").concat(longLinkURL).concat("?faceId=").concat(faceId));
            JSONObject optionJSON = new JSONObject();
            optionJSON.put("option", "SHORT");
            payload.put("suffix", optionJSON);
            String request = payload.toString();
            Log.d(TAG, request);

            OutputStream os = client.getOutputStream();
            byte[] outputInBytes = request.getBytes("UTF-8");
            os.write( outputInBytes );
            os.close();

            int responseCode = client.getResponseCode();
            String responseMessage = client.getResponseMessage();
            Log.d(TAG, String.format("%d %s", responseCode, responseMessage));

            BufferedReader responseReader = null;
            if( responseCode == HttpURLConnection.HTTP_OK ) {
                responseReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            }
            else if ( responseCode >= 400 ) {
                responseReader = new BufferedReader(new InputStreamReader(client.getErrorStream()));
            }

            String responseLine = "";
            StringBuilder sb = new StringBuilder();
            while ((responseLine = responseReader.readLine()) != null) {
                sb.append(responseLine);
            }
            String response = sb.toString();
            Log.d(TAG, response);

            JSONObject jsonResponse = new JSONObject(response);
            String shortLink = jsonResponse.getString("shortLink");

            return new URI(shortLink);

        } catch(Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(URI shortURI) {
        if( mCallback != null )
            mCallback.onShortened(shortURI, mUserData);
    }

}
