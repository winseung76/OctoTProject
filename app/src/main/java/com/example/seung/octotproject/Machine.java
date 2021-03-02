package com.example.seung.octotproject;

import java.io.Serializable;

public class Machine implements Serializable{
    private String ip;
    private int port,machineNum;
    private String[] sensors=new String[8];
    private String factory,group;
    private String machinename;

    String getSensor(int i){ return sensors[i]; }
    public void setIP(String ip){ this.ip=ip; }
    public String getIP(){ return this.ip; }

    public void setPort(int port){this.port=port; }
    public int getPort(){ return this.port; }

    public void setSensor(String sensor,int i){ this.sensors[i]=sensor; }

    public void setFactory(String factory){ this.factory=factory; }
    public String getFactory() { return factory; }

    public void setGroup(String group){ this.group=group; }
    public String getGroup() { return group; }

    public void setMachinename(String machinename){ this.machinename=machinename; }
    public String getMachinename(){ return machinename; }

    public void setMachineNum(int machineNum){ this.machineNum=machineNum; }
    public int getMachineNum(){ return machineNum; }

}