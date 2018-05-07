package com.fn.rivers.locks;

import java.io.IOException;

import com.fn.rivers.correspond.NodeCPU;
import com.fn.rivers.server.Locker;
/**
 * 
 * @author chenwen
 *
 */
public class LockerContainer {
	
	private static Locker locker;
	 
	public static void init(Locker _locker) throws IOException {
		locker = _locker;
		NodeCPU serverCnxn = new NodeCPU();
		serverCnxn.init(); 
	} 
	
	public Locker getLocker() {
		return locker;
	}
}
