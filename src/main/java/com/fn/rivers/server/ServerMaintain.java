package com.fn.rivers.server;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

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
public class ServerMaintain extends Thread {

	private static final Logger LOG = LoggerFactory.getLogger(ServerMaintain.class);

	public void run() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				serverCheck();
			}
		};
		new Timer().schedule(task, 2000, 3000);
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
				LOG.info("Node " + ip + " Auto Remove From Cloud!");
			}
		}
	}

	public static void setLeader(String ip) {
		GlobalParam.CLOUD_NODES.get(ip).updateOnline();
		GlobalParam.CLOUD_NODES.get(ip).isLeader.set(true);
	}

	public static void serverAdd(String ip) {
		synchronized (GlobalParam.CLOUD_NODES) {
			if (!GlobalParam.CLOUD_NODES.containsKey(ip)) {
				GlobalParam.CLOUD_NODES.put(ip, new Server(ip));
				LOG.info("Node " + ip + " Join Cloud!");
			} else {
				GlobalParam.CLOUD_NODES.get(ip).updateOnline();
			}
		}
	}
}
