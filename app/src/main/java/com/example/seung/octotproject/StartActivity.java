package com.example.seung.octotproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class StartActivity extends AppCompatActivity {

    MachineInfoReader machineInfoReader;
    ArrayList<String> factories=new ArrayList<>();
    ArrayList<String> machineNumber=new ArrayList<>();
    ArrayList<String> groupNames=new ArrayList<>();
    ArrayList<String> machineNames=new ArrayList<>();
    ArrayList<String> octotIPs=new ArrayList<>();
    ArrayList<String> octotPorts=new ArrayList<>();
    HashMap<String,ArrayList<HashMap<String,ArrayList<String>>>> factory_pair_groups=new HashMap<>();
    HashMap<String,ArrayList<String>> group_pair_machines=new HashMap<>();
    HashMap<String,Integer> machine_pair_machineNum=new HashMap<>();
    HashMap<String,ArrayList<String>> machines_sensor=new HashMap<>();
    ArrayList<String> groups=new ArrayList<>();
    String update_date="";
    MyApplication octotdata;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        octotdata=new MyApplication();

        machineInfoReader=new MachineInfoReader("http://"+octotdata.serverIP+"/machineinfo_data.php");
        machineInfoReader.execute();

    }

    protected void showAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        builder.setTitle("Alert");
        builder.setMessage("서버와의 접속이 원할하지 않습니다.\n다시 시도하시겠습니까?");
        builder.setPositiveButton("재시도", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                machineInfoReader.cancel(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    machineInfoReader = new MachineInfoReader("http://"+octotdata.serverIP+"/machineinfo_data.php");
                    machineInfoReader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
                }
                else {
                    machineInfoReader = new MachineInfoReader("http://" + octotdata.serverIP + "/machineinfo_data.php");
                    machineInfoReader.execute();
                }
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    protected void showAlertDialog2(){
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        builder.setTitle("Alert");
        builder.setMessage("등록된 기계 없음!\n먼저 기계 등록을 하시길 바랍니다.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public class MachineInfoReader extends AsyncTask<Void,Void,Boolean> {
        String urlstr;
        ProgressDialog dialog=new ProgressDialog(StartActivity.this,R.style.AppCompatAlertDialogStyle);

        int count=-100;
        String jsonstr;


        MachineInfoReader(String url){
            octotdata=(MyApplication)getApplication();
            octotdata.init();
            urlstr=url;
        }

        @Override
        protected void onPreExecute() {
            //octotdata.machines=new ArrayList<>();
            //octotdata.factorylist=new ArrayList<>();

        }

        @Override
        protected void onProgressUpdate(Void... values) {

            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("데이터 불러오는 중...\n잠시만 기다려주세요.");
            dialog.show();

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            BufferedReader reader;
            JSONObject jObject;
            Boolean return_value=false;


            try {
                publishProgress();
                URL url = new URL(urlstr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                if (conn != null) {
                    conn.setConnectTimeout(5000);   //연결 timeout
                    conn.setRequestMethod("GET");   //데이터 전송 방식

                    conn.setDoInput(true);   //데이터 input 허용

                    int resCode = conn.getResponseCode();

                    if (resCode == HttpURLConnection.HTTP_OK) {

                        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));


                        while ((jsonstr = reader.readLine()) == null);

                        JSONArray jarray = new JSONArray(jsonstr);
                        System.out.println("jsonstr : "+jsonstr);

                        count = jarray.length(); //등록된 기계의 개수
                        System.out.println(count);

                        if(count>0) {
                            //System.out.println("팩토리 개수 : "+octotdata.factories.get("factory1").get("그룹01").getMachineList().size());
                            for (int i = 0; i < count; i++) {
                                jObject = jarray.getJSONObject(i);

                                if (i == 0) {
                                    update_date = jObject.getString("date");
                                }

                                Machine machine=new Machine();
                                String machineName=jObject.getString("machineName");
                                String ip=jObject.getString("ip");
                                int port=Integer.parseInt(jObject.getString("port"));
                                String groupName=jObject.getString("groupName");
                                String factoryName=jObject.getString("factory");
                                int machineNum=Integer.parseInt(jObject.getString("machineNum"));

                                machine.setMachinename(machineName);
                                machine.setMachineNum(machineNum);
                                machine.setIP(ip);
                                machine.setPort(port);
                                machine.setFactory(factoryName);
                                machine.setGroup(groupName);
                                machine.setSensor(jObject.getString("sensor00"),0);
                                machine.setSensor(jObject.getString("sensor01"),1);
                                machine.setSensor(jObject.getString("sensor02"),2);
                                machine.setSensor(jObject.getString("sensor03"),3);
                                machine.setSensor(jObject.getString("sensor04"),4);
                                machine.setSensor(jObject.getString("sensor05"),5);
                                machine.setSensor(jObject.getString("sensor06"),6);
                                machine.setSensor(jObject.getString("sensor07"),7);

                                if(!octotdata.factories.containsKey(factoryName)){
                                    System.out.println("미포함1");
                                    Factory factory=new Factory();
                                    octotdata.factories.put(factoryName,factory);
                                    if(!octotdata.factorylist.contains(factoryName)){
                                        octotdata.factorylist.add(factoryName);
                                    }

                                }
                                else
                                    System.out.println("factory 이미 포함");

                                if(octotdata.factories.get(factoryName).get(groupName)==null){
                                    System.out.println("미포함2");
                                    Group group=new Group();
                                    octotdata.factories.get(factoryName).addGroup(groupName,group);
                                    octotdata.factories.get(factoryName).addGroupname(groupName);
                                }
                                else
                                    System.out.println("group 이미 포함");


                                octotdata.factories.get(factoryName).get(groupName).addMachine(machineName,machine);
                                octotdata.factories.get(factoryName).get(groupName).addMachinename(machineName);
                                octotdata.machines.add(machine);
                                System.out.println("개수 : "+octotdata.factories.get(factoryName).get(groupName).machines.size());

                            }
                            //setMyApplicationVariable();
                            return_value=true;
                        }
                        else
                            return_value=false;

                    }
                }
            }
            catch (SocketTimeoutException e) {
                System.out.println("SocketTimeoutException: " + e.getMessage());
                return_value=false;
            }
            catch (Exception e) {
                e.printStackTrace();
                return_value=false;
            }
            return return_value;
        }
        public void setMyApplicationVariable(){
            try{

                for(int i=0;i<count;i++){
                    String factory=factories.get(i);
                    String group_name=groupNames.get(i);
                    String machine_name=machineNames.get(i);
                    int machine_num=Integer.parseInt(machineNumber.get(i));

                    if(factory_pair_groups.get(factory)==null){
                        factory_pair_groups.put(factory,new ArrayList<HashMap<String, ArrayList<String>>>());
                    }
                    if(group_pair_machines.get(group_name)==null) {   //그룹이름이 없으면
                        group_pair_machines.put(group_name, new ArrayList<String>());
                        groups.add(group_name);
                    }
                    group_pair_machines.get(group_name).add(machine_name);
                    factory_pair_groups.get(factory).add(group_pair_machines);

                    machine_pair_machineNum.put(machine_name,machine_num);
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }
        @Override
        protected void onPostExecute(Boolean bool) {

            dialog.dismiss();

            if(count==0){
                showAlertDialog2();
            }
            else {
                if (bool) {
                    octotdata.setUpdate_date(update_date);
                    //System.out.println("update_date : "+octotdata.getUpdate_date());
                    octotdata.setMachineCount(count);
                    octotdata.setGroups(groups);
                    octotdata.setFactory_pair_groups(factory_pair_groups);
                    octotdata.setGroup_pair_machines(group_pair_machines);
                    octotdata.setMachineNum_pair_machines(machine_pair_machineNum);
                    octotdata.setSensors(machines_sensor);
                    octotdata.setOctoTIPs(octotIPs);
                    octotdata.setOctoTPorts(octotPorts);
                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else
                    showAlertDialog();
            }

        }
    }
}

