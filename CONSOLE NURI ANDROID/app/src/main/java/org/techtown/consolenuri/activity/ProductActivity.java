package org.techtown.consolenuri.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.consolenuri.FileUpload.ChangeActivity;
import org.techtown.consolenuri.R;
import org.techtown.consolenuri.app.EndPoints;
import org.techtown.consolenuri.app.MyApplication;
import org.techtown.consolenuri.gcm.GcmIntentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kr.co.bootpay.BootpayAnalytics;
import me.relex.circleindicator.CircleIndicator;

//해당 페이지는 클릭한 제품에 대한 정보를 나타내주는 페이지 입니다.
public class ProductActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    String product_id,productname,productprice,productwriter,imagelist,productdescription,username,userid,writerid,
    categorycreated,productthumbnail,productcategory;

    boolean writer=true;

    private int stuck = 10; //구매이용시, 재고를 체크하는 함수.


    //제품의 텍스트뷰 등록을 진행합니다.
    TextView textname,textprice,textwriter,textdescription,textcategorycreated;

    //Menu 값을 탄력적으로 변경하기 위해서 Menu값을 변경하도록 합니다.
    Button buttonstartchat;

    //거래와 관련된 버튼을 정합니다.
    Button paymentButton,shoppingCartButton;

    //제품정보에 대한 이미지 리스트를 나타낸다.
    ArrayList<String> image = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_page);

        //상품페이지 이름 연결
        getSupportActionBar().setTitle("상품정보");


        //뷰를 연결합니다.
        textname=(TextView)findViewById(R.id.productnameView);
        textprice=(TextView)findViewById(R.id.productpriceView);
        textwriter=(TextView)findViewById(R.id.productwriterView);
        textdescription=(TextView)findViewById(R.id.productdescriptionView);
        textcategorycreated=(TextView)findViewById(R.id.categorycreatedView);

        //결제관련 버튼 뷰 연결.
        paymentButton=(Button)findViewById(R.id.paymentView);
        shoppingCartButton=(Button)findViewById(R.id.shoppingcargView);

        buttonstartchat=(Button)findViewById(R.id.startchatView);
        ////////////////////////////////////////////////////////////////////

        //productlistpage에서 받아온 id값을 product_id에 담습니다.
        Intent intent = getIntent();
        product_id = intent.getStringExtra("product_id");
        categorycreated = intent.getStringExtra("categorycreated");
        username = MyApplication.getInstance().getPrefManager().getUser().getName(); //user이름의 값을 참조한다.
        userid = MyApplication.getInstance().getPrefManager().getUser().getId(); //user의 고유 id값을 가지고 온다.
        Log.e(TAG,"현재 접속한 유저의 name과 id 값은 : " + username + "," +userid);

        if(intent.getBooleanExtra("not_writer",false)==true){
            Log.e(TAG,"[당신은 작성자가 아닙니다.]");
            writer=false; // writer가 false경우 상단메뉴바, 그리고 다른 속성들을 보이지 않게 합니다.

        }
        else{
            buttonstartchat.setVisibility(View.GONE); //작성자인 경우에는 채팅 시작 버튼을 사라지도록 함.
            paymentButton.setVisibility(View.GONE); //작성자인 경우에는 바로결제 버튼을 없어지도록.
            shoppingCartButton.setVisibility(View.GONE); //작성자인 경우에는 제품추가 버튼이 없어지도록 합니다.
        }

        Log.e(TAG,"현재 넘긴 아이템의 값은 " + product_id + " 입니다.");
        Log.e(TAG,"현재 가지고 온 카테고리정보와 값은 " + categorycreated + " 입니다.");
        textcategorycreated.setText(categorycreated);

        fetchProduct(); //제품 불러오기 밑 값 저장하기.

        ViewPager viewPager = findViewById(R.id.viewPager);
        //viewPager.setClipToPadding(false);

        //float density = getResources().getDisplayMetrics().density;
        //int margin = (int) (DP * density);
        //viewPager.setPadding(margin, 0, margin, 0);
        //viewPager.setPageMargin(margin/2);

        viewPager.setAdapter(new ViewPagerAdapter(this, image));
        viewPager.getAdapter().notifyDataSetChanged();


        //해당 버튼을 눌렀을 때에, 채팅 방이 생성되어지도록 하기.
        buttonstartchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //해당 버튼을 누르면 채팅방으로 이동하여 거래가 시작된다.
                Log.e(TAG,"등록 제품의 고유번호 : " + product_id + " 현재 거래를 시작한 USER의 이름 : " + username);
                //채팅룸을 생성하는 VOLLY REQUEST실행하기
                createroom(); //이 함수 내에서 채팅방으로 이동 시키기.
                AlertToWriter(); //채팅 메시지 보내기.
                //제작자에게 알림을 하기 위한 함수. alerttowriter();
            }
        });

        //결제버튼을 눌렀을때 값을 전달하여 해당 제품에 대한결제가 진행 되어지도록 하기.
        //결제 모듈 부트페이를 초기화
        // 초기설정 - 해당 프로젝트(안드로이드)의 application id 값을 설정합니다. 결제와 통계를 위해 꼭 필요합니다.
        BootpayAnalytics.init(this, "5e6ec35b02f57e002e4b4edd");
        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onClick_request(); // 결제 모듈 실행 함수.
                Intent intent = new Intent(ProductActivity.this,PaymentActivity.class); //해당 결제 진행 액티비티로 넘어가지도록 합니다.
                Log.e(TAG,product_id+"해당 값을 넘깁니다.");
                intent.putExtra("product_id",product_id); //제품의 등록아이디
                intent.putExtra("product_name",productname); //제품의 이름
                intent.putExtra("product_price",productprice); //제품의 가격
                intent.putExtra("writerid",writerid); //제품 작성자의 고유아이디
                intent.putExtra("product_thumbnail",productthumbnail);//제품 썸네일 url정보
                intent.putExtra("product_category",productcategory);//제품의 카테고리 정보

                startActivity(intent); //값을 담은 intent 실행
            }
        });
        checkUserTrading();
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
                            textname.setText(productname);


                            //제품의 가격
                            productprice=chatRoomsObj.getString("price");
                            productprice=productprice.replace("\"","");
                            Log.e(TAG,productprice);
                            //뷰에 값을 넣음.
                            textprice.setText(productprice);

                            //제품의 작성자
                            productwriter=chatRoomsObj.getString("writer");
                            productwriter=productwriter.replace("\"","");
                            Log.e(TAG,productwriter);
                            //뷰에 값을 넣음.
                            textwriter.setText("작성자 : "+productwriter);

                            //제품 작성자의 고유 id
                            writerid=chatRoomsObj.getString("userid");
                            Log.e(TAG,writerid);

                            //제품의 카테고리
                            productcategory=chatRoomsObj.getString("category");


                            //제품의 상세 설명란
                            productdescription=chatRoomsObj.getString("description");
                            productdescription=productdescription.replace("\"","");
                            Log.e(TAG,productdescription);
                            //뷰에 값을 넣음.
                            textdescription.setText(productdescription);

                            imagelist=chatRoomsObj.getString("imagelist");


