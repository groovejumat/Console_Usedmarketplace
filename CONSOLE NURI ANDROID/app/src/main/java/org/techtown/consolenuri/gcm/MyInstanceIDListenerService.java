package org.techtown.consolenuri.gcm;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

public class MyInstanceIDListenerService extends FirebaseMessagingService {

    private static final String TAG = MyInstanceIDListenerService.class.getSimpleName();

    //토큰이 새로 바뀔 때, 이를 다시 서버로 재등록 해주기 위한 용도.
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN",s);

        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, GcmIntentService.class);
        startService(intent);
    }
}
