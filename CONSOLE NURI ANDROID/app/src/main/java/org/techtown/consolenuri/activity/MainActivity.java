package org.techtown.consolenuri.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.consolenuri.FileUpload.UploadActivity;
import org.techtown.consolenuri.R;
import org.techtown.consolenuri.adapter.ChatRoomsAdapter;
import org.techtown.consolenuri.app.Config;
import org.techtown.consolenuri.app.EndPoints;
import org.techtown.consolenuri.app.MyApplication;
import org.techtown.consolenuri.gcm.GcmIntentService;
import org.techtown.consolenuri.helper.SimpleDividerItemDecoration;
import org.techtown.consolenuri.model.ChatRoom;
import org.techtown.consolenuri.model.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;



    //Bottom 네비게이션 뷰 생성 이를 통해서 글작성기능을 추가할 예정. Onresult가 필요할 것 같음. 아니 없어도 됌. 일단은..
    BottomNavigationView bottomNavigationView;


    //채팅 리사이클러뷰를 만들기 위한 재료들 추가.
    private ArrayList<ChatRoom> chatRoomArrayList;
    private ChatRoomsAdapter mAdapter;
    private RecyclerView recyclerView;

    private String username; //현재 접속한 유저의 정보를 확인.

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MyApplication.getInstance().getPrefManager();

        /**
         * Check for login session. If not logged in launch
         * login activity
         * */
        //로그인 세션을 체크하고, 세션이 null(없음)상태라면 로그인 액티비티로 다시 간다?
        if (MyApplication.getInstance().getPrefManager().getUser() == null) {
            launchLoginActivity(); //로그인 액티비티로 가는 메소드.
        }
        username=MyApplication.getInstance().getPrefManager().getUser().getName();
        Log.e(TAG,"현재 로그인 상태인 유저 : "+username);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottomnavigationView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        /**
         * Broadcast receiver calls in two scenarios
         * 1. gcm registration is completed
         * 2. when new push notification is received
         * */
        //메시지를 받아오는 리시버 생성//
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG,"브로드캐스트 리시버가 인텐트를 받음 인텐트 키값에 따라 다양한 처리를 한다.");

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    subscribeToGlobalTopic();

                } else if (intent.getAction().equals(Config.SENT_TOKEN_TO_SERVER)) {
                    // gcm registration id is stored in our server's MySQL
                    // 토큰이 서버로 저장이 되어졌을 때를 확인
                    // gcm registration id is stored in our server's MySQL
                    Log.e(TAG, "GCM registration id is sent to our server");

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    // 새로운 push 가 왔을 때.
                    handlePushNotification(intent);
                }
            }
        };


        //채팅 리사이클러뷰를 생성//
        chatRoomArrayList = new ArrayList<>();
        mAdapter = new ChatRoomsAdapter(this, chatRoomArrayList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new ChatRoomsAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new ChatRoomsAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // when chat is clicked, launch full chat thread activity
                ChatRoom chatRoom = chatRoomArrayList.get(position);
                Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
                intent.putExtra("chat_room_id", chatRoom.getId()); //채팅룸으로 갈 때에 넘어가는 값 2개
                intent.putExtra("name", chatRoom.getName()); //채팅룸으로 갈 때에 넘어가는 값 2개
                intent.putExtra("product_id",chatRoom.getProductid()); //productid값을 넘기도록 함.

                chatRoomArrayList.get(position).setLastMessage(""); //읽은 내용
                chatRoomArrayList.get(position).setUnreadCount(0); //읽은 값에 대해서 0처리
                //해당 아이템이 있는 값을 초기화로 셰어드에 저장처리
                MyApplication.getInstance().getPrefManager().resetChatroom(chatRoom.getId());

                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        /**
         * Always check for google play services availability before
         * proceeding further with GCM
         * */
        if (checkPlayServices()) {
            registerGCM(); //FCM키값을 등록.
            //fetchChatRooms();
            Log.e(TAG,"채팅방을 패치를 실행 하도록 합니다.");

            Intent intent = getIntent();
            if(intent.getStringExtra("seller")==null){ //내가 구매중인 채팅방을 표시
                getSupportActionBar().setTitle("구매하려는 제품 채팅");
                Log.e(TAG,"내가 구매중인 채팅방을 패치합니다.");
                fetchTradeChatRooms(); //현재 유저가 보유하고 있는 채팅룸을 패치.
                bottomNavigationView.setSelectedItemId(R.id.action_chat); //현재 초이스 되어진 채팅룸을 패치
            }
            else{ //내가 판매중인 채팅방을 표시
                getSupportActionBar().setTitle("판매하려는 제품 채팅");
                Log.e(TAG,"내가 판매중인 채팅방을 패치합니다.");
                fetchSellerChatRooms(); //현재 판매자가 작성한 제품에 대한 채팅룸을 패치.
                bottomNavigationView.setSelectedItemId(R.id.action_sellchat); //현재 판매중인 채팅룸을 패치
            }
        }


        //바텀네비게시연 뷰 리스너를 달도록 한다.
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.action_productlist: {
                        Log.e(TAG,"제품리스트 버튼을 클릭했음.");
                        //ItemUploadActvitiy 실행 인텐트 작성//
                        Intent intent=new Intent(getApplicationContext(), productListActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();

                        return true;
                    }


                    case R.id.action_write: {
                        Log.e(TAG,"바텀 글작성 버튼을 클릭했음.");
                        //ItemUploadActvitiy 실행 인텐트 작성//
                        Intent intent=new Intent(getApplicationContext(), UploadActivity.class);
                        startActivity(intent);


                        return true;
                    }


                    case R.id.action_chat: {
                        Log.e(TAG,"바텀 채팅실행 버튼을 클릭했음.");
                        //채팅 실행 인텐트 작성//
                        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();

                        return true;
                    }

                    case R.id.action_sellchat: {
                        Log.e(TAG,"바텀 채팅실행 버튼을 클릭했음.");
                        //채팅 실행 인텐트 작성 (판매자 전용)//
                        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("seller","seller");
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();

                        return true;
                    }


                    default:
                        return false;
                }
            }
        });
    }


    /**
     * Handles new push notification 새로운 푸쉬가 왔을 때에 대한 처리.
     */
    public void handlePushNotification(Intent intent) {
        int type = intent.getIntExtra("type", -1);

        // if the push is of chat room message // 해당 채팅룸에 푸쉬가 온다면
        // simply update the UI unread messages count // 간단하게 읽지않음의 카운트를 ui로 표시해주도록 한다. //이 부분에서 덤으로 세이브 처리를 활용 할 수도 있겠다.
        if (type == Config.PUSH_TYPE_CHATROOM) {
            Message message = (Message) intent.getSerializableExtra("message");
            String chatRoomId = intent.getStringExtra("chat_room_id");

            if (message != null && chatRoomId != null) {
                updateRow(chatRoomId, message); //이 부분이 줄 수를 말하는 듯 하다.
            }
        } else if (type == Config.PUSH_TYPE_USER) {
            // push belongs to user alone 메시지가 한명한테 가는 것이라면?
            // just showing the message in a toast 토스트로 메세지를 알려준다.
            Log.e(TAG,"해당 메시지는 개인에게 보내는 메시지 입니다.");
            Message message = (Message) intent.getSerializableExtra("message");
            Toast.makeText(getApplicationContext(), "New push: " + message.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Updates the chat list unread count and the last message 마지막으로 안읽은 메시지를 표시해주도록 한다.
     */
    //해당 부분에서 안읽어온 알람에 대한 리사이클러뷰 처리를 해준다.
    public void updateRow(String chatRoomId, Message message) {
        for (ChatRoom cr : chatRoomArrayList) {
            if (cr.getId().equals(chatRoomId)) {
                int index = chatRoomArrayList.indexOf(cr);
                //cr.setLastMessage(message.getMessage());
                cr.setLastMessage(message.getUser().getName() + " : " + message.getMessage());
                cr.setUnreadCount(cr.getUnreadCount() + 1);
                chatRoomArrayList.remove(index);
                chatRoomArrayList.add(index, cr);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }


    /**
     * fetching the chat rooms by making http call
     */
    //http call을 통한 채팅방을 불러오기 (모든 방 기존에 사용 중이던 방법. 건드리지 말 것.)
    private void fetchChatRooms() {
        StringRequest strReq = new StringRequest(Request.Method.GET,
                EndPoints.CHAT_ROOMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms");
                        for (int i = 0; i < chatRoomsArray.length(); i++) {
                            JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                            ChatRoom cr = new ChatRoom();
                            cr.setId(chatRoomsObj.getString("chat_room_id"));
                            cr.setName(chatRoomsObj.getString("name"));
                            cr.setLastMessage("");
                            cr.setUnreadCount(0);
                            cr.setTimestamp(chatRoomsObj.getString("created_at"));

                            chatRoomArrayList.add(cr);
                        }

                    } else {
                        // error in fetching chat rooms
                        Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                mAdapter.notifyDataSetChanged();

                // subscribing to all chat room topics
                subscribeToAllTopics(); //불러온 채팅룸에 대해서 모두 구독처리를 한다.
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }



    /**
     * fetching the chat rooms by making http call
     */
    //http call을 통한 내가 현재 신청한 채팅룸 보기.
    private void fetchTradeChatRooms() {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                EndPoints.TRADE_CHATROOMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms");
                        for (int i = 0; i < chatRoomsArray.length(); i++) {
                            JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                            ChatRoom cr = new ChatRoom();
                            cr.setId(chatRoomsObj.getString("chat_room_id"));
                            String crid=chatRoomsObj.getString("chat_room_id"); //String 값으로 저장;
                            cr.setName(chatRoomsObj.getString("product_name")); //여기서는 제품품목 명을 기준으로 채팅방의 이름을 세팅한다.
                            if(MyApplication.getInstance().getPrefManager().getChatid(crid)!=null){
                                cr.setLastMessage(MyApplication.getInstance().getPrefManager().getLastMessage(crid));
                                cr.setUnreadCount(MyApplication.getInstance().getPrefManager().getUnreadCount(crid));
                            }
                            else
                            {
                                cr.setLastMessage(""); //마지막 메시지
                                cr.setUnreadCount(0); //안읽은 메시지의 카운트 수
                            }
                            //제품의 아이디값 세팅하기
                            cr.setProductid(chatRoomsObj.getString("product_id"));
                            Log.e(TAG,"채팅룸이 가지고 있는 제품아이디값 : " + chatRoomsObj.getString("product_id"));

                            cr.setTimestamp(chatRoomsObj.getString("created_at"));
                            //여기다가 proudct생성 아이디를 추가.


                            chatRoomArrayList.add(cr);
                        }

                    } else {
                        // error in fetching chat rooms
                        Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                mAdapter.notifyDataSetChanged();

                // subscribing to all chat room topics
                subscribeToAllTopics();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){

            //post로 보내는 값 담기.
            //post처리를 할 때 파라미터 값을 넣어준다. (현재 테스트하는 값은 user2)
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>(); //파라미터 값을 넣는 HashMap
                params.put("user_name", username); //파라 미터 값을 넣는 처리.

                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };


        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }




    /**
     * fetching the chat rooms by making http call
     */
    //http call을 통한 내가 현재 내가 판매중인 제품에 관한 채팅 리스트 보기
    private void fetchSellerChatRooms() {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                EndPoints.SELLER_CHATROOMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "seller response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms");
                        for (int i = 0; i < chatRoomsArray.length(); i++) {
                            JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                            ChatRoom cr = new ChatRoom();
                            cr.setId(chatRoomsObj.getString("chat_room_id"));
                            String crid=chatRoomsObj.getString("chat_room_id"); //String 값으로 저장;
                            cr.setName(chatRoomsObj.getString("product_name")); //여기서는 제품품목 명을 기준으로 채팅방의 이름을 세팅한다.
                            if(MyApplication.getInstance().getPrefManager().getChatid(crid)!=null){
                                cr.setLastMessage(MyApplication.getInstance().getPrefManager().getLastMessage(crid));
                                cr.setUnreadCount(MyApplication.getInstance().getPrefManager().getUnreadCount(crid));
                            }
                            else
                            {
                                cr.setLastMessage(""); //마지막 메시지
                                cr.setUnreadCount(0); //안읽은 메시지의 카운트 수
                            }
                            //제품의 아이디값 세팅하기
                            cr.setProductid(chatRoomsObj.getString("product_id"));
                            Log.e(TAG,"채팅룸이 가지고 있는 제품아이디값 : " + chatRoomsObj.getString("product_id"));

                            cr.setTimestamp(chatRoomsObj.getString("created_at"));



                            chatRoomArrayList.add(cr);
                        }

                    } else {
                        // error in fetching chat rooms
                        Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                mAdapter.notifyDataSetChanged();

                // subscribing to all chat room topics
                subscribeToAllTopics();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){

            //post로 보내는 값 담기.
            //post처리를 할 때 파라미터 값을 넣어준다. (현재 테스트하는 값은 user2)
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>(); //파라미터 값을 넣는 HashMap
                params.put("seller_name", username); //파라 미터 값을 넣는 처리.

                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };


        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }





    // subscribing to global topic 모든 채팅방에 대한 청취
    private void subscribeToGlobalTopic() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE); //현재 채팅으로 거래를 시작하는 사람에 대해서 구동 실행하기.
        intent.putExtra(GcmIntentService.TOPIC, Config.TOPIC_GLOBAL);
        startService(intent);
    }

    // Subscribing to all chat room topics 현존하는 모든 채팅룸 topic에 대한 청취??
    // each topic name starts with `topic_` followed by the ID of the chat room 각각의 방이름들은 topic_ 의 형식으로 지정됌.
    // Ex: topic_1, topic_2 예시
    private void subscribeToAllTopics() {
        for (ChatRoom cr : chatRoomArrayList) { //현재 불러온 채팅룸에 대한 모든 채팅 배열에 대해서 subscribe를 합니다. 여기서 문제가 생길 확률이 높음.

            Intent intent = new Intent(this, GcmIntentService.class);
            intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
            intent.putExtra(GcmIntentService.TOPIC, "topic_" + cr.getId()); //현재 구독한 토픽에 대한 아이디 값을 보내어 준다.(아이디 값을 기준으로 토픽을 구분한다.)
            Log.e(TAG,"구독을 진행하는 토픽에 대한 정보를 출력"+cr.getId()+""+cr.getName());
            startService(intent);
        }
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    //이 부분에 대해서 셰어드 값을 0로 처리한다면 되지않을까.
    @Override
    protected void onResume() {
        super.onResume();
        //방상태를 새로 패치하도록 하기.
        for(int i =0; i<chatRoomArrayList.size(); i++){
            if(MyApplication.getInstance().getPrefManager().getChatid(chatRoomArrayList.get(i).getId())!=null){
                chatRoomArrayList.get(i).setLastMessage(MyApplication.getInstance().getPrefManager().getLastMessage(chatRoomArrayList.get(i).getId()));
                chatRoomArrayList.get(i).setUnreadCount(MyApplication.getInstance().getPrefManager().getUnreadCount(chatRoomArrayList.get(i).getId()));
            }
        }
        mAdapter.notifyDataSetChanged();





        //공용 셰어드 프리퍼런스를 활용하여 방 넘버값을 저장 시킵니다. 0은 현재 채팅방에 나와 있음을 의미합니다.
        MyApplication.getInstance().getPrefManager().storeRoomnum("0","roomlist");
        Log.e(TAG,"현재 채팅방 리스트 페이지로 와있는 상태입니다.");


        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives //해당 부분이 제대로 동작을해야지 실시간 채팅창이 변화가 가능하다.
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION)); //이 부분을 클래스화 시키면 되지 않을까??

        //채팅룸이 바로생성된 시점에서 생긴 채팅룸이라면, 액티비티를 해당 채팅룸으로 바로 이동 시킨다.
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver); //중지해당 브로드 케스트리시버를 unregister한다.
        MyApplication.getInstance().getPrefManager().storeChatRooms(chatRoomArrayList); //셰어드 프리퍼런스로 해당 값들을 저장합니다.
        super.onPause();
    }

    // starting the service to register with GCM 키등록을 실행하는 메서드
    private void registerGCM() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra("key", "register");
        //startService(intent);

        //최근 버젼에서는 다음과 같이 서비스를 실행해주어야 에러가 나지않는다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(intent);
        } else {
            this.startService(intent);
        }
    }

    //서비스가 돌고있는지에 대한 확인 메서드
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported. Google Play Services not installed!");
                Toast.makeText(getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }


    //상단 메뉴바 생성 하기.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_logout:
                MyApplication.getInstance().logout();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
