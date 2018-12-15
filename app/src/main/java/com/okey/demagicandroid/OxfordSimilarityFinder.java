package com.okey.demagicandroid;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.SimilarPersistedFace;

import java.util.UUID;

public class OxfordSimilarityFinder extends AsyncTask<UUID, Void, SimilarPersistedFace[]> {

    private final String TAG = "OxfordSimilarityFinder";
    private String mLargeFaceListId;
    private IOxfordSimilarityFinder mCallback;

    OxfordSimilarityFinder(String largeFaceListId, IOxfordSimilarityFinder callback) {
        mLargeFaceListId = largeFaceListId;
        mCallback = callback;
    }

    @Override
    protected SimilarPersistedFace[] doInBackground(UUID... faceIds) {

        FaceServiceClient faceServiceClient = DeMagicApp.getFaceServiceClient();
        try {
            return faceServiceClient.findSimilarInLargeFaceList(faceIds[0],
                    mLargeFaceListId,
                    4 /* max number of candidate returned*/);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            return null;
        }

    }

    @Override
    protected void onPostExecute(SimilarPersistedFace[] result) {
        if( mCallback != null ) {
            mCallback.onFoundSimilarFaces(result);
        }
    }
}
