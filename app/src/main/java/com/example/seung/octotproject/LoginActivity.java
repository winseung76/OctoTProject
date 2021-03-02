package com.example.seung.octotproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoginActivity extends AppCompatActivity implements View.OnFocusChangeListener{

    Button login;
    EditText id,pwd;
    TextView alertmsg,sign_up;
    CheckBox checkbox;
    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor cursor;
    boolean checked=false;
    int recent_checked=0;
    String recent_id="";
    MyApplication octotData;
    Background background;
    String getId,getPwd;
    MyService myService;
    boolean isService = false; // 서비스 중인 확인용

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        System.out.println("LoginActivity의 onCreate()");

        Intent service_intent = new Intent(this,MyService.class); // 다음넘어갈 컴퍼넌트
        bindService(service_intent,conn, Context.BIND_AUTO_CREATE);

        octotData=(MyApplication)getApplication();
        login=(Button)findViewById(R.id.login);
        sign_up=findViewById(R.id.sign_up);
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });

        id=(EditText)findViewById(R.id.id);
        pwd=(EditText)findViewById(R.id.pwd);
        id.setOnFocusChangeListener(this);
        pwd.setOnFocusChangeListener(this);
        alertmsg=(TextView)findViewById(R.id.alertmsg);
        checkbox=(CheckBox)findViewById(R.id.checkbox);

        dbHelper=new DBHelper(this);
        try{
            db=dbHelper.getWritableDatabase();
        }catch(SQLException ex){
            db=dbHelper.getReadableDatabase();
        }
        db.execSQL("CREATE TABLE IF NOT EXISTS members(date TEXT,id TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS checkbox(date TEXT, checked INTEGER);");


        Cursor cursor2=db.rawQuery("SELECT checked FROM checkbox ORDER BY date DESC LIMIT 1",null);

        if(cursor2.moveToNext()) {
            recent_checked=cursor2.getInt(0);
            if(recent_checked==1){            //직전에 체크가 되어 있으면,
                checkbox.setChecked(true);    //체크가 된 상태로 설정
                checked=true;

                /* db에서 아이디 가져오기*/
                cursor=db.rawQuery("SELECT id FROM members ORDER BY date DESC LIMIT 1",null);
                if(cursor.moveToNext()) {
                    recent_id = cursor.getString(0);   //최근에 '아이디 저장'했던 아이디를 불러옴
                    id.setText(recent_id);  //id 에디트 텍스트에 표시
                    System.out.println("아이디 : "+recent_id);
                }

            }
        }
        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getId=id.getText().toString();
                getPwd=pwd.getText().toString();

                if(checked){
                    if (!recent_id.equals(getId)) {
                        db.execSQL("INSERT INTO members (date,id) VALUES(DATETIME('now','localtime'), '" + getId + "');");
                    }
                    db.execSQL("INSERT INTO checkbox (date,checked) VALUES(DATETIME('now','localtime'), " + "1);");
                }
                else{
                    db.execSQL("INSERT INTO checkbox (date,checked) VALUES(DATETIME('now','localtime'), " + "0);");
                }
                //회원 db 가져오기
                background=new Background("http://"+octotData.serverIP+"/memberdata.php",getId,getPwd);
                background.execute();
            }
        });
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){   //체크박스에 체크되있는 경우
                    checked=true;
                }

            }
        });
    }
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            v.setBackgroundResource(R.drawable.edittext_touchmode);
        }
        else{
            v.setBackgroundResource(R.drawable.login_design);
        }
    }
    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            MyService.LocalBinder mb = (MyService.LocalBinder) service;
            myService = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
            Toast.makeText(getApplicationContext(), "서비스 연결 해제", Toast.LENGTH_LONG).show();
        }
    };
    class Background extends AsyncTask<Void,Void,Void> {
        String urlstr,id,password;
        String result;

        Background(String urlstr,String id,String password){
            this.urlstr=urlstr;
            this.id=id;
            this.password=password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(result.equals("false")){
                alertmsg.setVisibility(View.VISIBLE);
                alertmsg.setText("아이디 혹은 비밀번호가 일치하지 않습니다.");
            }
            else {
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                result=result.substring(4);
                octotData.setMembername(result);
                startActivity(intent);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            result=post();
            System.out.println("result : "+result);

            return null;
        }

        public String loginData(){
            JSONObject jsonObject = new JSONObject();
            String jarray_str=null;
            try {
                jsonObject.put("id", id);
                jsonObject.put("pwd",password);

                String str = jsonObject.toString();

                JSONArray jArray = new JSONArray();

                jArray.put(jsonObject);

                jarray_str = jArray.toString();
                Log.e("JH", jarray_str);
            }catch(Exception e){ e.printStackTrace();}

            return jarray_str;
        }

        /* 입력한 아이디와 비밀번호를 json으로 php파일에 전송*/
        /* php파일은 입력한 아이디와 비밀번호가 회원 db에 있으면 true, 아니면 false를 echo */
        public String post(){
            InputStream inputStream = null;
            BufferedReader rd;

            String result = "";
            try {

                // 1. HttpClient 생성
                HttpClient httpclient = new DefaultHttpClient();

                // 2. PHP 서버 URL의 POST request를 만든다.
                HttpPost httpPost = new HttpPost(urlstr);

                // 3. JSONArray형태로 데이터를 만든다.
                // 4. JSONArray를 String 문자열로 변환한다.
                String json = loginData();

                // 5. StringEntity에 JSON 문자열을 UTF-8형태로 설정한다.
                StringEntity se = new StringEntity(json,"UTF-8");

                // 6. httpPost Entity 설정
                httpPost.setEntity(se);

                // 7. 서버에 전송을 하기 위해 headers 정보를 설정 한다.
                httpPost.setHeader("Accept", "application/json;");
                httpPost.setHeader("Content-type", "application/json;");
                httpPost.setHeader("Accept-Charset", "UTF-8");

                // 8. POST request를 실행한다.
                HttpResponse httpResponse = httpclient.execute(httpPost);

                // 9. 서버로 부터 응답 메세지를 받는다.
                inputStream = httpResponse.getEntity().getContent();
                rd = new BufferedReader(new InputStreamReader(inputStream));

                // 10. 수신한 응답 메세지의 inputstream을 string형태로 변환 한다.
                if(inputStream != null) {
                    String str;
                    result="";
                    while((str=rd.readLine())!=null) {
                        result +=str;
                    }
                }
                else
                    result = "Did not work!";

            } catch (Exception e) {
                Log.e("post()", e.getLocalizedMessage());
                result="networkproblem";
            }

            // 11. return result
            return result;
        }

    }

}



