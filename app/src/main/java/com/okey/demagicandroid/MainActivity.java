package com.okey.demagicandroid;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.microsoft.projectoxford.face.contract.SimilarPersistedFace;
import com.okey.demagicandroid.common.CameraSourcePreview;
import com.okey.demagicandroid.common.GraphicOverlay;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
        implements IOxfordFaceDetector, IOxfordSimilarityFinder, IOxfordGetter, IShortener {

    private static final String TAG = "deMagic:MainActivity";
    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private final String mLargeFaceListId = "11";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        String[] PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
        };
        int PERMISSION_ALL = 1;
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        createCameraSource();

//        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
//        if (rc == PackageManager.PERMISSION_GRANTED) {
//            createCameraSource();
//        } else {
//            requestCameraPermission();
//        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.ok, listener)
                .show();
    }

    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    @Override
    public void onFaceDetected(UUID faceId) {
        Log.d(TAG, "Detected faceId: " + faceId.toString());
        new OxfordSimilarityFinder(mLargeFaceListId, (IOxfordSimilarityFinder)this).execute(faceId);
    }

    @Override
    public void onFoundSimilarFaces(SimilarPersistedFace[] foundFaces) {
        if( foundFaces.length > 0 ) {
            for(SimilarPersistedFace persistedFace: foundFaces) {
                if( persistedFace.confidence > 0.6 ) {
                    new OxfordFaceGetter(mLargeFaceListId, (IOxfordGetter) this).execute(persistedFace);
                }
            }
        }
    }

    @Override
    public void onGotPersistedFace(UUID persistedFaceId, JSONObject userData) {
        Log.d(TAG, "Got persisted faceId");
        new Shortener(userData, (IShortener)this).execute(persistedFaceId);
    }

    @Override
    public void onShortened(URI shortLink, JSONObject userData) {
        Log.d(TAG, "Shortened");
        new KarixNotificator(shortLink).execute(userData);
    }

    public Bitmap mergeBitmaps(Bitmap face, Bitmap overlay) {
        // Create a new image with target size
        int width = face.getWidth();
        int height = face.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Rect faceRect = new Rect(0,0,width,height);
        Rect overlayRect = new Rect(0,0,overlay.getWidth(),overlay.getHeight());

        // Draw face and then overlay (Make sure rects are as needed)
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(face, faceRect, faceRect, null);
        canvas.drawBitmap(overlay, overlayRect, faceRect, null);
        return newBitmap;
    }

    private void takeShot(final float left, final float top,
                          final float right, final float bottom) {

        final Context context = getApplicationContext();
        final IOxfordFaceDetector callback = this;

        mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {

//                // Generate the Face Bitmap
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                Bitmap face = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
//
//                // Generate the Eyes Overlay Bitmap
//                mPreview.setDrawingCacheEnabled(true);
//                Bitmap overlay = mPreview.getDrawingCache();
//
//                // Generate the final merged image
//                Bitmap croppedBmp = mergeBitmaps(face, overlay);

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                int bitmapHeight = bitmap.getHeight();
                int bitmapWidth = bitmap.getWidth();
                Log.d(TAG, String.format("%d %d", bitmapWidth, bitmapHeight));

                int faceWidth = Math.round(right - left);
                int faceHeight = Math.round(bottom - top);
                Bitmap croppedBmp = Bitmap.createBitmap(bitmap,
                        Math.round(left), Math.round(top),
                        faceWidth * 3, faceHeight * 3);

                ByteArrayOutputStream output = new ByteArrayOutputStream();
                croppedBmp.compress(Bitmap.CompressFormat.JPEG, 100, output);

                // For test purposes
                new FileSaver(context).execute(croppedBmp);

                InputStream inputStream = new ByteArrayInputStream(output.toByteArray());
                new OxfordFaceDetector(callback).execute(inputStream);

                // Uncomment this AsyncTask c'tor to apply Logic App flow on Azure:
                // This flow means triggering event upon a image uploading to Azure Store
                // Such a event calls Logic App that perform Face Detection with the same Oxford Api
                // but invoked from cloud.
                //new AzureBlobUploader(bytes, callback).execute();

            }
        });
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        List<Integer> detectedFacesIds = new ArrayList<>();

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {

            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);

            Integer faceId = face.getId();
            int index = detectedFacesIds.indexOf(faceId);
            if( index == -1 ) {
                detectedFacesIds.add(faceId);

                float x = mFaceGraphic.translateX(face.getPosition().x + face.getWidth() / 2.0f);
                float y = mFaceGraphic.translateY(face.getPosition().y + face.getHeight() / 2.0f);
                float xOffset = mFaceGraphic.scaleX(face.getWidth() / 2.0f);
                float yOffset = mFaceGraphic.scaleY(face.getHeight() / 2.0f);
                float left = x - xOffset;
                float top = y - yOffset;
                float right = x + xOffset;
                float bottom = y + yOffset;

                takeShot(left, top, right, bottom);
            }

        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }
}

