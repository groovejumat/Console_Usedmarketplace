package org.techtown.consolenuri.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.techtown.consolenuri.R;
import org.techtown.consolenuri.app.MyApplication;
import org.techtown.consolenuri.model.Address;

import kr.co.bootpay.Bootpay;
import kr.co.bootpay.enums.UX;
import kr.co.bootpay.listener.CancelListener;
import kr.co.bootpay.listener.CloseListener;
import kr.co.bootpay.listener.ConfirmListener;
import kr.co.bootpay.listener.DoneListener;
import kr.co.bootpay.listener.ErrorListener;
import kr.co.bootpay.listener.ReadyListener;
import kr.co.bootpay.model.BootExtra;
import kr.co.bootpay.model.BootUser;

public class PaymentActivity extends AppCompatActivity {
    private String TAG = PaymentActivity.class.getSimpleName();

    //제품 상단의 뷰 정보들
    ImageView thumbnailView;
    TextView productpriceView,productnameView,priceView; //제품의 가격과 카테고리
    TextView addressView,addressdetailView,addressnamecontactView,addressrequestView;
    Button startpayment;
    int stuck=10; //임시 재고 수량 확인.

    //startActivityForResult
    static final int REQUEST_CODE1 = 1;

    //주소 리사이클러뷰 페이지를 보여주기 위한 리니어 레이아웃 리스너 적용.
    LinearLayout linearLayout;

    //변수 지정 제품 정보 파트
    String productid,productcategory,productprice,productname,productthumbnail,writerid;

    //변수 지정 주소 정보 파트
    String address,addressdetail,nameAndcontact,request;

    //프로그래스 다이얼로그
    private ProgressDialog progressDialog; //어느페이지에서든 쓸 수 있도록 프로그레스바를 준비.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_activity);

        //뷰 연결
        thumbnailView=(ImageView)findViewById(R.id.thumbnail);
        productpriceView=(TextView)findViewById(R.id.productprice);
        productnameView=(TextView)findViewById(R.id.productname);
        priceView=(TextView)findViewById(R.id.priceView);//하단 최종 결제 가격 확인내용.
        startpayment=(Button)findViewById(R.id.startpaymentView);//결제 진행 실행.
        linearLayout=(LinearLayout)findViewById(R.id.AddressLayout);

        //주소 정보 뷰 연결하기
        addressView=(TextView)findViewById(R.id.addressView); //주소
        addressdetailView=(TextView)findViewById(R.id.addressdetailView); //주소 상세
        addressnamecontactView=(TextView)findViewById(R.id.namePhoneView); //이름과 연락처
        addressrequestView=(TextView)findViewById(R.id.deliveryRequestView); //요청 사항

        //제품 정보 가지고 오기
        Intent intent=getIntent();
        productid=intent.getStringExtra("product_id");//제품 고유 id
        Log.e(TAG,productid+"해당값을 가지고 왔습니다.");
        productname=intent.getStringExtra("product_name");//제품의 이름
        productprice=intent.getStringExtra("product_price");//제품 가격
        productcategory=intent.getStringExtra("product_category");//제품카테고리
        productthumbnail=intent.getStringExtra("product_thumbnail");//제품썸네일 url
        writerid=intent.getStringExtra("writerid");//제품의 작성자 id;

        //[제품 변수를 참조하여 정보를 세팅]
        //글라이드로 이미지 뷰에 고정 시킴.
        Glide.with(PaymentActivity.this)
                .load(productthumbnail)
                .centerCrop()
                .into(thumbnailView);

        //이름과 가격 세팅
        productnameView.setText(productname); //이름
        productpriceView.setText(productprice+ " 원"); //가격
        priceView.setText((productprice+" 원")); //결제확인 가격

