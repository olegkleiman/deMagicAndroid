package com.okey.demagicandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.microsoft.projectoxford.face.contract.Face;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileSaver extends AsyncTask<Bitmap, Void, Void> {

    private static final String TAG = "deMagic:FileSaver";
    private final Context mContext;

    FileSaver(Context context) {
        mContext = context;
    }

    private File getOutputFile(){
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File facesDir = new File(root + "/faces");
        facesDir.mkdirs();
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm", Locale.US).format(new Date());
        String faceFileName = "deMagic_"+ timeStamp +".jpg";
        return new File(facesDir, faceFileName);
    }


    @Override
    protected Void doInBackground(Bitmap... bitmaps) {
        try {
            File pictureFile = getOutputFile();
            if( pictureFile.exists() )
                if( pictureFile.delete() );

            FileOutputStream fos = new FileOutputStream(pictureFile);
            bitmaps[0].compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            if( mContext != null ) {
                MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
                        pictureFile.getAbsolutePath(),
                        pictureFile.getName(),
                        pictureFile.getName());
            }

        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }
}
