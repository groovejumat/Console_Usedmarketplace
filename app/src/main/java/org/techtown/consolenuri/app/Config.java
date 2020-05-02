package org.techtown.consolenuri.app;

//해당 부분은 fcm과 관련되어지는 구성 정보들을 변수화 시키기 위해서 사용한다.
public class Config {

    // flag to identify whether to show single line
    // or multi line text in push notification tray
    public static boolean appendNotificationMessages = true;

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // type of push messages
    public static final int PUSH_TYPE_CHATROOM = 1;
    public static final int PUSH_TYPE_USER = 2;
    public static final int PUSH_PROGRESS_CHANGED = 3; //아직예정에는 없으나 제품이 변경되어지면 상태가 즉각적으로 변경처리되어지도록 하기위해서추가를 해 볼 예정.

    // id to handle the notification in the notification try
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;


    //이전 fcm을 작업하기 위해서 남겨두었던 파일들//

//    // global topic to receive app wide push notifications
//    public static final String TOPIC_GLOBAL = "global";
//
//    // broadcast receiver intent filters
//    public static final String REGISTRATION_COMPLETE = "registrationComplete";
//    public static final String PUSH_NOTIFICATION = "pushNotification";
//
//    // id to handle the notification in the notification tray
//    public static final int NOTIFICATION_ID = 100;
//    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;
//
    public static final String SHARED_PREF = "ah_firebase";

}