//                            Log.e(TAG,""+product_id);
//                            Log.e(TAG,""+productname);
//                            Log.e(TAG,""+imagelist);
                        }
                        String[] splitStr = imagelist.split(" ");
                        for(int i=0; i<splitStr.length; i++){
                            image.add("http://192.168.244.105/UploadImage/uploads/"+splitStr[i]);
                            Log.e(TAG, image.get(i));
                            if(i==0){
                                productthumbnail=image.get(i); // 첫번째 사진의 url정보를 담아넨다. 결제 엑티비티에서 해당 정보를 보여주기 위함.
                            }
                        }


//                        //현재 userid와, db에서 가지고 온 writerid(작성자의 고유id)가 같다면, 채팅 생성을 할 수 없다. 그리고 수정 삭제도 불가능하다.
//                        if(writerid.equals(userid)==false){
//                            Log.e(TAG,"유저가 다른 판매자의 글에 들어왔습니다.");
//                            buttonstartchat.setVisibility(View.INVISIBLE);  //버튼 생성 뷰 안보이게 처리
//                        }

                        //ViewPager 생성하기
                        ViewPager viewPager = findViewById(R.id.viewPager);
                        //viewPager.setClipToPadding(false);

//                        float density = getResources().getDisplayMetrics().density;
//                        int margin = (int) (DP * density);
//                        viewPager.setPadding(margin, 0, margin, 0);
//                        viewPager.setPageMargin(margin/2);

                        viewPager.setAdapter(new ViewPagerAdapter(getApplicationContext(), image));
                        viewPager.getAdapter().notifyDataSetChanged();

                        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
                        indicator.setViewPager(viewPager);

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


    //상단 메뉴바 생성 하기.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu.getItem(0).setVisible(false); //로그아웃 메뉴바를 현재페이지에서는 보이지 않게 함.
        //현재 userid와, db에서 가지고 온 writerid(작성자의 고유id)가 같다면, 채팅 생성을 할 수 없다. 그리고 수정 삭제도 불가능하다.
        if(writer==false){
            Log.e(TAG,writer + "현재 작성자의 글이 아닙니다.");
            menu.getItem(1).setVisible(false);
            menu.getItem(2).setVisible(false);
        }
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_itemchage:
                Log.e(TAG,"제품수정 버튼을 클릭했습니다.");
                //MyApplication.getInstance().logout();
                Intent intent = new Intent(ProductActivity.this, ChangeActivity.class);
                intent.putExtra("product_id", product_id);
                startActivity(intent);

                break;

            case R.id.action_itemdelete:
                Log.e(TAG,"제품삭제 버튼을 클릭했습니다.");

                deletedialogshow();

                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }



    /**
     * fetching the chat rooms by making http call
     */
    //http call을 통한 채팅방을 불러오기.
    private void deleteProcut() {

        //_ID_ 로 되어진 문자열 부분에 대한 값을 chatRoomId에 있는 값으로 대체 한다.
        String endPoint = EndPoints.PRODUCT_DLELTE.replace("_ID_",product_id);

        StringRequest strReq = new StringRequest(Request.Method.GET,
                endPoint, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //productArrayList=new ArrayList<>();
                Log.e(TAG, "response: " + response);
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


    private void deletedialogshow()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("해당 제품을 정말 삭제하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProcut();
                        Intent intent = new Intent(ProductActivity.this, productListActivity.class);
                        startActivity(intent); // 화면이동.
                        finish();//다이얼로그 액티비티 종료
                        //Toast.makeText(getApplicationContext(),"예를 선택했습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
    }


    //제품채팅을 등록하는 api를 실행하는 http요청 구문

    /**
     * logging in user. Will make http post request with name, email
     * as parameters
     */
    private void createroom() {
        //post 방식으로 volly를 이용한 로그인 처리하기 예제

//        //유효하지 않은 이름이면 취소(형식을 확인함)
//        if (!validateName()) {
//            return;
//        }
//
//        //유효하지 않은 이메일이면 취소(형식을 확인함)
//        if (!validateEmail()) {
//            return;
//        }

        //edittextview에 입력된 값을 변수에 담음.
        final String chatusername = username;
        final String chatproduct_id = product_id;

        //요청 strReq를 만듬. 엔드포인트를 등록.
        StringRequest strReq = new StringRequest(Request.Method.POST,
                EndPoints.CREATE_CHATROOM, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        Log.e(TAG,"채팅방이 생성되어졌습니다. 생성된 채팅 방으로 이동 합니다.");


                        //생성된 jsonobject에서 다음 값을 가지고 온다.
                        JSONObject createdroom = obj.getJSONObject("createdroom");
                        String chat_room_id = createdroom.getString("chat_room_id");
                        String product_name = createdroom.getString("product_name");
                        Log.e(TAG, chat_room_id+product_name);



                        Intent serviceintent = new Intent(ProductActivity.this, GcmIntentService.class);
                        serviceintent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
                        serviceintent.putExtra(GcmIntentService.TOPIC, "topic_" + chat_room_id); //현재 구독한 토픽에 대한 아이디 값을 보내어 준다.(아이디 값을 기준으로 토픽을 구분한다.)
                        Log.e(TAG,"구독을 진행하는 토픽에 대한 정보를 출력"+chat_room_id+""+product_name);
                        startService(serviceintent);


                        //생성한 채팅 룸으로 이동. response값을 받아서 chatroomid를 받아 이동한다.
                        Intent intent = new Intent(ProductActivity.this, ChatRoomActivity.class);
                        intent.putExtra("chat_room_id", chat_room_id); //채팅룸으로 갈때 넘어가는 채팅룸 id.
                        intent.putExtra("name", product_name); //채팅룸으로 갈 때에 넘어가는 채팅룸의 이름.
                        intent.putExtra("product_id", product_id); //채팅룸으로 갈 때 넘어가는 등록 제품의 고유 번호.
                        startActivity(intent);

                        finish();

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
                params.put("user_name", chatusername); //파라 미터 값을 넣는 처리.
                params.put("product_id", chatproduct_id); //파라 미터 값을 넣는 처리.
                params.put("product_name", productname); //제품이름을 넣음.
                params.put("product_writer", productwriter);//제품 작성자를 넣음.
                params.put("writerid",writerid);
                //params.put("writer_id", ) //제품 작성자의 아이디를 넣음.

                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };
        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }


    /**
     * 채팅방이 생성시에, 메시지를 보내도록 함.
     */
    private void AlertToWriter() {

        //edittextview에 입력된 값을 변수에 담음.
        final String to_user_id = writerid;
        final String from_user_id = MyApplication.getInstance().getPrefManager().getUser().getId();
        final String message = "님이 거래요청을 하셨습니다.";

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



    /**
     * 해당 제품이 현재 내가 거래중인지에 대해서 확인을 하도록 해주는 메서드이다.
     */
    private void checkUserTrading() {

        //edittextview에 입력된 값을 변수에 담음.
        final String check_product_id = product_id;
        final String buy_user_id = MyApplication.getInstance().getPrefManager().getUser().getId();

        //요청 strReq를 만듬. 엔드포인트를 등록.
        StringRequest strReq = new StringRequest(Request.Method.POST,
                EndPoints.CHECK_USER_TRADING, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "제품 거래중인지 확인 api response: " + response);
                //응답값을 확인 후 다시 세팅//
                if(response.equals("true")){
                    paymentButton.setText("거래 페이지로 가기");
                    //그리고 인텐트 변경처리
                    paymentButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ProductActivity.this, tradingProductActivity.class); // 해당 정보로 바로 넘어가지도록 처리한다.
                            intent.putExtra("IamSeller",false);
                            startActivity(intent);
                        }
                    });
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
                params.put("product_id", check_product_id); //보내는 대상
                params.put("buy_user_id", buy_user_id);//보내는 사람(자기 자신)

                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };
        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }


}
