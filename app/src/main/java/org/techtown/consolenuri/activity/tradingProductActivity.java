package org.techtown.consolenuri.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.consolenuri.R;
import org.techtown.consolenuri.adapter.ChatRoomsAdapter;
import org.techtown.consolenuri.adapter.TradingProductsAdapter;
import org.techtown.consolenuri.app.Config;
import org.techtown.consolenuri.app.EndPoints;
import org.techtown.consolenuri.app.MyApplication;
import org.techtown.consolenuri.helper.SimpleDividerItemDecoration;
import org.techtown.consolenuri.model.Message;
import org.techtown.consolenuri.model.TradingProduct;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//거래가 시작된 제품의 어레이 리스트를 생성한다.
public class tradingProductActivity extends AppCompatActivity {
    private String TAG = tradingProductActivity.class.getSimpleName();

    //private final int MY_PERMISSION_REQUEST_SMS = 1001; //문자메시지 퍼미션.

    private String productname,createdDate,productThumbnail,productPrice,imagelist;

    boolean IamSeller = true;

    Button trsellView,trbuyView; //제품의 판매중거래와 구매중거래를 따로 구분하여 표시하기 위한 역할.

    private BroadcastReceiver mRegistrationBroadcastReceiver; //리시버 생성.

    //swipelayout
    private SwipeRefreshLayout mSwipeRefreshLayout;


