package com.fn.rivers.server;

import java.util.Timer;
import java.util.TimerTask;

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

	private static long electTime;

	public void run() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				leaderCheck(false);
			}
		};
		new Timer().schedule(task, 2000, 3000);
	}

	public static void leaderCheck(boolean forceElect) {
		if (forceElect) {
			electLeader();
		} else {
			long time = System.currentTimeMillis();
			Server sev = GlobalParam.CLOUD_NODES.getLeader();
			if (sev != null) {
				if (sev.isOnline.get()) {
					if (time - sev.timeStamp.get() > GlobalParam.nodeHeartBeatTime)
						GlobalParam.SendRequestProcessor.put(
								new Request(MESSAGE_TYPE.LEADER_LIVECHECK, GlobalParam.UNI_PORT, sev.getIp(), GlobalParam.CLOUD_NAME, null),
								MESSAGE_SEND_TYPE.UNICAST);
				} else {
					ServerMaintain.serverRemove(sev.getIp());
					electLeader();
				}
			}else {
				electLeader();
			}
		}
	}

	private static void electLeader() {
		if (System.currentTimeMillis() - electTime > GlobalParam.nodeHeartBeatTime) {
			electTime = System.currentTimeMillis();
			GlobalParam.SendRequestProcessor.put(
					new Request(MESSAGE_TYPE.LEADER_VOTE, GlobalParam.BC_PORT, "", GlobalParam.CLOUD_NAME, null), MESSAGE_SEND_TYPE.BROCAST);
		}
	}
}