        //버튼 클릭시 결제 실행
        startpayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick_request();
            }
        });


        //주소부분 리니어 레이아웃 클릭시 주소 등록 실행 startActivity For Result로..
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivityForResult로 AddressActivity를 실행.
                Intent intent = new Intent(PaymentActivity.this,AddressActivity.class);
                startActivityForResult(intent,REQUEST_CODE1);
            }
        });

    }


    //엑티비티 결과를 받아왔을 때에 대한 처리 실행.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.e(TAG,"성공적으로 받아 왔습니다. 11111");
                //받아온 address의 값을 textView에 담는 처리를 진행합니다.
                Address address=(Address)data.getSerializableExtra("address");
                addressView.setText(address.getAddress()); // 주소
                addressdetailView.setText(address.getAddressdetail()); // 주소상세정보
                addressnamecontactView.setText(address.getName() + " / " +address.getContact()); // 이름 연락처
                addressrequestView.setText(address.getRequset()); //배송요청사항

            }
        }
    }



    //결제다이얼로그 띄우기 현재 연결되어진 PG는 KG이니시스
    public void onClick_request() {
        loading();
        // 결제호출
        //BootUser bootUser = new BootUser().setPhone("010-1234-5678");
        BootUser bootUser = new BootUser(); //휴대폰 입력번호 없음.
        BootExtra bootExtra = new BootExtra().setQuotas(new int[] {0,2,3});

        Bootpay.init(getFragmentManager())
                .setApplicationId("5e6ec35b02f57e002e4b4edd") // 해당 프로젝트(안드로이드)의 application id 값
                .setContext(this)
                .setBootUser(bootUser)
                .setBootExtra(bootExtra)
                .setUX(UX.PG_DIALOG)
//                .setUserPhone("010-1234-5678") // 구매자 전화번호
                .setName(productname) // 결제할 상품명
                .setOrderId("1234") // 결제 고유번호expire_month
                .setPrice(Integer.parseInt(productprice)) // 결제할 금액(정수화 시켜야 한다.)
                //.addItem("마우's 스", 1, "ITEM_CODE_MOUSE", 100) // 주문정보에 담길 상품정보, 통계를 위해 사용
                //.addItem("키보드", 1, "ITEM_CODE_KEYBOARD", 200, "패션", "여성상의", "블라우스") // 주문정보에 담길 상품정보, 통계를 위해 사용
                .onConfirm(new ConfirmListener() { // 결제가 진행되기 바로 직전 호출되는 함수로, 주로 재고처리 등의 로직이 수행
                    @Override
                    public void onConfirm(@Nullable String message) {

                        if (0 < stuck) Bootpay.confirm(message); // 재고가 있을 경우.
                        else Bootpay.removePaymentWindow(); // 재고가 없어 중간에 결제창을 닫고 싶을 경우
                        Log.d("confirm", message);
                    }
                })
                .onDone(new DoneListener() { // 결제완료시 호출, 아이템 지급 등 데이터 동기화 로직을 수행합니다
                    @Override
                    public void onDone(@Nullable String message) {
                        Log.d("done", message);
                        //해당 값을 통해서 결과 값을 긁어오면 된다. json포멧의 형식으로.
                        //그리고 엑티비티를 실행하도록 한다.

                        Intent intent=new Intent(PaymentActivity.this,paymentCompletedActivity.class);
                        Log.e(TAG,productid+"해당 값을 putextra로 넘깁니다.");
                        intent.putExtra("product_id",productid);  //제품의 고유 id
                        intent.putExtra("writerid",writerid);  //제품의 작성자 id
                        intent.putExtra("userid", MyApplication.getInstance().getPrefManager().getUser().getId());//구매자의 id
                        intent.putExtra("shippinginfo", addressnamecontactView.getText()+ "+" + addressView.getText() +"+" + addressdetailView.getText() ); //배송정보를 담아서 처리합니다.
                        intent.putExtra("IamSeller",false); // 유저가 구매자라는 것을 보내서, 구매자 페이지로 넘어가 질 수 있도록 함.
                        startActivity(intent); //결제 완료 페이지로 넘어갑니다.
                        finish();//결제가 완료 되어진경우 해당 액티비티를 종료시킨다

                    }
                })
                .onReady(new ReadyListener() { // 가상계좌 입금 계좌번호가 발급되면 호출되는 함수입니다.
                    @Override
                    public void onReady(@Nullable String message) {
                        Log.d("ready", message);
                    }
                })
                .onCancel(new CancelListener() { // 결제 취소시 호출
                    @Override
                    public void onCancel(@Nullable String message) {
                        Log.d("cancel", message);
                    }
                })
                .onError(new ErrorListener() { // 에러가 났을때 호출되는 부분
                    @Override
                    public void onError(@Nullable String message) {
                        Log.d("error", message);
                    }
                })
                .onClose(
                        new CloseListener() { //결제창이 닫힐때 실행되는 부분
                            @Override
                            public void onClose(String message) {
                                Log.d("close", "close");
                            }
                        })
                .request();
    }


    //로딩바 생성하기
    public void loading() {
        //로딩
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(PaymentActivity.this);
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
