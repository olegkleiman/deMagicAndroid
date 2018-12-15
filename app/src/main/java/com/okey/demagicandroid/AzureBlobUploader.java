package com.okey.demagicandroid;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AzureBlobUploader extends AsyncTask<String, Void, Void> {

    private String TAG = "deMagic:AzureBlobUploader";
    private static final String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=demagic;AccountKey=o8h+B9V0Zj2yZTRxbH6JAiPWl/2kqPR4ytzzKnZStmSn+EUZ1sj8GUfVHVaSXcrOH34dcJcd7g9BIxTAUrWWGw==;EndpointSuffix=core.windows.net";
    private byte[] mImageBytes;
    private IBlobUploader mUploaderCallback;
    private URI mBlobURI;


    AzureBlobUploader(byte[] imageBytes, IBlobUploader uploaderCallback) {
        mImageBytes = imageBytes;
        mUploaderCallback = uploaderCallback;
    }

    @Override
    protected Void doInBackground(String... strings) {

        try {
            CloudStorageAccount account = CloudStorageAccount
                    .parse(storageConnectionString);
            CloudBlobClient blobClient = account.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference("faces");
            container.createIfNotExists();

            BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
            containerPermissions
                    .setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
            container.uploadPermissions(containerPermissions);

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String blobName = timeStamp + ".jpg";
            CloudBlockBlob blob = container.getBlockBlobReference(blobName);
            blob.uploadFromByteArray(mImageBytes, 0, mImageBytes.length);

            mBlobURI = blob.getUri();


        } catch(Throwable t) {
            Log.e(TAG, t.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if( mUploaderCallback != null ) {
            mUploaderCallback.onBlobUploaded(mBlobURI);
        }

    }
}
