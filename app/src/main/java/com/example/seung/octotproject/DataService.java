package com.example.seung.octotproject;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

public class DataService extends Service {

    private final IBinder mBinder = new DataService.LocalBinder();
    MyApplication octotdata;
    MyService myService=null;
    DataInput dataInput;
    final static String MY_ACTION = "MY_ACTION";
    Intent intent;

    boolean isService = false; // 서비스 중인 확인용

    class LocalBinder extends Binder {
        DataService getService() {
            return DataService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("DataService의 onDestroy()");
        unbindService(conn);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("DaraService의 onCreate()");
        intent = new Intent(this,MyService.class); // 다음넘어갈 컴퍼넌트
        bindService(intent,conn, Context.BIND_AUTO_CREATE);

        octotdata=(MyApplication)getApplication();
        dataInput=new DataInput();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            dataInput.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        } else
            dataInput.execute();
        Toast.makeText(getApplicationContext(), "Service Created", Toast.LENGTH_SHORT).show();

    }
    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            MyService.LocalBinder mb = (MyService.LocalBinder) service;
            myService = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            System.out.println("myService : "+myService);
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;
            //Toast.makeText(getApplicationContext(), "서비스 연결 성공", Toast.LENGTH_LONG).show();
        }

        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
            Toast.makeText(getApplicationContext(), "서비스 연결 해제", Toast.LENGTH_LONG).show();
        }
    };
    class DataInput extends AsyncTask<Void,HashMap<String,Float>,Void> {
        private int port=-1;
        private String date;
        private boolean stop=false;            //데이터를 읽어들어온게 stop상태인지 아닌지
        private boolean exception=false;
        private boolean socket_connect=false;  //소켓 연결이 된 상태인지, 아닌 상태인지를 알려줌
        private String jsonstr=null;
        Socket socket=null;

        public void setStop(boolean bool){
            this.stop=bool;
        }
        public void reset(){
            this.stop=false;
            this.exception=false;
            this.socket_connect=false;
        }
        public void setPort(int port){
            this.port=port;
            socket_connect=false;
            Intent intent = new Intent();
            intent.setAction(MY_ACTION);

            intent.putExtra("state","ready");
            sendBroadcast(intent);
        }
        @Override
        protected void onPreExecute() {
            System.out.println("스레드 시작됨");
            /*
            Intent intent = new Intent();
            intent.setAction(MY_ACTION);

            intent.putExtra("state","ready");
            sendBroadcast(intent);
            */

        }
        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, Float> hm=null;

            while (true) {

                System.out.println("doInBackground 진입!");
                if (isCancelled()) break;

                /*기계정보를 변경하고 다시 재진입하는 경우,
                myService가 계속 null이 되어 무한반복을 빠져나가지 못함
                 */
                System.out.println("myService2 : "+myService);
                while (myService == null) {
                    try {
                        //sleep이 없는게 원인이었음.
                        Thread.sleep(500);
                    }catch (InterruptedException e){}
                }

                //기계정보가 변경되어 서비스가 기계정보 변경 대화상자를 띄우는 경우 스레드 종료
                System.out.println("myService.getShowAlert() : "+myService.getShowAlert());

                if (myService.getShowAlert()) {
                    myService.setShowAlert(false);
                    System.out.println("거쳤음");
                    break;
                }
                /*
                if (startAnimation != null)
                    startAnimation.cancel();
                    */

                try {
                    System.out.println(socket_connect);
                    System.out.println("exception : "+exception);

                    //socket 연결이 되지 않은 상태인 경우 소켓 연결 계속 재시도
                    if(!socket_connect) {
                        System.out.println("소켓 연결 시도");
                        if(socket!=null)
                            socket.close();
                        socket = new Socket();

                        while (port==-1) {
                            try {
                                Thread.sleep(500);
                            }catch (InterruptedException e){}
                        }
                        //System.out.println(port);
                        //소켓 연결 timeout을 5초로 설정. 5초가 지나면 연결 시도를 그만함
                        try {
                            socket.connect(new InetSocketAddress(octotdata.serverIP, port), 5000);
                        }catch(Exception e){
                            exception=true;
                        }

                        if(socket.isConnected()){
                            System.out.println("소켓 연결 성공!");
                            socket_connect=true;
                            exception=false;
                        }
                        //소켓 연결 성공시, socket_connect을 참으로 변경
                        /*
                        if(socket!=null){
                            System.out.println("소켓 연결 성공!");
                            socket_connect=true;
                            exception=false;
                        }
                        */
                    }


                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    jsonstr = reader.readLine();
                    //System.out.println(jsonstr);
                    out.println("ok");

                    //받아온 str이 false이면 새로 들어온 데이터가 없음을 의미
                    if (jsonstr.equals("false")) {
                        System.out.println("소켓 연결은 성공했으나, 들어오는 데이터가 없음!");
                        exception = true;
                    }

                    //jsonstr이 null이 아닌 경우에만 그 다음 작업을 진행하도록 함
                    else if (jsonstr != null) {
                        hm = setHashMap(jsonstr);
                    }
                    else
                        exception = true;

                }catch (Exception e){
                    e.printStackTrace();
                    exception=true;
                }
                publishProgress(hm);
                try {
                    Thread.sleep(500);
                }catch (InterruptedException e) {
                }
            }
            System.out.println("dataInput 스레드 종료!");
            //loop_out=true;

            return null;
        }

        @Override
        protected void onProgressUpdate(HashMap<String, Float>... values) {

            Intent intent = new Intent();
            intent.setAction(MY_ACTION);

            intent.putExtra("data", values[0]);
            intent.putExtra("date",date);

            System.out.println("onProgressUpdate()");
            //예외가 발생하였으며, 그 전까지 계속 데이터를 받아왔던 경우
            if(!stop && exception){
                //멈춤상태로 변환한다.
                System.out.println("멈춤 상태로 변환");
                intent.putExtra("state","stop");
                sendBroadcast(intent);
                exception=false;
                stop=true;
                socket_connect=false;
            }
            //예외 발생이 해결되었으나, 그 전까지 멈춤상태였던 경우
            else if(stop && !exception){
                //다시 새로운 데이터를 받아온다.
                intent.putExtra("state","updating");
                sendBroadcast(intent);
                stop=false;
            }
            else if(!stop && values[0]==null){
                intent.putExtra("state","stop");
                sendBroadcast(intent);
                stop=true;
                socket_connect=false;
            }
            else if(!stop && !exception){
                intent.putExtra("state","updating");
                sendBroadcast(intent);
            }


            //System.out.println("개수 : "+dbOutputs.size());
            /*
            for(int i=0;i<dbOutputs.size();i++) {
                dbOutputs.get(i).checkValueChanged(values[0]);
            }
            */

            //dialogManager.stopProgressDialog();
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            System.out.println("스레드 종료됨");

            Intent intent = new Intent();
            intent.setAction(MY_ACTION);

            //intent.putExtra("DATAPASSED", values[0]);
            intent.putExtra("date",date);
            intent.putExtra("state","stop");
            sendBroadcast(intent);

            myService.setShowAlert(false);
            try {
                if(socket!=null) {
                    socket.close();
                }
            }catch (IOException e){}

        }
        /*jsonstr을 받아서 JSONObject를 통해 hashmap으로 파싱하는 메소드
         */
        public HashMap<String, Float> setHashMap(String jsonstr){
            HashMap<String, Float> hm = new HashMap<>();
            String[] type={"S0","T0","R0","S1","T1","R1","S2","T2","R2","S3","T3","R3","T4","T5","T6","T7"};
            String[] toggle_type={"L4","L5","L6","L7"};

            try {
                JSONObject jObject = new JSONObject(jsonstr);

                date = jObject.getString("date");
                //System.out.println(date);

               /*
               JSONObject를 hashmap으로 변경하여 저장
               파싱과정을 거쳐서 가져다 쓰기 편한 float형으로 만들기 위해서
                */
                for (int i = 0; i < 16; i++)
                    hm.put(type[i], Float.parseFloat(jObject.getString(type[i])) / 10);
                for (int i = 0; i < toggle_type.length; i++)
                    hm.put(toggle_type[i], Float.parseFloat(jObject.getString(toggle_type[i])));

                hm.put("OK", Float.parseFloat(jObject.getString("OK")));
                hm.put("FL", Float.parseFloat(jObject.getString("FL")));
            }catch (JSONException e){
                e.printStackTrace();
            }
            return hm;

        }
    }
}
