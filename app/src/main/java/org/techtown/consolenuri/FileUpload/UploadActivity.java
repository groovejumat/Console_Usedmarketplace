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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.techtown.consolenuri.R;
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

public class UploadActivity extends AppCompatActivity {
    private static final String TAG = UploadActivity.class.getSimpleName();

    //보낼 정보를 담을 변수 값. 해당 정보에 값을 담아서 http요청으로 저장작업을 실행한다.
    String themepart;
    String pricepart;
    String descriptionpart, category;
    //부가적인 제품에 대한 정보들 저장.
    EditText itemtheme, itemprice, itemdescription;

    //Declare Using Veiw....
    private RecyclerView listView;
    private ProgressBar mProgressBar;
    private Spinner categoryspinner;
    private MaterialButton btnChoose, btnUpload;

    //프로그래스 다이얼로그
    private ProgressDialog progressDialog; //어느페이지에서든 쓸 수 있도록 프로그레스바를 준비.

    private ArrayList<Uri> arrayList;

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

        //해당 코드로 에디트 텍스트가 가려지는 현상을 방지하도록 합니다.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Log.e(TAG,"현재 접속중인 상태의 유저 이름 : " + MyApplication.getInstance().getPrefManager().getUser().getName());

        //Connect to View.
        listView = findViewById(R.id.listView); //리사이클러뷰로 변경함.
        mProgressBar = findViewById(R.id.progressBar);
        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);

        //제품의 제목
        itemtheme = findViewById(R.id.ItemThemeView);

        //제품의 가격
        itemprice = findViewById(R.id.ItemPriceView);

        //제품 상세 설명
        itemdescription = findViewById(R.id.itemDescriptionView);


        getSupportActionBar().setTitle("상품등록");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);



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
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading();
                if(arrayList.size()==0){
                    Toast.makeText(UploadActivity.this, "이미지를 최소 하나 이상 등록해 주셔야 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                uploadImagesToServer();//이미지 정보들과 바디 정보들을 함께 php서버로 보냄.

            }
        });

        arrayList = new ArrayList<>();
    }

    private void showChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_READ_STORAGE);
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
                                arrayList.add(imageUri);
                                MyAdapter mAdapter = new MyAdapter(UploadActivity.this, arrayList, onClickItem); //어댑터를 새로 생성.
                                listView.setAdapter(mAdapter);
                                LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                                listView.setLayoutManager(layoutManager); // 가로 방향으로 리사이클러뷰 재설정.

                            } catch (Exception e) {
                                Log.e(TAG, "File select error", e);
                            }
                        }
                    } else if (resultData.getData() != null) {

                        final Uri uri = resultData.getData();
                        Log.i(TAG, "Uri = " + uri.toString());

                        try {
                            arrayList.add(uri);
                            MyAdapter mAdapter = new MyAdapter(UploadActivity.this, arrayList, onClickItem);
                            listView.setAdapter(mAdapter);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                            listView.setLayoutManager(layoutManager); // 가로 방향으로 리사이클러뷰 재설정.

                        } catch (Exception e) {
                            Log.e(TAG, "File select error", e);
                        }
                    }
                }
            }
        }
    }

    private View.OnClickListener onClickItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String str = (String) v.getTag();
            Toast.makeText(UploadActivity.this, str, Toast.LENGTH_SHORT).show();
        }
    };


    //이미지 파일들을 서버로 보내주는 작업을 처리한다.
    private void uploadImagesToServer() {
        //인터넷 연결확인
        if (InternetConnection.checkConnection(UploadActivity.this)) {
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
            RequestBody userid = createPartFromString(""+MyApplication.getInstance().getPrefManager().getUser().getId());
            RequestBody username = createPartFromString(""+MyApplication.getInstance().getPrefManager().getUser().getName());
            RequestBody categorypart = createPartFromString(""+category);
            RequestBody description = createPartFromString("www.androidlearning.com");
            RequestBody size = createPartFromString(""+parts.size());
            themepart=itemtheme.getText().toString();
            pricepart=itemprice.getText().toString();
            descriptionpart=itemdescription.getText().toString();

            Log.e(TAG,"categorypart에 대한 정보 : "+categorypart+"");
            Log.e(TAG,"themepart에 대한 정보 : "+themepart+"");
            Log.e(TAG,"pricepart에 대한 정보 : "+pricepart+"");
            Log.e(TAG,"descriptionpart에 대한 정보 : "+descriptionpart+"");
            Log.e(TAG,"parts에 대한 정보 : "+parts+"");
            Log.e(TAG,"size에 대한 정보 : "+size+"");

            // finally, execute the request 서버에 있는 uploads.php함수를 실행하여, 서버에 이미지를 업로드한다. 여기서 필요한 값들을 다같이 담아서 보내게 된다??
            Call<ResponseBody> call = service.uploadMultiple(userid,username,themepart, pricepart,descriptionpart,categorypart,description ,size, parts);
            Log.e(TAG,call+""+"해당 정보를 보냄.");

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    hideProgress();
                    if(response.isSuccessful()) {
                        finish(); //해당 엑티비티 종료.
//                      Toast.makeText(UploadActivity.this,
//                                "Images successfully uploaded!" + response, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(UploadActivity.this,
                    R.string.string_internet_connection_not_available, Toast.LENGTH_SHORT).show();
        }
        loadingEnd();
    }

    //프로그래스바로 나타 냄...
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

        // create RequestBody instance from file 리퀘스트바디를 파일로부터 만든다.
        RequestBody requestFile = RequestBody.create (MediaType.parse(FileUtils.MIME_TYPE_IMAGE), file);

        // MultipartBody.Part is used to send also the actual file name //
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }


    /**
     *  Runtime Permission
     */
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
                        v -> ActivityCompat.requestPermissions(UploadActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_PERMISSIONS)).show();
            } else {
                /* Request for permission */
                ActivityCompat.requestPermissions(UploadActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_PERMISSIONS);
            }

        } else {
            showChooser();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                showChooser();
            } else {
                // Permission Denied
                Toast.makeText(UploadActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
        final AlertDialog dialog = builder.setMessage("You need to grant access to Read External Storage")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(arg0 -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(UploadActivity.this, android.R.color.holo_blue_light));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    ContextCompat.getColor(UploadActivity.this, android.R.color.holo_red_light));
        });

        dialog.show();
    }

    //스피너 추가하기
    public void setViews() {
        categoryspinner = (Spinner) findViewById(R.id.spinner);

        //어댑터 생성 및 연결.
        ArrayAdapter arrayAdapter;
        arrayAdapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_dropdown_item);

        categoryspinner.setAdapter(arrayAdapter);
    }


    //로딩바 생성하기
    public void loading() {
        //로딩
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(UploadActivity.this);
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
