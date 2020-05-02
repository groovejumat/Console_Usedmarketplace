package org.techtown.consolenuri.liveStreaming;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.techtown.consolenuri.R;
import org.techtown.consolenuri.app.MyApplication;

public class streamMainActivity extends AppCompatActivity {

    Button btnstart,btnwatch;


    //카메라 허용 기능 넣기.
    private final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streammain);

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }

        showPermissionsErrorAndRequest(); // 메니페스트에 등록되어진 권한들을 요청해주는 메서드이다. 활용여지가 높다.


        btnstart=(Button)findViewById(R.id.startStream);
        btnwatch=(Button)findViewById(R.id.watchStream);


        //방송시작 (테스트)
        btnstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(streamMainActivity.this,ExampleRtmpActivity.class);
                //채팅에 참가하는 유저의 닉네임 정보를 보냅니다.
                intent.putExtra("Username",MyApplication.getInstance().getPrefManager().getUser().getName());
                //생성되어진 방의 rtmp링크를 보냅니다.

                startActivity(intent);
            }
        });

        //방송보기 (테스트)
        btnwatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(streamMainActivity.this,StreamActivity.class);
                //채팅에 참가하는 유저의 닉네임 정보를 보냅니다.
                intent.putExtra("Username",MyApplication.getInstance().getPrefManager().getUser().getName());
                //생성된 방의 rtmp링크를 보냅니다.

                startActivity(intent);
            }
        });

    }

    //권한 요청 해주는 부분 체크.
    private void showPermissionsErrorAndRequest() {
        Toast.makeText(this, "You need permissions before", Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