    //이건 사용자가 쓴 제품들에 대한 리사이클러뷰를 따로 구분하기 위함.
    private ArrayList<TradingProduct> tradingproductArrayList;
    private TradingProductsAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trading_product_activity);

        //인텐트로 거래자의 값을 확인한다.
        Intent intent = getIntent();
        IamSeller = intent.getBooleanExtra("IamSeller",true); //해당 제품이 등록상태일 때에 대한 처리를 해줍니다.

        //ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.SEND_SMS}, MY_PERMISSION_REQUEST_SMS);

        //제품리프레시
        //swipe view를 만듬 탭으로 당길때마다 리프레쉬를 한다.
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperView);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e(TAG,"REFRESH동작");
                tradingproductArrayList.clear();
                if(IamSeller==true){
                    selltradingProductlist();
                }
                else {
                    buytradingProductlist();
                }
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        recyclerView = (RecyclerView) findViewById(R.id.tradingproductrecyclerview);
        tradingproductArrayList = new ArrayList<>();
        //mAdapter = new TradingProductsAdapter(this, tradingproductArrayList)

        //뷰연결 작업
        trsellView=(Button)findViewById(R.id.trsellView);
        trbuyView=(Button)findViewById(R.id.trbuyView);


        //버튼 클릭 리스너 연결 판매중거래
        trsellView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selltradingProductlist();
                Toast.makeText(getApplicationContext(), "현재 판매 진행중인 제품을 보여줍니다.", Toast.LENGTH_SHORT).show();
            }
        });

        //버튼 클릭 리스너 연결 구매중거래
        trbuyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buytradingProductlist();
                Toast.makeText(getApplicationContext(), "현재 구매 진행중인 제품을 보여줍니다.", Toast.LENGTH_SHORT).show();
            }
        });



        recyclerView = (RecyclerView) findViewById(R.id.tradingproductrecyclerview);
        tradingproductArrayList = new ArrayList<>();
        mAdapter = new TradingProductsAdapter(this, tradingproductArrayList); //어댑터 연결.
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        //구매중인 제품 정보를 가지고 온다.
        if(IamSeller==true){
            selltradingProductlist();
        }
        else{
            buytradingProductlist();
        }

        recyclerView.addOnItemTouchListener(new ChatRoomsAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new ChatRoomsAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                TradingProduct tradingProduct = tradingproductArrayList.get(position);

                if(tradingProduct.getSell_user_id().equals(MyApplication.getInstance().getPrefManager().getUser().getId())){
                    // 유저아이디와 셀러아이디가 같다면(판매자라면), 커스텀 다이얼로그를 생성한다.
                    AlertDialog.Builder builder = new AlertDialog.Builder(tradingProductActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View v = inflater.inflate(R.layout.custom_dialog_tradingproduct, null);
                    builder.setView(v);
                    final Button submit = (Button) v.findViewById(R.id.buttonSubmit);
                    final TextView productprogressview = (TextView)v.findViewById(R.id.tradingproductprogress);
                    productprogressview.setText(tradingProduct.getProductprgress());

                    // 제품 세팅 처리하기
                    //productprogress의 경우는 받아온 값에 따라 다른 색상의 텍스트를 처리.
                    String productprogress = tradingProduct.getProductprgress();
                    switch (productprogress) {
                        case "0": productprogressview.setText("[결제 완료]");
                            //updatetradingProgress("1", tradingProduct.getTradingproduct_id());
                            submit.setText("[결제 확인 처리하기]");
                            break;
                        case "1": productprogressview.setText("[결제 확인]");
                            submit.setText("[배송 준비 처리하기]");
                            break;
                        case "2": productprogressview.setText("[배송 준비중]");
                            submit.setText("[배송 처리하기]");
                            break;
                        case "3": productprogressview.setText("[배송 중]");
                            submit.setText("[거래 완료하기]");
                            submit.setVisibility(View.GONE);
                            break;
                        case "4": productprogressview.setText("[구매 확정]");
                            submit.setVisibility(View.GONE);
                            break;
                    }


                    String[] splitStr = tradingProduct.getShippingInfo().split("\\+");
                    final TextView namecontactView = (TextView) v.findViewById(R.id.namecontactView);
                    namecontactView.setText(splitStr[0]);
                    final TextView postalAddressView = (TextView) v.findViewById(R.id.postalAddressView);
                    postalAddressView.setText(splitStr[1]);
                    final TextView AddressView = (TextView) v.findViewById(R.id.AddressView);
                    AddressView.setText(splitStr[2]);


                    final AlertDialog dialog = builder.create();
                    submit.setOnClickListener(new View.OnClickListener() { //제품 클릭리스너를 하였을때의처리
                        public void onClick(View v) {
                            String productprogress = tradingProduct.getProductprgress();
                            switch (productprogress) {
                                case "0":
                                    updatetradingProgress("1", tradingProduct.getTradingproduct_id());
                                    tradingproductArrayList.get(position).setProductprgress("1");
                                    mAdapter.notifyDataSetChanged();
                                    AlertToBuyer(tradingProduct.getBuy_user_id(),1);
                                    break;
                                case "1":
                                    updatetradingProgress("2", tradingProduct.getTradingproduct_id());
                                    tradingproductArrayList.get(position).setProductprgress("2");
                                    mAdapter.notifyDataSetChanged();
                                    AlertToBuyer(tradingProduct.getBuy_user_id(),2);
                                    break;
                                case "2":
                                    updatetradingProgress("3", tradingProduct.getTradingproduct_id());
                                    tradingproductArrayList.get(position).setProductprgress("3");
                                    mAdapter.notifyDataSetChanged();
                                    AlertToBuyer(tradingProduct.getBuy_user_id(),3);
                                    break;
                                case "3":
                                    updatetradingProgress("4", tradingProduct.getTradingproduct_id());
                                    tradingproductArrayList.get(position).setProductprgress("4");
                                    mAdapter.notifyDataSetChanged();
                                    break;
                                case "4":
                                    break;
                            }

                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }

                else{
                    //현재 제품 프로그래스 상태가 "배송 중" 상태라면...
                    if(tradingProduct.getProductprgress().equals("3") || tradingProduct.getProductprgress().equals("4")){

                        // 유저아이디와 셀러아이디가 다르다면(구매자라면), 커스텀 다이얼로그를 생성한다.
                        AlertDialog.Builder builder = new AlertDialog.Builder(tradingProductActivity.this);
                        LayoutInflater inflater = getLayoutInflater();
                        View v = inflater.inflate(R.layout.custom_dialog_tradingproduct, null);
                        builder.setView(v);
                        final Button submit = (Button) v.findViewById(R.id.buttonSubmit);
                        final TextView productprogressview = (TextView)v.findViewById(R.id.tradingproductprogress);
                        productprogressview.setText(tradingProduct.getProductprgress());

                        // 제품 세팅 처리하기
                        //productprogress의 경우는 받아온 값에 따라 다른 색상의 텍스트를 처리.
                        String productprogress = tradingProduct.getProductprgress();
                        switch (productprogress) {
                            case "3": productprogressview.setText("[배송 중]");
                                submit.setText("[구매 확정하기]");
                                break;
                            case "4": productprogressview.setText("[구매 확정]");
                                submit.setVisibility(View.GONE);
                                break;
                        }

                        String[] splitStr = tradingProduct.getShippingInfo().split("\\+");
                        final TextView namecontactView = (TextView) v.findViewById(R.id.namecontactView);
                        namecontactView.setText(splitStr[0]);
                        final TextView postalAddressView = (TextView) v.findViewById(R.id.postalAddressView);
                        postalAddressView.setText(splitStr[1]);
                        final TextView AddressView = (TextView) v.findViewById(R.id.AddressView);
                        AddressView.setText(splitStr[2]);

                        final AlertDialog dialog = builder.create();
                        submit.setOnClickListener(new View.OnClickListener() { //제품 클릭리스너를 하였을때의처리
                            public void onClick(View v) {
                                String productprogress = tradingProduct.getProductprgress();
                                switch (productprogress) {
                                    case "3":
                                        updatetradingProgress("4", tradingProduct.getTradingproduct_id());
                                        tradingproductArrayList.get(position).setProductprgress("4");
                                        AlertToSeller(tradingProduct.getSell_user_id(),4);
                                        mAdapter.notifyDataSetChanged();
                                        break;
                                    case "4":
                                        break;
                                }

                                dialog.dismiss();
                            }
                        });
                        dialog.show();

                    }
                }


            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

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
                    // new push notification is received
                    // 새로운 push 가 왔을 때.
                    handlePushNotification(intent); //메시지가 왔을때에 대한 처리.
                }
            }
        };
    }



    /**
     * fetching the chat rooms by making http call
     */
    //판매거래중 제품리스트
    private void selltradingProductlist() {
        getSupportActionBar().setTitle("판매진행중"); //엑티비티 제목 변경
        tradingproductArrayList.clear();
        //loading();
        //productArrayList=new ArrayList<>(); //패치하기전에 리사이클러뷰를 초기화 시킵니다.
        StringRequest strReq = new StringRequest(Request.Method.POST,
                EndPoints.SELL_TRADINGPROCUT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //productArrayList=new ArrayList<>();
                Log.e(TAG, "response: " + response);
                mSwipeRefreshLayout.setRefreshing(false);
                try {

                    //productArrayList=new ArrayList<>();
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        JSONArray chatRoomsArray = obj.getJSONArray("tradingproducts");
                        for (int i = 0; i < chatRoomsArray.length(); i++) {
                            TradingProduct pr = new TradingProduct(); //새로운 trading product생성하여 이 객체를 arraylist에 저장시킨다.
                            JSONObject TradingProductObj = (JSONObject) chatRoomsArray.get(i);
                            //TradingProduct pr = new TradingProduct(); //새로운 trading product생성.
                            pr.setProduct_id(TradingProductObj.getString("product_id")); //제품 id정보는 담고있어야 한다.
                            //pr.setCreated_at(TradingProductObj.getString("created_at")); //거래 등록 일자 표시.

                            SimpleDateFormat format = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss"); //날짜 표시 포멧
                            SimpleDateFormat format2 = new SimpleDateFormat("yyyy년 MM월 dd일");
                            Date date = format.parse(TradingProductObj.getString("created_at"));
                            String created_at = format2.format(date);
                            pr.setCreated_at(created_at);

                            //Log.e(TAG,"받아온 시각을 세팅" + pr.getCreated_at());
                            pr.setProductprgress(TradingProductObj.getString("productprogress")); //제품 주문 상태.
                            //Log.i(TAG,"현재 받아온 제품의 제품 아이디 정보는 : " + TradingProductObj.getString("product_id"));
                            String imagelist=TradingProductObj.getString("imagelist");
                            String[] splitStr = imagelist.split(" ");
                            productThumbnail="http://192.168.244.105/UploadImage/uploads/"+splitStr[0];//해당 제품의 첫번째 썸네일 값을 넣는다.
                            pr.setThumbnail(productThumbnail); //productthumbnail세팅.
                            pr.setProductname(TradingProductObj.getString("productname").replace("\"","")); //제품 이름 세팅.
                            pr.setPrice(TradingProductObj.getString("product_price").replace("\"","")); //제품 가격 세팅.
                            pr.setTradingproduct_id(TradingProductObj.getString("tradingproduct_id")); //거래중인 제품 고유번호 세팅.
                            pr.setSell_user_id(TradingProductObj.getString("sell_user_id")); //판매중인 유저의 정보.
                            pr.setBuy_user_id(TradingProductObj.getString("buy_user_id")); //구매중인 유저의 정보.
                            pr.setShippingInfo(TradingProductObj.getString("shippingInfo"));//주소지 정보.

                            tradingproductArrayList.add(pr); //최종적으로 어레이 리스트에 들어 갈 값을 추가.
                        }


                    } else {
                        // Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //어탭터에 새롭게 적용 완료....
                //mAdapter.notifyDataSetChanged();
                //loadingEnd();
                //mSwipeRefreshLayout.setRefreshing(false); //리프래싱 기능 추가 예에정.
                mAdapter.notifyDataSetChanged(); //제품이 변경 되어짐.
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
                params.put("user_id",MyApplication.getInstance().getPrefManager().getUser().getId()); //파라 미터 값을 넣는 처리.

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
    //판매거래중 제품리스트
    private void buytradingProductlist() {
        getSupportActionBar().setTitle("구매진행중"); //엑티비티 제목 변경
        IamSeller=false; // 해당 메서드 호출시에,
        tradingproductArrayList.clear();
        //loading();
        //productArrayList=new ArrayList<>(); //패치하기전에 리사이클러뷰를 초기화 시킵니다.
        StringRequest strReq = new StringRequest(Request.Method.POST,
                EndPoints.BUY_TRADINGPROCUT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //productArrayList=new ArrayList<>();
                Log.e(TAG, "response: " + response);
                mSwipeRefreshLayout.setRefreshing(false);
                try {

                    //productArrayList=new ArrayList<>();
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        JSONArray chatRoomsArray = obj.getJSONArray("tradingproducts");
                        for (int i = 0; i < chatRoomsArray.length(); i++) {
                            TradingProduct pr = new TradingProduct(); //새로운 trading product생성하여 이 객체를 arraylist에 저장시킨다.
                            JSONObject TradingProductObj = (JSONObject) chatRoomsArray.get(i);
                            //TradingProduct pr = new TradingProduct(); //새로운 trading product생성.
                            pr.setProduct_id(TradingProductObj.getString("product_id")); //제품 id정보는 담고있어야 한다.
                            //pr.setCreated_at(TradingProductObj.getString("created_at")); //거래 등록 일자 표시.

                            SimpleDateFormat format = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss"); //날짜 표시 포멧
                            SimpleDateFormat format2 = new SimpleDateFormat("yyyy년 MM월 dd일");
                            Date date = format.parse(TradingProductObj.getString("created_at"));
                            String created_at = format2.format(date);
                            pr.setCreated_at(created_at);

                            //Log.e(TAG,"받아온 시각을 세팅" + pr.getCreated_at());
                            pr.setProductprgress(TradingProductObj.getString("productprogress")); //제품 주문 상태.
                            //Log.i(TAG,"현재 받아온 제품의 제품 아이디 정보는 : " + TradingProductObj.getString("product_id"));
                            String imagelist=TradingProductObj.getString("imagelist");
                            String[] splitStr = imagelist.split(" ");
                            productThumbnail="http://192.168.244.105/UploadImage/uploads/"+splitStr[0];//해당 제품의 첫번째 썸네일 값을 넣는다.
                            pr.setThumbnail(productThumbnail); //productthumbnail세팅.
                            pr.setProductname(TradingProductObj.getString("productname").replace("\"","")); //제품 이름 세팅.
                            pr.setPrice(TradingProductObj.getString("product_price").replace("\"","")); //제품 가격 세팅.
                            pr.setTradingproduct_id(TradingProductObj.getString("tradingproduct_id")); //거래중인 제품 고유번호 세팅.
                            pr.setSell_user_id(TradingProductObj.getString("sell_user_id")); //판매중인 유저의 정보.
                            pr.setBuy_user_id(TradingProductObj.getString("buy_user_id")); //구매중인 유저의 정보.
                            pr.setShippingInfo(TradingProductObj.getString("shippingInfo"));//주소지 정보.


                            tradingproductArrayList.add(pr); //최종적으로 어레이 리스트에 들어 갈 값을 추가.
                        }


                    } else {
                        // Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //어탭터에 새롭게 적용 완료....
                //mAdapter.notifyDataSetChanged();
                //loadingEnd();
                //mSwipeRefreshLayout.setRefreshing(false); //리프래싱 기능 추가 예에정.
                mAdapter.notifyDataSetChanged(); //제품이 변경 되어짐.
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
                params.put("user_id",MyApplication.getInstance().getPrefManager().getUser().getId()); //파라 미터 값을 넣는 처리.

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
    //판매거래중 제품리스트
    private void updatetradingProgress(String progress, String tradingproduct_id) {

        //loading();
        //productArrayList=new ArrayList<>(); //패치하기전에 리사이클러뷰를 초기화 시킵니다.
        StringRequest strReq = new StringRequest(Request.Method.POST,
                EndPoints.UPDATE_TRADINGPROGRESS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //productArrayList=new ArrayList<>();
//                Log.e(TAG, "response: " + response);
//                if(IamSeller==true){
//                    selltradingProductlist(); // 패치후 해당 값을 다시 받아온다.
//                }
//                else{
//                    buytradingProductlist(); // 패치 후 해당 값을 다시 받아온다.
//                }

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
                params.put("progress",progress); //파라 미터 값을 넣는 처리.
                params.put("tradingproduct_id",tradingproduct_id);
                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }


    /**
     * 제품 업데이트 시에 메시지를 보내도록 함.
     */
    private void AlertToBuyer(String buy_user_id,int progress) {

        //edittextview에 입력된 값을 변수에 담음.
        final String to_user_id = buy_user_id;
        final String from_user_id = MyApplication.getInstance().getPrefManager().getUser().getId();
        String message = "";

        if(progress==1){
            message="제품의 결제확인이 완료되었습니다.";
        }
        else if(progress==2){
            message="제품이 배송 준비중 입니다.";
        }
        else if(progress==3){
            message="제품이 배송 중 입니다.";
        }

        Log.e(TAG,to_user_id+ "     " + from_user_id);

        //요청 strReq를 만듬. 엔드포인트를 등록.
        String finalMessage = message;
        StringRequest strReq = new StringRequest(Request.Method.POST,
                EndPoints.ALLERT_TO_WRITER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        Log.e(TAG,"채팅 알람 메시지를 보냈습니다.");

                    } else {
                        // login error - simply toast the message
                        //Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                    //json으로 바꿔 오는 과정에서 발생한 에러.
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

            //post처리를 할 때 반드시 필요한 작업.
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>(); //파라미터 값을 넣는 HashMap
                params.put("to_user_id", to_user_id); //보내는 대상
                params.put("from_user_id", from_user_id);//보내는 사람(자기 자신)
                params.put("message", finalMessage); //거래 메세지

                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };
        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }


    /**
     * 제품 업데이트 시에 메시지를 보내도록 함.
     */
    private void AlertToSeller(String sell_user_id, int progress) {

        //edittextview에 입력된 값을 변수에 담음.
        final String to_user_id = sell_user_id;
        final String from_user_id = MyApplication.getInstance().getPrefManager().getUser().getId();
        String message = " 제품구매 확정 처리를 하였습니다.";

        Log.e(TAG,to_user_id+ "     " + from_user_id);

        //요청 strReq를 만듬. 엔드포인트를 등록.
        String finalMessage = message;


        Log.e(TAG,to_user_id+ "     " + from_user_id);

        //요청 strReq를 만듬. 엔드포인트를 등록.
        StringRequest strReq = new StringRequest(Request.Method.POST,
                EndPoints.ALLERT_TO_WRITER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        Log.e(TAG,"채팅 알람 메시지를 보냈습니다.");

                    } else {
                        // login error - simply toast the message
                        //Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                    //json으로 바꿔 오는 과정에서 발생한 에러.
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

            //post처리를 할 때 반드시 필요한 작업.
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>(); //파라미터 값을 넣는 HashMap
                params.put("to_user_id", to_user_id); //보내는 대상
                params.put("from_user_id", from_user_id);//보내는 사람(자기 자신)
                params.put("message",message); //거래 메세지

                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };
        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }


    //메시지 알림처리를 받기 위함.
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
            //Toast.makeText(getApplicationContext(), message.getUser().getName()+ "" + message.getMessage(), Toast.LENGTH_LONG).show();
            //해당 리시버를 통해서 받아왔을때 패치 처리를합니다.
            if(IamSeller==true){
                selltradingProductlist(); // 패치후 해당 값을 다시 받아온다.
            }
            else{
                buytradingProductlist(); // 패치 후 해당 값을 다시 받아온다.
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");

        //리시버 등록.
        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION)); //이 부분을 클래스화 시키면 되지 않을까??
    }

    @Override
    protected void onPause() {

        //리시버 해제.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver); //중지해당 브로드 케스트리시버를 unregister한다.
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //뒤로가기 버튼이 눌렸을 때 누적되어진 엑티비티를 종료시키고, productlistactivity로 간다.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver); //중지해당 브로드 케스트리시버를 unregister한다.
        Intent intent = new Intent(tradingProductActivity.this,productListActivity.class); //이동
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 누적된 모든 엑티비티를 처리하고
        startActivity(intent); //액티비티를 실행.

    }



}
