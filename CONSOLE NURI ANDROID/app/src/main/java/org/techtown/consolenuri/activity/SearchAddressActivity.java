package org.techtown.consolenuri.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.techtown.consolenuri.R;

public class SearchAddressActivity extends AppCompatActivity {
    private String TAG = SearchAddressActivity.class.getSimpleName();

    private WebView webView;
    private Button addpostalCode;
    private TextView txt_address;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchaddresss_activity);

        txt_address = findViewById(R.id.txt_address);

        addpostalCode = findViewById(R.id.confirmaddressView);

        // WebView 초기화
        init_webView();

        // 핸들러를 통한 JavaScript 이벤트 반응
        handler = new Handler();


        //버튼 클릭시 우편 주소 등록
        addpostalCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultintent = new Intent();
                Log.e(TAG,txt_address.getText()+"");
                resultintent.putExtra("postaladdress",txt_address.getText());
                setResult(RESULT_OK,resultintent);
                finish();
            }
        });
    }
    public void init_webView() {
        // WebView 설정
        webView = (WebView) findViewById(R.id.webView_address);

        // JavaScript 허용
        webView.getSettings().setJavaScriptEnabled(true);

        // JavaScript의 window.open 허용
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);


        // JavaScript이벤트에 대응할 함수를 정의 한 클래스를 붙여줌
        webView.addJavascriptInterface(new AndroidBridge(), "TestApp");

        // web client 를 chrome 으로 설정
        webView.setWebChromeClient(new WebChromeClient());

        // webview url load. php 파일 주소
        webView.loadUrl("http://192.168.244.105/load.php");

    }


    private class AndroidBridge {
        @JavascriptInterface
        public void setAddress(final String arg1, final String arg2, final String arg3) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    txt_address.setText(String.format("(%s) %s %s", arg1, arg2, arg3));
                    String detailedAddress;

                    // WebView를 초기화 하지않으면 재사용할 수 없음
                    init_webView();
                }
            });
        }
    }
}
