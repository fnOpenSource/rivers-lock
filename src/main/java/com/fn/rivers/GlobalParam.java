package com.fn.rivers;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fn.rivers.correspond.SendRequestProcessor;
import com.fn.rivers.model.LockerNodesModel;
import com.fn.rivers.model.LockerStoreModel;
import com.fn.rivers.model.NodeServersModel;
import com.fn.rivers.server.Server;

/**
 * Store global static data
 * 
 * @author chenwen
 *
 */
public class GlobalParam {
	
	public static String StartMode;
	
	public static int mininum_nodes = 0;
	
	public static int discardMessageQueueSize = 300;
	
	public static int nodeHeartBeatTime = 60000;
	
	public static int lockMaxWaitTime = 30000;
	
	public static final int BC_PORT = 8618;
	
	public static final int UNI_PORT = 8619;
	
	public static String BC_IP = "230.0.0.1";
	 
	public static List<String> CLOUD_HOSTS = new ArrayList<>();
	
	public static volatile NodeServersModel CLOUD_NODES = new NodeServersModel();
	 
	public static volatile LockerStoreModel LOCKERS = new LockerStoreModel();
	
	public static volatile LockerNodesModel lockerHoldNodes = new LockerNodesModel(); 
	
	public static ConcurrentHashMap<String, PriorityQueue<Server>> waitLockerServers = new ConcurrentHashMap<String, PriorityQueue<Server>>();
	
	public static String CLOUD_NAME="RIVERS";
	
	public static SendRequestProcessor SendRequestProcessor;
	
	public static final Logger LOG = LoggerFactory.getLogger("River.Locker");
	
	public static enum MESSAGE_SEND_TYPE {  
		BROCAST, UNICAST 
	} 
	
	public static enum MESSAGE_TYPE{
		CLOUD_LIVECHECK(100),
		CLOUD_LIVERESPONSE(101),
		
		P2P_LOCK_VOTE(200),
		P2P_LOCK_AGREE(201),
		P2P_LOCK_DISAGREE(202),
		P2P_LOCK_BRC_GETLOCK(205),
		P2P_LOCK_RECEIVE(210),
		P2P_LOCK_BRC_FREELOCK(220),
		
		CS_LOCK_ASK(300),
		CS_LOCK_AGREE(301),
		CS_LOCK_DISAGREE(302), 
		CS_LOCK_RECEIVE(310),
		CS_LOCK_UNI_FREELOCK(320),
		
		
		LEADER_VOTE(400),
		LEADER_AGREE(401),
		LEADER_DISAGREE(402), 
		LEADER_LIVECHECK(410),
		LEADER_LIVERESPONSE(411),
		LEARDER_INFO_BRC(420),
		LEADER_LOCKREQUEST(520),
		LEADER_LOCKAGREE(521),
		LEADER_LOCKDISAGREE(521),
		LEADER_LOCKRELEASE(530);
		 
	    private int val; 
	    private MESSAGE_TYPE(int val) {   
	        this.val = val;  
	    }  
	    public int getVal() {
	    	return this.val;
	    }
	}
	
	public static enum MESSAGE_CLOUD{
		LIVECHECK,LIVERESPONSE
	}
	
	public static enum MESSAGE_P2P_LOCK{
		LIVECHECK,LIVERESPONSE
	}
	
	public static enum MESSAGE_CS_LOCK{
		LIVECHECK,LIVERESPONSE
	}
	
	public static enum MESSAGE_LEADER{
		LIVECHECK,LIVERESPONSE
	}
	
	/**current host ip */
	public static String NODE_IP;
	 
	static {
		NODE_IP = getHostIp();
	}
	
	static String getHostIp(){  
	    try{  
	        Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();  
	        while (allNetInterfaces.hasMoreElements()){  
	            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();  
	            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();  
	            while (addresses.hasMoreElements()){  
	                InetAddress ip = (InetAddress) addresses.nextElement();  
	                if (ip != null   
	                        && ip instanceof Inet4Address  
	                        && !ip.isLoopbackAddress()   
	                        && ip.getHostAddress().indexOf(":")==-1){
	                    return ip.getHostAddress();  
	                }   
	            }  
	        }  
	    }catch(Exception e){  
	        e.printStackTrace();  
	    }  
	    return null;  
	}  
}
