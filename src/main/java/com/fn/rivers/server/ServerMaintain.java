package com.fn.rivers.server;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import com.fn.rivers.GlobalParam;
import com.fn.rivers.GlobalParam.MESSAGE_SEND_TYPE;
import com.fn.rivers.GlobalParam.MESSAGE_TYPE;
import com.fn.rivers.correspond.Request;

/**
 * 
 * @author chenwen
 *
 */
public class ServerMaintain extends Thread { 

	public void run() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				serverCheck();
			}
		};
		new Timer().schedule(task, 0, 3000);
	}

	public static void serverCheck() {
		long time = System.currentTimeMillis();
		Iterator<Entry<String, Server>> etr = GlobalParam.CLOUD_NODES.entrySet().iterator();
		while (etr.hasNext()) {
			Server r = etr.next().getValue();
			if (r.isOnline.get()) {
				if (time - r.timeStamp.get() > GlobalParam.nodeHeartBeatTime)
					GlobalParam.SendRequestProcessor.put(new Request(MESSAGE_TYPE.LEADER_LIVECHECK,
							GlobalParam.UNI_PORT, r.getIp(), GlobalParam.CLOUD_NAME, null), MESSAGE_SEND_TYPE.UNICAST);
			} else {
				serverRemove(r.getIp());
			}
		}
	}

	public static void serverRemove(String ip) {
		synchronized (GlobalParam.CLOUD_NODES) {
			if (GlobalParam.CLOUD_NODES.containsKey(ip)) {
				GlobalParam.CLOUD_NODES.remove(ip);
				GlobalParam.LOG.info("Node " + ip + " Auto Remove From Cloud!");
			}
		}
	} 

	public static void serverAdd(String ip) {
		synchronized (GlobalParam.CLOUD_NODES) {
			if (!GlobalParam.CLOUD_NODES.containsKey(ip)) {
				GlobalParam.CLOUD_NODES.put(ip, new Server(ip));
				GlobalParam.LOG.info("Node " + ip + " Join Cloud!");
			} else {
				GlobalParam.CLOUD_NODES.get(ip).updateOnline();
			}
		}
	}
}
