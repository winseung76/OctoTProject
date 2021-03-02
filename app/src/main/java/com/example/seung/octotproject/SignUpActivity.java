package com.example.seung.octotproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity implements View.OnFocusChangeListener{

    EditText name,id,password,password2,email,phone;
    Drawable checkicon;
    Button check_overlap,register_btn;
    TextView pwdhint;
    MyApplication octotdata;
    FormCheker formCheker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        formCheker=new FormCheker();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            formCheker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        }
        else
            formCheker.execute();
        octotdata=(MyApplication)getApplication();
        checkicon = getApplicationContext().getResources().getDrawable( R.drawable.check );
        check_overlap=findViewById(R.id.check_overlap);
        register_btn=findViewById(R.id.register_btn);
        pwdhint=findViewById(R.id.pwdhint);
        name=findViewById(R.id.name);
        id=findViewById(R.id.id);
        password=findViewById(R.id.password);
        password2=findViewById(R.id.password2);
        email=findViewById(R.id.email);
        phone=findViewById(R.id.phone);

        name.setOnFocusChangeListener(this);
        id.setOnFocusChangeListener(this);
        password.setOnFocusChangeListener(this);
        password2.setOnFocusChangeListener(this);
        email.setOnFocusChangeListener(this);
        phone.setOnFocusChangeListener(this);

        id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //이미 중복체크 한경우
                if(formCheker.getId_ok()){
                    id.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                }
            }
        });
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(name.getText().toString().length()>1) {
                    name.setCompoundDrawablesWithIntrinsicBounds(null, null, checkicon, null);
                    formCheker.setName_ok(true);
                }
                else {
                    name.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    formCheker.setName_ok(false);
                }
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pwd=password.getText().toString();
                String pwd2=password2.getText().toString();
                if(pwd.length()>4){
                    password.setCompoundDrawablesWithIntrinsicBounds(null,null,checkicon,null);
                    pwdhint.setVisibility(View.GONE);
                    formCheker.setPwd_ok(true);
                }
                else {
                    password.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    pwdhint.setVisibility(View.VISIBLE);
                    formCheker.setPwd_ok(false);
                }

                if(pwd.length()>4 && pwd2.equals(pwd)){
                    password2.setCompoundDrawablesWithIntrinsicBounds(null,null,checkicon,null);
                    formCheker.setPwd2_ok(true);
                }
                else{
                    password2.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                    formCheker.setPwd2_ok(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String pwd=password.getText().toString();
                String pwd2=password2.getText().toString();
                if(pwd.length()>4){
                    password.setCompoundDrawablesWithIntrinsicBounds(null,null,checkicon,null);
                    pwdhint.setVisibility(View.GONE);
                    formCheker.setPwd_ok(true);
                }
                else {
                    password.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    pwdhint.setVisibility(View.VISIBLE);
                    formCheker.setPwd_ok(false);
                }

                if(pwd.length()>4 && pwd2.equals(pwd)){
                    password2.setCompoundDrawablesWithIntrinsicBounds(null,null,checkicon,null);
                    formCheker.setPwd2_ok(true);
                }
                else{
                    password2.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                    formCheker.setPwd2_ok(false);
                }
            }
        });
        password2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pwd=password.getText().toString();
                String pwd2=password2.getText().toString();
                if(pwd.length()>4 && pwd2.equals(pwd)){
                    password2.setCompoundDrawablesWithIntrinsicBounds(null,null,checkicon,null);
                    formCheker.setPwd2_ok(true);
                }
                else{
                    password2.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                    formCheker.setPwd2_ok(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String pwd=password.getText().toString();
                String pwd2=password2.getText().toString();
                if(pwd.length()>4 && pwd2.equals(pwd)){
                    password2.setCompoundDrawablesWithIntrinsicBounds(null,null,checkicon,null);
                }
                else{
                    password2.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                }
            }
        });
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String email_str=email.getText().toString();
                if(android.util.Patterns.EMAIL_ADDRESS.matcher(email_str).matches()){
                    email.setCompoundDrawablesWithIntrinsicBounds(null,null,checkicon,null);
                    formCheker.setEmail_ok(true);
                }
                else{
                    email.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                    formCheker.setEmail_ok(false);
                }

            }
        });
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String phone_num=phone.getText().toString();
                if(Pattern.matches("(01[016789])(\\d{3,4})(\\d{4})", phone_num)){
                    phone.setCompoundDrawablesWithIntrinsicBounds(null,null,checkicon,null);
                    formCheker.setPhone_ok(true);
                }
                else {
                    phone.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    formCheker.setPhone_ok(false);
                }
            }
        });
        check_overlap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    new MemberIdChecker(id.getText().toString()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
                }
                else
                    new MemberIdChecker(id.getText().toString()).execute();

            }
        });
        register_btn.setEnabled(false);
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formCheker.cancel(true);
                MemberRegister memberRegister= new MemberRegister(id.getText().toString(), password.getText().toString(),
                        name.getText().toString(),email.getText().toString(),phone.getText().toString(),
                        "http://" + octotdata.serverIP + "/register_member_mobile.php");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    memberRegister.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
                }
                else
                    memberRegister.execute();

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
    class MemberIdChecker extends AsyncTask<Void,Void,String>{

        final String urlstr="http://"+octotdata.serverIP+"/check_Id_overlap.php";
        String idstr="";

        MemberIdChecker(String id){
            this.idstr=id;
        }

        @Override
        protected String doInBackground(Void... voids) {

            InputStream inputStream = null;
            BufferedReader rd;

            String result="";
            try {

                // 1. HttpClient 생성
                HttpClient httpclient = new DefaultHttpClient();

                // 2. PHP 서버 URL의 POST request를 만든다.
                HttpPost httpPost = new HttpPost(urlstr);

                // 3. JSONArray형태로 데이터를 만든다.
                // 4. JSONArray를 String 문자열로 변환한다.
                String json = getJsonStr();

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
                    result=rd.readLine();
                }

            } catch (Exception e) {
                Log.e("post()", e.getLocalizedMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            //System.out.println(s);
            if(s.contains("false")){
                showOKDialog();
                id.setText(idstr);
                id.setCompoundDrawablesWithIntrinsicBounds(null,null,checkicon,null);
                formCheker.setId_ok(true);
            }
            else if(s.contains("true")){
                showNotOKDialog();
                id.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                formCheker.setId_ok(false);
            }
            //아이디 입력칸이 빈칸인데 중복확인 버튼을 누른경우
            else if(s.contains("no")){
                showAskDialog();
                id.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                formCheker.setId_ok(false);
            }
        }

        public String getJsonStr(){
            JSONObject jsonObject = new JSONObject();
            String jarray_str=null;
            try {
                jsonObject.put("id",idstr);

                JSONArray jArray = new JSONArray();

                jArray.put(jsonObject);
                jarray_str = jArray.toString();
            }catch(Exception e){ e.printStackTrace();}

            return jarray_str;
        }
        public void showOKDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
            builder.setTitle("아이디 중복 체크");
            builder.setMessage("사용 가능한 아이디입니다.");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        public void showNotOKDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
            builder.setTitle("아이디 중복 체크");
            builder.setMessage("이미 존재하는 아이디입니다.");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        public void showAskDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
            builder.setTitle("아이디 중복 체크");
            builder.setMessage("아이디를 입력해주세요.");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
    class MemberRegister extends AsyncTask<Void,Void,String>{

        HttpClient client;
        HttpPost httpPost;
        String urlstr;
        String id,password,name,email,phone;
        CopyOnWriteArrayList<NameValuePair> post = new CopyOnWriteArrayList<>();

        MemberRegister(String id,String password,String name,String email, String phone,String urlstr) {
            this.urlstr=urlstr;
            this.id=id;
            this.password=password;
            this.name=name;
            this.email=email;
            this.phone=phone;
        }

        @Override
        protected void onPreExecute() {
            post.add(new BasicNameValuePair("id", ""));
            post.add(new BasicNameValuePair("password", ""));
            post.add(new BasicNameValuePair("name", ""));
            post.add(new BasicNameValuePair("email", ""));
            post.add(new BasicNameValuePair("phone", ""));
        }

        @Override
        protected String doInBackground(Void... voids) {

            InputStream inputStream = null;
            BufferedReader rd=null;
            String result=null;
            try {
                // 연결 HttpClient 객체 생성
                client = new DefaultHttpClient();

                // 객체 연결 설정 부분, 연결 최대시간 등등
                HttpParams params = client.getParams();
                HttpConnectionParams.setConnectionTimeout(params, 5000);
                HttpConnectionParams.setSoTimeout(params, 5000);

                // Post객체 생성
                httpPost = new HttpPost(urlstr);

                post.set(0, new BasicNameValuePair("id", id));
                post.set(1, new BasicNameValuePair("password", password));
                post.set(2, new BasicNameValuePair("name", name));
                post.set(3, new BasicNameValuePair("email", email));
                post.set(4, new BasicNameValuePair("phone", phone));

                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(post, "UTF-8");
                httpPost.setEntity(entity);
                HttpResponse httpResponse = client.execute(httpPost);

                System.out.println("post : " + post);

                // 9. 서버로 부터 응답 메세지를 받는다.
                inputStream = httpResponse.getEntity().getContent();
                rd = new BufferedReader(new InputStreamReader(inputStream));
                result=rd.readLine();

            }catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            if(result.contains("1")){
                showRegisterOKDialog();
            }
            else {
                showRegisterFLDialog();
            }

        }
        public void showRegisterOKDialog(){
            AlertDialog.Builder builder=new AlertDialog.Builder(SignUpActivity.this);
            builder.setTitle("등록 처리");
            builder.setMessage("등록이 완료되었습니다.");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(intent);
                }
            });
            AlertDialog dialog=builder.create();
            dialog.show();
        }
        public void showRegisterFLDialog(){
            AlertDialog.Builder builder=new AlertDialog.Builder(SignUpActivity.this);
            builder.setTitle("등록 처리");
            builder.setMessage("등록 실패!다시 시도해주세요.");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog=builder.create();
            dialog.show();
        }
    }
    class FormCheker extends AsyncTask<Void,Void,Void>{
        private boolean id_ok=false,pwd_ok=false,pwd2_ok=false,name_ok=false,email_ok=false,phone_ok=false;

        @Override
        protected Void doInBackground(Void... voids) {

            while(true){
                try {
                    if(isCancelled())break;
                    publishProgress();
                    Thread.sleep(2000);
                }catch (Exception e){

                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if(isPermission()){
                register_btn.setEnabled(true);
            }
            else
                register_btn.setEnabled(false);
        }

        public boolean isPermission(){

            return id_ok && pwd_ok && pwd2_ok && name_ok && email_ok && phone_ok;
        }

        public void setId_ok(boolean id_ok) {
            this.id_ok = id_ok;
        }
        public boolean getId_ok(){return this.id_ok;}

        public void setPwd_ok(boolean pwd_ok) {
            this.pwd_ok = pwd_ok;
        }

        public void setPwd2_ok(boolean pwd2_ok) {
            this.pwd2_ok = pwd2_ok;
        }

        public void setName_ok(boolean name_ok) {
            this.name_ok = name_ok;
        }

        public void setEmail_ok(boolean email_ok) {
            this.email_ok = email_ok;
        }

        public void setPhone_ok(boolean phone_ok) {
            this.phone_ok = phone_ok;
        }
    }
}
