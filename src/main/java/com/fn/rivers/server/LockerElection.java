package com.fn.rivers.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fn.rivers.GlobalParam;
import com.fn.rivers.GlobalParam.MESSAGE_SEND_TYPE;
import com.fn.rivers.GlobalParam.MESSAGE_TYPE;
import com.fn.rivers.correspond.Request;

/**
 * 
 * @author chenwen
 *
 */
public class LockerElection implements Locker { 

	private static final Logger LOG = LoggerFactory.getLogger(LockerElection.class); 

	public LockerElection() {
		new ServerMaintain().start();
		LOG.info("River Lock start success with Election mode!");
	}

	public boolean acquire(String LockName) { 
		if (LockName != null && checkIsReady()) { 
			synchronized (GlobalParam.LOCKERS.getAndSet(LockName)) {
				try { 
					if (GlobalParam.LOCKERS.get(LockName).get()) {
						return true;
					} else {
						GlobalParam.SendRequestProcessor.put(new Request(MESSAGE_TYPE.P2P_LOCK_VOTE, GlobalParam.BC_PORT, "", LockName, null),
								MESSAGE_SEND_TYPE.BROCAST);
					}
					GlobalParam.LOCKERS.get(LockName).wait(GlobalParam.lockMaxWaitTime);
					if(GlobalParam.LOCKERS.get(LockName).get())
						return true;
				} catch (InterruptedException e) {
					LOG.warn("Acquire Lock " + LockName + " InterruptedException", e);
				}
			}
		}
		return false;
	}

	public boolean release(String LockName) {
		if (GlobalParam.waitLockerServers.containsKey(LockName)) {
			Server server = GlobalParam.waitLockerServers.get(LockName).poll();
			GlobalParam.SendRequestProcessor.put(new Request(MESSAGE_TYPE.P2P_LOCK_BRC_GETLOCK, GlobalParam.UNI_PORT, server.getIp(), LockName, null),
					MESSAGE_SEND_TYPE.UNICAST);
			GlobalParam.waitLockerServers.remove(LockName);
		}
		GlobalParam.LOCKERS.remove(LockName);
		return false;
	}

	private boolean checkIsReady() {
		if (GlobalParam.SendRequestProcessor != null && GlobalParam.CLOUD_NODES.liveNums()>=GlobalParam.mininum_nodes) {
			return true;
		}else {
			LOG.info("Cloud start nodes not match mininum_nodes");
			return false;
		}
	}

}
