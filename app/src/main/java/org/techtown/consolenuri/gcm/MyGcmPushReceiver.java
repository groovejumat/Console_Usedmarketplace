package org.techtown.consolenuri.gcm;

import android.content.Context;
import android.content.Intent;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.consolenuri.activity.ChatRoomActivity;
import org.techtown.consolenuri.activity.MainActivity;
import org.techtown.consolenuri.activity.tradingProductActivity;
import org.techtown.consolenuri.app.Config;
import org.techtown.consolenuri.app.MyApplication;
import org.techtown.consolenuri.model.Message;
import org.techtown.consolenuri.model.User;

//This is a receiver class in which onMessageReceived() method will be triggered whenever device receives new push notification.
public class MyGcmPushReceiver extends FirebaseMessagingService {

    private static final String TAG = MyGcmPushReceiver.class.getSimpleName();
    private NotificationUtils notificationUtils;

    //서버로부터 메시지를 받았을 때에 대한 처리를 해주는 메서드
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived가 호출되어짐!!!!!!!!!!");


        String data = remoteMessage.getData().get("data");
        String title = remoteMessage.getData().get("title");
        String flag = remoteMessage.getData().get("flag"); //보낸 메시지에 대한 플래그 확인...
        Boolean isBackground = Boolean.valueOf(remoteMessage.getData().get("is_background"));
//        String title = bundle.getString("title");
//        Boolean isBackground = Boolean.valueOf(bundle.getString("is_background"));
//        String flag = bundle.getString("flag");
//        String data = bundle.getString("data");
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "title: " + title);
        Log.d(TAG, "isBackground: " + isBackground);
        Log.d(TAG, "flag: " + flag);
        Log.d(TAG, "data: " + data);
        Log.e(TAG, "MessageRecevied."); //메시지를 받았습니다.
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        Log.e(TAG,"");

        if (remoteMessage == null)
            return;

        if(MyApplication.getInstance().getPrefManager().getUser() == null){
            // user is not logged in, skipping push notification
            Log.e(TAG, "user is not logged in, skipping push notification");
            return;
        }

        if (remoteMessage.getFrom().startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }


        switch (Integer.parseInt(flag)) {
            case Config.PUSH_TYPE_CHATROOM:
                Log.e(TAG,"processChatRoomPush를 실행.");
                // push notification belongs to a chat room
                processChatRoomPush(title, isBackground, data);
                break;
            case Config.PUSH_TYPE_USER:
                Log.e(TAG,"processUserMessage를 실행.");
                // push notification is specific to user
                processUserMessage(title, isBackground, data);
                break;
        }
    }

    /**
     * Processing chat room push message
     * this message will be broadcasts to all the activities registered
     * */
    //채팅 룸 푸시 메시지 생성
    private void processChatRoomPush(String title, boolean isBackground, String data) {

        if (!isBackground) {

            try {


                JSONObject datObj = new JSONObject(data);

                //가지고 온 채팅방 아이디 값
                String chatRoomId = datObj.getString("chat_room_id");
                Log.e(TAG,chatRoomId);

                JSONObject mObj = datObj.getJSONObject("message");
                Message message = new Message();
                message.setMessage(mObj.getString("message"));
                message.setId(mObj.getString("message_id"));
                message.setCreatedAt(mObj.getString("created_at"));

                JSONObject uObj = datObj.getJSONObject("user");

                User user = new User();
                user.setId(uObj.getString("user_id"));
                user.setEmail(uObj.getString("email"));
                user.setName(uObj.getString("name"));
                message.setUser(user); //유저클래스도 메시지 정보에 포함이 되어져 있다.
                //메시지 객체에 대한 정보를 모두 담았다.


                // skip the message if the message belongs to same user as
                // the user would be having the same message when he was sending
                // but it might differs in your scenario
                if (uObj.getString("user_id").equals(MyApplication.getInstance().getPrefManager().getUser().getId())) {
                    Log.e(TAG, "Skipping the push message as it belongs to same user");
                    return;
                }

                // 여기서 셰어드 프리퍼런스로 저장처리를 한다. 그러면 어디서든지 푸시가 셰어드 처리가 될 것이다.
                MyApplication.getInstance().getPrefManager().updaterowOtherActivity(chatRoomId,message.getMessage());

                //방상태에 대해서 확인 처리를 한다. (해당 방에 들어와 있는지에 대한 여부.)
                //현재 채팅방리스트 페이지에 와있는 상태가 아니라면, 채팅방에서 듣는 리시버 처리에 대한 검사를 합니다.
                if(MyApplication.getInstance().getPrefManager().getRoomnum().equals("0")==false){
                    Log.e(TAG, "현재 방에 들어와 있는 상태임으로 방번호를 체크하여 리시브 처리를 하도록 합니다.");
                    //여기서 유저가 들어가 있는 방이, 현재 메시지가 나타내고 있는 방과 다르다면, 리시브 처리를 하지않습니다.
                    if (chatRoomId.equals(MyApplication.getInstance().getPrefManager().getRoomnum())== false) {
                        Log.e(TAG, chatRoomId + "   "+ MyApplication.getInstance().getPrefManager().getRoomnum());
                        Log.e(TAG, "현재 들어와 있는 방정보가 다르므로, 메시지 처리를 하지 않습니다.");
                        return;
                    }

                }








                // verifying whether the app is in background or foreground
                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {

                    // app is in foreground, broadcast the push message
                    Log.e(TAG, "앱이 화면에 있는 상태라면.....");
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_TYPE_CHATROOM);
                    pushNotification.putExtra("message", message);
                    pushNotification.putExtra("chat_room_id", chatRoomId);



                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                    // play notification sound
                    NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                    notificationUtils.playNotificationSound();
                } else {
                    Log.e(TAG, "앱이 백그라운드에 있는 상태라면.....");
                    // app is in background. show the message in notification try
                    Intent resultIntent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                    resultIntent.putExtra("chat_room_id", chatRoomId);
                    showNotificationMessage(getApplicationContext(), title, user.getName() + " : " + message.getMessage(), message.getCreatedAt(), resultIntent); //해당 부분이 외부 노티피케이션 메시지를 나타낸다. **
                }

            } catch (JSONException e) {
                Log.e(TAG, "json parsing error: " + e.getMessage());
                //Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
    }

    /**
     * Processing user specific push message
     * It will be displayed with / without image in push notification tray
     * */
    private void processUserMessage(String title, boolean isBackground, String data) {
        if (!isBackground) {

            try {
                JSONObject datObj = new JSONObject(data);

                String imageUrl = datObj.getString("image");

                JSONObject mObj = datObj.getJSONObject("message");
                Message message = new Message();
                message.setMessage(mObj.getString("message"));
                message.setId(mObj.getString("message_id"));
                message.setCreatedAt(mObj.getString("created_at"));

                JSONObject uObj = datObj.getJSONObject("user");
                User user = new User();
                user.setId(uObj.getString("user_id"));
                user.setEmail(uObj.getString("email"));
                user.setName(uObj.getString("name"));
                message.setUser(user);

                //노티피케이션 함께 띄우기
                // app is in background. show the message in notification try
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class); //클릭시에 해당 액티비티로 넘어갑니다.
                Intent resultIntent2 = new Intent(getApplicationContext(), tradingProductActivity.class); //클릭시에 해당 액티비티로 넘어갑니다.
                Intent resultIntent3 = new Intent(getApplicationContext(), tradingProductActivity.class); //클릭시에 해당 액티비티로 넘어갑니다.
                resultIntent3.putExtra("IamSeller",true);

                // check for push notification image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    if(message.getMessage().contains("제품의 결제")){
                        showNotificationMessage(getApplicationContext(), title, user.getName() + "님과의 거래 : " + message.getMessage(), message.getCreatedAt(), resultIntent2);
                    }
                    else{
                        showNotificationMessage(getApplicationContext(), title, user.getName() + "님과의 거래 : " + message.getMessage(), message.getCreatedAt(), resultIntent2);
                    }

                } else {
                    // push notification contains image
                    // show it with the image
                    showNotificationMessageWithBigImage(getApplicationContext(), title, message.getMessage(), message.getCreatedAt(), resultIntent, imageUrl);
                }

                // verifying whether the app is in background or foreground
                if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {

                    Log.e(TAG, "앱이 현재안쪽에 있는 상태입니다.");

                    // app is in foreground, broadcast the push message
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", Config.PUSH_TYPE_USER); //PUSH_TYPE_USER인지에 대해서 확인한다.
                    pushNotification.putExtra("message", message);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                    // play notification sound
                    NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                    notificationUtils.playNotificationSound();
                } else {

//                    // app is in background. show the message in notification try
//                    Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
//
//                    // check for push notification image attachment
//                    if (TextUtils.isEmpty(imageUrl)) {
//                        showNotificationMessage(getApplicationContext(), title, user.getName() + " 님으로 부터 " + message.getMessage(), message.getCreatedAt(), resultIntent);
//                    } else {
//                        // push notification contains image
//                        // show it with the image
//                        showNotificationMessageWithBigImage(getApplicationContext(), title, message.getMessage(), message.getCreatedAt(), resultIntent, imageUrl);
//                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "json parsing error: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
    }


    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }


}


