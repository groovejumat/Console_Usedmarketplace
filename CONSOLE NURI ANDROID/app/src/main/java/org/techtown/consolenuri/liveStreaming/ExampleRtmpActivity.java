package org.techtown.consolenuri.liveStreaming;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import net.ossrs.rtmp.ConnectCheckerRtmp;

import org.techtown.consolenuri.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExampleRtmpActivity extends AppCompatActivity
    implements ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback {

        // 채팅 엑티비티의 변수 목록
        // 서버 접속 여부를 판별하기 위한 변수
        boolean isConnect = false;
        EditText edit1;
        Button btn1;
        Button btn2;
        LinearLayout container;
        ScrollView scroll;
        ProgressDialog pro;
        // 어플 종료시 스레드 중지를 위해...
        boolean isRunning = false;
        // 서버와 연결되어있는 소켓 객체
        Socket member_socket;
        // 사용자 닉네임( 내 닉넴과 일치하면 내가보낸 말풍선으로 설정 아니면 반대설정)
        String user_nickname;
        String username;
        /////////////

        private RtmpCamera1 rtmpCamera1;
        private Button button;
        private Button bRecord;
        private EditText etUrl;

        private String currentDateAndTime = "";
        private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/rtmp-rtsp-stream-client-java");

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setContentView(R.layout.activity_example);
            SurfaceView surfaceView = findViewById(R.id.surfaceView);
            button = findViewById(R.id.b_start_stop);
            button.setOnClickListener(this);
            bRecord = findViewById(R.id.b_record);
            bRecord.setOnClickListener(this);
            Button switchCamera = findViewById(R.id.switch_camera);
            switchCamera.setOnClickListener(this);
            etUrl = findViewById(R.id.et_rtp_url);
            etUrl.setHint("rtmp://15.165.147.160:1935/myapp/test");
            etUrl.setText("rtmp://15.165.147.160:1935/myapp/test");
            rtmpCamera1 = new RtmpCamera1(surfaceView, this);
            rtmpCamera1.setReTries(10);

            surfaceView.getHolder().addCallback(this);


            //채팅 기능을 하는 뷰의 연결작업 진행.
            edit1 = findViewById(R.id.editText);
            btn1 = findViewById(R.id.button);
            btn2 = findViewById(R.id.exit);
            container = findViewById(R.id.container);
            scroll = findViewById(R.id.scroll);


            /**
             * 닉네임 정보를 가지고 와서 접속을 실행하는 작업.
             */
            //채팅 연결 작업 진행
            Intent intent = getIntent();
            String nickName = intent.getStringExtra("Username");
            username = intent.getStringExtra("Username");
            Log.e("가지고온 유저 닉네임 정보 : ", nickName+"");
            if (nickName.length() > 0 && nickName != null) {
                //서버에 접속한다.
                pro = ProgressDialog.show(this, null, "접속중입니다");
                // 접속 스레드 가동
                ExampleRtmpActivity.ConnectionThread thread = new ExampleRtmpActivity.ConnectionThread();
                thread.start();
            }
            // 닉네임이 입력되지않을경우 다이얼로그창 띄운다.
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("닉네임을 입력해주세요");
                builder.setPositiveButton("확인", null);
                builder.show();
            }

            // 텍스트뷰의 객체를 생성
            TextView tv = new TextView(ExampleRtmpActivity.this);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            // 메세지의 시작 이름이 내 닉네임과 일치한다면
            String msg = nickName + "님 반갑습니다.";
