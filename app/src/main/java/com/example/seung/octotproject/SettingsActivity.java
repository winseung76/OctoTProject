package com.example.seung.octotproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class SettingsActivity extends AppCompatActivity {

    EditText search;
    Spinner factory,group;
    MyApplication octotdata;
    String fn,gn;
    ListManager listManager;
    FloatingActionButton fab;
    MyService myService;
    TextView edit_name;
    boolean isService=false;
    private ArrayList<String> temp_factories=new ArrayList<>();
    private ArrayList<String> temp_groups=new ArrayList<>();
    ArrayAdapter<String> group_adapter,fad;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Intent intent = new Intent(this, MyService.class); // 다음넘어갈 컴퍼넌트
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        octotdata=(MyApplication)getApplication();
        listManager=new ListManager();
        listManager.initTempFactories();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        search=(EditText)findViewById(R.id.search);
        factory=(Spinner)findViewById(R.id.factory);
        group=(Spinner)findViewById(R.id.group);
        edit_name=(TextView)findViewById(R.id.edit_name);
        edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditNameDialog();
            }
        });
        fab=(FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View listview= listManager.createListView();
                listManager.showEditDialog(listview,null);
            }
        });

        setFactorySpinnerAdapter();
        //setGroupSpinnerAdapter();
        //factory 스피너에서 아이템을 선택할때 발생하는 이벤트
        factory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                fn = factory.getSelectedItem().toString();
                ((TextView) adapterView.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                ((TextView) adapterView.getChildAt(0)).setGravity(Gravity.CENTER);

                listManager.initTempGroups();
                setGroupSpinnerAdapter();

                group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                        gn = group.getSelectedItem().toString();
                        listManager.loadMachineInfo();
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
        //setFactorySpinnerAdapter();
    }
    public void setGroupSpinnerAdapter() {

        if(group_adapter==null) {
            group_adapter = new ArrayAdapter<String>(SettingsActivity.this, android.R.layout.simple_spinner_dropdown_item,
                    temp_groups) {
                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15); //dp
                    tv.setTextColor(Color.BLACK);

                    return view;
                }

            };
        }
        else
            System.out.println("호출"+group_adapter.getCount()+" "+temp_groups.size());

        group.setAdapter(group_adapter);

    }
    public void setFactorySpinnerAdapter(){
        fad = new ArrayAdapter<String>(SettingsActivity.this, android.R.layout.simple_spinner_dropdown_item,
                temp_factories) {
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15); //dp
                return view;
            }
        };
        factory.setAdapter(fad);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_item, menu) ;

        return true ;
    }
    //액션바의 메뉴를 선택했을 시에 발생하는 이벤트
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.save) {
            MachineInfoSender sender=new MachineInfoSender("http://"+octotdata.serverIP+"/register_machine_mobile.php");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                sender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            }
            else {
                sender.execute();
            }
        }
        return super.onOptionsItemSelected(menuItem);
    }
    public void showEditNameDialog(){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(SettingsActivity.this);
        View mView = layoutInflaterAndroid.inflate(R.layout.editname_dialog_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        final EditText factory=mView.findViewById(R.id.factory_name);
        final EditText group=mView.findViewById(R.id.group_name);

        factory.setText(fn);
        group.setText(gn);

        builder.setView(mView);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int f_index=temp_factories.indexOf(fn);
                String str1=factory.getText().toString();
                for(int i=0;i<listManager.temp_machines.size();i++){
                    if(listManager.getTemp_machines().get(i).getFactory().equals(fn)){
                        listManager.getTemp_machines().get(i).setFactory(str1);
                    }
                }
                temp_factories.set(f_index,str1);

                int g_index=temp_groups.indexOf(gn);
                String str2=group.getText().toString();
                for(int i=0;i<listManager.temp_machines.size();i++){
                    if(listManager.getTemp_machines().get(i).getGroup().equals(gn)){
                        listManager.getTemp_machines().get(i).setGroup(str2);
                    }
                }
                temp_groups.set(g_index,str2);
                setFactorySpinnerAdapter();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    class MachineInfoSender extends AsyncTask<Void,Void,Void>{
        private CopyOnWriteArrayList<NameValuePair> post = new CopyOnWriteArrayList<NameValuePair>();
        private String urlstr;
        ProgressDialog dialog=new ProgressDialog(SettingsActivity.this);

        MachineInfoSender(String urlstr){this.urlstr=urlstr;}

        @Override
        protected void onPreExecute() {
            dialog.setMessage("기계 정보를 수정중입니다.\n잠시만 기다려주세요.");
            dialog.show();
        }

        public void setPost(){
            System.out.println("machine_list 크기 : "+listManager.getTemp_machines().size());
            for(int i=0;i<listManager.getTemp_machines().size();i++){
                post.add(new BasicNameValuePair("ip[]", listManager.getTemp_machines().get(i).getIP()));
                post.add(new BasicNameValuePair("port[]", String.valueOf(listManager.getTemp_machines().get(i).getPort())));
                post.add(new BasicNameValuePair("machine[]", listManager.getTemp_machines().get(i).getMachinename()));
                post.add(new BasicNameValuePair("group[]", listManager.getTemp_machines().get(i).getGroup()));
                post.add(new BasicNameValuePair("factory[]",listManager.getTemp_machines().get(i).getFactory()));
                post.add(new BasicNameValuePair("sensor00[]", listManager.getTemp_machines().get(i).getSensor(0)));
                post.add(new BasicNameValuePair("sensor01[]", listManager.getTemp_machines().get(i).getSensor(1)));
                post.add(new BasicNameValuePair("sensor02[]", listManager.getTemp_machines().get(i).getSensor(2)));
                post.add(new BasicNameValuePair("sensor03[]", listManager.getTemp_machines().get(i).getSensor(3)));
                post.add(new BasicNameValuePair("sensor04[]", listManager.getTemp_machines().get(i).getSensor(4)));
                post.add(new BasicNameValuePair("sensor05[]", listManager.getTemp_machines().get(i).getSensor(5)));
                post.add(new BasicNameValuePair("sensor06[]", listManager.getTemp_machines().get(i).getSensor(6)));
                post.add(new BasicNameValuePair("sensor07[]", listManager.getTemp_machines().get(i).getSensor(7)));

            }
        }
        @Override
        protected Void doInBackground(Void... voids) {

            InputStream inputStream = null;
            BufferedReader rd;
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

                    System.out.println(rd.readLine());
                }
                else
                    System.out.println("inputStream=null");

            }catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //기계정보가 변경되었다는 대화상자가 나타날때까지 반복하여 대기
            while(!myService.getShowAlert()){
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e){}
            }
            //기계정보가 변경되었다는 대화상자가 나타나면 기존의 progress 대화상자를 종료한다.
            dialog.dismiss();
        }
    }
    class ListManager  {
        private LinearLayout list_layout;
        private LayoutInflater inflater;
        private ArrayList<Machine> temp_machines;


        ListManager(){
            list_layout=(LinearLayout)findViewById(R.id.list_layout);
            inflater = (LayoutInflater)getApplicationContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            //temp machine list를 초기화
            initTempMachines();
        }

        public ArrayList<Machine> getTemp_machines(){return temp_machines;}

        public View createListView(){
            View listview= inflater.inflate(R.layout.listitem_layout,null);

            return listview;
        }
        public void initTempFactories(){
            temp_factories=new ArrayList<>();
            System.out.println("factory list : "+octotdata.factorylist.size());
            for(int i=0;i<octotdata.factorylist.size();i++){

                temp_factories.add(octotdata.factorylist.get(i));
            }

        }
        public void initTempGroups(){
            temp_groups=new ArrayList<>();
            for(int i=0;i<temp_machines.size();i++){
                if(temp_machines.get(i).getFactory().equals(fn)){
                    temp_groups.add(temp_machines.get(i).getGroup());
                }
            }
            /*
            for(int i=0;i<octotdata.factories.get(fn).groupnames.size();i++){
                temp_groups.add(octotdata.factories.get(fn).groupnames.get(i));
            }
            */
        }
        public void initTempMachines(){
            temp_machines=new ArrayList<>();
            for(int i=0;i<octotdata.machines.size();i++){
                Machine machine=octotdata.machines.get(i);

                Machine new_machine=new Machine();

                new_machine.setIP(machine.getIP());
                new_machine.setPort(machine.getPort());
                new_machine.setGroup(machine.getGroup());
                new_machine.setFactory(machine.getFactory());
                for(int k=0;k<8;k++){
                    new_machine.setSensor(machine.getSensor(k),k);
                }
                new_machine.setMachineNum(machine.getMachineNum());
                new_machine.setMachinename(machine.getMachinename());

                temp_machines.add(new_machine);
            }
        }
        //기계정보를 리스트뷰의 기계정보에 넣는 메소드
        public void putMachineInfo(View listview,Machine machine){

            TextView name=listview.findViewById(R.id.name);
            TextView address=listview.findViewById(R.id.address);
            TextView port=listview.findViewById(R.id.port);
            TextView sensors=listview.findViewById(R.id.sensors);

            name.setText(machine.getMachinename());
            address.setText(machine.getIP());
            port.setText(String.valueOf(machine.getPort()));

            String str="";
            for(int j=0;j<8;j++){
                str+=machine.getSensor(j);
                if(j<7)
                    str+=", ";
            }
            sensors.setText(str);
        }

        //각 팩토리와 그룹에 속한 기계들을 불러와서 리스트뷰에 로드시키는 메소드
        public void loadMachineInfo(){

            list_layout.removeAllViews();

            for(int i=0;i<temp_machines.size();i++){
                System.out.println(temp_machines.get(i).getGroup()+" and "+gn);
                //현재 선택된 팩토리 정보와 그룹 정보가 일치하는 기계일경우, Machine 객체를 가져온다.
                if(temp_machines.get(i).getFactory().equals(fn) && temp_machines.get(i).getGroup().equals(gn)){
                    final Machine machine=temp_machines.get(i);
                    final View listview= createListView();
                    ImageView edit=listview.findViewById(R.id.edit);
                    ImageView delete=listview.findViewById(R.id.delete);

                    putMachineInfo(listview,machine);

                    edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showEditDialog(listview,machine);
                        }
                    });
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder=new AlertDialog.Builder(SettingsActivity.this);
                            builder.setMessage(machine.getMachinename()+"를 삭제하시겠습니까?");
                            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteMachineInfo(listview, machine);
                                }
                            });
                            builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            Dialog dialog=builder.create();
                            dialog.show();
                        }
                    });
                    list_layout.addView(listview);

                }
            }
        }
        public void deleteMachineInfo(View listview, Machine machine){
            temp_machines.remove(machine);
            list_layout.removeView(listview);
        }
        public void showEditDialog(final View view,final Machine machineinfo){

            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(SettingsActivity.this);
            View mView = layoutInflaterAndroid.inflate(R.layout.edit_dialog_layout, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);

            final EditText factory=mView.findViewById(R.id.factory);
            final EditText group=mView.findViewById(R.id.group);
            final EditText machine=mView.findViewById(R.id.machine);
            final EditText address=mView.findViewById(R.id.address);
            final EditText port=mView.findViewById(R.id.port);
            final EditText[] sensors=new EditText[8];
            sensors[0]= mView.findViewById(R.id.sensor0);
            sensors[1]= mView.findViewById(R.id.sensor1);
            sensors[2]= mView.findViewById(R.id.sensor2);
            sensors[3]= mView.findViewById(R.id.sensor3);
            sensors[4]= mView.findViewById(R.id.sensor4);
            sensors[5]= mView.findViewById(R.id.sensor5);
            sensors[6]= mView.findViewById(R.id.sensor6);
            sensors[7]= mView.findViewById(R.id.sensor7);

            factory.setText(fn);
            group.setText(gn);
            if(machineinfo!=null) {
                //기존의 machine 정보를 edittext에 띄우기

                machine.setText(machineinfo.getMachinename());
                address.setText(machineinfo.getIP());
                port.setText(String.valueOf(machineinfo.getPort()));
                for (int i = 0; i < 8; i++) {
                    sensors[i].setText(machineinfo.getSensor(i));
                }
            }

            builder.setView(mView);
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //사용자가 '저장'버튼을 누르기 전에 수정된 기계 정보를 임시로 저장해두는 Machine 객체 생성
                    Machine temp_machine=new Machine();

                    temp_machine.setIP(address.getText().toString());
                    temp_machine.setPort(Integer.parseInt(port.getText().toString()));
                    temp_machine.setFactory(factory.getText().toString());
                    temp_machine.setGroup(group.getText().toString());
                    temp_machine.setMachinename(machine.getText().toString());
                    for(int i=0;i<8;i++){
                        temp_machine.setSensor(sensors[i].getText().toString(),i);
                    }

                    //machineinfo가 null이란 뜻은 기존에 기계정보가 없었다는 의미이다.
                    if(machineinfo==null) {
                        temp_machine.setMachineNum(temp_machines.size());
                        temp_machines.add(temp_machine);
                        list_layout.addView(view);

                        if(!temp_factories.contains(temp_machine.getFactory())){
                            temp_factories.add(temp_machine.getFactory());
                        }
                        if(!temp_groups.contains(temp_machine.getGroup())){
                            temp_groups.add(temp_machine.getGroup());
                        }
                    }
                    else{
                        temp_machine.setMachineNum(machineinfo.getMachineNum());
                        int k=listManager.getTemp_machines().indexOf(machineinfo);
                        temp_machines.set(k,temp_machine);
                    }
                    putMachineInfo(view,temp_machine);
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog=builder.create();
            dialog.show();

            Display display=getWindowManager().getDefaultDisplay();
            Point size=new Point();
            display.getSize(size);
            Window window=dialog.getWindow();

            int x=size.x;
            int y=(int)(size.y*0.7f);
            window.setLayout(x,y);

        }
    }
}
