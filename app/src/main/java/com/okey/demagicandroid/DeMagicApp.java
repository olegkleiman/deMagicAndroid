package com.okey.demagicandroid;

import android.app.Application;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;

import java.util.ArrayList;

public class DeMagicApp  extends Application {
    private static FaceServiceClient sFaceServiceClient;
    private static ArrayList<String> notifiedNumbers = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        sFaceServiceClient = new FaceServiceRestClient(getString(R.string.endpoint), getString(R.string.subscription_key));
    }

    public static FaceServiceClient getFaceServiceClient() {
        return sFaceServiceClient;
    }

    public static Boolean isNotified(String phoneNumber) {
        return notifiedNumbers.contains(phoneNumber);
    }

    public static Boolean addNotifiedNumber(String phoneNumber) {
        return notifiedNumbers.add(phoneNumber);
    }
}