//    //notification핸들링 백그라운드인지에 대한 유무를 확인하여 처리
//    private void handleNotification(String message) {
//        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
//            // app is in foreground, broadcast the push message
//            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
//            pushNotification.putExtra("message", message);
//            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
//
//            // play notification sound
//            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
//            notificationUtils.playNotificationSound();
//        }else{
//            // If the app is in background, firebase itself handles the notification
//        }
//    }
//
//    //데이터를 json처리를 해주어야 한다....
//    private void handleDataMessage(JSONObject json) {
//        Log.e(TAG, "push json: " + json.toString());
//
//        try {
//            JSONObject data = json.getJSONObject("data");
//
//            String title = data.getString("title");
//            String message = data.getString("message");
//            boolean isBackground = data.getBoolean("is_background");
//            String imageUrl = data.getString("image");
//            String timestamp = data.getString("timestamp");
//            JSONObject payload = data.getJSONObject("payload");
//
//            Log.e(TAG, "title: " + title);
//            Log.e(TAG, "message: " + message);
//            Log.e(TAG, "isBackground: " + isBackground);
//            Log.e(TAG, "payload: " + payload.toString());
//            Log.e(TAG, "imageUrl: " + imageUrl);
//            Log.e(TAG, "timestamp: " + timestamp);
//
//
//            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
//                Log.e(TAG, "You are in foreground.");
//                // app is in foreground, broadcast the push message
//                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
//                pushNotification.putExtra("message", message);
//                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
//
//                // play notification sound
//                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
//                notificationUtils.playNotificationSound();
//            } else {
//                Log.e(TAG, "You are in background.");
//                // app is in background, show the notification in notification tray
//                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
//                resultIntent.putExtra("message", message);
//
//
//                // 해당 부분에서 notification을 실행 합니다//
//                // check for image attachment
//                if (TextUtils.isEmpty(imageUrl)) {
//                    Log.e(TAG, "showNotivicationMessage를 시작합니다.");
//                    Log.e(TAG, title);
//                    Log.e(TAG, message);
//                    Log.e(TAG, timestamp);
//                    //Log.e(TAG, resultIntent);
//                    NotificationCompat.Builder notificationBuilder =
//                            new NotificationCompat.Builder(this, "channelId")
//                                    .setSmallIcon(R.mipmap.ic_launcher)
//                                    .setContentTitle(title)
//                                    .setContentText(message)
//                                    .setAutoCancel(false);
//                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        String channelName = "Channel Name";
//                        NotificationChannel channel = new NotificationChannel("channelId", channelName, NotificationManager.IMPORTANCE_HIGH);
//                        notificationManager.createNotificationChannel(channel);
//                    }
//                    notificationManager.notify(0, notificationBuilder.build());
//
//                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
//                } else {
//                    // image is present, show notification with image
//                    showNotificationMessageWithBigImage(getApplicationContext(), title, message, timestamp, resultIntent, imageUrl);
//                }
//            }
//        } catch (JSONException e) {
//            Log.e(TAG, "Json Exception: " + e.getMessage());
//        } catch (Exception e) {
//            Log.e(TAG, "Exception: " + e.getMessage());
//        }
//    }
