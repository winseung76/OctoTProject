package com.example.seung.octotproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChartActivity extends AppCompatActivity {
    private LineChart lineChart;
    private BarChart barChart;
    private ScatterChart scatterChart;
    private PieChart pieChart;
    private String chartname,startdate,enddate,minvalue,maxvalue;
    private ArrayList<Integer> selected_sensors;
    private String machineNum;
    private MyApplication octotData=new MyApplication();
    private Background background;
    private Machine machineinfo;
    private Chart chart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //풀 화면으로 만들기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_chart);

        Intent intent=getIntent();
        chartname=intent.getStringExtra("chartname");
        startdate=intent.getStringExtra("startdate");
        enddate=intent.getStringExtra("enddate");
        minvalue= String.valueOf(Float.parseFloat(intent.getStringExtra("minvalue"))-30);
        maxvalue=String.valueOf(Float.parseFloat(intent.getStringExtra("maxvalue"))+30);
        machineNum=String.valueOf(intent.getIntExtra("machineNum",0));
        selected_sensors=intent.getIntegerArrayListExtra("sensors");
        machineinfo=(Machine)intent.getSerializableExtra("machineinfo");

        lineChart = (LineChart) findViewById(R.id.linechart);
        barChart = (BarChart) findViewById(R.id.barchart);
        scatterChart = (ScatterChart) findViewById(R.id.scatterchart);
        pieChart=(PieChart)findViewById(R.id.piechart);

        if (background!=null) {
            background.cancel(false);
        }
        background=new Background("http://"+octotData.serverIP+"/visualizingdata.php");
        background.setNameList();

        chart=new Chart(background.Tname_list.size(),background.Sname_list.size());
        chart.setSensorNum(selected_sensors.size());
        //설정된 정보를 php에 전송

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            background.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        }
        else {
            background.execute();
        }

    }

    class Background extends AsyncTask<Void,Integer,String> {
        private String urlstr;
        private ArrayList<String> Tname_list=new ArrayList<>();  //현재값 이름 T+
        private ArrayList<String> Sname_list=new ArrayList<>();  //설정값 이름 S+
        private CopyOnWriteArrayList<NameValuePair> post = new CopyOnWriteArrayList<NameValuePair>();
        private ProgressDialog dialog=new ProgressDialog(ChartActivity.this,R.style.AppCompatAlertDialogStyle);
        private String pre_date="";

        Background(String urlstr){
            this.urlstr=urlstr;

        }
        //T와S의 이름을 리스트에 추가하여 셋팅하는 메소드
        public void setNameList(){
            for(int k=0;k<selected_sensors.size();k++){
                Tname_list.add("T"+selected_sensors.get(k));
            }

            for(int k=0;k<selected_sensors.size();k++){
                if(selected_sensors.get(k)<4)
                    Sname_list.add("S"+selected_sensors.get(k));
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("잠시만 기다려주세요.");
            dialog.setCanceledOnTouchOutside(false);
            //값을 읽어들이는 동안 대화상자의 메시지만 변경될 것이기 때문에 미리 show()상태는 해둔다.
            dialog.show();

            post.add(new BasicNameValuePair("machineNum", ""));
            post.add(new BasicNameValuePair("startdate", ""));
            post.add(new BasicNameValuePair("enddate", ""));
            post.add(new BasicNameValuePair("sensors", ""));

        }


        @Override
        protected void onPostExecute(String result) {
            ArrayList<String> date=new ArrayList<>();

            if(result!=null) {
                String[] array = result.split("\n");
                int size = array.length;
                //System.out.println(size);
                Collections.sort(selected_sensors);

                try {
                    for (int i = 0; i < size; i++) {
                        //System.out.println(array[0]);
                        JSONArray jarray = new JSONArray(array[i]);   //[]

                        JSONObject jObject=jarray.getJSONObject(0);

                        date.add(jObject.getString("date"));
                        String new_date=jObject.getString("date").split(" ")[0];
                        String new_time=jObject.getString("date").split(" ")[1];

                        //System.out.println(new_date+" "+new_time);

                        if(new_date.equals(pre_date)){
                            chart.addXvalue(new_time);
                        }
                        else{
                            chart.addXvalue(new_date+"\n"+new_time);
                        }
                        pre_date=new_date;

                        for (int j = 0; j < Tname_list.size(); j++) {
                            String t_name=Tname_list.get(j);
                            chart.addTvalue(j,Float.parseFloat(jObject.getString(t_name))/10);
                            //chart.addTvalue(j,);
                        }

                        for(int j=0;j<Sname_list.size();j++){
                            String s_name=Sname_list.get(j);
                            chart.addSvalue(j,Float.parseFloat(jObject.getString(s_name))/10);
                        }
                    }
                } catch (Exception e) {
                }
                chart.lineData=new LineData(chart.getXvalue());
                for(int i=0;i<Tname_list.size();i++){
                    chart.createDataSet(chart.lineData,chart.getTvalue(i),date,i,Tname_list.get(i));

                }
                for(int i=0;i<Sname_list.size();i++){
                    chart.createDataSet(chart.lineData,chart.getSvalue(i),date,i,Sname_list.get(i));
                }

                chart.setDataNum();
                chart.setXYAxisValue();
                //chart.y_avg=chart.getAverage();

                chart.showChart();

                if(dialog.isShowing())
                    dialog.dismiss();

                Toast.makeText(getApplicationContext(),"총 date 개수 : "+String.valueOf(chart.dataNum),Toast.LENGTH_SHORT).show();
            }
            else
                System.out.print("받아온 데이터 없음!");

        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            try {

                dialog.setMessage("잠시만 기다려주세요.\n데이터를 읽어오는 중...("+values[0]+"%)");
                dialog.setCanceledOnTouchOutside(false);
            }catch (Exception e){}
        }

        public void setPost(){
            post.set(0, new BasicNameValuePair("machineNum", machineNum));
            post.set(1, new BasicNameValuePair("startdate", startdate));
            post.set(2, new BasicNameValuePair("enddate", enddate));

            String string="";
            for(int i=0;i<selected_sensors.size();i++){
                string+="T"+selected_sensors.get(i);
                string+=", ";
            }
            for(int i=0;i<selected_sensors.size();i++){
                if(selected_sensors.get(i)<4){
                    string+="S"+selected_sensors.get(i);
                    if(i<selected_sensors.size()-1)
                        string+=", ";
                }
            }
            //SELECT 할 센서 이름들을 string형태로 보낸다.
            post.set(3,new BasicNameValuePair("sensors",string));
        }
        @Override
        protected String doInBackground(Void... voids) {

            InputStream inputStream = null;
            BufferedReader rd;
            String result=null;
            HttpPost httpPost;
            HttpClient client;

            try {
                // 연결 HttpClient 객체 생성
                client = new DefaultHttpClient();

                // 객체 연결 설정 부분, 연결 최대시간 등등
                HttpParams params = client.getParams();
                HttpConnectionParams.setConnectionTimeout(params, 5000);
                HttpConnectionParams.setSoTimeout(params, 5000);

                // Post객체 생성
                httpPost = new HttpPost(urlstr);
                setPost();
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(post, "UTF-8");
                httpPost.setEntity(entity);
                //생성한 post 객체를 client에 전송한다.
                HttpResponse httpResponse = client.execute(httpPost);

                System.out.println("post : " + post);

                // 9. 서버로 부터 응답 메세지를 받는다.
                inputStream = httpResponse.getEntity().getContent();
                rd = new BufferedReader(new InputStreamReader(inputStream));

                // 10. 수신한 응답 메세지의 inputstream을 string형태로 변환 한다.
                if (inputStream != null) {
                    String str;
                    result = "";

                    String size=rd.readLine();
                    rd.readLine();   //column 개수

                    //한 row씩 한줄로 읽어들인다.
                    int count=0;
                    while ((str = rd.readLine()) != null) {
                        //System.out.println("받아온 스트리잉 : " + str);
                        result += str;
                        result += "\n";
                        count++;
                        int percent=(int)Math.round((double)count/Double.parseDouble(size)*100);
                        publishProgress(percent);
                    }
                    //System.out.println("result : "+result);

                }
                else
                    System.out.println("inputStream=null");

            }catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

    }
    class Chart {
        //ArrayList<String> labels;
        private int dataNum,sensorNum;
        private ArrayList<String> x_value=new ArrayList<>();  //x축 데이터
        private ArrayList<Float>[] Tvalue; //현재값
        private ArrayList<Float>[] Svalue; //설정값
        private String x_axis,y_axis;
        LineData lineData;
        private int[] color={Color.parseColor("#FF0000"),Color.parseColor("#FFFF00"),Color.parseColor("#00FF00"),
                Color.parseColor("#00FFFF"),Color.parseColor("#0000FF"),Color.parseColor("#FF00FF"),
                Color.parseColor("#FF7F00"),Color.parseColor("#444444")};
        private LimitLine upper = new LimitLine(40f, "Upper Limit");

        /* chart value 클릭 시 나타나는 markerview */
        private MarkerView mv=new MarkerView(getApplicationContext(),R.layout.markerview) {
            TextView tvContent=(TextView)findViewById(R.id.tvContent);

            @Override
            public void refreshContent(Entry e, Highlight highlight) {
                if(chartname.equals("ScatterChart"))
                    tvContent.setText(x_axis+" : " + x_value.get(e.getXIndex())+"(%)\n"+y_axis+" : " +String.valueOf(e.getVal())+"(%)");
                else
                    tvContent.setText(x_value.get(e.getXIndex())+"\nvalue : "+String.valueOf(e.getVal()));
            }

            @Override
            public int getXOffset(float xpos) {

                // this will center the marker-view horizontally
                int min_offset = 130;
                if (xpos < min_offset)
                    return 0;

                WindowManager wm = (WindowManager)ChartActivity.this.getSystemService(Context.WINDOW_SERVICE);
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


        Chart(int ty_size,int sy_size){

            Tvalue=new ArrayList[ty_size];
            Svalue=new ArrayList[sy_size];
            for(int i=0;i<ty_size;i++){
                this.Tvalue[i]=new ArrayList<>();
            }
            for(int i=0;i<sy_size;i++){
                this.Svalue[i]=new ArrayList<>();
            }
        }

        public ArrayList<Float> getTvalue(int index){return this.Tvalue[index];}
        public ArrayList<Float> getSvalue(int index){return this.Svalue[index];}
        public ArrayList<String> getXvalue(){return this.x_value;}
        public void addTvalue(int index,float value){ this.Tvalue[index].add(value); }
        public void addSvalue(int index,float value){ this.Svalue[index].add(value); }
        public void addXvalue(String value){ this.x_value.add(value); }
        public void setSensorNum(int num){
            this.sensorNum=num;
        }
        public float getAverage(){
            float sum=0;
            float average;

            for(int i=0;i<Tvalue[0].size();i++){
                sum+=Tvalue[0].get(i);
            }
            average=sum/Tvalue[0].size();

            return average;
        }
        public void showChart(){

            /* 조건에 맞는 데이터를 찾아서 배열에 저장 */
            switch(chartname){
                case "LineChart":
                    drawLinechart();
                    break;
                case "BarChart":
                    drawBarchart(Tvalue);
                    break;
                case "ScatterChart":
                    drawScatterchart();
                    break;
                case "PieChart":
                    drawPiechart();
                    break;
            }
        }
        public void setXYAxisValue(){

            if(chartname.equals("ScatterChart")){
                x_axis=machineinfo.getSensor(selected_sensors.get(0));
                y_axis=machineinfo.getSensor(selected_sensors.get(1));
            }
            else if(chartname.equals("PieChart")){
                x_axis="";
                y_axis=machineinfo.getSensor(selected_sensors.get(0));
            }
            else{
                x_axis="";
                y_axis="";
            }

        }
        public void setDataNum(){this.dataNum=x_value.size();}

        public void createDataSet(LineData lineData, ArrayList<Float> data,ArrayList<String> date,int index,String name){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date beginDate;
            Date endDate;
            ArrayList<Entry> entries=new ArrayList<>();

            for(int i=index;i<date.size();i++){

                try {

                    beginDate = formatter.parse(date.get(i));

                    if (i < date.size() - 1) {

                        //현재 데이터의 날짜와 그 다음 데이터의 날짜를 비교하기 위하여
                        endDate = formatter.parse(date.get(i + 1));

                        //두 날짜의 차이를 계산하여 밀리초로 반환함.
                        long diff = endDate.getTime() - beginDate.getTime();

                        //두 날짜의 차이가 10초보다 작은 경우
                        if (diff < 100000) {
                            float ypoint = data.get(i);
                            //새로운 entry를 생성한다.
                            Entry entry = new Entry(ypoint, i);
                            entries.add(entry);
                            //entriesAll.add(entry);
                        }
                        //두 날짜의 차이가 10초이상 나는 경우
                        //새로운 dataSet을 생성한다
                        else if(diff>=100000){
                            createDataSet(lineData, data, date,i+1,name);
                            break;
                        }
                    }
                }catch (ParseException e){
                    e.printStackTrace();
                }
            }
            /*
            for (int i = index; i < data.size(); i++) {

                //데이터가 null값이 아니면
                if (data.get(i) != null) {
                    float ypoint = data.get(i);
                    //새로운 entry를 생성한다.
                    Entry entry = new Entry(ypoint, i);
                    entries.add(entry);
                    //entriesAll.add(entry);

                }
                //데이터가 null일 경우
                //그 다음 데이터 값이 null이 아니면
                else if ((i+1) < data.size()) {
                    if (data.get(i+1) != null) {
                        //새로운 dataset을 생성한다.
                        createDataSet(lineData, data, date,i+1);
                        break;
                    }
                }
            }
            */
            if (entries.size() > 0) {
                int sensor_number=Integer.parseInt(name.substring(1));
                LineDataSet dataSet = new LineDataSet(entries, machineinfo.getSensor(selected_sensors.get(sensor_number)));
                if(name.contains("T"))
                    setTLineDataSet(dataSet,sensor_number);
                else if(name.contains("S"))
                    setSLineDataSet(dataSet,sensor_number);

                lineData.addDataSet(dataSet);
            }
        }
        public void setTLineDataSet(LineDataSet lineDataSet,int i){
            lineDataSet.setLineWidth(1.5f);  //그래프 라인 두께
            lineDataSet.setColor(color[i]);
            lineDataSet.setCircleColor(color[i]);
            lineDataSet.setDrawCircleHole(false);
            lineDataSet.setCircleSize(2.5f);   //circle size
            //lineDataSet.setDrawCubic(true); //line을 직선형태로
            lineDataSet.setDrawValues(false);
            lineDataSet.setHighlightEnabled(true);
            lineDataSet.setHighLightColor(R.color.colorPrimary);
            lineDataSet.setHighlightLineWidth(1.5f);
            lineDataSet.setDrawFilled(false);  //그래프 선 밑에 채워지는 배경 색을 설정할건지 여부
        }
        public void setSLineDataSet(LineDataSet lineDataSet,int i){
            lineDataSet.setLineWidth(1.5f);  //그래프 라인 두께
            lineDataSet.setColor(color[i]);
            //lineDataSet.setCircleColor(color[i]);
            //lineDataSet.setDrawCircleHole(false);
            lineDataSet.setCircleSize(0f);   //circle size
            //lineDataSet.setDrawCubic(true); //line을 직선형태로
            lineDataSet.setDrawValues(false);
            lineDataSet.setHighlightEnabled(true);
            lineDataSet.setHighLightColor(R.color.colorPrimary);
            lineDataSet.setHighlightLineWidth(1.5f);
            lineDataSet.setDrawFilled(false);  //그래프 선 밑에 채워지는 배경 색을 설정할건지 여부
            lineDataSet.enableDashedLine(10f,8f,5f);
        }
        public void setYAxis(){
            YAxis yaxis = lineChart.getAxisLeft();
            yaxis.setTextColor(Color.BLACK);
            yaxis.setAxisMinValue(Float.parseFloat(minvalue));
            yaxis.setAxisMaxValue(Float.parseFloat(maxvalue));

            yaxis.setGranularity(0.1f);   //얼마나 촘촘하게 할건지.. -> 0.1f는 0.0,0.1,0.2,0.3....
            yaxis.setGranularityEnabled(true);
            //yaxis.addLimitLine(upper);
            //yaxis.addLimitLine(lower);

            //yaxis.setDrawTopYLabelEntry(false);
            yaxis.setDrawLimitLinesBehindData(true);
            yaxis.setSpaceBottom(10);
            yaxis.setGridColor(Color.GRAY);

            YAxis rightYAxis = lineChart.getAxisRight();
            rightYAxis.setEnabled(false);
        }
        public void setXAxis(){

            XAxis xaxis = lineChart.getXAxis();
            xaxis.setTextColor(Color.BLACK);
            xaxis.setTextSize(11);
            xaxis.setAvoidFirstLastClipping(true);
            //xaxis.setMultiLineLabel(true);
            //xaxis.setYOffset(10);
            //xaxis.setDrawGridLines(false);
        }
        public void lineChartSetting(){
            lineChart.setMarkerView(mv);
            //lineChart.setGridBackgroundColor(Color.TRANSPARENT);  //chart background color
            lineChart.setPinchZoom(true);
            lineChart.zoomOut();
            lineChart.setHighlightPerTapEnabled(true);   //tap할 경우 하이라이트 가능
            lineChart.setHighlightPerDragEnabled(false);  //drag할 경우 하이라이트 불가능
            lineChart.setBackgroundColor(Color.TRANSPARENT);
            lineChart.setDrawGridBackground(false);
            lineChart.setExtraTopOffset(10);
            lineChart.getLegend().setEnabled(false);
        }
        protected void drawLinechart(){
            ArrayList<ArrayList<Entry>> T_entries=new ArrayList<>();
            ArrayList<ArrayList<Entry>> S_entries=new ArrayList<>();
            ArrayList<Entry> T_entry=null;
            ArrayList<Entry> S_entry=null;

            lineChart.setVisibility(View.VISIBLE);
            barChart.setVisibility(View.GONE);
            scatterChart.setVisibility(View.GONE);
            pieChart.setVisibility(View.GONE);


            for(int k=0;k<Tvalue.length;k++) {
                T_entry = new ArrayList<>();
                for (int i = 0; i < dataNum; i++) {
                    T_entry.add(new Entry(Tvalue[k].get(i), i));
                }
                T_entries.add(T_entry);
            }

            for(int k=0;k<Svalue.length;k++){
                S_entry=new ArrayList<>();

                for (int i = 0; i < dataNum; i++) {
                    S_entry.add(new Entry(Svalue[k].get(i), i));
                }
                S_entries.add(S_entry);
            }

            //LineData lineData = new LineData(x_value);
            lineData.setValueTextSize(12);
                /*
                for(int i=0;i<labels.size();i++){
                    System.out.println("라벨 : "+labels.get(i));
                }
                */

            /*상한선 지정 */
            LimitLine upper= new LimitLine(Float.parseFloat(maxvalue)-30);
            upper.setLineWidth(1.0f);  //상한선 선 두께
            upper.setLineColor(Color.parseColor("#000000"));  //상한선 선 색
            upper.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);  //상한선 라벨 위치

            LimitLine lower= new LimitLine(Float.parseFloat(minvalue)+30);
            lower.setLineWidth(1.0f);  //상한선 선 두께
            lower.setLineColor(Color.parseColor("#000000"));  //상한선 선 색
            lower.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);  //상한선 라벨 위치


            lineChartSetting();

            /*
            for(int i=0;i<Tvalue.length;i++){
                String label=machineinfo.getSensor(selected_sensors.get(i));
                LineDataSet lineDataSet=new LineDataSet(T_entries.get(i), label);
                //System.out.println("사이즈 : "+entries.get(i).size());

                setTLineDataSet(lineDataSet,i);

                lineData.addDataSet(lineDataSet);
            }
            for(int i=0;i<Svalue.length;i++){
                if(selected_sensors.get(i)<4){
                    String label=machineinfo.getSensor(selected_sensors.get(i));
                    LineDataSet lineDataSet=new LineDataSet(S_entries.get(i),null);
                    //System.out.println("사이즈 : "+entries.get(i).size());

                    setSLineDataSet(lineDataSet,i);
                    lineData.addDataSet(lineDataSet);
                }

            }
            */

            lineChart.setData(lineData); // set the data and list of lables into chart

            /*
            YAxis yaxis = lineChart.getAxisLeft();
            yaxis.setTextColor(Color.BLACK);
            yaxis.setAxisMinValue(Float.parseFloat(minvalue));
            yaxis.setAxisMaxValue(Float.parseFloat(maxvalue));

            yaxis.setGranularity(0.1f);   //얼마나 촘촘하게 할건지.. -> 0.1f는 0.0,0.1,0.2,0.3....
            yaxis.setGranularityEnabled(true);
            //yaxis.addLimitLine(upper);
            //yaxis.addLimitLine(lower);

            //yaxis.setDrawTopYLabelEntry(false);
            yaxis.setDrawLimitLinesBehindData(true);
            yaxis.setSpaceBottom(10);
            yaxis.setGridColor(Color.GRAY);

            YAxis rightYAxis = lineChart.getAxisRight();
            rightYAxis.setEnabled(false);
            */
            setYAxis();
            setXAxis();

            Legend legend = lineChart.getLegend();
            legend.setTextColor(Color.BLACK);

            lineChart.animateXY(2000, 2000); //애니메이션 기능 활성화
            lineChart.invalidate();

        }
        protected void drawBarchart(ArrayList<Float>[] y){
            ArrayList<ArrayList<BarEntry>> entries=new ArrayList<>();
            //ArrayList<BarEntry> barEntries = new ArrayList<>();
            ArrayList<BarEntry> entry_list=null;

            /*상한선 지정 */
            /*
                LimitLine upper= new LimitLine(y_avg+5, "Upper Limit");
                upper.setLineWidth(1.2f);  //상한선 선 두께
                upper.setLineColor(Color.parseColor("#FF0000"));  //상한선 선 색
                upper.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);  //상한선 라벨 위치
                upper.setTextSize(10f);  //상한선 텍스트 크기
                upper.setTextColor(Color.parseColor("#FF0000"));
*/
            lineChart.setVisibility(View.GONE);
            barChart.setVisibility(View.VISIBLE);
            scatterChart.setVisibility(View.GONE);
            pieChart.setVisibility(View.GONE);


            for(int k=0;k<sensorNum;k++) {
                entry_list = new ArrayList<>();

                for (int i = 0; i < dataNum; i++) {
                    entry_list.add(new BarEntry(y[k].get(i), i));
                }
                entries.add(entry_list);
                System.out.println("y : "+y[k].size());
                System.out.println("dataNum"+dataNum);

            }

            barChart.setMarkerView(mv);
            barChart.setGridBackgroundColor(Color.parseColor("#353535"));  //chart background color
            barChart.setPinchZoom(true);
            barChart.zoomOut();
            barChart.setHighlightPerTapEnabled(true);
            barChart.setHighlightPerDragEnabled(false);

            BarData barData = new BarData(x_value);
            ArrayList<BarDataSet> barDataSets=new ArrayList<>();

            for(int i=0;i<selected_sensors.size();i++){
                String label=machineinfo.getSensor(selected_sensors.get(i));
                BarDataSet barDataSet = new BarDataSet(entries.get(i), label);  // add entries to dataset
                //System.out.println("사이즈 : "+entries.get(i).size());

                barDataSet.setColor(color[i]);
                barDataSet.setDrawValues(false);
                barDataSet.setHighlightEnabled(true);
                barDataSet.setHighLightColor(Color.parseColor("#FFE400"));
                //barDataSets.add(barDataSet);
                barData.addDataSet(barDataSet);
            }

            barChart.setData(barData); // set the data and list of lables into chart

            YAxis byaxis = barChart.getAxisLeft();
            byaxis.setTextColor(Color.BLACK);
            byaxis.addLimitLine(upper);
            byaxis.setAxisMinValue(Float.parseFloat(minvalue));
            byaxis.setAxisMaxValue(Float.parseFloat(maxvalue));
            YAxis brightYAxis = barChart.getAxisRight();
            brightYAxis.setEnabled(false);
            XAxis bxaxis = barChart.getXAxis();
            bxaxis.setTextColor(Color.BLACK);
            Legend blegend = barChart.getLegend();
            blegend.setTextColor(Color.BLACK);

            barChart.animateXY(2000, 2000); //애니메이션 기능 활성화
            barChart.invalidate();
        }
        protected void drawScatterchart(){
            ArrayList<ArrayList<Entry>> t_entries=new ArrayList<>();
            ArrayList<Entry> x_entry_list=null;
            ArrayList<Entry> y_entry_list=null;
            ArrayList<Entry> entries = new ArrayList<>();

            lineChart.setVisibility(View.GONE);
            barChart.setVisibility(View.GONE);
            scatterChart.setVisibility(View.VISIBLE);
            pieChart.setVisibility(View.GONE);

            y_entry_list=new ArrayList<>();

            for (int i = 0; i < dataNum; i++) {
                y_entry_list.add(new Entry(Tvalue[1].get(i), i));
            }
            t_entries.add(y_entry_list);
                /*
                for(int i=0;i<dataNum;i++){
                    entries.add(new Entry(ty[1].get(i),ty[0].get(i)));
                }
                */
            scatterChart.setMarkerView(mv);
            scatterChart.setGridBackgroundColor(Color.parseColor("#353535"));  //chart background color
            scatterChart.setPinchZoom(true);
            scatterChart.zoomOut();
            scatterChart.setHighlightPerTapEnabled(true);
            scatterChart.setHighlightPerDragEnabled(false);

            String label="machine"+String.format("%02d",machineNum);
            ScatterDataSet scatterDataSet = new ScatterDataSet(entries, label);  // add entries to dataset
            //scatterDataSet.setColor(color);
            scatterDataSet.setDrawValues(false);
            scatterDataSet.setHighlightEnabled(true);
            scatterDataSet.setHighLightColor(Color.parseColor("#FFE400"));
            scatterDataSet.setScatterShapeSize(4);

            //ScatterData scatterData = new ScatterData(x,scatterDataSet);
            //scatterChart.setData(scatterData); // set the data and list of lables into chart

            YAxis syaxis = scatterChart.getAxisLeft();
            syaxis.setTextColor(Color.WHITE);
            YAxis srightYAxis = scatterChart.getAxisRight();
            srightYAxis.setEnabled(false);
            XAxis sxaxis = scatterChart.getXAxis();
            sxaxis.setTextColor(Color.WHITE);
            Legend slegend = scatterChart.getLegend();
            slegend.setTextColor(Color.WHITE);

            scatterChart.animateXY(2000, 2000); //애니메이션 기능 활성화
            scatterChart.invalidate();
        }
        protected void drawPiechart(){
            ArrayList<Entry> entries = new ArrayList<Entry>();
            ArrayList<String> labels;
            float[] yData=new float[3];
            float y_avg=getAverage();
            //Description description = new Description();

            System.out.println("평균 : "+y_avg);

            for(int i=0;i<Tvalue[0].size();i++){
                if(y_avg-1<=Tvalue[0].get(i) && Tvalue[0].get(i)<=y_avg+1)
                    yData[0]++;
                else if(Tvalue[0].get(i)>y_avg+1)
                    yData[1]++;
                else if(Tvalue[0].get(i)<y_avg-1)
                    yData[2]++;
            }
            String[] xData = { "-1~+1","+1~","~-1" };

                /*
                Description[] xd=new Description[3];
                xd[0].setText(xData[0]);
                xd[1].setText(xData[1]);
                xd[2].setText(xData[2]);
                */

            lineChart.setVisibility(View.GONE);
            barChart.setVisibility(View.GONE);
            scatterChart.setVisibility(View.GONE);
            pieChart.setVisibility(View.VISIBLE);

            pieChart.setUsePercentValues(true);
            String label=machineinfo.getSensor(selected_sensors.get(0));
            //description.setText(label);

            pieChart.setDescription(label);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleColor(Color.TRANSPARENT);
            pieChart.setHoleRadius(50);               //안쪽 구멍 반지름
            pieChart.setTransparentCircleRadius(55);  //투명 반지름

            //pieChart.setDrawSliceText(false);

            pieChart.setRotationAngle(0);
            pieChart.setRotationEnabled(true);

            // customize legends
            Legend l = pieChart.getLegend();
            l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
            l.setXEntrySpace(7);
            l.setYEntrySpace(5);

            labels=new ArrayList<>();
            //Description d2=new Description();

            for(int i=0;i<yData.length;i++){
                labels.add(xData[i]);
                entries.add(new Entry(yData[i], i));
            }

            // create pie data set
            PieDataSet dataSet = new PieDataSet(entries, y_axis);
            dataSet.setSliceSpace(1);
            dataSet.setSelectionShift(7);

            // add many colors
            ArrayList<Integer> colors = new ArrayList<Integer>();

            colors.add(Color.parseColor("#1DDB16"));  //초록
            colors.add(Color.parseColor("#FF0000"));
            colors.add(Color.parseColor("#4641D9"));

            dataSet.setColors(colors);

            // instantiate pie data object now
            //PieData data = new PieData(labels, dataSet);
            PieData data = new PieData(labels,dataSet);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(10f);
            data.setValueTextColor(Color.BLACK);

            pieChart.setData(data);
            pieChart.setBackgroundColor(Color.TRANSPARENT);

            // undo all highlights
            pieChart.highlightValues(null);
            pieChart.animateXY(2000, 2000); //애니메이션 기능 활성화
            pieChart.invalidate();
        }
    }
}
