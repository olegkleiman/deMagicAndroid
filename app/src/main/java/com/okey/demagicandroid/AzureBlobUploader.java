package com.okey.demagicandroid;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

public class AzureBlobUploader extends AsyncTask<String, Void, Void> {

    private String TAG = "deMagic:AzureBlobUploader";
    private static final String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=demagic;AccountKey=o8h+B9V0Zj2yZTRxbH6JAiPWl/2kqPR4ytzzKnZStmSn+EUZ1sj8GUfVHVaSXcrOH34dcJcd7g9BIxTAUrWWGw==;EndpointSuffix=core.windows.net";
    private byte[] mImageBytes;

    public AzureBlobUploader(byte[] imageBytes) {
        mImageBytes = imageBytes;
    }

    @Override
    protected Void doInBackground(String... strings) {

        try {
            CloudStorageAccount account = CloudStorageAccount
                    .parse(storageConnectionString);
            CloudBlobClient blobClient = account.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference("faces");

            CloudBlockBlob blob = container
                    .getBlockBlobReference("blob1.jpg");
            blob.uploadFromByteArray(mImageBytes, 0, mImageBytes.length);
        } catch(Throwable t) {
            Log.e(TAG, t.getMessage());
        }

        return null;
    }
}
