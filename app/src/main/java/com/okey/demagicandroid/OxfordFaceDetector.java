package com.okey.demagicandroid;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.InputStream;

public class OxfordFaceDetector extends AsyncTask<InputStream, String, Face[]> {

    private static final String TAG = "deMagic:OxfordDetector";
    IOxfordFaceDetector mCallback;

    OxfordFaceDetector(IOxfordFaceDetector callback) {
        mCallback = callback;
    }

    @Override
    protected Face[] doInBackground(InputStream... params) {
        FaceServiceClient faceServiceClient = DeMagicApp.getFaceServiceClient();

        try {

            return faceServiceClient.detect(params[0],
                    true,       /* Whether to return face ID */
                    true,       /* Whether to return face landmarks */
                    new FaceServiceClient.FaceAttributeType[] {
                            FaceServiceClient.FaceAttributeType.Age,
                            FaceServiceClient.FaceAttributeType.Gender,
                            FaceServiceClient.FaceAttributeType.Smile,
                            FaceServiceClient.FaceAttributeType.Glasses,
                            FaceServiceClient.FaceAttributeType.FacialHair,
                            FaceServiceClient.FaceAttributeType.Emotion,
                            FaceServiceClient.FaceAttributeType.HeadPose,
                            FaceServiceClient.FaceAttributeType.Accessories,
                            FaceServiceClient.FaceAttributeType.Blur,
                            FaceServiceClient.FaceAttributeType.Exposure,
                            FaceServiceClient.FaceAttributeType.Hair,
                            FaceServiceClient.FaceAttributeType.Makeup,
                            FaceServiceClient.FaceAttributeType.Noise,
                            FaceServiceClient.FaceAttributeType.Occlusion
                    });

        } catch( Exception ex ) {
            Log.e(TAG, ex.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(Face[] result) {
        if( result != null ) {
            Log.d(TAG, result.length + " face"
                    + (result.length != 1 ? "s" : "") + " detected");

            mCallback.onFaceDetected(result[0].faceId);
        }
    }
}
