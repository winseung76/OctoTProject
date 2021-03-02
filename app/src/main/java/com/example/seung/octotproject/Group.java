package com.example.seung.octotproject;

import java.util.ArrayList;
import java.util.HashMap;

public class Group {

    HashMap<String, Machine> machines=new HashMap<>();
    ArrayList<String> machinenames=new ArrayList<>();

    ArrayList<String> getMachineList(){
        return machinenames;
    }
    Machine get(String machine_name){
        return machines.get(machine_name);
    }
    public void addMachine(String machinename,Machine machine){
        this.machines.put(machinename,machine);
    }
    public void addMachinename(String machinename){
        if(!machinenames.contains(machinename)) {
            this.machinenames.add(machinename);
        }
    }

}
