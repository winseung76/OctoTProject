package com.example.seung.octotproject;

import android.app.Application;

import java.util.ArrayList;
import java.util.HashMap;


public class MyApplication extends Application {

    private HashMap<String,ArrayList<String>> sensors=new HashMap<>();
    private HashMap<String,ArrayList<HashMap<String,ArrayList<String>>>> factory_pair_groups=new HashMap<>();
    private HashMap<String,ArrayList<String>> group_pair_machines=new HashMap<>();  //key : groupName, value : 기계 배열
    private HashMap<String,Integer> machine_pair_machineNum=new HashMap<>();
    private int machine_count;
    private ArrayList<String> groups; //그룹 스피너 string 배열
    //String serverIP="truecoex.iptime.org:8080";
    String serverIP="172.30.1.57";
    private ArrayList<String> octoTIPs;
    private ArrayList<String> octoTPorts;
    private String membername;
    String update_date;
    ArrayList<Machine> machines=new ArrayList<>();
    HashMap<String,Factory> factories=new HashMap<>();
    ArrayList<String> factorylist=new ArrayList<>();


    public void init(){
        machines=new ArrayList<>();
        factories=new HashMap<>();
        factorylist=new ArrayList<>();
        groups=new ArrayList<>();
        octoTIPs=new ArrayList<>();
        octoTPorts=new ArrayList<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void setSensors(HashMap<String,ArrayList<String>> sensors){
        this.sensors=sensors;
    }
    public HashMap<String, ArrayList<String>> getSensors(){
        return this.sensors;
    }

    public void setGroup_pair_machines(HashMap<String,ArrayList<String>> group_pair_machines){
        this.group_pair_machines=group_pair_machines;
    }
    public  HashMap<String,ArrayList<String>> getGroup_pair_machines(){
        return group_pair_machines;
    }
    public void setMachineNum_pair_machines(HashMap<String,Integer> machineNum_pair_machines){
        this.machine_pair_machineNum=machineNum_pair_machines;
    }
    public  HashMap<String,Integer> getMachine_pair_machineNum(){
        return machine_pair_machineNum;
    }
    public void setGroups(ArrayList<String> groups){
        this.groups=groups;
    }
    public ArrayList<String> getGroups(){
        return groups;
    }

    public void setMachineCount(int count){ this.machine_count=count;}
    public int getMachineCount(){return this.machine_count;}

    public void setOctoTIPs(ArrayList<String> octoTIPs){this.octoTIPs=octoTIPs;}
    public ArrayList<String> getOctoTIPs(){return octoTIPs;}

    public void setOctoTPorts(ArrayList<String> octoTPorts){this.octoTPorts=octoTPorts;}
    public ArrayList<String> getOctoTPorts(){return octoTPorts;}

    public void setMembername(String membername){this.membername=membername;}
    public String getMembername(){return membername;}

    public void setUpdate_date(String update_date){this.update_date=update_date;}
    public String getUpdate_date(){return this.update_date;}

    public void setFactory_pair_groups(HashMap<String,ArrayList<HashMap<String,ArrayList<String>>>> factory_pair_groups){
        this.factory_pair_groups=factory_pair_groups;
    }

    public HashMap<String,ArrayList<HashMap<String,ArrayList<String>>>> getFactory_pair_groups(){
        return factory_pair_groups;
    }
    public ArrayList<String> getFactoryList(){
        return factorylist;
    }



}