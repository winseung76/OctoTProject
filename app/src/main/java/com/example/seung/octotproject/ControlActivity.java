package com.example.seung.octotproject;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ControlActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Spinner factory, group, machine;
    ToggleButton[] toggle_btn = new ToggleButton[4];
    ImageButton[] chartbtn = new ImageButton[8];
    TextView[] S = new TextView[4];
    TextView[] T = new TextView[8];
    TextView R0, R1, R2, R3, OK, FL, time, member;
    TextView[] sensor = new TextView[8];
    String membername;
    EditText dialogSetValue, dialogSetRoc, sucess_count, fail_count;
    MyApplication octotdata;
    MyService myService;
    boolean isService = false; // 서비스 중인 확인용
    ArrayList<DBOutput> dbOutputs = new ArrayList<>();
    Animation startAnimation;
    DialogManager dialogManager;
    String fn, gn, mn;
    Machine machineinfo;
    DataService dataService;
    LinearLayout[] settings_layout = new LinearLayout[4];
    MyReceiver myReceiver;
    State state;
    boolean Tchecking=false;

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("onStart()");

    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy()");
        /*
        if(dbinput!=null) {
            dbinput.cancel(true);
            dbinput = null;
        }
        */
        /*
        if (dataInput != null) {
            dataInput.cancel(true);
            try {
                dataInput.socket.close();
                System.out.println("소켓 닫힘 여부 : " + dataInput.socket.isClosed());
            } catch (Exception e) {
            }
            dataInput = null;
        }
        */
        //unbindService(conn);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("onRestart()");
        /* RealtimeChartActivity에서 되돌아 올때는 onCreate()가 호출되지 않기 때문에
           onRestart()에 DataService를 바인드 시키는 코드를 추가하였다.
         */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

        Intent intent2 = new Intent(this, DataService.class);
        bindService(intent2, conn2, Context.BIND_AUTO_CREATE);
        /*honeycomb 버전 이상부터는
          executeOnExcutor()을 사용하여 스레드를 병렬적으로 처리.
          그냥 execute()를 사용할 경우, 순차적으로 하나의 스레드를 처리함
        */
        /*
        dbinput=new DBInput("http://"+octotdata.serverIP+"/octotdata.php");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            dbinput.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        else
            dbinput.execute();
            */

    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop()");

        //unbindService(conn);
        //서비스 내의 무한루프 스레드도 종료를 해줘야함.
        dataService.dataInput.cancel(true);
        unbindService(conn2);
        //broadcastreceiver가 더이상 메시지를 받지 못하도록
        //등록을 취소하는 메소드를 호출한다.
        unregisterReceiver(myReceiver);



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        System.out.println("onCreate()");

        octotdata = (MyApplication) getApplication();
        //machineinfo=new MachineInfo();
        dialogManager = new DialogManager();
        state=new State();
        myReceiver = new MyReceiver();

        setDefault();

        //Intent intent = new Intent(this, MyService.class); // 다음넘어갈 컴퍼넌트
       // bindService(intent, conn, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DataService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

        Intent intent2 = new Intent(this, DataService.class);
        bindService(intent2, conn2, Context.BIND_AUTO_CREATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink_animation);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nav_header_view = navigationView.getHeaderView(0);

        TextView logout = (TextView) nav_header_view.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
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
                membername = octotdata.getMembername();
                System.out.println("이름 : " + membername);
                member.setText(Html.fromHtml("<b>" + membername + "</b>" + "  님"));
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        ImageView info = (ImageView)findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogManager.showMachineInfoDialog();
            }
        });

        //factory 스피너에서 아이템을 선택할때 발생하는 이벤트
        factory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            ArrayAdapter<String> group_adapter = null;

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                fn = factory.getSelectedItem().toString();
                ((TextView) adapterView.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                ((TextView) adapterView.getChildAt(0)).setGravity(Gravity.CENTER);

                group_adapter = new ArrayAdapter<String>(ControlActivity.this, android.R.layout.simple_spinner_dropdown_item,
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
                        gn = group.getSelectedItem().toString();
                        ((TextView) adapterView.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                        ((TextView) adapterView.getChildAt(0)).setGravity(Gravity.CENTER);

                        adapter = new ArrayAdapter<String>(ControlActivity.this, android.R.layout.simple_spinner_dropdown_item,
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
                                ((TextView) adapterView.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

                                mn = machine.getSelectedItem().toString();
                                machineinfo = octotdata.factories.get(fn).get(gn).get(mn);
                                //dialogManager.showProgressDialog();
                                for (int j = 0; j < 8; j++) {
                                    sensor[j].setText(machineinfo.getSensor(j));
                                }
                                dataService.dataInput.setPort(machineinfo.getPort()+1000);
                                dataService.dataInput.reset();
                                //state.setStateReady();

                                //state_led.setImageResource(R.drawable.led_yellow);
                                //dbinput.pre_date="";


                                /*
                                if (dataInput != null) {
                                    dataInput.cancel(true);
                                }
                                */

                                //dataInput = new DataInput(machineinfo.getPort() + 1000);

                                /*
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    dataInput.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
                                } else
                                    dataInput.execute();
                                    */

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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        ArrayAdapter<String> fad = new ArrayAdapter<String>(ControlActivity.this, android.R.layout.simple_spinner_dropdown_item,
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

        OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogManager.showOKDialog();
            }
        });
        FL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogManager.showFLDialog();
            }
        });

    }

    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            MyService.LocalBinder mb = (MyService.LocalBinder) service;
            myService = mb.getService(); // 서비스가 제공하는 메소드 호출하여
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

    /*broadcastReceiver을 이용하여서 서비스에서 소켓통신을 통해 받아온
    octot데이터를 인텐트를 통해 읽어온다.
    */
     class MyReceiver extends BroadcastReceiver {
        private String channel="";

        public void setChannel(String channel){
            this.channel=channel;
        }

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            HashMap<String,Float> hm=(HashMap<String, Float>) arg1.getSerializableExtra("data");
            String date=arg1.getStringExtra("date");
            String s=arg1.getStringExtra("state");

            System.out.println(hm);

            switch (s){
                case "ready":
                    state.setStateReady();
                    break;
                case "updating":
                    state.setStateUpdating(hm);
                    state.setTime(date);
                    break;
                case "stop":
                    state.setStateStop(date);
                    break;
            }
            for(int i=0;i<dbOutputs.size();i++) {
                dbOutputs.get(i).checkValueChanged(hm);
            }
            if(Tchecking){
                blinkTValue(channel);
            }

        }
    }


    public void onSettingLayoutClick(View view) {
        dialogManager.showDialog(view);
    }

    public void onToggleClick(View view) {
        int index = -1;
        DBOutput dbOutput = new DBOutput(machineinfo.getIP(), machineinfo.getPort());
        dbOutput.setValue02(null);
        dbOutput.setCommand("DO");
        ToggleButton btn = (ToggleButton) findViewById(view.getId());

        switch (view.getId()) {
            case R.id.first_toggle:
                dbOutput.setChannel("4");
                index = 0;
                break;
            case R.id.second_toggle:
                dbOutput.setChannel("5");
                index = 1;
                break;
            case R.id.third_toggle:
                dbOutput.setChannel("6");
                index = 2;
                break;
            case R.id.fourth_toggle:
                dbOutput.setChannel("7");
                index = 3;
                break;
        }
        System.out.println("토클 클릭");
        if (btn.isChecked()) {
            state.changeOnButton(index);
            dbOutput.setValue01("0");

        } else {
            state.changeOffButton(index);
            dbOutput.setValue01("1");

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            dbOutput.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        else
            dbOutput.execute();

        dbOutputs.add(dbOutput);
    }

    public void onShowChartBtnClick(View view) {
        String s_type, t_type;
        String index;
        switch (view.getId()) {
            case R.id.ib0:
                s_type = "S0";
                t_type = "T0";
                index = "0";
                break;
            case R.id.ib1:
                s_type = "S1";
                t_type = "T1";
                index = "1";
                break;
            case R.id.ib2:
                s_type = "S2";
                t_type = "T2";
                index = "2";
                break;
            case R.id.ib3:
                s_type = "S3";
                t_type = "T3";
                index = "3";
                break;
            case R.id.ib4:
                s_type = null;
                t_type = "T4";
                index = "4";
                break;
            case R.id.ib5:
                s_type = null;
                t_type = "T5";
                index = "5";
                break;
            case R.id.ib6:
                s_type = null;
                t_type = "T6";
                index = "6";
                break;
            case R.id.ib7:
                s_type = null;
                t_type = "T7";
                index = "7";
                break;
            default:
                s_type = null;
                t_type = null;
                index = "-1";
                break;
        }
        dialogManager.showAskDialog(s_type, t_type, index);

    }

    public void setDefault() {

        factory = (Spinner)findViewById(R.id.factory);
        group = (Spinner) findViewById(R.id.group);
        machine = (Spinner) findViewById(R.id.machine);

        toggle_btn[0] = (ToggleButton) findViewById(R.id.first_toggle);
        toggle_btn[1] = (ToggleButton) findViewById(R.id.second_toggle);
        toggle_btn[2] = (ToggleButton) findViewById(R.id.third_toggle);
        toggle_btn[3] = (ToggleButton) findViewById(R.id.fourth_toggle);

        chartbtn[0] = (ImageButton)findViewById(R.id.ib0);
        chartbtn[1] = (ImageButton)findViewById(R.id.ib1);
        chartbtn[2] = (ImageButton)findViewById(R.id.ib2);
        chartbtn[3] = (ImageButton)findViewById(R.id.ib3);
        chartbtn[4] = (ImageButton)findViewById(R.id.ib4);
        chartbtn[5] = (ImageButton)findViewById(R.id.ib5);
        chartbtn[6] = (ImageButton)findViewById(R.id.ib6);
        chartbtn[7] = (ImageButton)findViewById(R.id.ib7);

        sensor[0] = (TextView) findViewById(R.id.m0name);
        sensor[1] = (TextView) findViewById(R.id.m1name);
        sensor[2] = (TextView) findViewById(R.id.m2name);
        sensor[3] = (TextView) findViewById(R.id.m3name);
        sensor[4] = (TextView) findViewById(R.id.m4name);
        sensor[5] = (TextView) findViewById(R.id.m5name);
        sensor[6] = (TextView) findViewById(R.id.m6name);
        sensor[7] = (TextView) findViewById(R.id.m7name);

        S[0] = (TextView) findViewById(R.id.S0);
        S[1] = (TextView) findViewById(R.id.S1);
        S[2] = (TextView) findViewById(R.id.S2);
        S[3] = (TextView) findViewById(R.id.S3);

        R0 = (TextView) findViewById(R.id.R0);
        R1 = (TextView) findViewById(R.id.R1);
        R2 = (TextView) findViewById(R.id.R2);
        R3 = (TextView) findViewById(R.id.R3);

        T[0] = (TextView) findViewById(R.id.T0);
        T[1] = (TextView) findViewById(R.id.T1);
        T[2] = (TextView) findViewById(R.id.T2);
        T[3] = (TextView) findViewById(R.id.T3);
        T[4] = (TextView) findViewById(R.id.T4);
        T[5] = (TextView) findViewById(R.id.T5);
        T[6] = (TextView) findViewById(R.id.T6);
        T[7] = (TextView) findViewById(R.id.T7);


        OK = (TextView) findViewById(R.id.OK);
        FL = (TextView) findViewById(R.id.FL);


        settings_layout[0] = (LinearLayout)findViewById(R.id.setting_layout0);
        settings_layout[1] = (LinearLayout)findViewById(R.id.setting_layout1);
        settings_layout[2] = (LinearLayout)findViewById(R.id.setting_layout2);
        settings_layout[3] = (LinearLayout)findViewById(R.id.setting_layout3);
    }

    public void blinkTValue(String channel) {
        ObjectAnimator anim = ObjectAnimator.ofInt(T[Integer.parseInt(channel)], "textColor", Color.parseColor("#000000"), Color.BLUE,
                Color.parseColor("#000000"));
        float t_value = Float.parseFloat(T[Integer.parseInt(channel)].getText().toString());
        float s_value = Float.parseFloat(S[Integer.parseInt(channel)].getText().toString());
        if (t_value <= s_value - 1.0 || t_value >= s_value + 1.0) {
            anim.setDuration(2000);
            anim.setEvaluator(new ArgbEvaluator());
            anim.setRepeatMode(ValueAnimator.REVERSE);
            anim.setRepeatCount(1);
            anim.start();
        }
        else
            Tchecking=false;

    }
    private void blinkSValue(String channel) {
        ObjectAnimator anim = ObjectAnimator.ofInt(S[Integer.parseInt(channel)], "textColor", Color.parseColor("#000000"), Color.RED,
                Color.parseColor("#000000"));

        anim.setDuration(2000);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setRepeatCount(3);
        anim.start();
    }

    class DBOutput extends AsyncTask<Void,Void,Void>{
        private String urlstr;
        private String value01, value02, channel, command;
        private int port;
        private String cmd_string;
        private boolean exception = false;
        private int tryCount = 0;

        DBOutput(String url, int port) {
            urlstr = url;
            this.port = port;
        }

        public void setValue01(String value01) { this.value01 = value01; }
        public void setValue02(String value02) { this.value02 = value02; }
        public void setChannel(String channel) { this.channel = channel; }
        public void setCommand(String command) { this.command = command; }

        /*변경한 값이 잘 적용되있는지를 확인하는 메소드 */
        protected void checkValueChanged(HashMap<String, Float> value) {
            String str = "";
            int n=-1;
            ObjectAnimator anim=null;
            switch (command) {
                case "SVL":
                    n=Math.round(value.get("S"+channel) * 10);
                    break;
                case "DO":
                    n = Math.round(value.get("L"+channel));
                    if(n==1)
                        n=0;
                    else if(n==0)
                        n=1;
                    break;
                case "SM":
                    switch (channel) {
                        case "0": str = "OK";break;
                        case "1": str = "FL";break;
                    }
                    n = Math.round(value.get(str));
                    break;
            }
            if (Integer.parseInt(value01)==n) {
                Toast.makeText(getApplicationContext(), "변경 성공", Toast.LENGTH_SHORT).show();
                if(command.equals("SVL")) {
                    //설정값 파란색 점멸 효과
                    Tchecking=true;
                    myReceiver.setChannel(channel);
                    //blinkTValue(channel);
                    //설정값 빨간색 점멸 효과
                    blinkSValue(channel);
                }
                dbOutputs.remove(this);
                this.cancel(true);
            } else {
                /* 3번 시도를 해도 바뀌지 않는다면 변경 실패 사실을 사용자에게 알린다.*/
                if (tryCount >= 3) {
                    tryCount = 0;
                    dialogManager.showSendCmdFailDialog(command, cmd_string);
                    dbOutputs.remove(this);
                    this.cancel(true);
                }
                else {
                    //IllegalMonitorStateException발생에 대비하여 synchronized로 감싸기
                    //다시 한번 시도하기 위해 notify()로 깨움
                    synchronized (this) {
                        this.notify();
                    }
                    tryCount++;
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            Toast.makeText(getApplicationContext(), cmd_string, Toast.LENGTH_SHORT).show();

            //예외 발생 시 '변경 실패' 토스트 메시지 띄우기
            if (exception) {
                Toast.makeText(getApplicationContext(), "예외 발생!변경 실패", Toast.LENGTH_SHORT).show();
                exception = false;
                dbOutputs.remove(this);
                this.cancel(true);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Toast.makeText(getApplicationContext(), cmd_string, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Socket socket = null;

            try {
                while (true) {

                    if(isCancelled())break;

                    if (value02 == null) {
                        cmd_string = command + " " + channel + " " + value01;
                    } else {
                        cmd_string = command + " " + channel + " " + value01 + " " + value02;
                    }
                    if (socket != null)
                        socket.close();

                    socket = new Socket(urlstr, port);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    bw.write(cmd_string + "\r"); //원인은 \r이었음..
                    bw.newLine();
                    bw.close();

                    publishProgress();

                    //명령어를 octot에 전달한 후에 wait()상태로 들어감
                    synchronized (this) {
                        this.wait();
                    }
                }
            } catch (IllegalMonitorStateException e2) {

            } catch(InterruptedException e3){
               System.out.println("Interrupt!");
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("에러 메시지 : " + e.getMessage());
                exception = true;
            }
            return null;
        }


    }
    public class DialogManager{
        AlertDialog dataStopDialog=null;
        AlertDialog setDialog=null;
        AlertDialog OKDialog,FLDialog=null;
        AlertDialog sendCmdFailDialog=null;
        AlertDialog machineInfoDialog=null;
        AlertDialog askDialog=null;
        ProgressDialog progressDialog=new ProgressDialog(ControlActivity.this, R.style.AppCompatAlertDialogStyle);

        public void showDialog(View v) {

            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ControlActivity.this);
            View mView = layoutInflaterAndroid.inflate(R.layout.dialoglayout, null);
            AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(ControlActivity.this);
            alertDialogBuilderUserInput.setView(mView);
            dialogSetValue = mView.findViewById(R.id.dialogSetValue);
            dialogSetRoc=mView.findViewById(R.id.dialogSetRoc);

            final DBOutput dbOutput = new DBOutput(machineinfo.getIP(), machineinfo.getPort());

            switch (v.getId()) {
                case R.id.setting_layout0:
                    dbOutput.setChannel("0");
                    dialogSetValue.setText(S[0].getText().toString());
                    dialogSetRoc.setText(R0.getText().toString());
                    break;
                case R.id.setting_layout1:
                    dbOutput.setChannel("1");
                    dialogSetValue.setText(S[1].getText().toString());
                    dialogSetRoc.setText(R1.getText().toString());
                    break;
                case R.id.setting_layout2:
                    dbOutput.setChannel("2");
                    dialogSetValue.setText(S[2].getText().toString());
                    dialogSetRoc.setText(R2.getText().toString());
                    break;
                case R.id.setting_layout3:
                    dbOutput.setChannel("3");
                    dialogSetValue.setText(S[3].getText().toString());
                    dialogSetRoc.setText(R3.getText().toString());
                    break;
            }
            dialogSetValue.setSelection(dialogSetValue.length());
            dialogSetRoc.setSelection(dialogSetRoc.length());

            alertDialogBuilderUserInput
                    .setTitle("변경할 값을 입력하세요.")
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton("변경", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //dbOutput.command="SVL";
                            dbOutput.setCommand("SVL");
                            //value01=count.getText().toString();
                            dbOutput.setValue01(String.valueOf((int)(Float.parseFloat(dialogSetValue.getText().toString())*10)));
                            dbOutput.setValue02(String.valueOf((int)(Float.parseFloat(dialogSetRoc.getText().toString())*10)));

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                dbOutput.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
                            }
                            else {
                                dbOutput.execute();
                            }
                            dbOutputs.add(dbOutput);
                        }
                    });

            setDialog = alertDialogBuilderUserInput.create();
            setDialog.show();

        }
        public void showAlertDialog(String date){
            AlertDialog.Builder builder = new AlertDialog.Builder(ControlActivity.this);
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
        public void stopAlertDialog(){
            if(dataStopDialog!=null && dataStopDialog.isShowing())
                dataStopDialog.dismiss();
        }
        public void showOKDialog(){
            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ControlActivity.this);
            View mView = layoutInflaterAndroid.inflate(R.layout.ok_dialog_layout, null);
            AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(ControlActivity.this);
            alertDialogBuilderUserInput.setView(mView);
            sucess_count=mView.findViewById(R.id.success_count);
            sucess_count.setText(OK.getText().toString());
            sucess_count.setSelection(sucess_count.length());

            final DBOutput dbOutput = new DBOutput(machineinfo.getIP(), machineinfo.getPort());

            alertDialogBuilderUserInput
                    .setTitle("개수를 입력하세요.")
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton("변경", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbOutput.setCommand("SM");
                            dbOutput.setChannel("0");
                            dbOutput.setValue01(sucess_count.getText().toString());
                            //value01=count.getText().toString();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                dbOutput.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
                            }
                            else {
                                dbOutput.execute();

                            }
                            dbOutputs.add(dbOutput);
                        }
                    });

            OKDialog = alertDialogBuilderUserInput.create();
            OKDialog.show();

        }
        public void showFLDialog(){
            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ControlActivity.this);
            View mView = layoutInflaterAndroid.inflate(R.layout.fl_dialog_layout, null);
            AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(ControlActivity.this);
            alertDialogBuilderUserInput.setView(mView);
            fail_count=mView.findViewById(R.id.fail_count);
            fail_count.setText(FL.getText().toString());

            final DBOutput dbOutput = new DBOutput(machineinfo.getIP(), machineinfo.getPort());
            alertDialogBuilderUserInput
                    .setTitle("개수를 입력하세요.")
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setPositiveButton("변경", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbOutput.setCommand("SM");
                            dbOutput.setChannel("1");
                            dbOutput.setValue01(sucess_count.getText().toString());
                            //value01=count.getText().toString();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                dbOutput.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);

                            }
                            else {
                                dbOutput.execute();
                            }
                            dbOutputs.add(dbOutput);
                        }
                    });

            FLDialog = alertDialogBuilderUserInput.create();
            FLDialog.show();
        }
        public void showSendCmdFailDialog(String command,String cmd_string){
            String msg="";
            switch(command){
                case "SVL":
                    msg="설정값 변경을 실패하였습니다.(시도 횟수 : 3)\n명령어 : "+cmd_string;
                    break;
                case "DO":
                    msg="토글 변경을 실패하였습니다.(시도 횟수 : 3)\n명령어 : "+cmd_string;
                    break;
                case "SM":
                    msg="양품/불량품 변경을 실패하였습니다.(시도 횟수 : 3)\n명령어 : "+cmd_string;
                    break;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(ControlActivity.this);
            builder.setTitle("Alert");
            builder.setMessage(msg);
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            sendCmdFailDialog = builder.create();
            sendCmdFailDialog.show();
        }
        public void showProgressDialog(){
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("잠시만 기다려주세요.");
            //dialog.setCancelable(false); //dialog 외부 터치시 dialog창이 없이지지 않게함.
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            if(!progressDialog.isShowing())
                progressDialog.show();
        }

        public void stopProgressDialog(){
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();
        }
        public void showMachineInfoDialog(){

            AlertDialog.Builder builder = new AlertDialog.Builder(ControlActivity.this);
            builder.setTitle(machineinfo.getMachinename());
            builder.setMessage("factory : "+machineinfo.getFactory()+"\ngroup : "+machineinfo.getGroup()+
                    "\nIP : "+machineinfo.getIP()+"\nPort : "+machineinfo.getPort());

            machineInfoDialog = builder.create();
            machineInfoDialog.show();
        }
        public void showAskDialog(String s_type,String t_type,String i){
            final String s=s_type;
            final String t=t_type;
            final String index=i;
            final String sensorname=machineinfo.getSensor(Integer.parseInt(index));
            AlertDialog.Builder builder = new AlertDialog.Builder(ControlActivity.this);
            builder.setTitle(machineinfo.getMachinename());
            builder.setMessage(sensorname +"의 실시간 그래프를 확인하시겠습니까?");
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent=new Intent(ControlActivity.this,RealtimeChartActivity.class);
                    intent.putExtra("machineInfo",machineinfo);
                    intent.putExtra("index",index);
                    /*
                    intent.putExtra("S_type",s);
                    intent.putExtra("T_type",t);
                    intent.putExtra("machineNum",machineinfo.getMachineNum());
                    intent.putExtra("machineName",machineinfo.getMachinename());
                    intent.putExtra("groupName",machineinfo.getGroup());
                    intent.putExtra("factory",machineinfo.getFactory());
                    intent.putExtra("sensorname",sensorname);
                    */
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            askDialog = builder.create();
            askDialog.show();
        }

    }
    class State {
        ImageView state_led = (ImageView) findViewById(R.id.state_led);
        TextView time = (TextView) findViewById(R.id.time);

        public void setTime(String date){
            time.setText("마지막 데이터 : "+date); //마지막으로 받아온 데이터의 날짜를 time에 표시
        }
        public void setStateStop(String date) {
            state_led.setImageResource(R.drawable.led_red);
            for (int i = 0; i < 4; i++) {
                S[i].setTextColor(Color.GRAY);
                settings_layout[i].setEnabled(false);
            }
            for (int i = 0; i < 8; i++) {
                chartbtn[i].setEnabled(false);
                T[i].setTextColor(Color.GRAY);
            }
            R0.setTextColor(Color.GRAY);
            R1.setTextColor(Color.GRAY);
            R2.setTextColor(Color.GRAY);
            R3.setTextColor(Color.GRAY);
            OK.setTextColor(Color.GRAY);
            FL.setTextColor(Color.GRAY);
            for (int i = 0; i < 4; i++)
                toggle_btn[i].setEnabled(false);

            dialogManager.showAlertDialog(date);
            dialogManager.stopProgressDialog();
        }

        public void setStateUpdating(HashMap<String, Float> value) {

            dialogManager.stopProgressDialog();
            dialogManager.stopAlertDialog();
            changeValue(value);

            state_led.setImageResource(R.drawable.led_green);
            for (int i = 0; i < 4; i++) {
                S[i].setTextColor(Color.BLACK);
                settings_layout[i].setEnabled(true);
            }
            for (int i = 0; i < 8; i++) {
                chartbtn[i].setEnabled(true);
                T[i].setTextColor(Color.BLACK);
            }
            R0.setTextColor(Color.BLACK);
            R1.setTextColor(Color.BLACK);
            R2.setTextColor(Color.BLACK);
            R3.setTextColor(Color.BLACK);
            OK.setTextColor(Color.BLACK);
            FL.setTextColor(Color.BLACK);
            for (int i = 0; i < 4; i++)
                toggle_btn[i].setEnabled(true);

            startAnimation.setRepeatCount(1);
            state_led.startAnimation(startAnimation);
        }

        public void setStateReady() {
            dialogManager.showProgressDialog();
            setTime("준비중....");
            state_led.setImageResource(R.drawable.led_yellow);
            for (int i = 0; i < 4; i++) {
                S[i].setTextColor(Color.BLACK);
                settings_layout[i].setEnabled(true);
            }
            for (int i = 0; i < 8; i++) {
                chartbtn[i].setEnabled(true);
                T[i].setTextColor(Color.BLACK);
            }
            R0.setTextColor(Color.BLACK);
            R1.setTextColor(Color.BLACK);
            R2.setTextColor(Color.BLACK);
            R3.setTextColor(Color.BLACK);
            OK.setTextColor(Color.BLACK);
            FL.setTextColor(Color.BLACK);
            for (int i = 0; i < 4; i++)
                toggle_btn[i].setEnabled(true);
        }

        public void changeOnButton(int i) {
            switch (i) {
                case 0:
                    toggle_btn[0].setBackgroundDrawable(getResources().getDrawable(R.drawable.onstroke));
                    toggle_btn[0].setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.on, 0, 0);
                    break;
                case 1:
                    toggle_btn[1].setBackgroundDrawable(getResources().getDrawable(R.drawable.onstroke));
                    toggle_btn[1].setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.fan_on, 0, 0);
                    break;
                case 2:
                    toggle_btn[2].setBackgroundDrawable(getResources().getDrawable(R.drawable.onstroke));
                    toggle_btn[2].setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.light_on, 0, 0);
                    break;
                case 3:
                    toggle_btn[3].setBackgroundDrawable(getResources().getDrawable(R.drawable.onstroke));
                    toggle_btn[3].setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.alarm_on, 0, 0);
                    break;

            }

        }
        public void changeOffButton(int i) {
            switch (i) {
                case 0:
                    toggle_btn[0].setBackgroundDrawable(getResources().getDrawable(R.drawable.offstroke));
                    toggle_btn[0].setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.off, 0, 0);
                    break;
                case 1:
                    toggle_btn[1].setBackgroundDrawable(getResources().getDrawable(R.drawable.offstroke));
                    toggle_btn[1].setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.fan_off, 0, 0);
                    break;
                case 2:
                    toggle_btn[2].setBackgroundDrawable(getResources().getDrawable(R.drawable.offstroke));
                    toggle_btn[2].setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.light_off, 0, 0);
                    break;
                case 3:
                    toggle_btn[3].setBackgroundDrawable(getResources().getDrawable(R.drawable.offstroke));
                    toggle_btn[3].setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.alarm_off, 0, 0);
                    break;
            }

        }

        protected void changeValue(HashMap<String, Float> value){
            S[0].setText(String.format("%.1f", value.get("S0")));
            T[0].setText(String.format("%.1f", value.get("T0")));
            R0.setText(String.format("%.1f", value.get("R0")));

            S[1].setText(String.format("%.1f", value.get("S1")));
            T[1].setText(String.format("%.1f", value.get("T1")));
            R1.setText(String.format("%.1f", value.get("R1")));

            S[2].setText(String.format("%.1f", value.get("S2")));
            T[2].setText(String.format("%.1f", value.get("T2")));
            R2.setText(String.format("%.1f", value.get("R2")));

            S[3].setText(String.format("%.1f", value.get("S3")));
            T[3].setText(String.format("%.1f", value.get("T3")));
            R3.setText(String.format("%.1f", value.get("R3")));

            T[4].setText(String.format("%.1f", value.get("T4")));
            T[5].setText(String.format("%.1f", value.get("T5")));
            T[6].setText(String.format("%.1f", value.get("T6")));
            T[7].setText(String.format("%.1f", value.get("T7")));

            OK.setText(String.format("%.0f", value.get("OK")));
            FL.setText(String.format("%.0f", value.get("FL")));

            if(value.get("L4")==1.0 && dbOutputs.size()==0){
                toggle_btn[0].setChecked(true);
                changeOnButton(0);
            }
            else if(value.get("L4")==0.0 && dbOutputs.size()==0){
                toggle_btn[0].setChecked(false);
                if(dbOutputs.size()==0)
                    changeOffButton(0);
            }

            if(value.get("L5")==1.0 && dbOutputs.size()==0){
                toggle_btn[1].setChecked(true);
                changeOnButton(1);
            }
            else if(value.get("L5")==0.0 && dbOutputs.size()==0){
                toggle_btn[1].setChecked(false);
                changeOffButton(1);
            }

            if(value.get("L6")==1.0 && dbOutputs.size()==0){
                toggle_btn[2].setChecked(true);
                changeOnButton(2);
            }
            else if(value.get("L6")==0.0 && dbOutputs.size()==0){
                toggle_btn[2].setChecked(false);
                changeOffButton(2);
            }

            if(value.get("L7")==1.0 && dbOutputs.size()==0){
                toggle_btn[3].setChecked(true);
                changeOnButton(3);
            }
            else if(value.get("L7")==0.0 && dbOutputs.size()==0){
                toggle_btn[3].setChecked(false);
                changeOffButton(3);
            }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id==R.id.nav_datachart){
            Intent intent=new Intent(getApplicationContext(),DataVisualizingActivity.class);
            startActivity(intent);
            finish();
        }
        else if(id==R.id.nav_settings){
            Intent intent=new Intent(getApplicationContext(),SettingsActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}