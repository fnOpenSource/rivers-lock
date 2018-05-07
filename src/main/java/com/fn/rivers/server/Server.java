package com.fn.rivers.server;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
/**
 * 
 * @author chenwen
 *
 */
public class Server implements Comparable<Server>{
	
	private String ip;
	
	public AtomicBoolean isLeader = new AtomicBoolean(false);
	
	public AtomicBoolean isOnline = new AtomicBoolean(true); 
	
	public AtomicLong timeStamp  = new AtomicLong();

	public Server(String ip) {
		this.ip = ip;
		this.timeStamp.set(System.currentTimeMillis());
	}
	
	public String getIp() {
		return ip;
	} 
	
	public void updateOnline() {
		this.isOnline.set(true);
		this.timeStamp.set(System.currentTimeMillis());
	}

	@Override
	public int compareTo(Server sev) { 
		if(this.ip.equals(sev.getIp())) {
			return 0;
		}else {
			return 1;
		} 
	}
}
