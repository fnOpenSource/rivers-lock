package com.fn.rivers.model;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.fn.rivers.GlobalParam;
import com.fn.rivers.server.Server;

/**
 * 
 * @author chengwen
 *
 */
public class NodeServersModel extends ConcurrentHashMap<String,Server>{
 
	private static final long serialVersionUID = 3853697334487290009L;
	
	public int liveNums() {
		int i=0;
		Iterator<Entry<String, Server>>  etr = entrySet().iterator();
		while(etr.hasNext()) {
			if(etr.next().getValue().isOnline.get()) {
				i++;
			}
		}
		return i;
	}
	
	public Server currentServer() {
		if(!this.containsKey(GlobalParam.NODE_IP)) {
			this.put(GlobalParam.NODE_IP, new Server(GlobalParam.NODE_IP));
		}
		return this.get(GlobalParam.NODE_IP); 
	}
	
	public boolean isLeader() {
		return currentServer().isLeader.get();
	}
	
	public Server getLeader() {
		Iterator<Entry<String, Server>>  etr = entrySet().iterator();
		while(etr.hasNext()) {
			Server sev = etr.next().getValue();
			if(sev.isOnline.get() && sev.isLeader.get()) {
				return sev;
			}
		}
		return null;
	} 
}
