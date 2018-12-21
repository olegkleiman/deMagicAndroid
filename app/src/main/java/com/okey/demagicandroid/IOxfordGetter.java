package com.okey.demagicandroid;

import org.json.JSONObject;

import java.util.UUID;

public interface IOxfordGetter {
    void onGotPersistedFace(UUID persistedFaceId, JSONObject userData);
}
