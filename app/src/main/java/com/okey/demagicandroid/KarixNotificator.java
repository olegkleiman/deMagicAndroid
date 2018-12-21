package com.okey.demagicandroid;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.projectoxford.face.contract.Face;

import org.apache.http.NameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    final String karixBaseURL = "https://api.karix.io/message";

    KarixNotificator(URI link) {
        mLink = link;
    }

    @Override
    protected Void doInBackground(JSONObject... params) {

        try {

            JSONObject useData = params[0];
            String userName = useData.getString("firstName");

            final URL karixURL = new URL(karixBaseURL);
            HttpsURLConnection client = (HttpsURLConnection) karixURL.openConnection();
            client.setRequestMethod("POST");

            client.setRequestProperty("Authorization", "Basic OTkyZDU1MjgtOGM3Zi00ODBmLThjNzktZWFjYmY3YTJhZTMyOjAyYWFlMTllLWFmYzQtNDNhMi1hZDY1LTFkMWI0NjBiOGIwMQ==");
            client.setRequestProperty("Content-Type", "application/json");
            client.setDoInput(true);

            String temp =  "{\"source\": \"Tlv Conf\",\"destination\":[\"+972543307026\"]}";
            JSONObject payload = new JSONObject(temp);
            StringBuilder sb = new StringBuilder();
            sb.append("שלום");
            sb.append(" ");
            sb.append(userName);
            sb.append(mLink);
            payload.put("text", sb.toString());

            String request = payload.toString();

            //String str =  "{\"source\": \"Tlv Conf\",\"destination\":[\"+972543307026\"], \"text\": \"שלום, הנה כרטיס הכניסה שלך לכנס אגף המיחשוב\"}";
            byte[] outputInBytes = request.getBytes("UTF-8");
            OutputStream os = client.getOutputStream();
            os.write( outputInBytes );
            os.close();

            int responseCode = client.getResponseCode();
            String responseMessage = client.getResponseMessage();
            Log.d(TAG, String.format("%d %s", responseCode, responseMessage));

            BufferedReader responseReader = null;
            if( responseCode == HttpURLConnection.HTTP_ACCEPTED ) {
                responseReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            }
            else if ( responseCode >= 400 ) {
                responseReader = new BufferedReader(new InputStreamReader(client.getErrorStream()));
            }

            String responseLine = "";
            sb.setLength(0);
            while ((responseLine = responseReader.readLine()) != null) {
                sb.append(responseLine);
            }
            String response = sb.toString();
            Log.d(TAG, response);

        } catch( Exception e ) {
            Log.e(TAG, e.getMessage());
        }


        return null;
    }
}
