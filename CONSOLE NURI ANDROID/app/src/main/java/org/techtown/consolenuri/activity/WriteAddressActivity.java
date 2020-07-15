package org.techtown.consolenuri.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.techtown.consolenuri.R;
import org.techtown.consolenuri.model.Address;

public class WriteAddressActivity extends AppCompatActivity {
    private String TAG = WriteAddressActivity.class.getSimpleName();

    ImageView searchaddress;
    Button addaddress;

    //주소 입력값 지정
    TextView postaladdress;
    EditText addressname,addresscontact,addressdetail,addressrequset;

    //체크 박스 지정
    CheckBox storedefaultshipping; //기본 배송지로 지정하기. 아직 미구현함 우선순위 아님.


    //startActivityForResult
    static final int REQUEST_CODE3 = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addresswrite_activity);

        //뷰 연결하기
        addressname=(EditText)findViewById(R.id.addressnameView); //성함
        addresscontact=(EditText)findViewById(R.id.addresscontactView); //연락처
        postaladdress=(TextView)findViewById(R.id.postaladdressView); //우편주소
        addressdetail=(EditText)findViewById(R.id.addressDetailView); //상세주소
        addressrequset=(EditText)findViewById(R.id.addressrequestView); //요청사항


        //우편번호 검색 뷰를 실행합니다.
        searchaddress=(ImageView)findViewById(R.id.searchaddressView);
        searchaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //주고 검색 엑티비티로 넘어간다.
                Intent intent = new Intent(WriteAddressActivity.this,SearchAddressActivity.class);
                startActivityForResult(intent,REQUEST_CODE3);//request코드를 들고 다음 엑티비티로 이동한다.
            }
        });



        //결과물을 들고, AddressActivity로 되돌아갑니다.
        addaddress=(Button)findViewById(R.id.addaddressView);
        addaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //결과물을 들고, AddressActivity로 넘긴다.
                //객체를 생성시키도록 합니다.
                Address address = new Address();
                address.setName(addressname.getText().toString()); //이름
                address.setAddress(postaladdress.getText().toString()); //우편주소
                address.setAddressdetail(addressdetail.getText().toString()); //상세주소
                address.setContact(addresscontact.getText().toString()); //연락처
                address.setRequset(addressrequset.getText().toString()); //요청사항

                Intent resultintent = new Intent();
                resultintent.putExtra("address",address); // 이곳에 serialize시킨 데이터를 넣습니다.
                setResult(RESULT_OK,resultintent);
                finish();
            }
        });



        //리사이클러뷰를 클릭했을 때, 해당 배송지 정보를 넘겨주도록 합니다.

    }


    //SearchAddress로 부터 전달받은 값을 처리한다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE3) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.e(TAG,"SearchAddrss로 부터 성공적으로 돌아 왔습니다.33333");
                Log.e(TAG,data.getStringExtra("postaladdress")+"");
                postaladdress.setText(data.getStringExtra("postaladdress"));

            }
        }
    }

}
