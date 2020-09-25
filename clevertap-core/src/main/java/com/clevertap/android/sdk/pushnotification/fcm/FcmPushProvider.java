package com.clevertap.android.sdk.pushnotification.fcm;

import static com.clevertap.android.sdk.pushnotification.PushConstants.ANDROID_PLATFORM;
import static com.clevertap.android.sdk.pushnotification.PushConstants.PushType.FCM;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.clevertap.android.sdk.ManifestInfo;
import com.clevertap.android.sdk.PackageUtils;
import com.clevertap.android.sdk.pushnotification.CTPushProvider;
import com.clevertap.android.sdk.pushnotification.CTPushProviderListener;
import com.clevertap.android.sdk.pushnotification.PushConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@SuppressLint(value = "unused")
public class FcmPushProvider implements CTPushProvider {

    private static String LOG_TAG = FcmPushProvider.class.getSimpleName();

    private CTPushProviderListener listener;

    @SuppressLint(value = "unused")
    public FcmPushProvider(CTPushProviderListener ctPushListener) {
        this.listener = ctPushListener;
    }

    @Override
    public int getPlatform() {
        return ANDROID_PLATFORM;
    }

    @NonNull
    @Override
    public PushConstants.PushType getPushType() {
        return FCM;
    }

    /**
     * App supports FCM
     *
     * @return boolean true if FCM services are available
     */
    @Override
    public boolean isAvailable() {
        try {
            if (!PackageUtils.isGooglePlayServicesAvailable(listener.context())) {
                listener.config().log(LOG_TAG, "Google Play services is currently unavailable.");
                return false;
            }

            String senderId = getSenderId();
            if (senderId == null) {
                listener.config().log(LOG_TAG, "The FCM sender ID is not set. Unable to register for FCM.");
                return false;
            }
        } catch (Exception e) {
            listener.config().log(LOG_TAG, "Unable to register with FCM.", e);
            return false;
        }
        return true;
    }

    /**
     * Device supports FCM
     *
     * @return - true if FCM is supported in the platform
     */
    @Override
    public boolean isSupported() {
        return PackageUtils.isGooglePlayStoreAvailable(listener.context());
    }

    @Override
    public int minSDKSupportVersionCode() {
        return 0;// supporting FCM from base version
    }

    @Override
    public void requestToken() {
        try {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                listener.config().log(LOG_TAG, "getInstanceId failed", task.getException());
                                if (listener != null) {
                                    listener.onNewToken(null, getPushType());
                                }
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult() != null ? task.getResult().getToken() : null;
                            listener.config().log(LOG_TAG, "FCM token for Sender Id - " + token);
                            if (listener != null) {
                                listener.onNewToken(token, getPushType());
                            }
                        }
                    });

        } catch (Throwable t) {
            listener.config().log(LOG_TAG, "Error requesting FCM token", t);
            if (listener != null) {
                listener.onNewToken(null, getPushType());
            }
        }
    }

    private String getFCMSenderID() {
        return ManifestInfo.getInstance(listener.context().getApplicationContext()).getFCMSenderId();
    }

    private String getSenderId() {
        String senderId = getFCMSenderID();
        if (!TextUtils.isEmpty(senderId)) {
            return senderId;
        }
        FirebaseApp app = FirebaseApp.getInstance();
        return app.getOptions().getGcmSenderId();
    }
}