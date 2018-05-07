package com.fn.rivers.model;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.fn.rivers.GlobalParam;
import com.fn.rivers.server.Server;

public class LockerNodesModel extends ConcurrentHashMap<String, ArrayList<Server>>{
	 
	private static final long serialVersionUID = 5049642819442333794L;

	public ArrayList<Server> getAndSet(String key) {
		if(!this.containsKey(key)) {
			this.put(key, new ArrayList<Server>());
		}
		return this.get(key);
	}  
	
	public void addServers(String key,ArrayList<Server> sevs) { 
		for(Server sev:sevs) {
			addAndSet(key, sev);
		}
	}
	
	public void addAndSet(String key,Server sev) { 
		if(!getAndSet(key).contains(sev)) {
			get(key).add(sev);
		} 
	} 
	
	public void removeAndSet(String key,String ip) { 
		Server sev = GlobalParam.CLOUD_NODES.get(ip);
		if(getAndSet(key).contains(sev)) {
			get(key).remove(sev);
		} 
	}
}
