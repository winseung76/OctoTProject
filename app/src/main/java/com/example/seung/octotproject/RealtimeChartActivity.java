package com.example.seung.octotproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class RealtimeChartActivity extends AppCompatActivity {
    MyService myService;
    boolean isService = false; // 서비스 중인 확인용
    ChartDataInput chartDataInput;
    MyApplication octotdata;
    int machineNum;
    DialogManager dialogManager;
    Chart chart;
    String sensorname;
    TextView machinename;
    //String machineName,groupName,factory;
    Machine machineinfo;
    DataInput dataInput;
    MyReceiver myReceiver;
    DataService dataService;

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("onStart()");
        dataInput=new DataInput(machineinfo.getPort());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy()");
        if(chartDataInput!=null) {
            chartDataInput.cancel(true);
            chartDataInput = null;
        }
        if(dataInput!=null) {
            dataInput.cancel(true);
            dataInput = null;
        }
        unbindService(conn);
        unbindService(conn2);
        unregisterReceiver(myReceiver);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_chart);

        octotdata=(MyApplication)getApplication();
        dialogManager=new DialogManager();
        LineChart lineChart=findViewById(R.id.linechart);
        machinename=findViewById(R.id.machinename);
        chart=new Chart();
        chart.chartSettings(lineChart);

        Intent intent = new Intent(this,MyService.class); // 다음넘어갈 컴퍼넌트
        bindService(intent,conn, Context.BIND_AUTO_CREATE);

        Intent intent2=getIntent();
        machineinfo=(Machine)intent2.getSerializableExtra("machineInfo");
        String index=intent2.getStringExtra("index");

        chart.setT_type("T"+index);

        if(Integer.parseInt(index)<4)
            chart.setS_type("S"+index);
        else
            chart.setS_type(null);
        sensorname=machineinfo.getSensor(Integer.parseInt(index));
        machinename.setText(machineinfo.getFactory()+"  -  "+machineinfo.getGroup()+"  -  "+machineinfo.getMachinename());

        myReceiver=new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
        Intent ds_intent = new Intent(this, DataService.class);
        bindService(ds_intent, conn2, Context.BIND_AUTO_CREATE);


        /*
        chartDataInput=new ChartDataInput("http://" + octotdata.serverIP + "/octotdata.php");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            chartDataInput.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        }
        else
            chartDataInput.execute();
         */



    }
    /*broadcastReceiver을 이용하여서 서비스에서 소켓통신을 통해 받아온
 octot데이터를 인텐트를 통해 읽어온다.
  */
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            HashMap<String,Float> hm=(HashMap<String, Float>) arg1.getSerializableExtra("data");
            String date=arg1.getStringExtra("date");
            System.out.println("realtime : "+hm);
            setEntryForChart(hm,date);

        }
    }
    public void setEntryForChart(HashMap<String, Float> hm,String date){
        float T_data,S_data;

        //T_data=hm.get(chart.getT_type())/10;
        T_data=Float.parseFloat(String.format("%.1f",hm.get(chart.getT_type())));
        if(chart.S_type!=null) {
            S_data=Float.parseFloat(String.format("%.1f",hm.get(chart.getS_type())));
            //S_data = hm.get(chart.getS_type()) / 10;
        }
        else{
            S_data=-100;
        }
        String data_new_time=date.split(" ")[1];
        System.out.println("T_data : "+T_data+", S_data : "+S_data);
        chart.addEntry(S_data,T_data,data_new_time);
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
    ServiceConnection conn2 = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            DataService.LocalBinder mb = (DataService.LocalBinder) service;
            dataService = mb.getService(); // 서비스가 제공하는 메소드 호출하여
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
    class DataInput extends AsyncTask<Void,HashMap<String,Float>,Void>{
        private int port;
        private String date;
        private boolean stop=false;            //데이터를 읽어들어온게 stop상태인지 아닌지
        private boolean exception=false;
        private boolean socket_connect=false;  //소켓 연결이 된 상태인지, 아닌 상태인지를 알려줌
        private boolean loop_out=false;
        private String jsonstr=null;
        Socket socket=null;

        DataInput(int port){
            this.port=port;
        }
        @Override
        protected void onPreExecute() {
            System.out.println("스레드 시작됨");
            //setStateReady();
            dialogManager.showProgressDialog();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            HashMap<String, Float> hm=null;

            while (true) {

                if (isCancelled()) break;

                while (myService == null) ;
                //기계정보가 변경되어 서비스가 기계정보 변경 대화상자를 띄우는 경우 스레드 종료
                System.out.println("myService.getShowAlert() : "+myService.getShowAlert());
                    /*
                    if (myService.getShowAlert()) {
                        myService.setShowAlert(false);
                        System.out.println("거쳤음");
                        break;
                    }
                    */

                try {
                    System.out.println(socket_connect);

                    //socket 연결이 되지 않은 상태인 경우 소켓 연결 계속 재시도
                    if(!socket_connect) {
                        System.out.println("소켓 연결 시도");
                        if(socket!=null){
                            socket.close();
                        }
                        socket = new Socket();
                        //소켓 연결 timeout을 5초로 설정. 5초가 지나면 연결 시도를 그만함
                        socket.connect(new InetSocketAddress("172.30.1.51", port), 5000);
                        //System.out.println("RealtimeChartActivity : "+socket.getLocalSocketAddress());

                        //소켓 연결 성공시, socket_connect을 참으로 변경
                        if(socket!=null){
                            System.out.println("소켓 연결 성공!");
                            socket_connect=true;
                            exception=false;
                        }
                    }
                        /*
                        if(socket.isClosed())
                            socket_connect=false;
                            */

                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    jsonstr = reader.readLine();
                    System.out.println(jsonstr);
                    out.println("ok");

                    //받아온 str이 false이면 새로 들어온 데이터가 없음을 의미
                    if(jsonstr.equals("false")){
                        System.out.println("소켓 연결은 성공했으나, 들어오는 데이터가 없음!");
                        exception=true;
                    }

                    //jsonstr이 null이 아닌 경우에만 그 다음 작업을 진행하도록 함
                    else if (jsonstr != null) {
                        hm=setHashMap(jsonstr);

                    } else
                        exception=true;


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
            loop_out=true;

            return null;
        }

        @Override
        protected void onProgressUpdate(HashMap<String, Float>... values) {

            //예외가 발생한 경우, dialog를 띄운다
            if(!stop && exception){
                dialogManager.showAlertDialog(date);
                //setStateStop();
                exception=false;
                stop=true;
                socket_connect=false;
            }
            else if(stop && !exception){
                dialogManager.stopAlertDialog();
               // setStateUpdating();
                //changeValue(values[0]);
                setEntryForChart(values[0]);
                stop=false;
            }
            else if(!stop && values[0]==null){
                dialogManager.showAlertDialog(date);
                //setStateStop();
                stop=true;
                socket_connect=false;
            }
            else if(!stop && !exception){
                dialogManager.stopAlertDialog();
                //setStateUpdating();
                //changeValue(values[0]);
                setEntryForChart(values[0]);
            }


            //System.out.println("개수 : "+dbOutputs.size());

            dialogManager.stopProgressDialog();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            System.out.println("스레드 종료됨");
            //setStateStop();
            dialogManager.showAlertDialog(date);
            dialogManager.stopProgressDialog();
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
        public void setEntryForChart(HashMap<String, Float> hm){
            float T_data,S_data;

            T_data=hm.get(chart.getT_type())/10;
            if(chart.S_type!=null) {
                S_data = hm.get(chart.getS_type()) / 10;
            }
            else{
                S_data=-100;
            }
            String data_new_time=date.split(" ")[1];
            chart.addEntry(S_data,T_data,data_new_time);
        }

    }
    class ChartDataInput extends AsyncTask<Void, HashMap<String, Float>, Void> {

        private HashMap<String, Float> hm;
        private String urlstr = null;
        private String date,pre_date="";
        private boolean stop=false;
        private boolean exception=false;
        private boolean socketexception=false;
        private String[] array=new String[octotdata.getMachineCount()];
        private String jsonstr;

        public ChartDataInput(String url) { urlstr = url; }

        @Override
        protected void onPreExecute() {
            dialogManager.showProgressDialog();
        }
        @Override
        protected void onProgressUpdate(HashMap<String, Float>... values) {


            //예외가 발생하거나 json스트링을 받아들이지 못하는경우
            if(exception || jsonstr==null){
                dialogManager.showAlertDialog(date);
                exception=false;
            }

            else {
                //새롭게 받아들인 데이터의 날짜가 전의 날짜와 다를 경우
                if(!date.equals(pre_date)) {

                    dialogManager.stopAlertDialog();
                    pre_date=date;
                    stop=false;
                }
                //새롭게 받아들인 데이터의 날짜가 전의 날짜와 같을 경우
                else if(date.equals(pre_date)){
                    if(!stop) {
                        dialogManager.showAlertDialog(date);
                        stop=true;
                    }
                }
            }
            //System.out.println("개수 : "+dbOutputs.size());
            dialogManager.stopProgressDialog();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            System.out.println("스레드 종료됨");

            if(socketexception){
                Intent intent=new Intent(getApplicationContext(),StartActivity.class);
                startActivity(intent);
            }
        }

        @Override
        protected Void doInBackground(Void[] objects) {

            while (true) {

                getOctoTData();

                /*
                asynctask에서 doInBackground()가 무한루프 실행 시,
                액티비티를 종료할 때 스레드를 종료시키기 위해 cancel(true)를 사용해도 스레드가 종료되지 않음.
                ->  doInBackground()의 무한루프 안에 if(isCancelled()) break;를 넣어서
                    cancel(true) 호출 시에 무한루프를 빠져나오도록 해야한다.
                */
                if (isCancelled()) break;

                while(myService==null);

                //기계정보가 변경되어 서비스가 기계정보 변경 대화상자를 띄우는 경우 스레드 종료
                if(myService.getShowAlert()){
                    myService.setShowAlert(false);
                    break;
                }

                if(socketexception)break; //서버에 접속이 안되는 경우(SocketException) 스레드 종료

                publishProgress(hm);
                try {
                    Thread.sleep(5000);
                }catch (InterruptedException e){}
            }
            return null;
        }


        protected void getOctoTData() {
            BufferedReader reader;
            JSONObject jObject=null;

            try {
                URL url = new URL(urlstr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                if (conn != null) {
                    conn.setConnectTimeout(5000);   //연결 timeout
                    conn.setRequestMethod("GET");   //데이터 전송 방식

                    conn.setDoInput(true);   //데이터 input 허용

                    int resCode=-100;
                    try {
                        resCode = conn.getResponseCode();
                    } catch (SocketTimeoutException e) {
                        socketexception = true;
                    }

                    if (resCode == HttpURLConnection.HTTP_OK) {

                        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        hm = new HashMap<>();

                        reader.readLine();    //<meta>태그 버리기

                        int index = 0;
                        while ((jsonstr = reader.readLine()) != null) {
                            array[index++] = jsonstr;
                        }

                        jsonstr = array[machineNum - 1];
                        //System.out.println("jsonstr : " + jsonstr);

                        JSONArray jarray = new JSONArray(jsonstr);

                        jObject = jarray.getJSONObject(0);
                        date = jObject.getString("date");
                        System.out.println(date);

                        /* 직전에 받아온 데이터의 날짜가
                        새로 받아온 데이터의 날짜와 다른 경우에만 그래프를 그림*/
                        float S_data,T_data;
                        if(!date.equals(pre_date)){
                            if(chart.S_type!=null) {
                                S_data = Float.parseFloat(jObject.getString(chart.getS_type())) / 10;
                            }
                            else{
                                S_data=-100;
                            }
                            T_data=Float.parseFloat(jObject.getString(chart.getT_type()))/10;
                            String data_new_time=date.split(" ")[1];
                            chart.addEntry(S_data,T_data,data_new_time);
                        }

                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                exception=true;
                //dialogManager.stopProgressDialog();
            }
        }


    }
    class Chart {
        ArrayList<String> x=new ArrayList<>();  //x축 데이터
        private String S_type,T_type;
        int color=Color.parseColor("#FF0000");
        LineChart lineChart=null;

        /* chart value 클릭 시 나타나는 markerview */
        MarkerView mv=new MarkerView(getApplicationContext(),R.layout.markerview) {
            TextView tvContent=(TextView)findViewById(R.id.tvContent);

            @Override
            public void refreshContent(Entry e, Highlight highlight) {

                tvContent.setText("time : "+x.get(e.getXIndex())+"\nvalue : "+e.getVal());
            }

            @Override
            public int getXOffset(float xpos) {

                // this will center the marker-view horizontally
                int min_offset = 130;
                if (xpos < min_offset)
                    return 0;

                WindowManager wm = (WindowManager)RealtimeChartActivity.this.getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics metrics = new DisplayMetrics();
                wm.getDefaultDisplay().getMetrics(metrics);
                //For right hand side
                if (metrics.widthPixels - xpos < min_offset)
                    return -getWidth();
                    //For left hand side
                else if (metrics.widthPixels - xpos < 0)
                    return -getWidth();
                return -(getWidth() / 2);
            }

            @Override
            public int getYOffset(float ypos) {
                return -getHeight()-10;
            }
        };

        public void setS_type(String s_type) { S_type = s_type; }
        public void setT_type(String t_type) { T_type = t_type; }
        public String getS_type() { return S_type; }
        public String getT_type() { return T_type; }
        public void chartSettings(LineChart lineChart){

            this.lineChart=lineChart;
            LineData lineData = new LineData();
            //lineData.setValueTextSize(12);

            lineChart.setMarkerView(mv);
            //lineChart.setGridBackgroundColor(Color.TRANSPARENT);  //chart background color
            lineChart.setPinchZoom(true);
            lineChart.zoomOut();
            lineChart.setHighlightPerTapEnabled(true);   //tap할 경우 하이라이트 가능
            lineChart.setHighlightPerDragEnabled(false);  //drag할 경우 하이라이트 불가능
            lineChart.setBackgroundColor(Color.TRANSPARENT);
            lineChart.setDrawGridBackground(false);
            lineChart.setExtraTopOffset(10);
            lineChart.setData(lineData); // set the data and list of lables into chart

            YAxis yaxis = lineChart.getAxisLeft();
            yaxis.setTextColor(Color.BLACK);

            yaxis.setGranularity(0.1f);   //얼마나 촘촘하게 할건지.. -> 0.1f는 0.0,0.1,0.2,0.3....
            yaxis.setGranularityEnabled(true);

            yaxis.setDrawLimitLinesBehindData(true);
            yaxis.setSpaceBottom(10);
            yaxis.setGridColor(Color.GRAY);

            YAxis rightYAxis = lineChart.getAxisRight();
            rightYAxis.setEnabled(false);

            XAxis xaxis = lineChart.getXAxis();
            xaxis.setTextColor(Color.BLACK);
            xaxis.setTextSize(11);
            xaxis.setAvoidFirstLastClipping(true);

            Legend legend = lineChart.getLegend();
            legend.setTextColor(Color.BLACK);

            lineChart.animateXY(2000, 2000); //애니메이션 기능 활성화
            lineChart.invalidate();

        }
        public void addEntry(float S_data,float T_data,String date){
            // <참고> LineData 안에 LineDataSet이 들어있음
            LineData data=lineChart.getLineData();

            x.add(date);

            if(data!=null){
                ILineDataSet s_set=data.getDataSetByIndex(0);
                ILineDataSet t_set=data.getDataSetByIndex(1);

                if(s_set==null && S_type!=null){   //s_set이 없을 경우, set을 새로 생성하고 data에 추가하기
                    s_set=createS_Set();
                    data.addDataSet(s_set);
                }
                if(t_set==null){   //t_set이 없을 경우, set을 새로 생성하고 data에 추가하기
                    t_set=createT_Set();
                    data.addDataSet(t_set);
                }
                data.addXValue(date);

                if(S_type!=null) {
                    data.addEntry(new Entry(S_data,s_set.getEntryCount()),0);
                }
                data.addEntry(new Entry(T_data, t_set.getEntryCount()), 1);

                //차트에 데이터가 변경되었음을 알림
                lineChart.notifyDataSetChanged();

                //화면에 보여지는 데이터의 수의 범위
                lineChart.setVisibleXRange(30,30);

                lineChart.moveViewToX(data.getXValCount()-7);

            }
        }
        //현재값(T)의 set을 생성하는 메소드(실선으로 나타냄)
        public LineDataSet createT_Set(){
            LineDataSet set=new LineDataSet(null,sensorname+" 현재값(실선)");
            set.setDrawCubic(true);
            set.setCubicIntensity(0.2f);
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setColor(color);
            set.setCircleColor(color);
            set.setLineWidth(2f);
            set.setCircleSize(4f);
            set.setValueTextSize(10f);

            return set;
        }
        //설정값(S)의 set을 생성하는 메소드(점선으로 나타냄)
        public LineDataSet createS_Set(){
            LineDataSet set=new LineDataSet(null,sensorname+" 설정값(점선)");
            set.setDrawCubic(true);
            set.setCubicIntensity(0.2f);
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setColor(color);
            set.setLineWidth(2f);
            set.setCircleSize(0f);
            set.setDrawValues(false);
            set.setLineWidth(1.5f);  //그래프 라인 두께
            set.enableDashedLine(10f,8f,5f);

            return set;
        }

    }
    class DialogManager{
        AlertDialog dataStopDialog=null;
        ProgressDialog progressDialog=new ProgressDialog(RealtimeChartActivity.this, R.style.AppCompatAlertDialogStyle);

        protected void showAlertDialog(String date){
            AlertDialog.Builder builder = new AlertDialog.Builder(RealtimeChartActivity.this);
            builder.setTitle("Alert");
            builder.setMessage("데이터를 읽어올 수 없습니다.\n마지막 데이터 날짜 : "+date);
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            if(dataStopDialog!=null && dataStopDialog.isShowing())
                dataStopDialog.dismiss();

            dataStopDialog = builder.create();
            dataStopDialog.show();

        }
        protected  void stopAlertDialog(){
            if(dataStopDialog!=null && dataStopDialog.isShowing())
                dataStopDialog.dismiss();
        }
        protected void showProgressDialog(){
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("잠시만 기다려주세요.");
            //dialog.setCancelable(false); //dialog 외부 터치시 dialog창이 없이지지 않게함.
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            if(!progressDialog.isShowing())
                progressDialog.show();
        }
        protected void stopProgressDialog(){
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();
        }


    }
}
