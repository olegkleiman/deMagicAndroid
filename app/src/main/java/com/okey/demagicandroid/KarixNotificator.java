package com.okey.demagicandroid;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.projectoxford.face.contract.Face;

import org.apache.http.NameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;


public class KarixNotificator extends AsyncTask<JSONObject, Void, Void> {

    final String TAG = "deMagic:Notificator";
    final URI mLink;

    KarixNotificator(URI link) {
        mLink = link;
    }

    @Override
    protected Void doInBackground(JSONObject... params) {

        try {

            final URL karixURL = new URL("https://api.karix.io/message");

            HttpsURLConnection client = (HttpsURLConnection) karixURL.openConnection();
            client.setRequestMethod("POST");

            client.setRequestProperty("Authorization", "Basic OTkyZDU1MjgtOGM3Zi00ODBmLThjNzktZWFjYmY3YTJhZTMyOjAyYWFlMTllLWFmYzQtNDNhMi1hZDY1LTFkMWI0NjBiOGIwMQ==");
            client.setRequestProperty("Content-Type", "application/json");
            OutputStream os = client.getOutputStream();
            String str =  "{\"source\": \"Tlv Conf\",\"destination\":[\"+972543307026\"], \"text\": \"שלום, הנה כרטיס הכניסה שלך לכנס אגף המיחשוב\"}";
            byte[] outputInBytes = str.getBytes("UTF-8");
            os.write( outputInBytes );
            os.close();

            //client.setDoOutput(true);
            int responseCode = client.getResponseCode();
            String response = client.getResponseMessage();
            Log.d(TAG, String.format("%d %s", responseCode, response));

        } catch( Exception e ) {
            Log.e(TAG, e.getMessage());
        }


        return null;
    }
}
