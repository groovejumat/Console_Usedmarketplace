package org.techtown.consolenuri.gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.consolenuri.R;
import org.techtown.consolenuri.app.Config;
import org.techtown.consolenuri.app.EndPoints;
import org.techtown.consolenuri.app.MyApplication;
import org.techtown.consolenuri.model.User;

import java.util.HashMap;
import java.util.Map;


//This service extends IntentService which acts as a background service. This service basically used for three purposes.
//To connect with gcm server and fetch the registration token. Uses registerGCM() method. //fcm서버를 이용하여 토큰을 fetch한다. 이때 registerGCM 메서드를 사용한다.
//Subscribe to a topic. Uses subscribeToTopic(“topic”) method.
//Unsubscribe from a topic. Uses unsubscribeFromTopic(“topic”) method.

public class GcmIntentService extends IntentService {

    private static final String TAG = GcmIntentService.class.getSimpleName();

    public GcmIntentService() {
        super(TAG);
    }

    public static final String KEY = "key";
    public static final String TOPIC = "topic";
    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";

    @Override
    protected void onHandleIntent(Intent intent) {

        //키값을 가지고 온다.
        String key = intent.getStringExtra(KEY);
        switch (key) {
            case SUBSCRIBE:
                // subscribe to a topic
                String topic = intent.getStringExtra(TOPIC);
                subscribeToTopic(topic);
                break;
            case UNSUBSCRIBE:
                break;
            default:
                // if key is not specified, register with FCM
                registerGCM();//GCM을 썻어서 여기서는 그냥 이 함수명으로 사용하자. 수정은 차후에.
        }
    }

    /**
     * Registering with GCM and obtaining the gcm registration id
     */
    //해당 메서드에서, 토큰을 생성하여 브로드캐스팅으로 호출하는 것 까지 모두 진행한다.
    private void registerGCM() {
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        getToken();
    }

    //서버로 등록정보를 보내는 메서드 이다. 회원가입을 진행하는데에 쓰는 듯 하다.
    private void sendRegistrationToServer(final String token) {
        // checking for valid login session
        User user = MyApplication.getInstance().getPrefManager().getUser();
        if (user == null) {
            // TODO
            // user not found, redirecting him to login screen
            return;
        }

        String endPoint = EndPoints.USER.replace("_ID_", user.getId());

        Log.e(TAG, "endpoint: " + endPoint);

        StringRequest strReq = new StringRequest(Request.Method.PUT,
                endPoint, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error
                    if (obj.getBoolean("error") == false) {
                        // broadcasting token sent to server
                        Intent registrationComplete = new Intent(Config.SENT_TOKEN_TO_SERVER);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(registrationComplete);
                    } else {
                        Toast.makeText(getApplicationContext(), "Unable to send gcm registration id to our sever. " + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("gcm_registration_id", token);

                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

    /**
     * Subscribe to a topic
     */
    public void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
        Log.e(TAG, "Subscribed to topic: " + topic);
    }



    public void unsubscribeFromTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/"+topic);
        Log.e(TAG, "unSubscribed to topic: " + topic);
        //FirebaseMessaging pubSub = FirebaseMessaging.getInstance();
        //파이어 베이스 토픽 subscribe//
//        if (token != null) {
//            FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/"+topic);
//            Log.e(TAG, "unSubscribed to topic: " + topic);
//        } else {
//            Log.e(TAG, "error: gcm registration id is null");
//        }
    }


    //registerationid를 받아오는 메서드를 만듬.
    public void getToken(){
        final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        //토큰 받아오기//
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) { //해당 태스크가 실패했다면 인스턴스 실패!!
                            Log.w("FCM Log", "getInstanceId failed", task.getException());
                            return;
                        }

                        String token = task.getResult().getToken();
                        Log.d("FCM Log", "FCM 토큰: " + token); //FCM토큰을 받아 왔다.

                        // Notify UI that registration has completed, so the progress indicator can be hidden.
                        sendRegistrationToServer(token); //해당 메서드로 토큰 값을 서버에 전송함.

                        sharedPreferences.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER, true).apply(); //해당 셰어드 프리퍼런스 boolean이 true값으로 저장 됌.

                        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE); //Config.REGISTERATION_COMPLETE로 인텐트 생성
                        registrationComplete.putExtra("token", token);

                        Log.e(TAG,"토큰이 저장 됌."+token);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(registrationComplete); //저장이 되어졌음을 broadcasting으로 알림.
                    }
                });
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String strId = getString(R.string.project_id);
            final String strTitle = getString(R.string.app_name);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = notificationManager.getNotificationChannel(strId);
            if (channel == null) {
                channel = new NotificationChannel(strId, strTitle, NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            Notification notification = new NotificationCompat.Builder(this, strId).build();
            startForeground(1, notification);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

}
