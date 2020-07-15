package org.techtown.consolenuri.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.consolenuri.FileUpload.ApiService;
import org.techtown.consolenuri.FileUpload.FileUtils;
import org.techtown.consolenuri.FileUpload.InternetConnection;
import org.techtown.consolenuri.Fragment.ChatImageFragment;
import org.techtown.consolenuri.R;
import org.techtown.consolenuri.adapter.ChatRoomThreadAdapter;
import org.techtown.consolenuri.adapter.ChatRoomsAdapter;
import org.techtown.consolenuri.app.Config;
import org.techtown.consolenuri.app.EndPoints;
import org.techtown.consolenuri.app.MyApplication;
import org.techtown.consolenuri.gcm.NotificationUtils;
import org.techtown.consolenuri.model.ChatRoom;
import org.techtown.consolenuri.model.Message;
import org.techtown.consolenuri.model.User;
import org.techtown.consolenuri.webRTC.ConnectActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatRoomActivity extends AppCompatActivity {

    private String TAG = ChatRoomActivity.class.getSimpleName();

    private String chatRoomId;
    private RecyclerView recyclerView;
    private ChatRoomThreadAdapter mAdapter;
    private ArrayList<Message> messageArrayList;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private EditText inputMessage;
    private Button btnSend, btnStreamingbutton;
    private ImageView sendImage;

    //이미지 팝업 프래그먼트를 추가
    ChatImageFragment chatimagefragmet = new ChatImageFragment();

    //상단에 제품정보를 간단하게 담는 product와 관련된 뷰들
    private TextView productcategoryView,productpriceView;
    private ImageView productthumbnailView;

    //직거래 지도 정보를 확인하기 위한 버튼
    private Button tradinglocationView;

    public ArrayList<ChatRoom> chatRoomArrayList = new ArrayList<ChatRoom>();
    private ArrayList<Uri> arrayList = new ArrayList<>(); // 사진 Uri정보를 담는 어레이 리스트

    //프로그래스 다이얼로그
    private ProgressDialog progressDialog; //어느페이지에서든 쓸 수 있도록 프로그레스바를 준비.

    //사진을 참조 허가를 받았을때와 읽어 왓을 때의 result 결과를 받기 위한 값.
    private final int REQUEST_CODE_PERMISSIONS  = 1;
    private final int REQUEST_CODE_READ_STORAGE = 2;


    //상단에 제품정보를 간략하게 나타대기 위한 표시 뷰.
    private String product_id,productname,productprice,productwriter,imagelist,productdescription,username,userid,writerid,
            categorycreated;


    //제품정보에 대한 이미지 리스트를 나타낸다.
    ArrayList<String> image = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //새로추가(이미지기능)
        sendImage=(ImageView)findViewById(R.id.uploadimageView);
        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //해당 제품을 클릭하면 가져온 이미지가 Uri리스트에 담기게 되어지고, 해당 Uri에 있는리스트를 파일화하여, php서버로 전송합니다.
                showChooser();
            }
        });

        //방의 정보를 불러옵니다.
        fetchChatRooms();

        inputMessage = (EditText) findViewById(R.id.message);
        btnSend = (Button) findViewById(R.id.btn_send);
        btnStreamingbutton = (Button) findViewById(R.id.streamingbutton);


        //스트리밍 버튼을 추가했습니다. 일단은 버튼을 누르면 webrtc엑티비티로 넘어가지도록합니다.
        //스트리밍 버튼 리스너 : 해당 버튼을 누르면(판매자한테만 보이도록) 스트리밍을 시작하고, 유저에게 알림을 줍니다.
        btnStreamingbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //구매자 유저에게 알림을 줍니다.
                loading();
                AlertMessage();
            }
        });


        Intent intent = getIntent();
        chatRoomId = intent.getStringExtra("chat_room_id");
        Log.e(TAG,"현재 들어온 방은 " + chatRoomId + "입니다.");


        String title = intent.getStringExtra("name");
        Log.e(TAG,"현재 들어온 방이름 " + title + "입니다.");


        product_id = intent.getStringExtra("product_id");
        Log.e(TAG,"해당 제품의 productid는 " + product_id + "입니다.");

        //상단에 제품정보 요약해서 보여주기
        productthumbnailView=(ImageView)findViewById(R.id.thumbnail);
        productcategoryView=(TextView)findViewById(R.id.productcategory);
        productpriceView=(TextView)findViewById(R.id.productprice);
        fetchProduct(); // product_id를 받은 제품을 패치한다.

        //공용 셰어드 프리퍼런스를 활용하여 방 넘버값을 저장 시킵니다.
        MyApplication.getInstance().getPrefManager().storeRoomnum(chatRoomId,title);

        Log.e(TAG,"현재 들어온 방의 이름 : " +title);

        //노티피케이션 pending intent로 제목 값을 넘겨 줘야 한다면??
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); //뒤로가기 버튼 취소

        if (chatRoomId == null) {
            Toast.makeText(getApplicationContext(), "Chat room not found!", Toast.LENGTH_SHORT).show();
            finish();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        messageArrayList = new ArrayList<>();

        // self user id is to identify the message owner
        String selfUserId = MyApplication.getInstance().getPrefManager().getUser().getId();

        mAdapter = new ChatRoomThreadAdapter(this, messageArrayList, selfUserId); //채팅 룸 어댑터

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        //터치리스너생성
        recyclerView.addOnItemTouchListener(new ChatRoomThreadAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new ChatRoomsAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.e(TAG,position+"채팅내용을 클릭함.");
                if(messageArrayList.get(position).getMessage().contains("http://192.168.244.105")){
                    Bundle bundle = new Bundle();
                    bundle.putString("url",messageArrayList.get(position).getMessage());
                    chatimagefragmet.setArguments(bundle);
                    //프레그먼트로 이미지 띄우기
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,chatimagefragmet).commit();
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

//        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
//                    int type =intent.getIntExtra("type",0);
//                    Log.e(TAG, type+" : 현재 푸시의 메시지 타입 넘버 확인.");
//
//
//                    // new push message is received 가 되었다면, 해당 메시지를 다루는 인텐트를 실행.
//                    handlePushNotification(intent);
//                }
//            }
//        };

        //입력한 메시지를 보내기.
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //임시메시지 추가기능//
                //userid와 메시지를 클릭시 바로추가하고 (임시), 실제 리사이클러뷰에 대한 처리는 onMessageReceive를 통해 받아왔을 때에 처리가 되어지도록 한다.
                Message tmpmessage = new Message();
                tmpmessage.setId("tmp");
                final String message = inputMessage.getText().toString().trim();
                tmpmessage.setMessage(message);

                //현재 임시 시간 가지고 오기
                long now = System.currentTimeMillis();
                // Data 객체에 시간을 저장한다.
                Date date = new Date(now);
                // 각자 사용할 포맷을 정하고 문자열로 만든다.
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String createdAt = sdfNow.format(date);
                tmpmessage.setCreatedAt(createdAt);
                tmpmessage.setUser(MyApplication.getInstance().getPrefManager().getUser());

                messageArrayList.add(tmpmessage);
                mAdapter.notifyDataSetChanged();
                recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                ////

                sendMessage();
            }
        });

        fetchChatThread();
        //recyclerView.getLayoutManager().scrollToPosition(mAdapter.getItemCount() - 1); //


        //리사이클러뷰의 내용이 바뀌었을 때, 리사이클러뷰가 이를 감지하고 채팅을 맨아래로 내리도록 한다.
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {

                if (bottom < oldBottom) {
                    if(messageArrayList.size()>0){
                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.smoothScrollToPosition(
                                        recyclerView.getAdapter().getItemCount() - 1);
                            }
                        }, 100);
                    }
                }
            }
        });

