package org.techtown.consolenuri.FileUpload;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    String BASE_URL = "http://192.168.244.105/UploadImage/";
    //String BASE_URL = "http://192.168.244.105/";

    @Multipart
    @POST("upload.php")
    Call<ResponseBody> uploadMultiple(
            @Part("userid") RequestBody userid,
            @Part("username") RequestBody username,
            @Part("themepart") String themepart,
            @Part("pricepart") String pricepart,
            @Part("descriptionpart") String descriptionpart,
            @Part("category") RequestBody category, // 카테고리 정보를 추가 하였다.
            @Part("description") RequestBody description,
            @Part("size") RequestBody size,
            @Part List<MultipartBody.Part> files);



    //upload.php 이미지 메시지를 보내기 위한 바디처리. (나중에는 복수로 올릴 수 있게끔 하자. 지금은 일단 1장만.);
    @Multipart
    @POST("messageimage.php")
    Call<ResponseBody> uploadImageMessage(
            @Part("description") RequestBody description,
            @Part("size") RequestBody size,
            @Part List<MultipartBody.Part> files); // 이부분에 uri를 파일화 시킨 정보를 담는다.


    //멀티파트로써 서버로 해당 값들을 보내고, change.php를 실행해서 내용을 수정하도록 한다.
    @Multipart
    @POST("change.php")
    Call<ResponseBody> ChageMultiple(
            @Part("productidpart") RequestBody productidpart,
            @Part("themepart") RequestBody themepart,
            @Part("pricepart") RequestBody pricepart,
            @Part("notfilepart") RequestBody notfilepart,
            @Part("descriptionpart") RequestBody descriptionpart,
            @Part("category") RequestBody category,
            @Part("description") RequestBody description,
            @Part("size") RequestBody size,
            @Part List<MultipartBody.Part> files);
}