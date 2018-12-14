package com.okey.demagicandroid;

import android.os.AsyncTask;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CloudSender<T> extends Request<T> {

    private MultipartEntityBuilder mBuilder = MultipartEntityBuilder.create();
    private final Response.Listener<T> mListener;

    public CloudSender(String url,
                       Response.ErrorListener errorListener,
                       Response.Listener<T> listener,
                       byte[] imageBytes)   {
        super(Method.POST, url, errorListener);
        mListener = listener;

        buildMultipartEntity();
    }

    private void buildMultipartEntity() {

    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();
        if (headers == null
                || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<String, String>();
        }
        headers.put("Accept", "application/json");
        return headers;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response){
        T result = null;
        return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public String getBodyContentType()   {
        String content = mBuilder.build().getContentType().getValue();
        return content;
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }


}
