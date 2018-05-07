package com.fn.rivers;

import java.io.IOException;
import java.math.BigInteger;

import com.fn.rivers.locks.LockerContainer;
import com.fn.rivers.server.Locker;
import com.fn.rivers.server.LockerElection;
import com.fn.rivers.server.LockerLeader;

/**
 * 
 * @author chenwen
 *
 */
public class river { 
	
	public static void setHosts(String hosts) {
		for(String ip:hosts.split(",")) {
			GlobalParam.CLOUD_HOSTS.add(ip);
		}  
	}
	
	public static void setMininum_nodes(int mininum_nodes) {
		GlobalParam.mininum_nodes = mininum_nodes;
	}
	
	public static void setCloudName(String cloudName) {
		GlobalParam.CLOUD_NAME = cloudName;
		GlobalParam.BC_IP =  new BigInteger(cloudName.getBytes()).mod(new BigInteger("255")).toString();
	}
	
	/**
	 * 
	 * @param startMode  CS leader server Mode, P2P no leader mode.
	 */
	public static void setStartMode(String startMode) {
		GlobalParam.StartMode = startMode;
	}
	
	/**
	 * @param 
	 * @throws IOException 
	 */
	public static void start() throws IOException {
		Locker locker;
		GlobalParam.CLOUD_NAME = GlobalParam.CLOUD_NAME+"_"+GlobalParam.StartMode;
		if(GlobalParam.StartMode.equals("CS")) {
			locker = new LockerLeader();
		}else {
			locker = new LockerElection();
		}
		LockerContainer.init(locker);
	}
	
	public static String getLockerState() {
		return null; 
	}
}
