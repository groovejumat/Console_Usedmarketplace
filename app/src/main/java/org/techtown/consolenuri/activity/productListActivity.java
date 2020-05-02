package org.techtown.consolenuri.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.consolenuri.FileUpload.UploadActivity;
import org.techtown.consolenuri.R;
import org.techtown.consolenuri.adapter.ChatRoomsAdapter;
import org.techtown.consolenuri.adapter.ProductsAdapter;
import org.techtown.consolenuri.app.Config;
import org.techtown.consolenuri.app.EndPoints;
import org.techtown.consolenuri.app.MyApplication;
import org.techtown.consolenuri.gcm.GcmIntentService;
import org.techtown.consolenuri.helper.SimpleDividerItemDecoration;
import org.techtown.consolenuri.model.Message;
import org.techtown.consolenuri.model.Product;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class productListActivity extends AppCompatActivity {
    private String TAG = productListActivity.class.getSimpleName();

    //Bottom 네비게이션 뷰 생성 이를 통해서 글작성기능을 추가할 예정. Onresult가 필요할 것 같음. 아니 없어도 됌. 일단은..
    BottomNavigationView bottomNavigationView;

    //제품 리사이클러뷰를 만들기 위한 재료들 추가.
    private ArrayList<Product> productArrayList;

    //swipelayout
    private SwipeRefreshLayout mSwipeRefreshLayout;

    //이건 사용자가 쓴 제품들에 대한 리사이클러뷰를 따로 구분하기 위함.
    private ArrayList<Product> MyproductArrayList;
    private ProductsAdapter mAdapter;
    private RecyclerView recyclerView;

    //프로그래스 다이얼로그
    private ProgressDialog progressDialog; //어느페이지에서든 쓸 수 있도록 프로그레스바를 준비.

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);


        MyApplication.getInstance().getPrefManager();

        /**
         * Check for login session. If not logged in launch
         * login activity
         * */
        //로그인 세션을 체크하고, 세션이 null(없음)상태라면 로그인 액티비티로 다시 간다?
        if (MyApplication.getInstance().getPrefManager().getUser() == null) {
            launchLoginActivity(); //로그인 액티비티로 가는 메소드.
        }

        //swipe view를 만듬 탭으로 당길때마다 리프레쉬를 한다.
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperView);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e(TAG,"REFRESH동작");
                productArrayList.clear();
                fetchProductlist();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        getSupportActionBar().setTitle("판매리스트");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bottomNavigationView = findViewById(R.id.bottomnavigationView);

        recyclerView = (RecyclerView) findViewById(R.id.productrecyclerview);
        productArrayList = new ArrayList<>();
        mAdapter = new ProductsAdapter(this, productArrayList);
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
                Product product = productArrayList.get(position);

                // 해당 제품을 클릭시에 상세정보 페이지로 넘어갈 수 있도록 합니다.
                Log.e(TAG,"Product id call : " + product.getProduct_id());
                Intent intent = new Intent(productListActivity.this, ProductActivity.class);
                // 여기서 해당 제품이 내가쓴글인지 내가 쓴글이 아닌지에 따라서 다른 처리를 할 수 있도록 함.
                Log.e(TAG,MyApplication.getInstance().getPrefManager().getUser().getName()+ "와"+ product.getWriter());
                if(MyApplication.getInstance().getPrefManager().getUser().getName().equals(product.getWriter())==false){
                    intent.putExtra("not_writer",true);
                } //유저가 같은 유저가 아니라면 not_writer에 true값을 넣어 보낸다.

                intent.putExtra("product_id", product.getProduct_id());
                intent.putExtra("categorycreated", product.getCategory()+"   "+product.getCreated_at());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        //fetchProductlist();
        /**
         * Broadcast receiver calls in two scenarios
         * 1. gcm registration is completed
         * 2. when new push notification is received
         * */
        //메시지를 받아오는 리시버 생성//
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG,"브로드캐스트 리시버가 인텐트를 받음. 인텐트 키값에 따라 다양한 처리를 한다.");

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
                        overridePendingTransition(0, 0); //페이지 넘어가는 효과 없애기
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
                        overridePendingTransition(0, 0); //페이지 넘어가는 효과 없애기
                        finish();

                        return true;
                    }

                    case R.id.action_sellchat: {
                        Log.e(TAG,"바텀 채팅실행 버튼을 클릭했음.");
                        //채팅 실행 인텐트 작성 (판매자 전용)//
                        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("seller","seller");
                        startActivity(intent);
                        overridePendingTransition(0, 0); //페이지 넘어가는 효과 없애기
                        finish();

                        return true;
                    }

                    case R.id.action_trading: {
                        Log.e(TAG,"거래 중 제품에 대한 엑티비티로 이동.");
                        Intent intent=new Intent(getApplicationContext(), tradingProductActivity.class);
                        startActivity(intent);

                        return true;
                    }


                    default:
                        return false;
                }
            }
        });
        registerGCM(); //fcm 등록 실행한다. 이제 global topic에 대해서 메시지를 받는다.
    }


    /**
     * fetching the chat rooms by making http call
     */
    //http call을 통한 채팅방을 불러오기.
    private void fetchProductlist() {
        loading();
        //productArrayList=new ArrayList<>(); //패치하기전에 리사이클러뷰를 초기화 시킵니다.
        StringRequest strReq = new StringRequest(Request.Method.GET,
                EndPoints.PRODUCT_ALL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //productArrayList=new ArrayList<>();
                Log.e(TAG, "response: " + response);

                try {

                    //productArrayList=new ArrayList<>();
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        JSONArray chatRoomsArray = obj.getJSONArray("products");
                        for (int i = 0; i < chatRoomsArray.length(); i++) {
                            JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                            Product pr = new Product();
                            pr.setProduct_id(chatRoomsObj.getString("product_id"));

                            //제품 이름 세팅
                            String productname = chatRoomsObj.getString("productname").replace("\"","");
                            pr.setProductname(productname);

                            //가격 정보 세팅
                            String price = chatRoomsObj.getString("price").replace("\"","");
                            pr.setPrice(price+"원");

                            //카테고리 값 세팅
                            pr.setCategory(chatRoomsObj.getString("category"));

                            //글의 생성시각 세팅
                            String created_at =chatRoomsObj.getString("created_at");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = sdf.parse(created_at);
                            Log.e(TAG, date.getTime()+"");//현재 불러온 타이머의 값을 misec로 표현한 값입니다.
                            created_at = formatTimeString(date.getTime());
                            Log.e(TAG, created_at); // 몇일전// 몇분전// 몇시간전 등의 작성이 되었는지에 대해서 나타내는 값.
                            pr.setCreated_at(created_at); // 생성시간 세팅

                            //제품 제작자 이름 세팅
                            pr.setWriter(chatRoomsObj.getString("writer"));


                            //제품 메인 이미지 세팅
                            String thumbnail = chatRoomsObj.getString("imagelist");

                            String[] splitStr = thumbnail.split(" ");

                            thumbnail = "http://192.168.244.105/UploadImage/uploads/"+splitStr[0];

                            pr.setThumbnail(thumbnail);

                            Log.e(TAG,"섬네일 값 : "+thumbnail);


                            //이부분에서 자신인지 아닌지에 따라 분기를 나누어서 담는 처리를 진행하도록 한다.
                            productArrayList.add(pr);
                        }

                    } else {
                        // error in fetching chat rooms
                        Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //어탭터에 새롭게 적용 완료....
                mAdapter.notifyDataSetChanged();
                loadingEnd();
                mSwipeRefreshLayout.setRefreshing(false);

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
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");

        productArrayList.clear();//기존 생성되어진 어레이 리스트 값을 지우고 패치시킨다.
        fetchProductlist(); //이부분이 데이터를 처리하는거. 인데 이부분만 넣게 되는 경우에는 그대로 데이터 값을 넣어 버리게 된다. arrayList내용을 지우는 법이 따로있는건가?

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION)); //이 부분을 클래스화 시키면 되지 않을까??
    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver); //중지해당 브로드 케스트리시버를 unregister한다.
        super.onPause();
    }



    //빌려온 로직, 시간 값을 가지고 오면 n분전 n초전으로 만들어 주는 함수입니다.//
    /** 몇분전, 방금 전, */
    private static class TIME_MAXIMUM{
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }

    public static String formatTimeString(long regTime) {
        long curTime = System.currentTimeMillis();
        long diffTime = (curTime - regTime) / 1000;
        Log.i("현재시간값은 : " ,diffTime+"");
        String msg = null;
        if (diffTime < TIME_MAXIMUM.SEC) {
            msg = "방금 전";
        } else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {
            msg = diffTime + "분 전";
        } else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {
            msg = (diffTime) + "시간 전";
        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
            msg = (diffTime) + "일 전";
        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
            msg = (diffTime) + "달 전";
        } else {
            msg = (diffTime) + "년 전";
        }
        return msg;
    }


    //로딩바 생성하기
    public void loading() {
        //로딩
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(productListActivity.this);
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
                        progressDialog.dismiss();
                    }
                }, 0);
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
            Log.e(TAG, "브로드캐스트리시버를 통해서, 받아온 메시지를 다룬다 : "+ chatRoomId + " 시리얼화 되어진 메시지 객체 : " + message);

            //화면에서는 "row update"를 하지만, 외부 다른 액티비티에서는 알람을 받을 수 있게끔 실행한다.
            if (message != null && chatRoomId != null) {
                //Toast.makeText(getApplicationContext(), message.getUser().getName() + " : " + message.getMessage(), Toast.LENGTH_LONG).show();
                //updateRow(chatRoomId, message); //셰어드 프리퍼런스를 가지고 왔을 때에 해당 처리를 한다면 ?
                //MyApplication.getInstance().getPrefManager().updaterowOtherActivity(chatRoomId,message.getMessage());
            }
        } else if (type == Config.PUSH_TYPE_USER) {
            // push belongs to user alone 메시지가 한명한테 가는 것이라면?
            // just showing the message in a toast 토스트로 메세지를 알려준다.
            Log.e(TAG,"해당 메시지는 개인에게 보내는 메시지 입니다.");
            Message message = (Message) intent.getSerializableExtra("message");
            Toast.makeText(getApplicationContext(), message.getUser().getName()+ "" + message.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    // subscribing to global topic 모든 채팅방에 대한 청취
    private void subscribeToGlobalTopic() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE); //현재 채팅으로 거래를 시작하는 사람에 대해서 구동 실행하기.
        intent.putExtra(GcmIntentService.TOPIC, Config.TOPIC_GLOBAL);
        startService(intent);
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


    private void launchLoginActivity() {
        Intent intent = new Intent(productListActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}
