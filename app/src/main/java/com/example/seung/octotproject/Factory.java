package com.example.seung.octotproject;

import java.util.ArrayList;
import java.util.HashMap;

public class Factory {

    HashMap<String, Group> groups=new HashMap<>();
    ArrayList<String> groupnames=new ArrayList<>();

    Group get(String groupname){
        return groups.get(groupname);
    }

    ArrayList<String> getGroupList(){
        return groupnames;
    }
    public void addGroupname(String groupname) {
        if(!groupnames.contains(groupname)) {
            this.groupnames.add(groupname);
        }
    }

    public void addGroup(String groupName,Group group){
        this.groups.put(groupName,group);
    }
}
