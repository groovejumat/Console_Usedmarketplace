package org.techtown.consolenuri.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.consolenuri.R;
import org.techtown.consolenuri.adapter.ChatRoomsAdapter;
import org.techtown.consolenuri.adapter.DeliveryAdapter;
import org.techtown.consolenuri.app.MyApplication;
import org.techtown.consolenuri.model.Address;

import java.util.ArrayList;

public class AddressActivity extends AppCompatActivity {
    private String TAG = AddressActivity.class.getSimpleName();

    Button addressadd;

    //startActivityForResult
    static final int REQUEST_CODE2 = 2;

    //Address어레이 리스트 지정
    ArrayList<Address> addressarraylist;
    private DeliveryAdapter mAdapter;

    //recyclerView지정
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.address_activity);

        //리사이클러뷰 생성 진행.
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview); //리사이클러뷰 연결.
        addressarraylist=new ArrayList<>();
        addressarraylist=MyApplication.getInstance().getPrefManager().getAddresslist(); //셰어드 프리퍼런스로 해당 값들을 불러옵니다.
        mAdapter = new DeliveryAdapter(this, addressarraylist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(
//                getApplicationContext()
//        )); // 디바이더 적용
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        //mAdapter.notifyDataSetChanged();

        //주소 추가 버튼 생성 후 인텐트를 실행.
        addressadd=(Button)findViewById(R.id.addaddressView);
        addressadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //WriteActivity를 실행.
                Intent intent = new Intent(AddressActivity.this,WriteAddressActivity.class);
                startActivityForResult(intent,REQUEST_CODE2);
            }
        });


        recyclerView.addOnItemTouchListener(new ChatRoomsAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new ChatRoomsAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //리사이클러뷰를 클릭했을 때, 해당 adrress객체를 For result로 보내준다.
                Intent resultintent = new Intent();
                resultintent.putExtra("address",addressarraylist.get(position)); // 이곳에 serialize시킨 데이터를 넣습니다.
                setResult(RESULT_OK,resultintent);
                finish();
            }

            @Override
            public void onLongClick(View view, int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddressActivity.this);
                    builder.setTitle("등록된 주소지를 삭제하시겠습니까?");
                    //builder.setMessage("AlertDialog Content");
                    builder.setPositiveButton("예",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //Toast.makeText(getApplicationContext(),"예를 선택했습니다.",Toast.LENGTH_LONG).show();
                                    // 이 버튼 클릭시 삭제 진행
                                    //해당 위치 값에 있는 데이터를 삭제 처리 하도록 한다.
                                    addressarraylist.remove(position);
                                    mAdapter.notifyItemRemoved(position); //삭제 처리하기
                                    dialog.dismiss();
                                }
                            });
                    builder.setNegativeButton("아니오",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
                            });
                    builder.show();

            }
        }));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE2) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.e(TAG,"성공적으로 받아옴 22222");
                Address address=(Address)data.getSerializableExtra("address");
                Log.e(TAG,address.getName());
                Log.e(TAG,address.getAddress());
                Log.e(TAG,address.getAddressdetail());
                Log.e(TAG,address.getContact());
                Log.e(TAG,address.getRequset());
                //해당 어드레스 정보를 셰어드 프리퍼런스에 저장


                addressarraylist.add(address); //리사이클러뷰 어레이 리스트에 해당 정보를 추가한다.
                mAdapter.notifyDataSetChanged(); //변경이 완료 됌.

                // 받아온 address객체를 recyclerview에 저장시키는 작업을 진행함.
            }
        }
    }


    @Override
    protected void onPause() {
        MyApplication.getInstance().getPrefManager().storeAddresslist(addressarraylist); //셰어드 프리퍼런스로 해당 값들을 저장합니다.
        super.onPause();
    }


    @Override
    protected void onResume() {

        super.onResume();
    }
}


