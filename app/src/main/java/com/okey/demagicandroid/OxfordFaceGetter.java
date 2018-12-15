package com.okey.demagicandroid;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.FaceMetadata;
import com.microsoft.projectoxford.face.contract.SimilarPersistedFace;

import org.json.JSONObject;

public class OxfordFaceGetter extends AsyncTask<SimilarPersistedFace[], Void, Void> {

    private final String TAG = "deMagic:Getter";
    private final String mLargeFaceListId;

    OxfordFaceGetter(String largeFaceListId) {
        mLargeFaceListId = largeFaceListId;
    }

    @Override
    protected Void doInBackground(SimilarPersistedFace[]... similarFaces) {

        SimilarPersistedFace similarFace = similarFaces[0][0];

        FaceServiceClient faceServiceClient = DeMagicApp.getFaceServiceClient();
        try {
            FaceMetadata[] facesMetadata = faceServiceClient.listFacesFromLargeFaceList(mLargeFaceListId);
            for(FaceMetadata metadata: facesMetadata) {
                if( metadata.persistedFaceId.equals(similarFace.persistedFaceId) ) {
                    String userData = metadata.userData;
                    JSONObject jsonReader = new JSONObject(userData);
                    String name = jsonReader.getString("name");
                    Log.d(TAG, name);
                }
            }
        } catch (Exception ex ) {
            Log.e(TAG, ex.getMessage());
        }
        //similarFaces[0]
        return null;
    }
}
