package com.okey.demagicandroid;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceMetadata;
import com.microsoft.projectoxford.face.contract.SimilarPersistedFace;

import org.json.JSONObject;

import java.util.UUID;

public class OxfordFaceGetter extends AsyncTask<SimilarPersistedFace[], Void, JSONObject> {

    private final String TAG = "deMagic:Getter";
    private final String mLargeFaceListId;
    private IOxfordGetter mCallback;
    private UUID mPersistedFaceId;

    OxfordFaceGetter(String largeFaceListId, IOxfordGetter callback) {
        mLargeFaceListId = largeFaceListId;
        mCallback = callback;
    }

    @Override
    protected JSONObject doInBackground(SimilarPersistedFace[]... similarFaces) {

        SimilarPersistedFace similarFace = similarFaces[0][0];
        if( similarFace == null ) {
            return null;
        }

        mPersistedFaceId = similarFace.persistedFaceId;

        FaceServiceClient faceServiceClient = DeMagicApp.getFaceServiceClient();
        try {
            FaceMetadata[] facesMetadata = faceServiceClient.listFacesFromLargeFaceList(mLargeFaceListId);
            for(FaceMetadata metadata: facesMetadata) {
                if( metadata.persistedFaceId.equals(similarFace.persistedFaceId) ) {
                    String userData = metadata.userData;
                    JSONObject jsonUserData = new JSONObject(userData);
                    String name = jsonUserData.getString("name");
                    Log.d(TAG, name);

                    return jsonUserData;
                }
            }
        } catch (Exception ex ) {
            Log.e(TAG, ex.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject userData) {
        if( mCallback != null )
            mCallback.onGotPersistedFace(mPersistedFaceId, userData);
    }
}
