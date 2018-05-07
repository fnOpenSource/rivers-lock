package com.fn.rivers.server;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fn.rivers.GlobalParam;
import com.fn.rivers.GlobalParam.MESSAGE_SEND_TYPE;
import com.fn.rivers.GlobalParam.MESSAGE_TYPE;
import com.fn.rivers.correspond.Request;

/**
 * 
 * @author chengwen
 *
 */
public class LeaderMaintain extends Thread {

	public static volatile AtomicBoolean isElecting = new AtomicBoolean(false); 

	public void run() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				leaderCheck();
			}
		};
		new Timer().schedule(task, 2000, 3000);
	}

	public static void leaderCheck() {
		long time = System.currentTimeMillis();
		Server sev = GlobalParam.CLOUD_NODES.getLeader();
		if (sev != null) {
			if (sev.isOnline.get()) {
				if (time - sev.timeStamp.get() > GlobalParam.nodeHeartBeatTime)
					GlobalParam.SendRequestProcessor.put(new Request(MESSAGE_TYPE.LEADER_LIVECHECK,
							GlobalParam.UNI_PORT, sev.getIp(), GlobalParam.CLOUD_NAME, null),
							MESSAGE_SEND_TYPE.UNICAST);
			} else {
				ServerMaintain.serverRemove(sev.getIp());
				electLeader();
			}
		} else {
			electLeader();
		}
	}

	public static void setLeader(String ip) {
		GlobalParam.CLOUD_NODES.get(ip).updateOnline();
		GlobalParam.CLOUD_NODES.get(ip).isLeader.set(true);
	}

	private static void electLeader() { 
		synchronized (isElecting) {
			if(GlobalParam.CLOUD_NODES.liveNums() >= GlobalParam.mininum_nodes && !isElecting.getAndSet(true)) { 
				GlobalParam.LOG.info("Start Leader Elect...");
				GlobalParam.SendRequestProcessor.put(
						new Request(MESSAGE_TYPE.LEADER_VOTE, GlobalParam.BC_PORT, "", GlobalParam.CLOUD_NAME, null),
						MESSAGE_SEND_TYPE.BROCAST); 
			}
		} 
	}
}