//            if (msg.startsWith(user_nickname)) {
//                //tv.setBackgroundResource(R.drawable.me);
//
//            } else {
//                //tv.setBackgroundResource(R.drawable.you);
//
//            }
            tv.setText(msg);
            container.addView(tv);
            // 제일 하단으로 스크롤 한다
            scroll.fullScroll(View.FOCUS_DOWN);

        }

        @Override
        public void onConnectionSuccessRtmp() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ExampleRtmpActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onConnectionFailedRtmp(final String reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (rtmpCamera1.reTry(5000, reason)) {
                        Toast.makeText(ExampleRtmpActivity.this, "Retry", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(ExampleRtmpActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
                                .show();
                        rtmpCamera1.stopStream();
                        button.setText("방송시작");
                    }
                }
            });
        }

        @Override
        public void onNewBitrateRtmp(long bitrate) {

        }

        @Override
        public void onDisconnectRtmp() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ExampleRtmpActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onAuthErrorRtmp() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ExampleRtmpActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onAuthSuccessRtmp() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ExampleRtmpActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.b_start_stop:
                    if (!rtmpCamera1.isStreaming()) {
                        if (rtmpCamera1.isRecording()
                                || rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
                            button.setText("방송중단");
                            rtmpCamera1.startStream(etUrl.getText().toString());
                        } else {
                            Toast.makeText(this, "Error preparing stream, This device cant do it",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        button.setText("방송시작");
                        rtmpCamera1.stopStream();
                    }
                    break;
                case R.id.switch_camera:
                    try {
                        rtmpCamera1.switchCamera();
                    } catch (CameraOpenException e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.b_record:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        if (!rtmpCamera1.isRecording()) {
                            try {
                                if (!folder.exists()) {
                                    folder.mkdir();
                                }
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                                currentDateAndTime = sdf.format(new Date());
                                if (!rtmpCamera1.isStreaming()) {
                                    if (rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
                                        rtmpCamera1.startRecord(
                                                folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                                        bRecord.setText("방송중단");
                                        Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Error preparing stream, This device cant do it",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    rtmpCamera1.startRecord(
                                            folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                                    bRecord.setText("방송중단");
                                    Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                rtmpCamera1.stopRecord();
                                bRecord.setText("방송하기");
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            rtmpCamera1.stopRecord();
                            bRecord.setText("방송하기");
                            Toast.makeText(this,
                                    "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                                    Toast.LENGTH_SHORT).show();
                            currentDateAndTime = "";
                        }
                    } else {
                        Toast.makeText(this, "You need min JELLY_BEAN_MR2(API 18) for do it...",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            rtmpCamera1.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording()) {
                rtmpCamera1.stopRecord();
                bRecord.setText("녹화하기");
                Toast.makeText(this,
                        "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                        Toast.LENGTH_SHORT).show();
                currentDateAndTime = "";
            }
            if (rtmpCamera1.isStreaming()) {
                rtmpCamera1.stopStream();
                button.setText("방송하기");
            }
            rtmpCamera1.stopPreview();
        }


    //서버와 소켓을 활용한 채팅을 적용하기 위해 올려 놓은 메소드 실행.
    // 버튼과 연결된 메소드
    public void btnMethod(View v) {
        if (isConnect == false) {   //접속전
            //사용자가 입력한 닉네임을 받는다.
            String nickName = edit1.getText().toString();
            //스트르밍 전 방에서 보내온 닉네임 값을 전달한다.

            if (nickName.length() > 0 && nickName != null) {
                //서버에 접속한다.
                pro = ProgressDialog.show(this, null, "접속중입니다");
                // 접속 스레드 가동
                ExampleRtmpActivity.ConnectionThread thread = new ExampleRtmpActivity.ConnectionThread();
                thread.start();
            }
            // 닉네임이 입력되지않을경우 다이얼로그창 띄운다.
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("닉네임을 입력해주세요");
                builder.setPositiveButton("확인", null);
                builder.show();
            }
        } else {                  // 접속 후
            // 입력한 문자열을 가져온다.
            String msg = edit1.getText().toString();
            // 송신 스레드 가동
            ExampleRtmpActivity.SendToServerThread thread = new ExampleRtmpActivity.SendToServerThread(member_socket, msg);
            thread.start();
        }
    }


    // 서버접속 처리하는 스레드 클래스 - 안드로이드에서 네트워크 관련 동작은 항상
    // 메인스레드가 아닌 스레드에서 처리해야 한다.
    class ConnectionThread extends Thread {

        @Override
        public void run() {
            try {
                // 접속한다.
                final Socket socket = new Socket("15.165.147.160", 9000);
                member_socket = socket;
                // 미리 입력했던 닉네임을 서버로 전달한다.
                String nickName = username;
                //String nickName = edit1.getText().toString();
                user_nickname = username;     // 화자에 따라 말풍선을 바꿔주기위해
                // 스트림을 추출
                OutputStream os = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(os);
                // 닉네임을 송신한다.
                dos.writeUTF(nickName);
                // ProgressDialog 를 제거한다.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pro.dismiss();
                        edit1.setText("");
                        edit1.setHint("메세지 입력");
                        btn1.setText("전송");
                        // 접속 상태를 true로 셋팅한다.
                        isConnect = true;
                        // 메세지 수신을 위한 스레드 가동
                        isRunning = true;
                        ExampleRtmpActivity.MessageThread thread = new ExampleRtmpActivity.MessageThread(socket);
                        thread.start();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class MessageThread extends Thread {
        Socket socket;
        DataInputStream dis;

        public MessageThread(Socket socket) {
            try {
                this.socket = socket;
                InputStream is = socket.getInputStream();
                dis = new DataInputStream(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (isRunning) {
                    // 서버로부터 데이터를 수신받는다.
                    final String msg = dis.readUTF();
                    // 화면에 출력
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 텍스트뷰의 객체를 생성
                            TextView tv = new TextView(ExampleRtmpActivity.this);
                            tv.setTextColor(Color.BLACK);
                            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                            // 메세지의 시작 이름이 내 닉네임과 일치한다면
                            if (msg.startsWith(user_nickname)) {
                                //tv.setBackgroundResource(R.drawable.me);

                            } else {
                                //tv.setBackgroundResource(R.drawable.you);

                            }

                            tv.setText(msg);

                            container.addView(tv);
                            // 제일 하단으로 스크롤 한다
                            scroll.fullScroll(View.FOCUS_DOWN);

                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // 서버에 데이터를 전달하는 스레드
    class SendToServerThread extends Thread {
        Socket socket;
        String msg;
        DataOutputStream dos;

        public SendToServerThread(Socket socket, String msg) {
            try {
                this.socket = socket;
                this.msg = msg;
                OutputStream os = socket.getOutputStream();
                dos = new DataOutputStream(os);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // 서버로 데이터를 보낸다.
                dos.writeUTF(msg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        edit1.setText("");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        // 송신 스레드 가동
        //ExampleRtmpActivity.SendToServerThread thread = new ExampleRtmpActivity.SendToServerThread(member_socket, "나갓습니다.");
        //thread.start();

        super.onDestroy();
        try {

            //member_socket.close();
            //isRunning = false;

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

}