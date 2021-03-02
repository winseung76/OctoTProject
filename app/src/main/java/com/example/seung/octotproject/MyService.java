package com.example.seung.octotproject;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by USER on 2018-08-24.
 */
public class MyService extends Service implements Serializable{
    private final IBinder mBinder = new LocalBinder();
    MyApplication octotData=new MyApplication();
    private String update_date="";
    private boolean showAlert=false;
    private String pre_update_date="";
    MachineinfoRedaer machineinfoRedaer;

    class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "Service Created", Toast.LENGTH_SHORT).show();
        machineinfoRedaer= new MachineinfoRedaer("http://"+octotData.serverIP+"/machineinfo_data.php");
        machineinfoRedaer.start();

    }
    boolean getShowAlert(){
        return showAlert;
    }
    void setShowAlert(boolean bool){
        this.showAlert=bool;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("서비스 시작");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    class MachineinfoRedaer extends Thread{
        String urlstr;

        MachineinfoRedaer(String url){this.urlstr=url;}
        @Override
        public void run() {
            BufferedReader reader;
            JSONObject jObject;

            while(true) {
                try {
                    URL url = new URL(urlstr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    if (conn != null) {
                        conn.setConnectTimeout(5000);   //연결 timeout
                        conn.setRequestMethod("GET");   //데이터 전송 방식

                        conn.setDoInput(true);   //데이터 input 허용

                        int resCode = conn.getResponseCode();

                        if (resCode == HttpURLConnection.HTTP_OK) {

                            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String jsonstr;

                            while ((jsonstr = reader.readLine()) == null);

                            JSONArray jarray = new JSONArray(jsonstr);

                            jObject = jarray.getJSONObject(0);
                            update_date = jObject.getString("date");
                            if(pre_update_date=="")
                                pre_update_date=update_date;

                            if(!pre_update_date.equals(update_date)){
                                showAlert=true;
                                Intent popupIntent = new Intent(getApplicationContext(),AlertDialogActivity.class);

                                PendingIntent pie= PendingIntent.getActivity(getApplicationContext(), 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
                                pie.send();
                                pre_update_date=update_date;
                            }
                            //System.out.println("기계 정보 업데이트 날짜 : "+update_date);
                        }
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("SocketTimeoutException: " + e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(5000);
                }catch (Exception e){
                    break;
                }
            }
        }
    }

}