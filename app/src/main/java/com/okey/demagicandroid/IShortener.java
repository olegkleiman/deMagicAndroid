package com.okey.demagicandroid;

import org.json.JSONObject;

import java.net.URI;

public interface IShortener {
    void onShortened(URI link, JSONObject userData);
}
