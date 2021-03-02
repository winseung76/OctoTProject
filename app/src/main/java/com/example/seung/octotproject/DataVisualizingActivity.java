package com.example.seung.octotproject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.widget.Toast.makeText;

public class DataVisualizingActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,CompoundButton.OnCheckedChangeListener{


    private static final int START_DATE=0,END_DATE=1;
    private static final int START_TIME=2,END_TIME=3;

    TextView startdate,starttime,enddate,endtime;
    EditText from,to;
    Button display;
    Spinner chartList,group,machine,factory;

    boolean xCheck=false,yCheck=true;
    float startx,endx;
    float starty,endy;
    String start,end;    //구간 시작날짜, 구간 끝날짜
    Date date = new Date(System.currentTimeMillis());
    SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat CurTimeFormat = new SimpleDateFormat("HH:mm");
    String chartname,fn,gn,mn;
    TextView member,logout,selected_sensors_layout;
    String membername;
    CheckBox[] sensor=new CheckBox[8];
    int machineNum=0;
    MyApplication octotdata;
    Machine machineinfo;
    String selected_machinename="",group_name,minvalue,maxvalue;
    ArrayList<Integer> selected_sensors=new ArrayList<>();
    LinearLayout sensor_layout;
    //static ArrayList<String> sensors;

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("onRestart()");
        date = new Date(System.currentTimeMillis());
        endtime.setText(CurTimeFormat.format(date));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_visualizing);

        octotdata=(MyApplication)getApplication();

        setDefault();

        sensor_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSensorDialog();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nav_header_view = navigationView.getHeaderView(0);

        logout = (TextView) nav_header_view.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                member = (TextView) findViewById(R.id.member);
                membername=octotdata.getMembername();
                member.setText(Html.fromHtml("<b>" + membername + "</b>" + "  님"));
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        /*

        for(int i=0;i<8;i++){
            sensor[i].setOnCheckedChangeListener(this);
        }
        */

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(DataVisualizingActivity.this);
                builder.setMessage("로그아웃 하시겠습니까?");
                builder.setCancelable(false);
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                DataVisualizingActivity.this.finish();
                                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("아니요",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        // 다이얼로그를 취소한다
                                        dialog.cancel();
                                    }
                                });

                // 다이얼로그 생성
                AlertDialog alertDialog = builder.create();
                // 다이얼로그 보여주기
                alertDialog.show();
            }
        });


        chartList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                ((TextView)adapterView.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                chartname=chartList.getSelectedItem().toString();
                switch(chartList.getSelectedItem().toString()){
                    case "LineChart":
                        xCheck=false;yCheck=true;
                        break;
                    case "BarChart":
                        xCheck=false;yCheck=true;
                        break;
                    case "ScatterChart":
                        xCheck=true;yCheck=true;
                        break;
                    case "PieChart":
                        xCheck=false;yCheck=true;
                        break;
                }
                if(chartList.getSelectedItem().toString().equals("PieChart") && selected_sensors.size()>=1){
                    Toast.makeText(getApplicationContext(),"PieChart는 하나의 센서만 선택가능합니다.",Toast.LENGTH_SHORT).show();

                    for(int j=0;j<8;j++){
                        if(sensor[i].isChecked()) {
                            sensor[i].setChecked(false);
                            selected_sensors.remove((Object)i);
                        }
                    }

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        ArrayAdapter<String> cad=new ArrayAdapter<String>(DataVisualizingActivity.this,android.R.layout.simple_spinner_dropdown_item,
                new String[]{"LineChart","BarChart","ScatterChart","PieChart"}){
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                tv.setTextColor(Color.BLACK);
                return view;
            }
        };

        chartList.setAdapter(cad);
        factory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            ArrayAdapter<String> group_adapter = null;

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                fn=factory.getSelectedItem().toString();
                ((TextView)adapterView.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                ((TextView)adapterView.getChildAt(0)).setGravity(Gravity.CENTER);

                group_adapter = new ArrayAdapter<String>(DataVisualizingActivity.this, android.R.layout.simple_spinner_dropdown_item,
                        octotdata.factories.get(fn).getGroupList()) {
                    @Override
                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView tv = (TextView) view;
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15); //dp
                        tv.setTextColor(Color.BLACK);

                        return view;
                    }
                };
                group.setAdapter(group_adapter);
                group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    ArrayAdapter<String> adapter = null;

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                        gn=group.getSelectedItem().toString();
                        ((TextView)adapterView.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                        ((TextView)adapterView.getChildAt(0)).setGravity(Gravity.CENTER);

                        adapter = new ArrayAdapter<String>(DataVisualizingActivity.this, android.R.layout.simple_spinner_dropdown_item,
                                octotdata.factories.get(fn).get(gn).getMachineList()) {
                            @Override
                            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                View view = super.getDropDownView(position, convertView, parent);
                                TextView tv = (TextView) view;
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15); //dp
                                tv.setTextColor(Color.BLACK);

                                return view;
                            }
                        };
                        machine.setAdapter(adapter);
                        machine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                mn=machine.getSelectedItem().toString();
                                machineinfo=octotdata.factories.get(fn).get(gn).get(mn);

                                machineinfo.setMachinename(machine.getSelectedItem().toString());
                                machineinfo.setIP(octotdata.factories.get(fn).get(gn).get(mn).getIP());

                                ((TextView)adapterView.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

                                /*
                                for(int j=0;j<8;j++){
                                    sensor[j].setText(machineinfo.getSensor(j));
                                }
                                */

                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {}
                        });
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        ArrayAdapter<String> fad = new ArrayAdapter<String>(DataVisualizingActivity.this, android.R.layout.simple_spinner_dropdown_item,
                octotdata.factorylist) {
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                tv.setTextColor(Color.BLACK);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15); //dp
                //tv.setBackgroundColor(Color.parseColor("#333333"));
                return view;
            }
        };
        factory.setAdapter(fad);

        /* 그룹 스피너 아이템 선택시 기계 스피너 목록 변경 */
        /*
        group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            ArrayAdapter<String> adapter = null;

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                group_name=(String)group.getSelectedItem();
                ((TextView)adapterView.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                adapter = new ArrayAdapter<String>(DataVisualizingActivity.this, android.R.layout.simple_spinner_dropdown_item,
                        (octotdata.getGroup_pair_machines()).get(group_name)) {
                    @Override
                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView tv = (TextView) view;

                        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                        tv.setTextColor(Color.BLACK);
                        return view;
                    }
                };
                machine.setAdapter(adapter);
                machine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    ArrayAdapter<String> adapter = null;
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        ((TextView)adapterView.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                        selected_machinename = (String)machine.getSelectedItem();
                            sensors = octotdata.getSensors().get(selected_machinename);
                            for (int j = 0; j < 8; j++) {
                                sensor[j].setText(sensors.get(j));
                            }
                        adapter = new ArrayAdapter<String>(DataVisualizingActivity.this, android.R.layout.simple_spinner_dropdown_item,
                                sensors) {
                            @Override
                            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                View view = super.getDropDownView(position, convertView, parent);
                                TextView tv = (TextView) view;
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                                tv.setTextColor(Color.BLACK);

                                return view;
                            }
                        };
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        ArrayAdapter<String> gad = new ArrayAdapter<String>(DataVisualizingActivity.this, android.R.layout.simple_spinner_dropdown_item,
                octotdata.getGroups()) {
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                tv.setTextColor(Color.BLACK);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                return view;
            }
        };
        group.setAdapter(gad);
        */

        /*display버튼 눌렀을때 */
        display.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                CheckCondition checkClass = new CheckCondition(); //폼체크 클래스 생성

                setProperty();

                if (checkClass.checkForm()) { //메소드 호출해서 입력 알맞으면 쿼리 시작

                    Intent intent=new Intent(DataVisualizingActivity.this,ChartActivity.class);
                    intent.putExtra("chartname",chartname);
                    intent.putExtra("startdate",start);
                    intent.putExtra("enddate",end);
                    intent.putExtra("minvalue",minvalue);
                    intent.putExtra("maxvalue",maxvalue);
                    intent.putExtra("machineNum",machineNum);
                    intent.putExtra("sensors",selected_sensors);
                    intent.putExtra("machineinfo",machineinfo);

                    startActivity(intent);

                }
            }
            public void setProperty(){
                startx=0;endx=1000;starty=0;endy=1000;

                //machineNum=octotdata.getMachine_pair_machineNum().get(machine.getSelectedItem().toString());
                machineNum=machineinfo.getMachineNum();

                minvalue=from.getText().toString();
                maxvalue=to.getText().toString();

                start=startdate.getText().toString()+" "+starttime.getText().toString();//YYYY-MM-DD HH:MM
                end=enddate.getText().toString()+" "+endtime.getText().toString();

            }
        });
    }
    //센서 선택할수 있는 대화상자를 보여주는 메소드
    public void showSensorDialog(){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(DataVisualizingActivity.this);
        View mView = layoutInflaterAndroid.inflate(R.layout.sensorselect_dialoglayout, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(DataVisualizingActivity.this);
        alertDialogBuilderUserInput.setView(mView);

        sensor[0]=mView.findViewById(R.id.sensor0);
        sensor[1]=mView.findViewById(R.id.sensor1);
        sensor[2]=mView.findViewById(R.id.sensor2);
        sensor[3]=mView.findViewById(R.id.sensor3);
        sensor[4]=mView.findViewById(R.id.sensor4);
        sensor[5]=mView.findViewById(R.id.sensor5);
        sensor[6]=mView.findViewById(R.id.sensor6);
        sensor[7]=mView.findViewById(R.id.sensor7);

        for(int i=0;i<8;i++){
            sensor[i].setOnCheckedChangeListener(this);
            sensor[i].setText(machineinfo.getSensor(i));
        }

        for(int i=0;i<selected_sensors.size();i++){
            sensor[selected_sensors.get(i)].setChecked(true);
        }
        alertDialogBuilderUserInput
                .setTitle("센서값을 선택하세요.")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(selected_sensors.size()>0){
                            String string="";
                            for(int i=0;i<selected_sensors.size();i++){
                                string+=machineinfo.getSensor(selected_sensors.get(i))+" ";
                            }
                            System.out.println(selected_sensors.size());
                            selected_sensors_layout.setText("");
                            selected_sensors_layout.setText(string);
                            selected_sensors_layout.setVisibility(View.VISIBLE);
                        }
                        else
                            selected_sensors_layout.setVisibility(View.GONE);
                    }
                });

        Dialog dialog = alertDialogBuilderUserInput.create();
        dialog.show();
    }
    public void setDefault(){

        sensor_layout=(LinearLayout)findViewById(R.id.sensor_layout);


        chartList=(Spinner)findViewById(R.id.chartList);
        group=(Spinner)findViewById(R.id.group);
        machine=(Spinner)findViewById(R.id.machine);
        factory=(Spinner)findViewById(R.id.factory);

        display=(Button)findViewById(R.id.display);

        startdate=(TextView)findViewById(R.id.startdate);
        starttime=(TextView)findViewById(R.id.starttime);
        enddate=(TextView)findViewById(R.id.enddate);
        endtime=(TextView)findViewById(R.id.endtime);
        selected_sensors_layout=(TextView)findViewById(R.id.selected_sensors_layout);

        startdate.setText(CurDateFormat.format(date));
        enddate.setText(CurDateFormat.format(date));
        starttime.setText(CurTimeFormat.format(date));
        endtime.setText(CurTimeFormat.format(date));

        from=(EditText)findViewById(R.id.from);
        to=(EditText)findViewById(R.id.to);
        from.setSelection(from.length());
        to.setSelection(to.length());
    }
    /* 날짜, 시간 textview 클릭시 발생하는 콜벡 메소드 */
    public void onClick(View v){
        switch(v.getId()){
            case R.id.startdate:
                showDialog(START_DATE);
                break;
            case R.id.enddate:
                showDialog(END_DATE);
                break;
            case R.id.starttime:
                showDialog(START_TIME);
                break;
            case R.id.endtime:
                showDialog(END_TIME);
                break;
        }
    }

    protected Dialog onCreateDialog(int id) {

        Calendar calendar = Calendar.getInstance();
        Context context = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog);

        switch(id){
            case START_DATE :
                DatePickerDialog sd = new DatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        startdate.setText(year+"-"+String.format("%02d",month+1)+"-"+String.format("%02d",day));
                    }
                }
                        , // 사용자가 날짜설정 후 다이얼로그 빠져나올때
                        //    호출할 리스너 등록
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)); // 기본값 연월일
                return sd;
            case END_DATE :
                DatePickerDialog ed = new DatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int month,int day) {
                        enddate.setText(year+"-"+String.format("%02d",month+1)+"-"+String.format("%02d",day));
                    }
                }
                        , // 사용자가 날짜설정 후 다이얼로그 빠져나올때
                        //    호출할 리스너 등록
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)); // 기본값 연월일
                return ed;
            case START_TIME :
                TimePickerDialog st = new TimePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int min) {
                        starttime.setText(String.format("%02d",hour)+":"+String.format("%02d",min));
                    }
                }, // 값설정시 호출될 리스너 등록
                        0,0, true); // 기본값 시분 등록
                return st;
            case END_TIME :
                TimePickerDialog et = new TimePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int min) {
                        endtime.setText(String.format("%02d",hour)+":"+String.format("%02d",min));
                    }
                }, // 값설정시 호출될 리스너 등록
                        0,0, true); // 기본값 시분 등록
                return et;
        }
        return super.onCreateDialog(id);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        int index=0;
        switch(compoundButton.getId()){
            case R.id.sensor0:
                index=0;
                break;
            case R.id.sensor1:
                index=1;
                break;
            case R.id.sensor2:
                index=2;
                break;
            case R.id.sensor3:
                index=3;
                break;
            case R.id.sensor4:
                index=4;
                break;
            case R.id.sensor5:
                index=5;
                break;
            case R.id.sensor6:
                index=6;
                break;
            case R.id.sensor7:
                index=7;
                break;
        }
        //Toast.makeText(DataVisualizingActivity.this,String.valueOf(index),Toast.LENGTH_SHORT).show();
        if(b) {
            if(chartList.getSelectedItem().toString().equals("PieChart")){
                if(selected_sensors.size()>=1) {
                    Toast.makeText(getApplicationContext(), "PieChart는 하나의 센서만 선택가능합니다.", Toast.LENGTH_SHORT).show();
                    compoundButton.setChecked(false);
                }
                else {
                    if(!selected_sensors.contains(index))
                        selected_sensors.add(index);
                }
            }

            else if(chartList.getSelectedItem().toString().equals("ScatterChart")){
                if(selected_sensors.size()>=2) {
                    Toast.makeText(getApplicationContext(), "ScatterChart는 두 개의 센서만 선택가능합니다.", Toast.LENGTH_SHORT).show();
                    compoundButton.setChecked(false);
                }
                else{
                    if(!selected_sensors.contains(index))
                        selected_sensors.add(index);
                }
            }
            else {
                if(!selected_sensors.contains(index))
                    selected_sensors.add(index);
            }
        }
        else if(!b){
            selected_sensors.remove((Object)index);
        }
    }


    class CheckCondition{//전체 입력을 체크하는 클래스

        boolean checkD=false; //날짜 체크
        boolean checkS=false; //센서 선택했는지를 체그
        boolean checkR=false; //온도.습도 범위 체크

        public boolean checkDate(){

            if(start.compareTo(end)>0){
                Toast.makeText(getApplicationContext(),"시작일이 종료일보다 앞서야 합니다.",Toast.LENGTH_SHORT).show();
            }
            else
                checkD=true;

            return checkD;
        }

        public boolean checkRange(){

            boolean checkRx=false;
            boolean checkRy=false;
/*
            if(xCheck) {
                checkR=false;
                //from01=(EditText)findViewById(R.id.from01);
                //to01=(EditText)findViewById(R.id.to01);
                //from01str = from01.getText().toString();
                //to01str = to01.getText().toString();

                if (from01str.equals("") || to01str.equals("")) {

                    makeText(getApplicationContext(),"범위를 입력하시오.",Toast.LENGTH_SHORT).show();
                    checkRx=false;

                } else {
                    startx = Float.parseFloat(from01str);
                    endx = Float.parseFloat(to01str);

                    if (endx > 1000 || startx > endx) {
                        if (endx > 1000) {
                            makeText(getApplicationContext(), "최대값은 1000입니다.", Toast.LENGTH_SHORT).show();
                        }
                        if (startx > endx) {
                            makeText(getApplicationContext(), "From의 값이 To의 값보다 작아야 합니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        checkRx = true;
                    }
                }
            }*/

            String fromStr=from.getText().toString();
            String toStr=to.getText().toString();

            if (fromStr.equals("") || toStr.equals("")) {
                makeText(getApplicationContext(),"범위를 입력하시오.",Toast.LENGTH_SHORT).show();

            }
            else {
                starty = Integer.parseInt(fromStr);
                endy = Integer.parseInt(toStr);

                if (endy > 1000 || starty > endy) {
                    if (endy > 1000) {
                        makeText(getApplicationContext(), "최대값은 1000입니다.", Toast.LENGTH_SHORT).show();
                    }
                    if (starty > endy) {
                        makeText(getApplicationContext(),  "From의 값이 To의 값보다 작아야 합니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    checkRy=true;
                }
            }
            if(checkRy)
                checkR=true;

            return checkR;
        }
        public boolean checkSensor(){
            if(selected_sensors.size()==0){
                Toast.makeText(getApplicationContext(),"센서를 1개 이상 선택해주세요.",Toast.LENGTH_SHORT).show();
                checkS=false;
            }
            else
                checkS=true;

            return checkS;
        }
        public boolean checkForm(){ //전체 폼을 체크하는 메소드
            this.checkDate();
            this.checkRange();
            this.checkSensor();
            if(checkD&&checkR&&checkS){
                return true;
            }
            return false;
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id==R.id.nav_control){
            Intent intent=new Intent(getApplicationContext(),ControlActivity.class);
            intent.putExtra("membername",membername);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
