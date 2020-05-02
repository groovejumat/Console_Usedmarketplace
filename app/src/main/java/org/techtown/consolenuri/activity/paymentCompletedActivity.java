package org.techtown.consolenuri.activity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.consolenuri.R;
import org.techtown.consolenuri.app.EndPoints;
import org.techtown.consolenuri.app.MyApplication;

import java.util.HashMap;
import java.util.Map;

//결제가 완료되었음을 나타내는 엑티비티. 여기서 payment를 종료시키고 다음 액티비티로 넘어온다.
public class paymentCompletedActivity extends AppCompatActivity {

    //태그
    private String TAG = paymentCompletedActivity.class.getSimpleName();

    //POST로 보낼 파라미터 값 보내기
    String sellUserid,buyUserid,proudctid,shippinginfo;

    //프로그래스 다이얼로그
    private ProgressDialog progressDialog; //어느페이지에서든 쓸 수 있도록 프로그레스바를 준비.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loading();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paymetcompleted);


        //넘긴값 가지고 오기
        Intent intent = getIntent();
        sellUserid=intent.getStringExtra("writerid");
        buyUserid=intent.getStringExtra("userid");
        proudctid=intent.getStringExtra("product_id");
        shippinginfo=intent.getStringExtra("shippinginfo");


        Log.e(TAG,sellUserid+"");
        Log.e(TAG,buyUserid+"");
        Log.e(TAG,proudctid+"");


        Log.e(TAG,"결제가 완료 되었음을 알리는 액티비티 실행 됌.");

        Addtradingproduct();//제품 거래 정보 db등록 메소드 실행.

    }


    /**
     * 채팅방이 생성시에, 메시지를 보내도록 함.
     */
    private void Addtradingproduct() {


        //요청 strReq를 만듬. 엔드포인트를 등록.
        StringRequest strReq = new StringRequest(Request.Method.POST,
                EndPoints.ADD_TRADINGPRODUCT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        Log.e(TAG,"성공적으로 에러값을 받아 왔으므로, productlistactivity로 넘어간다.");
                        Intent intent = new Intent(paymentCompletedActivity.this,tradingProductActivity.class);
                        intent.putExtra("IamSeller",false);
                        startActivity(intent);

                    } else {
                        // login error - simply toast the message
                        //Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                    //json으로 바꿔 오는 과정에서 발생한 에러.
                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                AlertToWriter(); // 채팅으로 결제 알림 메시지를 보냄.
                loadingEnd();
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
                params.put("sell_user_id", sellUserid); //보내는 대상
                params.put("buy_user_id", buyUserid);//보내는 사람(자기 자신)
                params.put("product_id",proudctid); //거래 메세지
                params.put("productprogress","0"); //거래 메세지
                params.put("shippinginfo",shippinginfo); //거래 메세지


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
        final String to_user_id = sellUserid;
        final String from_user_id = MyApplication.getInstance().getPrefManager().getUser().getId();
        final String message = "제품의 결제를 하셨습니다.";

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



    //로딩바 생성하기
    public void loading() {
        //로딩
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(paymentCompletedActivity.this);
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

}
