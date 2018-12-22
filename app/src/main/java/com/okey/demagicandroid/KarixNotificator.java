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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;


public class KarixNotificator extends AsyncTask<JSONObject, Void, String> {

    final private String TAG = "deMagic:Notificator";
    final private URI mLink;
    final private String karixBaseURL = "https://api.karix.io/message";
    final private IKarixNotificator mCallback;

    KarixNotificator(URI link, IKarixNotificator callback) {
        mLink = link;
        mCallback = callback;
    }

    @Override
    protected String doInBackground(JSONObject... params) {

        try {

            JSONObject useData = params[0];
            String userName = useData.getString("firstName");
            String phoneNumber = useData.getString("phoneNumber");
            phoneNumber = phoneNumber.replaceFirst("^0*", "+972");
            Log.d(TAG, phoneNumber);
            if( DeMagicApp.isNotified(phoneNumber) ) {
                return "";
            }

            // Uncomment this line
            phoneNumber = "+972543307026";

            final URL karixURL = new URL(karixBaseURL);
            HttpsURLConnection client = (HttpsURLConnection) karixURL.openConnection();
            client.setRequestMethod("POST");

            client.setRequestProperty("Authorization", "Basic OTkyZDU1MjgtOGM3Zi00ODBmLThjNzktZWFjYmY3YTJhZTMyOjAyYWFlMTllLWFmYzQtNDNhMi1hZDY1LTFkMWI0NjBiOGIwMQ==");
            client.setRequestProperty("Content-Type", "application/json");
            client.setDoInput(true);

            //String temp =  "{\"source\": \"Tlv Conf\",\"destination\":[\"+972543307026\"]}";
            String temp =  "{\"source\": \"Tlv Conf\",\"destination\":[\"".concat(phoneNumber).concat("\"]}");
            JSONObject payload = new JSONObject(temp);
            String sb = " שלום ".concat(userName)
                    .concat("שמחים לראותך")
                    .concat(" ")
                    .concat(mLink.toString());
            payload.put("text", sb);

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
                DeMagicApp.addNotifiedNumber(phoneNumber);
            }
            else if ( responseCode >= 400 ) {
                responseReader = new BufferedReader(new InputStreamReader(client.getErrorStream()));
            }

            responseMessage = responseReader != null ?
                    getResponseBody(responseReader) : "Not expected response";
            Log.d(TAG, responseMessage);
            return phoneNumber;


        } catch( Exception e ) {
            Log.e(TAG, e.getMessage());
        }

        return "";
    }

    @Override
    protected void onPostExecute(String phoneNumber) {
        if( mCallback != null )
            mCallback.onSent(phoneNumber);
    }

    private String getResponseBody(BufferedReader reader) {

        StringBuilder sb = new StringBuilder();

        try {
            String responseLine = "";
            sb.setLength(0);
            while ((responseLine = reader.readLine()) != null) {
                sb.append(responseLine);
            }
            return sb.toString();
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }

        return "";
    }
}
