package org.techtown.consolenuri.FileUpload;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.consolenuri.R;
import org.techtown.consolenuri.activity.productListActivity;
import org.techtown.consolenuri.app.EndPoints;
import org.techtown.consolenuri.app.MyApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChangeActivity extends AppCompatActivity {
    private static final String TAG = UploadActivity.class.getSimpleName();

    String product_id,productname,productprice,productwriter,imagelist,productdescription,product_idpart,category;

    //보낼 정보를 담을 변수 값. 해당 정보에 값을 담아서 http요청으로 저장작업을 실행한다.
    String themepart;
    String pricepart;
    String descriptionpart;

    //부가적인 제품에 대한 정보들 저장.
    EditText itemtheme, itemprice, itemdescription;

    //프로그래스 다이얼로그
    private ProgressDialog progressDialog; //어느페이지에서든 쓸 수 있도록 프로그레스바를 준비.

    //Declare Using Veiw....
    private RecyclerView listView;
    MyAdapter mAdapter;
    private Spinner categoryspinner;
    private ProgressBar mProgressBar;
    private MaterialButton btnChoose, btnUpload;


    //해당 부분이 리사이클러뷰이미지 부분에 들어가는 재료로 쓰임.
    private ArrayList<Uri> arrayList= new ArrayList<Uri>();

    private final int REQUEST_CODE_PERMISSIONS  = 1;
    private final int REQUEST_CODE_READ_STORAGE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        //스피너 값 지정함.
        setViews(); //스피너를 생성하는 함수.
        categoryspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG,"선택된 아이템 : "+categoryspinner.getItemAtPosition(position));
                //지정된 값을 넣어 줍니다.
                category=categoryspinner.getItemAtPosition(position).toString();
                Log.e(TAG,category);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //

        Log.e(TAG,"현재 접속중인 상태의 유저 이름 : " + MyApplication.getInstance().getPrefManager().getUser());


        //해당 코드로 에디트 텍스트가 가려지는 현상을 방지하도록 합니다.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        //Connect to View.
        listView = findViewById(R.id.listView); //리사이클러뷰로 변경함.
        mProgressBar = findViewById(R.id.progressBar);
        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setText("제품정보변경");

        //교체할 아이템의 고유 아이디 값을 상품정보페이지로부터 받아 옵니다.
        Intent intent = getIntent();
        product_id = intent.getStringExtra("product_id");
        product_idpart=product_id;

        Log.e(TAG,"현재 넘긴 아이템의 값은 " + product_id + " 입니다.");


        //제품의 제목
        itemtheme = findViewById(R.id.ItemThemeView);

        //제품의 가격
        itemprice = findViewById(R.id.ItemPriceView);

        //제품 상세 설명
        itemdescription = findViewById(R.id.itemDescriptionView);


        getSupportActionBar().setTitle("상품정보변경페이지");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //아이템 불러오는 메소드를 실행합니다.
        fetchProduct();


        //get image permission to user.....

        btnChoose.setOnClickListener(v -> {
            // Display the file chooser dialog
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askForPermission();
            } else {
                showChooser();
            }
        });

        //중요한 실행 부분.
        btnUpload = findViewById(R.id.btnUpload);
        //btnUpload.setOnClickListener(v -> uploadImagesToServer());

        //여기서는 제품 수정 작업을 시작하는 것으로 한다. 그리고 프로덕트 리사이클러뷰 페이지로 이동.
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading();
                uploadImagesToServer();//이미지 정보들과 바디 정보들을 함께 php서버로 보냄.

                //ItemUploadActvitiy 실행 인텐트 작성//
                Intent intent=new Intent(ChangeActivity.this, productListActivity.class);
                startActivity(intent);
                finish();


            }
        });

        //arrayList = new ArrayList<>();
    }

    //선택자 열어줌 (다중 선택).
    private void showChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_READ_STORAGE);
    }

    //사진가지고 온거 결과 물 리사이클러뷰에 담는 메서드.
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
                                arrayList.add(imageUri);
                                Log.e(TAG,"현재 어레이 리스트에 추가된 사진의 URI 정보는 : " +imageUri);
                                //mAdapter = new MyAdapter(ChangeActivity.this, arrayList, onClickItem); //어댑터를 새로이 생성.
                                //listView.setAdapter(mAdapter);
                                //LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                                //listView.setLayoutManager(layoutManager); // 가로 방향으로 리사이클러뷰 재설정.
                                mAdapter.setItems(arrayList);
                                mAdapter.notifyDataSetChanged();


                            } catch (Exception e) {
                                Log.e(TAG, "File select error", e);
                            }
                        }
                    } else if (resultData.getData() != null) {

                        final Uri uri = resultData.getData();
                        Log.i(TAG, "Uri = " + uri.toString());

                        try {
                            //이부분에서 uri값을 넣어 준다. 로그를 확인해보자.
                            arrayList.add(uri);
                            Log.e(TAG,"현재 어레이 리스트에 추가된 사진의 URI 정보는 : " +uri);
                            //MyAdapter mAdapter = new MyAdapter(ChangeActivity.this, arrayList, onClickItem);
                            //listView.setAdapter(mAdapter);
                            mAdapter.setItems(arrayList);
                            mAdapter.notifyDataSetChanged();

                        } catch (Exception e) {
                            Log.e(TAG, "File select error", e);
                        }
                    }
                }
            }
        }
    }

    //아이템을 클릭했을 때 토스트 메시지를 띄워준다. 용도는 몰라.
    private View.OnClickListener onClickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String str = (String) v.getTag();
            Toast.makeText(ChangeActivity.this, str, Toast.LENGTH_SHORT).show();
        }
    };


    //이미지 파일들을 서버로 보내주는 작업을 처리한다.
    private void uploadImagesToServer() {
        //인터넷 연결확인
        if (InternetConnection.checkConnection(ChangeActivity.this)) {
            //레트로 피트 객체 생성 .기본 URL .GsonConverterFactory생성 .그리고 빌드.
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiService.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            //프로그래스바를 나타냄.
            loading();


            // create list of file parts (파일 정보들을 담는 리사이클러 뷰를 생성)
            List<MultipartBody.Part> parts = new ArrayList<>(); //파일 자체를 가지고 있음..??

            // create upload service client 업로드 php파일을 사용하기위해서 retrofit객체에 이를 연결 함.
            ApiService service = retrofit.create(ApiService.class);

            //해당 부분에서 arraylist값이 url형태로 가지고 온 것이라면, 이를 문자열로 한줄로 담고, 삭제처리를 한다.
            String notfile;
            String totalurlfile="";
            int deletecount=0;
            for (int i=0; i<arrayList.size() ;i++){
                if(arrayList.get(i).toString().contains("http://192.168.244.105")){
                    Log.e(TAG,"해당 값은 가져온 값입니다. 어레이 리스트에서 삭제하고 이를 처리합니다.");
                    deletecount++; // 번을 앞에서 지워야 됌.
                }
            }
            Log.e(TAG,""+deletecount); // 카운트 제대로 출력되고

            //출력된 부분 만큼을 앞머리에서 제거해나가면서 문자열에 추가해주어야함.
            for (int i=0; i<deletecount; i++){
                notfile=arrayList.get(i).toString().replace("http://192.168.244.105/UploadImage/uploads/","")+" ";
                totalurlfile+=notfile; // 문자열에 추가. 완료.
                Log.e(TAG,totalurlfile);
            }

            //ArrayList에서 처음 부분들을 모두 삭제처리 함.
            for (int i=0; i<deletecount; i++){
                arrayList.remove(0);
            }

            //arraylist값이 null이 아니라면 넣는 작업을 진행한다.
            if (arrayList != null) {
                Log.e(TAG,"파트에 추가하는 작업을 진행합니다.");
                // create part for file (photo, video, ...)
                for (int i = 0; i < arrayList.size(); i++) {
                    //parts 에 파일 정보들을 저장 시킵니다. 파트네임은 임시로 설정이 되고, uri값을 통해서 실제 파일을 담습니다.
                    parts.add(prepareFilePart("image"+i, arrayList.get(i))); //partName 으로 구분하여 이미지를 등록한다. 그리고 파일객체에 값을 넣어준다.
                }
            }

            // create a map of data to pass along
            RequestBody product_id = createPartFromString(""+product_idpart);
            RequestBody description = createPartFromString("www.androidlearning.com");
            RequestBody notfilelist = createPartFromString(""+totalurlfile);
            //RequestBody notfilelist = createPartFromString("테스트 테스트");
            RequestBody size = createPartFromString(""+parts.size()); // 이부분에서 스트링 값으로 전환을 하는구나 구태여 일일히 바꾸어줄 필요는 없었어.

            RequestBody categorypart=createPartFromString(category);
            Log.e(TAG,"넘겨지는 카테고리 정보 값을 확인.");
            RequestBody themepart=createPartFromString(itemtheme.getText().toString());
            RequestBody pricepart=createPartFromString(itemprice.getText().toString());
            RequestBody descriptionpart =createPartFromString(itemdescription.getText().toString());

            Log.e(TAG,"categorypart에 대한 정보 : "+themepart+"");
            Log.e(TAG,"themepart에 대한 정보 : "+themepart+"");
            Log.e(TAG,"pricepart에 대한 정보 : "+pricepart+"");
            Log.e(TAG,"notfilelist에 대한 정보 : "+notfilelist+"");
            Log.e(TAG,"descriptionpart에 대한 정보 : "+descriptionpart+"");
            Log.e(TAG,"parts에 대한 정보 : "+parts.size()+"");
            Log.e(TAG,"size에 대한 정보 : "+size+"");

            // finally, execute the request 서버에 있는 uploads.php함수를 실행하여, 서버에 이미지를 업로드한다. 여기서 필요한 값들을 다같이 담아서 보내게 된다??
            Call<ResponseBody> call = service.ChageMultiple(product_id,themepart, pricepart, notfilelist,descriptionpart,categorypart,description ,size, parts);
            Log.e(TAG,call+""+"해당 정보를 보냄.");

            //해당 바디를 보낸 콜의 결과물을 체크를 한다 자세히는 파악이 안됌.
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    hideProgress();
                    if(response.isSuccessful()) {
//                        Toast.makeText(ChangeActivity.this,
//                                "Images successfully uploaded!" + response, Toast.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content),
                                R.string.string_some_thing_wrong, Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    hideProgress();
                    Log.e(TAG, "Image upload failed!", t);
                    Snackbar.make(findViewById(android.R.id.content),
                            "Image upload failed!", Snackbar.LENGTH_LONG).show();
                }
            });

        } else {
            Toast.makeText(ChangeActivity.this,
                    R.string.string_internet_connection_not_available, Toast.LENGTH_SHORT).show();

            finish();//해당 엑티비티 종료.
        }
        loadingEnd();//프로그래스바 종료.
    }

    //프로그래스바로 나타 냄 그리고 사라짐. 이거 활용하고 싶으면 활용 할 것. //이거 안쓸꺼임.
    private void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        btnChoose.setEnabled(false);
        btnUpload.setVisibility(View.GONE);
    }

    private void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
        btnChoose.setEnabled(true);
        btnUpload.setVisibility(View.VISIBLE);
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
        Log.e(TAG,""+fileUri);
        Log.e(TAG,""+file);

        // create RequestBody instance from file 리퀘스트바디를 파일로부터 만든다.
        RequestBody requestFile = RequestBody.create (MediaType.parse(FileUtils.MIME_TYPE_IMAGE), file);

        // MultipartBody.Part is used to send also the actual file name //
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }


    /**
     *  Runtime Permission
     */
    //실행 허가 확인 (저장소)
    private void askForPermission() {
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                != PackageManager.PERMISSION_GRANTED) {
            /* Ask for permission */
            // need to request permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Snackbar.make(this.findViewById(android.R.id.content),
                        "Please grant permissions to write data in sdcard",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        v -> ActivityCompat.requestPermissions(ChangeActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_PERMISSIONS)).show();
            } else {
                /* Request for permission */
                ActivityCompat.requestPermissions(ChangeActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_PERMISSIONS);
            }

        } else {
            showChooser();
        }
    }

    //허가 결과 확인
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                showChooser();
            } else {
                // Permission Denied
                Toast.makeText(ChangeActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChangeActivity.this);
        final AlertDialog dialog = builder.setMessage("You need to grant access to Read External Storage")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(arg0 -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(ChangeActivity.this, android.R.color.holo_blue_light));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    ContextCompat.getColor(ChangeActivity.this, android.R.color.holo_red_light));
        });

        dialog.show();
    }

    //여기서 데이터를 받아오는 작업을 진행하여, 기존 값에 불러오는 작업을 진행하도록 합니다.
    /**
     * Get Product Information as json form.
     */
    //http call을 통한 채팅방을 불러오기.
    private void fetchProduct() {

        //_ID_ 로 되어진 문자열 부분에 대한 값을 chatRoomId에 있는 값으로 대체 한다.
        String endPoint = EndPoints.PRODUCT_DETAIL.replace("_ID_",product_id);

        //실행되어지는 api의 값.
        Log.e(TAG, "endPoint: " + endPoint);

        //요청 문자를 보냄 (get 방식의 endpoint)
        StringRequest strReq = new StringRequest(Request.Method.GET,
                endPoint, new com.android.volley.Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag 그리고 데이터를 받아서 처리하는 작업 진행.
                    if (obj.getBoolean("error") == false) {
                        JSONArray chatRoomsArray = obj.getJSONArray("product");
                        for (int i = 0; i < chatRoomsArray.length(); i++) {
                            JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                            product_id=chatRoomsObj.getString("product_id");

                            //제품의 이름
                            productname=chatRoomsObj.getString("productname");
                            productname=productname.replace("\"","");
                            Log.e(TAG,productname);
                            itemtheme.setText(productname);

                            //제품의 가격
                            productprice=chatRoomsObj.getString("price");
                            productprice=productprice.replace("\"","");
                            Log.e(TAG,productprice);
                            itemprice.setText(productprice);

                            //제품의 작성자
                            productwriter=chatRoomsObj.getString("writer");
                            productwriter=productwriter.replace("\"","");
                            Log.e(TAG,productwriter);
                            //textwriter.setText(productwriter);

                            //제품의 상세 설명란
                            productdescription=chatRoomsObj.getString("description");
                            productdescription=productdescription.replace("\"","");
                            Log.e(TAG,productdescription);
                            itemdescription.setText(productdescription);

                            //제품의 카테고리 정보
                            category=chatRoomsObj.getString("category");
                            Log.e(TAG,category);
                            //가지고 온 카테고리값으로, 스피너를 세팅한다.
                            if(category.equals("게임기")){
                                categoryspinner.setSelection(0);
                            }
                            else if(category.equals("소프트웨어")){
                                categoryspinner.setSelection(1);
                            }
                            else if(category.equals("게임 주변기기")){
                                categoryspinner.setSelection(2);
                            }
                            else if(category.equals("종합 제품")){
                                categoryspinner.setSelection(3);
                            }
                            else{
                                categoryspinner.setSelection(4);
                            }



                            imagelist=chatRoomsObj.getString("imagelist");


//                            Log.e(TAG,""+product_id);
//                            Log.e(TAG,""+productname);
//                            Log.e(TAG,""+imagelist);
                        }
                        String[] splitStr = imagelist.split(" ");
                        //ArrayList<Uri> arrayList = new ArrayList<>();

                        //해당 부분에서 arraylist에 값을 넣어 준다. 그전에 값을 uri포멧으로 바꾸어서 넘겨준다.
                        for(int i=0; i<splitStr.length; i++){
                            String tmp_uri="http://192.168.244.105/UploadImage/uploads/"+splitStr[i];
                            arrayList.add(Uri.parse(tmp_uri));
                            Log.e(TAG, arrayList.get(i)+"현재 들어가 있는 값.");
                        }

                        //여기서 데이터 셋이 바뀌었다는 것을 리사이클러뷰에게 알려 줌.
                        mAdapter = new MyAdapter(ChangeActivity.this, arrayList, onClickItem); //어댑터를 새로이 생성.
                        listView.setAdapter(mAdapter);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                        listView.setLayoutManager(layoutManager); // 가로 방향으로 리사이클러뷰 재설정.
                        mAdapter.setItems(arrayList);
                        mAdapter.notifyDataSetChanged();


                    } else {
                        // error in fetching chat rooms
                        Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {

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

    //스피너 추가하기
    public void setViews() {
        categoryspinner = (Spinner) findViewById(R.id.spinner);

        //어댑터 생성 및 연결.
        ArrayAdapter arrayAdapter;
        arrayAdapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_dropdown_item);

        categoryspinner.setAdapter(arrayAdapter);
    }

    //화면로딩 메소드 추가
    //로딩바 생성하기
    public void loading() {
        //로딩
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(ChangeActivity.this);
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
