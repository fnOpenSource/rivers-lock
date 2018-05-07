package com.fn.rivers.correspond;

import java.io.Serializable;
import java.util.Random;

import com.fn.rivers.GlobalParam;
import com.fn.rivers.GlobalParam.MESSAGE_TYPE;

/**
 * 
 * @author chenwen
 *
 */
public class Request implements Serializable{ 
	private static final long serialVersionUID = 6020511430031014157L;
	private final int id; 
	private final MESSAGE_TYPE type;
	private final String source_ip = GlobalParam.NODE_IP;
	private String destinationIp;
	private int port; 
	private final Object data;
	private final long createTime  = System.currentTimeMillis();
	private final String flag;

	/**
	 * 
	 * @param type Request Service Type
	 * @param port Message Send port
	 * @param destinationIp Message Send Destination
	 * @param flag Message Flag
	 * @param data Message contents
	 */
	public Request(MESSAGE_TYPE type,int port,String destinationIp,String flag,Object data) {
		this.id = new Random().nextInt(99999999); 
		this.type = type; 
		this.destinationIp = destinationIp;
		this.port = port;
		this.flag = flag;
		this.data = data;
	}
	
	public void setDestinationIp(int port,String destinationIp) {
		this.port = port;
		this.destinationIp = destinationIp;
	}
	
	public int getId() {
		return id;
	}

	public MESSAGE_TYPE getType() {
		return type;
	}
 
	public Object getData() {
		return data;
	} 

	public String getSourceIP() {
		return source_ip;
	}

	public int getPort() {
		return port;
	}

	public String getDestinationIp() {
		return destinationIp;
	}

	public long getCreateTime() {
		return createTime;
	}

	public String getFlag() {
		return flag;
	}  
}
