package org.techtown.consolenuri.liveStreaming;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
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

import com.pedro.vlc.VlcListener;
import com.pedro.vlc.VlcVideoLibrary;

import org.techtown.consolenuri.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

//버퍼 스트림 연결시키기

public class StreamActivity extends AppCompatActivity implements VlcListener, View.OnClickListener {

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
    /////////////



    //프로그래스 다이얼로그
    private ProgressDialog progressDialog; //어느페이지에서든 쓸 수 있도록 프로그레스바를 준비.

    private VlcVideoLibrary vlcVideoLibrary;
    private Button bStartStop;
    private EditText etEndpoint;

    private String[] options = new String[]{":fullscreen"}; //여기서 풀스트린 세팅 되어져있는데?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_stream);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        bStartStop = (Button) findViewById(R.id.b_start_stop);
        bStartStop.setOnClickListener(this);
        etEndpoint = (EditText) findViewById(R.id.et_endpoint);
        etEndpoint.setText("rtmp://15.165.147.160:1935/myapp/test");

        vlcVideoLibrary = new VlcVideoLibrary(this, this, surfaceView);
        vlcVideoLibrary.setOptions(Arrays.asList(options));

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
            StreamActivity.ConnectionThread thread = new StreamActivity.ConnectionThread();
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
        TextView tv = new TextView(StreamActivity.this);
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

        // 약 0.5초간의 딜레이 값을 주고 해당 방송 스트리밍을 실행 하도록 하자.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bStartStop.callOnClick();
            }
        }, 500);


    }

    @Override
    public void onResume(){
        super.onResume();
        //해당 부분이 비디오를 재생하는 기능 이다.
        //vlcVideoLibrary.play(etEndpoint.getText().toString()); //조금만 딜레이를 주면, 옵션이 적용 되어지지 않을까??
    }

    @Override
    public void onComplete() {
        Log.e("송출 시작 로그 : ","송출시작");

        loadingEnd();
        //Toast.makeText(this, "Playing", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError() {
        Toast.makeText(this, "Error, make sure your endpoint is correct", Toast.LENGTH_SHORT).show();
        vlcVideoLibrary.stop();
        bStartStop.setText("방송시청하기");
    }




    @Override
    public void onClick(View view) {
        if (!vlcVideoLibrary.isPlaying()) {
            loading();
            vlcVideoLibrary.play(etEndpoint.getText().toString());
            bStartStop.setText("방송중단하기");
        } else {
            vlcVideoLibrary.stop();
            bStartStop.setText("방송시청하기");
        }
    }


    //로딩바 생성하기
    public void loading() {
        //로딩
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(StreamActivity.this);
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
                        if(progressDialog!=null)
                        progressDialog.dismiss();
                    }
                }, 0);
    }


    //서버와 소켓을 활용한 채팅을 적용하기 위해 올려 놓은 메소드 실행.
    // 버튼과 연결된 메소드
    public void btnMethod(View v) {
        if (isConnect == false) {   //접속전
            //사용자가 입력한 닉네임을 받는다.
            String nickName = edit1.getText().toString();
            if (nickName.length() > 0 && nickName != null) {
                //서버에 접속한다.
                pro = ProgressDialog.show(this, null, "접속중입니다");
                // 접속 스레드 가동
                StreamActivity.ConnectionThread thread = new StreamActivity.ConnectionThread();
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
            StreamActivity.SendToServerThread thread = new StreamActivity.SendToServerThread(member_socket, msg);
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
                        StreamActivity.MessageThread thread = new StreamActivity.MessageThread(socket);
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
                            TextView tv = new TextView(StreamActivity.this);
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
        StreamActivity.SendToServerThread thread = new StreamActivity.SendToServerThread(member_socket, "나갓습니다.");
        thread.start();

        super.onDestroy();
        try {

            //member_socket.close();
            //isRunning = false;

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

}