//        //맨아래로~
//        recyclerView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                recyclerView.smoothScrollToPosition(
//                        recyclerView.getAdapter().getItemCount() - 1);
//            }
//        }, 300);


        //메시지를 받아오는 리시버 생성//
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG,"브로드캐스트 리시버가 인텐트를 받음. 인텐트 키값에 따라 다양한 처리를 한다.");

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    // subscribeToGlobalTopic();

                } else if (intent.getAction().equals(Config.SENT_TOKEN_TO_SERVER)) {
                    // gcm registration id is stored in our server's MySQL
                    // 토큰이 서버로 저장이 되어졌을 때를 확인
                    // gcm registration id is stored in our server's MySQL
                    Log.e(TAG, "GCM registration id is sent to our server");

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    int type = intent.getIntExtra("type",0);
                    Log.e(TAG,type+"");

                    //type에 따른 handleNotification 분기처리 실행.
                    if(type==1){
                        handlePushNotification(intent); //메시지가 왔을때에 대한 처리.
                    }
                    //USER메시지로 올경우에는 다른 알람처리를 실시한다.
                    else {
                        handleAlertNotification(intent); //특정 알림이 왔을 때에 대한 처리.
                    }

                }
            }
        };




        //버튼을 클릭하면 구글 맵으로 이동하여, 현재 직거래 장소를 확인해 볼 수 있습니다.
        tradinglocationView=(Button) findViewById(R.id.seeTradinglocation);
        tradinglocationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading();
                boolean IamSeller=writerid.equals(MyApplication.getInstance().getPrefManager().getUser().getId()); // 내가 판매자인지 아닌지에 대한 정보를 구글 맵 페이지로 보냅니다.
                Intent intent = new Intent(ChatRoomActivity.this,googleMapActivity.class);//구글맵으로 이동합니다.
                intent.putExtra("roomId",chatRoomId);
                intent.putExtra("IamSeller",IamSeller);
                Log.e(TAG,"전달하는 채팅룸 아이디 값 : " + chatRoomId);
                startActivity(intent); //해당 엑티비티로 이동.
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        //fetchChatThread();

        // registering the receiver for new notification
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION)); //새로운 notification처리를 하기 위한 리시버 등록.



        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }


    @Override
    protected void onStop() {
        loadingEnd();
        super.onStop();
    }


    /**
     * Handling new push message, will add the message to
     * recycler view and scroll it to bottom
     * */
    private void handlePushNotification(Intent intent) {
        //여기서 스트리밍 알림 분기처리를 실행시킨다.
        //if(//메시지가 어떤 스타일인지를 구분.)
        Log.e(TAG,"handlePushNotification Worked.");
        Message message = (Message) intent.getSerializableExtra("message");
        String chatRoomId = intent.getStringExtra("chat_room_id");

        if(message.getMessage().contains("[스트리밍 시작 알람]")){
            Log.e(TAG,"스트리밍 시작 알람이 들어왔습니다. 이에 따른 처리를 합니다.");
            Toast.makeText(getApplicationContext(), "판매자가 제품 리뷰를 시작했습니다.", Toast.LENGTH_SHORT).show();
            btnStreamingbutton.setVisibility(View.VISIBLE);
            btnStreamingbutton.setText("실시간 제품 보기");
        }

        else{
            if (message != null && chatRoomId != null) {
                messageArrayList.add(message);
                mAdapter.notifyDataSetChanged();
                if (mAdapter.getItemCount() > 1) {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG,"푸시를 받고 처리한뒤에, 스크롤을 최하단으로 이동합니다.");
                            recyclerView.getLayoutManager().scrollToPosition(mAdapter.getItemCount()-1);
                        }
                    }, 200);
                }
            }
        }
    }




    private void handleAlertNotification(Intent intent) {
        //여기서 스트리밍 알림 분기처리를 실행시킨다.
        //if(//메시지가 어떤 스타일인지를 구분.)
        Log.e(TAG,"handleAlertNotification Worked.");
        Message message = (Message) intent.getSerializableExtra("message");
        String chatRoomId = intent.getStringExtra("chat_room_id");
        Log.e(TAG,message+"");
    }

    /**
     * Posting a new message in chat room
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     * */
    private void sendMessage() {
        final String message = this.inputMessage.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(getApplicationContext(), "Enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        String endPoint = EndPoints.CHAT_ROOM_MESSAGE.replace("_ID_", chatRoomId);

        Log.e(TAG, "endpoint: " + endPoint);

        this.inputMessage.setText("");

        StringRequest strReq = new StringRequest(Request.Method.POST,
                endPoint, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error
                    if (obj.getBoolean("error") == false) {
                        //바로 나타나 지도록하기.

                        JSONObject commentObj = obj.getJSONObject("message");

                        String commentId = commentObj.getString("message_id");
                        String commentText = commentObj.getString("message");
                        String createdAt = commentObj.getString("created_at");

                        JSONObject userObj = obj.getJSONObject("user");
                        String userId = userObj.getString("user_id");
                        String userName = userObj.getString("name");
                        User user = new User(userId, userName, null);

                        Message message = new Message();
                        message.setId(commentId);
                        message.setMessage(commentText);
                        message.setCreatedAt(createdAt);
                        message.setUser(user);



                        //[리사이클러뷰를 통해 response값을 받아오는 처리를 하였으나, 자신이 보낸 메시지는 바로 처리되어지도록 하기위해서 해당 부분을 중지하였다.]
                        //messageArrayList.add(message);
                        //mAdapter.notifyDataSetChanged();
                        if (mAdapter.getItemCount() > 1) {
                            // scrolling to bottom of the recycler view
                            // recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                            recyclerView.getLayoutManager().scrollToPosition(mAdapter.getItemCount() - 1);
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "" + obj.getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                inputMessage.setText(message);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", MyApplication.getInstance().getPrefManager().getUser().getId());
                params.put("message", message);

                Log.e(TAG, "Params: " + params.toString());

                return params;
            };
        };


        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        strReq.setRetryPolicy(policy);

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }


    //채팅방에서 스트리밍이 시작되어졌음을 알려주는 메시지이다. 자신은 처리하지 않는다.
    private void AlertMessage() {
        Log.e(TAG,"알림 메시지가 동작하였음.");

        String endPoint = EndPoints.CHAT_ROOM_MESSAGE.replace("_ID_", chatRoomId);

        Log.e(TAG, "endpoint: " + endPoint);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                endPoint, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                Intent intent = new Intent(ChatRoomActivity.this, ConnectActivity.class); // Conntect엑티비티로 이동합니다. 해당 엑티비티에서 특정한 처리를 해 줄 예정입니다.
                intent.putExtra("roomid","room"+chatRoomId); // 채팅방에 값을 넣어 줍니다.
                if(writerid.equals(MyApplication.getInstance().getPrefManager().getUser().getId())){
                    Log.e(TAG,"당신이 작성자 입니다.");
                    intent.putExtra("IamSeller",true);
                }
                else{
                    //작성자가 아니라면, 클릭후 버튼을 다시 GONE 처리 해줘서 알람왔을 때만 들어올 수 있도록 합니다.
                    btnStreamingbutton.setVisibility(View.GONE);
                }
                startActivity(intent);
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
                params.put("user_id", MyApplication.getInstance().getPrefManager().getUser().getId());
                params.put("message", "[스트리밍 시작 알람]");

                Log.e(TAG, "Params: " + params.toString());

                return params;
            };
        };


        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        strReq.setRetryPolicy(policy);

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }



    /**
     * Posting a new message in chat room
     * will make an http call to our server. Our server again sends the message
     * 이미지에 대한 메시지징을합니다.
     * */
    private void sendImage(String url) {
        final String message = url;

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(getApplicationContext(), "Enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        String endPoint = EndPoints.CHAT_ROOM_MESSAGE.replace("_ID_", chatRoomId);

        Log.e(TAG, "endpoint: " + endPoint);

        this.inputMessage.setText("");

        StringRequest strReq = new StringRequest(Request.Method.POST,
                endPoint, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error
                    if (obj.getBoolean("error") == false) {
                        //바로 나타나 지도록하기.

                        JSONObject commentObj = obj.getJSONObject("message");

                        String commentId = commentObj.getString("message_id");
                        String commentText = commentObj.getString("message");
                        String createdAt = commentObj.getString("created_at");

                        JSONObject userObj = obj.getJSONObject("user");
                        String userId = userObj.getString("user_id");
                        String userName = userObj.getString("name");
                        User user = new User(userId, userName, null);

                        Message message = new Message();
                        message.setId(commentId);
                        message.setMessage(commentText);
                        message.setCreatedAt(createdAt);
                        message.setUser(user);



                        //[리사이클러뷰를 통해 response값을 받아오는 처리를 하였으나, 자신이 보낸 메시지는 바로 처리되어지도록 하기위해서 해당 부분을 중지하였다.]
                        //messageArrayList.add(message);
                        //mAdapter.notifyDataSetChanged();
                        if (mAdapter.getItemCount() > 1) {
                            // scrolling to bottom of the recycler view
                            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "" + obj.getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                inputMessage.setText(message);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", MyApplication.getInstance().getPrefManager().getUser().getId());
                params.put("message", message);

                Log.e(TAG, "Params: " + params.toString());

                return params;
            };
        };


        // disabling retry policy so that it won't make
        // multiple http calls
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        strReq.setRetryPolicy(policy);

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }


    /**
     * Fetching all the messages of a single chat room
     * */
    //만들어놓은 api링크를 통해서 채팅룸에 request를 요청. 여기서 채팅룸의 아이디 값을 받아온다. (새롭게 생긴 채팅룸에 대해서 처리를 해주면 되겠지.)
    private void fetchChatThread() {

        //_ID_ 로 되어진 문자열 부분에 대한 값을 chatRoomId에 있는 값으로 대체 한다.
        String endPoint = EndPoints.CHAT_THREAD.replace("_ID_", chatRoomId);
        Log.e(TAG, "endPoint: " + endPoint);

        StringRequest strReq = new StringRequest(Request.Method.GET,
                endPoint, new Response.Listener<String>() {

            //api를 호출하여 불러온 값 확인 및 이를 json으로 반환하여 데이터를 나타내는 처리를 한다.
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error
                    if (obj.getBoolean("error") == false) {
                        JSONArray commentsObj = obj.getJSONArray("messages");

                        for (int i = 0; i < commentsObj.length(); i++) {
                            JSONObject commentObj = (JSONObject) commentsObj.get(i);

                            String commentId = commentObj.getString("message_id");
                            String commentText = commentObj.getString("message");
                            String createdAt = commentObj.getString("created_at");

                            JSONObject userObj = commentObj.getJSONObject("user");
                            String userId = userObj.getString("user_id");
                            String userName = userObj.getString("username");
                            User user = new User(userId, userName, null);

                            Message message = new Message();
                            message.setId(commentId);
                            message.setMessage(commentText);
                            message.setCreatedAt(createdAt);
                            message.setUser(user);

                            //메시지 알림 처리.....
                            if(message.getMessage().contains("[스트리밍 시작 알람]")==false){
                                messageArrayList.add(message);
                            }
                        }

                        mAdapter.setMessageArrayList(messageArrayList);
                        mAdapter.notifyDataSetChanged();
                        if (mAdapter.getItemCount() > 1) {
                            //핸들러를 통해서 약간의 텀을 준 후 이동하는 것으로 조치를 취하자.
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(TAG,"스크롤을 최하단으로 이동합니다.");
                                    recyclerView.scrollToPosition(mAdapter.getItemCount()-1);
                                }
                            }, 200);
                            //recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                            //recyclerView.getLayoutManager().scrollToPosition(mAdapter.getItemCount()-1);
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Adding request to request queue (해당 작업이 필요한 이유는 뭘까....)
        MyApplication.getInstance().addToRequestQueue(strReq);
    }



    //이미지 파일들을 서버로 보내주는 작업을 처리한다.
    private void uploadImagesToServer() {
        //인터넷 연결확인
        if (InternetConnection.checkConnection(ChatRoomActivity.this)) {
            //레트로 피트 객체 생성 .기본 URL .GsonConverterFactory생성 .그리고 빌드.
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiService.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            //프로그래스바를 나타냄.
            //showProgress();

            // create list of file parts (파일 정보들을 담는 리사이클러 뷰를 생성)
            List<MultipartBody.Part> parts = new ArrayList<>(); //파일 자체를 가지고 있음..??

            // create upload service client 업로드 php파일을 사용하기위해서 retrofit객체에 이를 연결 함.
            ApiService service = retrofit.create(ApiService.class);

            //arraylist값이 null이 아니라면 넣는 작업을 진행한다.
            if (arrayList != null) {
                // create part for file (photo, video, ...)
                for (int i = 0; i < arrayList.size(); i++) {
                    //parts 에 파일 정보들을 저장 시킵니다. 파트네임은 임시로 설정이 되고, uri값을 통해서 실제 파일을 담습니다.
                    parts.add(prepareFilePart("image"+i, arrayList.get(i))); //partName 으로 구분하여 이미지를 등록한다. 그리고 파일객체에 값을 넣어준다.
                }
            }

            // create a map of data to pass along
//            RequestBody userid = createPartFromString(""+MyApplication.getInstance().getPrefManager().getUser().getId());
//            RequestBody username = createPartFromString(""+MyApplication.getInstance().getPrefManager().getUser().getName());
//            RequestBody categorypart = createPartFromString(""+category);
            RequestBody description = createPartFromString("www.androidlearning.com");
            RequestBody size = createPartFromString(""+parts.size());
//            themepart=itemtheme.getText().toString();
//            pricepart=itemprice.getText().toString();
//            descriptionpart=itemdescription.getText().toString();

//            Log.e(TAG,"categorypart에 대한 정보 : "+categorypart+"");
//            Log.e(TAG,"themepart에 대한 정보 : "+themepart+"");
//            Log.e(TAG,"pricepart에 대한 정보 : "+pricepart+"");
//            Log.e(TAG,"descriptionpart에 대한 정보 : "+descriptionpart+"");
            Log.e(TAG,"parts에 대한 정보 : "+parts+"");
            Log.e(TAG,"size에 대한 정보 : "+size+"");

            // finally, execute the request 서버에 있는 uploads.php함수를 실행하여, 서버에 이미지를 업로드한다. 여기서 필요한 값들을 다같이 담아서 보내게 된다??
            Call<ResponseBody> call = service.uploadImageMessage(description ,size, parts);
            Log.e(TAG,call+""+"해당 정보를 보냄.");

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                    //hideProgress();
                    if(response.isSuccessful()) {
                        //finish(); //해당 엑티비티 종료.
                      //Toast.makeText(ChatRoomActivity.this,
                                //"Images successfully uploaded!" + response, Toast.LENGTH_SHORT).show();
                        String imageurl="";
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            Log.e(TAG, jsonObject.getString("message"));
                            imageurl="http://192.168.244.105/UploadImage/uploads/"+jsonObject.getString("message");
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendImage(imageurl);


                        //
                        //recyclerView.getLayoutManager().scrollToPosition(mAdapter.getItemCount()-1);
                        ////




                    } else {
                        Snackbar.make(findViewById(android.R.id.content),
                                R.string.string_some_thing_wrong, Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    //hideProgress();
                    Log.e(TAG, "Image upload failed!", t);
                    Snackbar.make(findViewById(android.R.id.content),
                            "Image upload failed!", Snackbar.LENGTH_LONG).show();
                }
            });

        } else {
            //hideProgress();
            Toast.makeText(ChatRoomActivity.this,
                    R.string.string_internet_connection_not_available, Toast.LENGTH_SHORT).show();
        }
        //loadingEnd();
    }




    //http call을 통한 채팅방을 불러오기.
    public void fetchChatRooms() {
        StringRequest strReq = new StringRequest(Request.Method.GET,
                EndPoints.CHAT_ROOMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms"); // chat rooms 에 대한 객체를 받아옴.
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

                //mAdapter.notifyDataSetChanged();

                // subscribing to all chat room topics
                // subscribeToAllTopics();
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



    //문자열로 부터 파트 바디를 생성한다//
    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse(FileUtils.MIME_TYPE_TEXT), descriptionString);
    }

    //파일 파트를 준비하는 매서드 (파트이름, 그리고 파일의 Uri)
    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        // use the FileUtils to get the actual file by uri uri를 통해서 실제 파일을 받아온다.
        File file = FileUtils.getFile(this, fileUri);

        // create RequestBody instance from file 리퀘스트바디를 파일로부터 만든다.
        RequestBody requestFile = RequestBody.create (MediaType.parse(FileUtils.MIME_TYPE_IMAGE), file);

        // MultipartBody.Part is used to send also the actual file name //
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }


    //이미지 선택기능을 위한 chooser와 onresult메소드
    private void showChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); //falser로 바꾸면 여러개를 가지고 오는 것을 못하게 될 것으로 생각함.
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_READ_STORAGE); //이거머지?
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_READ_STORAGE) {
                if (resultData != null) {
                    if (resultData.getClipData() != null) {
                        int count = resultData.getClipData().getItemCount();
                        int currentItem = 0;
                        while (currentItem < count) {
                            Uri imageUri = resultData.getClipData().getItemAt(currentItem).getUri();
                            currentItem = currentItem + 1;

                            Log.d("Uri Selected", imageUri.toString());

                            try {
                                arrayList.add(imageUri); //리사이클러뷰는 여기서는 필요없으므로 어댑터와 함께 빼기
//                                MyAdapter mAdapter = new MyAdapter(UploadActivity.this, arrayList, onClickItem); //어댑터를 새로 생성.
//                                listView.setAdapter(mAdapter);
//                                LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//                                listView.setLayoutManager(layoutManager); // 가로 방향으로 리사이클러뷰 재설정.

                            } catch (Exception e) {
                                Log.e(TAG, "File select error", e);
                            }
                        }
                    } else if (resultData.getData() != null) {

                        final Uri uri = resultData.getData();
                        Log.i(TAG, "Uri = " + uri.toString());

                        try {
                            arrayList.add(uri);
                            //여기서 바로 추가작업을 하고.
//                            MyAdapter mAdapter = new MyAdapter(UploadActivity.this, arrayList, onClickItem);
//                            listView.setAdapter(mAdapter);
//                            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
//                            listView.setLayoutManager(layoutManager); // 가로 방향으로 리사이클러뷰 재설정.

                            //임시메시지 추가기능//
                            //userid와 메시지를 클릭시 바로추가하고 (임시), 실제 리사이클러뷰에 대한 처리는 onMessageReceive를 통해 받아왔을 때에 처리가 되어지도록 한다.
                            Message tmpmessage = new Message();
                            tmpmessage.setId("tmp");
                            final String message = inputMessage.getText().toString().trim();
                            tmpmessage.setMessage(uri.toString());
                            //현재 임시 시간 가지고 오기
                            long now = System.currentTimeMillis();
                            // Data 객체에 시간을 저장한다.
                            Date date = new Date(now);
                            // 각자 사용할 포맷을 정하고 문자열로 만든다.
                            SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String createdAt = sdfNow.format(date);
                            tmpmessage.setCreatedAt(createdAt);
                            tmpmessage.setUser(MyApplication.getInstance().getPrefManager().getUser());
                            messageArrayList.add(tmpmessage);
                            mAdapter.notifyDataSetChanged();


                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // 제일 마지막으로 이동하는 처리까지.
                                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                                }
                            }, 200);


                        } catch (Exception e) {
                            Log.e(TAG, "File select error", e);
                        }
                    }
                }
                uploadImagesToServer(); //result작업으로 arralist가 세팅이 완료 되어 졌다면, 이를 파일서버로 보내도록 한다.
            }
        }
    }


    /**
     * fetching the chat rooms by making http call
     */
    //http call을 통한 채팅방을 불러오기.
    private void fetchProduct() {

        //_ID_ 로 되어진 문자열 부분에 대한 값을 chatRoomId에 있는 값으로 대체 한다.
        String endPoint = EndPoints.PRODUCT_DETAIL.replace("_ID_",product_id);

        //실행되어지는 api의 값.
        Log.e(TAG, "endPoint: " + endPoint);

        //요청 문자를 보냄 (get 방식의 endpoint)
        StringRequest strReq = new StringRequest(Request.Method.GET,
                endPoint, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        JSONArray chatRoomsArray = obj.getJSONArray("product");
                        for (int i = 0; i < chatRoomsArray.length(); i++) {
                            JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                            product_id=chatRoomsObj.getString("product_id");

                            //제품의 이름
                            productname=chatRoomsObj.getString("productname");
                            productname=productname.replace("\"","");
                            Log.e(TAG,productname);
                            //뷰에 값을 넣음.
                            //textname.setText(productname);


                            //제품의 가격
                            productprice=chatRoomsObj.getString("price");
                            productprice=productprice.replace("\"","");
                            Log.e(TAG,productprice);
                            productpriceView.setText("거래 가격 : " + productprice +" 원");
                            //뷰에 값을 넣음.
                            //textprice.setText(productprice);

                            //제품의 작성자
                            productwriter=chatRoomsObj.getString("writer");
                            productwriter=productwriter.replace("\"","");
                            Log.e(TAG,productwriter);
                            //뷰에 값을 넣음.
                            //textwriter.setText("작성자 : "+productwriter);

                            //제품의 카테고리
                            categorycreated=chatRoomsObj.getString("category");
                            Log.e(TAG,categorycreated);
                            productcategoryView.setText("제품 분류 : " +categorycreated); //상단 정보에 세팅.

                            //제품 작성자의 고유 id
                            writerid=chatRoomsObj.getString("userid");
                            Log.e(TAG,writerid);
                            if(writerid.equals(MyApplication.getInstance().getPrefManager().getUser().getId())==false){//작성자가 아닐 때에 대한 뷰처리를 해당 if문 안에서 처리합니다.
                                btnStreamingbutton.setVisibility(View.GONE);
                                tradinglocationView.setText("직거래 장소 확인하기");
                            }


                            //제품의 상세 설명란
                            productdescription=chatRoomsObj.getString("description");
                            productdescription=productdescription.replace("\"","");
                            Log.e(TAG,productdescription);
                            //뷰에 값을 넣음.
                            //textdescription.setText(productdescription);

                            imagelist=chatRoomsObj.getString("imagelist");

                        }
                        String[] splitStr = imagelist.split(" ");
                        for(int i=0; i<1; i++){ //일단 첫장만 이미지뷰에 홀딩시키기.
                            image.add("http://192.168.244.105/UploadImage/uploads/"+splitStr[i]);
                            Log.e(TAG, image.get(i));
                            //글라이드로 이미지 뷰에 고정 시킴.
                            Glide.with(ChatRoomActivity.this)
                                    .load(image.get(i))
                                    .centerCrop()
                                    .into(productthumbnailView);
                        }


                    } else {
                        // error in fetching chat rooms
                        Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
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




    @Override
    public void onBackPressed() {
        //super.onBackPressed(); 기존 뒤로가기 버튼을 눌렀을 때에 기능.
        MyApplication.getInstance().getPrefManager().resetChatroom(chatRoomId);
        finish();
    }




    //로딩바 생성하기
    public void loading() {
        //로딩
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(ChatRoomActivity.this);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("잠시만 기다려 주세요");
                        progressDialog.show();
                    }
                }, 0);
    }

    public void loadingEnd() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        if(progressDialog!=null){
                            progressDialog.dismiss();
                        }
                    }
                }, 0);
    }

}