package org.techtown.consolenuri.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.techtown.consolenuri.model.Address;
import org.techtown.consolenuri.model.ChatRoom;
import org.techtown.consolenuri.model.User;

import java.util.ArrayList;

//This class stores data in SharedPreferences. 셰어드 프리퍼런스에 데이터를 저장하기 위한 클래스이다.
//Here we temporarily stores the unread push notifications in order to append them to new messages. //일시적으로 새로 온 메시지를 읽지않은 경우에
//이를 저장처리 하기 위한 용도로 쓰일 예정이다.
//These methods will be called once the user successfully logged in.
public class MyPreferenceManager {

    private String TAG = MyPreferenceManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "androidhive_gcm";

    // All Shared Preferences Keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_ROOMNUM= "room_number";
    private static final String KEY_ROOMNAME= "room_name";

    // Constructor
    public MyPreferenceManager(Context context) {
        Log.e(TAG,"매니저 생성자 호출.");
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    // Store Room infomation
    public void storeRoomnum(String roomnum,String roomname) {
        Log.e(TAG,"공용 셰어드 프리퍼런스에 방정보 값을 저장 시켰습니다. 이는 onreceive처리시에 활용합니다.");
        editor.putString(KEY_ROOMNUM,roomnum);
        editor.putString(KEY_ROOMNAME,roomname);
        editor.commit(); //이거는 밥먹듯이 까먹더라.....
    }

    public String getRoomnum(){
        String Roomnum=pref.getString(KEY_ROOMNUM,null);
        return Roomnum;
    }

    //노티피케이션을 타서 방에 들어왔을 경우에 대한 처리를 도와줍니다. 이 부분은 문제가 될 여지가 많아서 차후 수정이 필요.
    public String getRoomname(){
        String Roomname=pref.getString(KEY_ROOMNAME,null);
        return Roomname;
    }



    public void storeUser(User user) {
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.commit();

        Log.e(TAG, "User is stored in shared preferences. " + user.getName() + ", " + user.getEmail());
    }

    public User getUser() {
        //키값에 유저가 있으면 유저정보를 리턴
        if (pref.getString(KEY_USER_ID, null) != null) {
            String id, name, email;
            id = pref.getString(KEY_USER_ID, null);
            name = pref.getString(KEY_USER_NAME, null);
            email = pref.getString(KEY_USER_EMAIL, null);

            User user = new User(id, name, email);
            return user;
        }
        //없으면 null값을 리턴
        return null;
    }


    public void addNotification(String notification) {

        // get old notifications
        String oldNotifications = getNotifications();

        if (oldNotifications != null) {
            oldNotifications += "|" + notification;
        } else {
            oldNotifications = notification;
        }

        editor.putString(KEY_NOTIFICATIONS, oldNotifications);
        editor.commit();
    }

    public String getNotifications() {
        return pref.getString(KEY_NOTIFICATIONS, null);
    }



    //임시적으로 채팅룸 상태를 저장하는 리스트 추가. 저장 된 값만을 불러와서 해당 어레이 리스트 값에 덮어 씌워 주도록 한다. 패치 할 때.
    public void storeChatRooms(ArrayList<ChatRoom> arrayList){ //상황에 따라 구분해서 넣어주면 된다.
        if(arrayList!=null){
            for(int i=0;i<arrayList.size();i++){
                Log.e(TAG,arrayList.get(i).getId()+"를 저장합니다.");
                editor.putString("chatid"+arrayList.get(i).getId(),arrayList.get(i).getId()); // 채팅방의 id
                editor.putString("LastMessage"+arrayList.get(i).getId(),arrayList.get(i).getLastMessage()); // 채팅방의 상태
                editor.putInt("UnreadCount"+arrayList.get(i).getId(),arrayList.get(i).getUnreadCount()); // 채팅방의 수정
                editor.commit();
            }
        }
    }
    //채팅방 상태 저장하기
    public void storeChatid(int i){

    }
    public void storeLastMessage(int i){

    }
    public void storeUnreadCount(int i){

    }

    //읽었을 때에 해당 chatid값의 셰어드 프리퍼런스 저장처리.
    public void resetChatroom(String i){
        Log.e(TAG,i +"를 초기화 합니다.");
        editor.putString("LastMessage"+i,"");
        editor.putInt("UnreadCount"+i,0);
        editor.commit();
    }

    //다른 액티비티에서 채팅룸 상태를 저장하기 위한 메서드
    public void updaterowOtherActivity(String chatid, String message){
        //unreadcount값을 일단 받아와야함
        Log.e(TAG,"다른엑티비티에 있을 때 메시지를 셰어드로 저장처리하는 메서드가 실행 됌.");

        int unreadcount = pref.getInt("UnreadCount"+chatid,0);

        //그리고 받아온 lastmessage와 unreadcount를 추가해 준다.
        editor.putString("LastMessage"+chatid,message); //푸시알람을 통해서 받아온 메시지를 추가
        editor.putInt("UnreadCount"+chatid,unreadcount+1);//불러온 값에서 +1을 해준다
        editor.commit();
    }


    //채팅 상태 가지고오기 (채팅방의 고유 아이디 정보를 받음.)
    public String getChatid(String i){
        return pref.getString("chatid"+i,null);
    }
    public String getLastMessage(String i){
        return pref.getString("LastMessage"+i,null);
    }
    public int getUnreadCount(String i){
        return pref.getInt("UnreadCount"+i,0);
    }



    //onpause시에 해당 주소지 정보를 모두 입력한다.
    public void storeAddresslist(ArrayList<Address> arrayList){
        if(arrayList!=null){
            for(int i=0;i<arrayList.size();i++){
                Gson gson = new GsonBuilder().create(); // GSON빌더 생성.
                String strAddress = gson.toJson(arrayList.get(i),Address.class); //어드래스 클래스 객체를 생성.
                Log.e(TAG,strAddress);
                editor.putString("address"+i,strAddress); // 채팅방의 id
            }
            editor.commit();
        }
    }

    public ArrayList<Address> getAddresslist(){
        ArrayList<Address> arraylist = new ArrayList<>();
        Gson gson = new GsonBuilder().create(); // GSON빌더 생성.
        int i = 0;
        while(pref.getString("address"+i,null)!=null){
            Address address = gson.fromJson(pref.getString("address"+i,null),Address.class);
            Log.e(TAG,"받아온 값 : " + address);
            arraylist.add(address);
            i++;
        }
        return arraylist;
    }


    public void clear() {
        editor.clear();
        editor.commit();
    }

}
