package com.fn.rivers.correspond;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fn.rivers.GlobalParam;
import com.fn.rivers.GlobalParam.MESSAGE_SEND_TYPE;
import com.fn.rivers.GlobalParam.MESSAGE_TYPE;
import com.fn.rivers.server.Server;
import com.fn.rivers.server.ServerMaintain; 

/**
 * 
 * @author chenwen
 *
 */
public class NodeCPU {
	private static final Logger LOG = LoggerFactory.getLogger(NodeCPU.class);
	
	/**sum each vote numbers*/
	private static volatile ConcurrentHashMap<String, AtomicInteger> ackPassNums = new ConcurrentHashMap<>();
	
	/**store discard message id*/
	private static volatile ConcurrentLinkedQueue<Integer> discardMessageId = new ConcurrentLinkedQueue<>(); 
	
	private List<String> node_ips = new ArrayList<>();
 
	public void init() throws UnknownHostException, IOException {
		node_ips.addAll(GlobalParam.CLOUD_HOSTS);
		broadServiceStart();
		unicastServiceStart();
	}

	private void broadServiceStart() {
		new Thread(() -> {
			try (MulticastSocket socket = new MulticastSocket(GlobalParam.BC_PORT);) {
				InetAddress bcAddr = InetAddress.getByName(GlobalParam.BC_IP);
				socket.joinGroup(bcAddr);
				socket.setLoopbackMode(true);
				GlobalParam.SendRequestProcessor = new SendRequestProcessor();
				GlobalParam.SendRequestProcessor.init(socket, bcAddr);
				LOG.info("Start Auto Cloud Building...");
				AutoCloudInit();
				DatagramPacket inpack = new DatagramPacket(new byte[4096], 4096);
				while (true) {
					socket.receive(inpack);
					ByteArrayInputStream BAIS = new ByteArrayInputStream(inpack.getData());
					Request rq = (Request) new ObjectInputStream(BAIS).readObject();
					if(rq != null && !discardMessage(rq)) {
						if (rq.getType().getVal() < 200) {
							autoCloud(rq, MESSAGE_SEND_TYPE.BROCAST);
						} else if (rq.getType().getVal() < 300) {
							p2pLockMessage(rq, MESSAGE_SEND_TYPE.BROCAST);
						} else if (rq.getType().getVal() < 400) {
							csLockMessage(rq, MESSAGE_SEND_TYPE.BROCAST);
						} else if (rq.getType().getVal() < 500) {
							leaderMessage(rq, MESSAGE_SEND_TYPE.BROCAST);
						}
					} 
				}
			} catch (Exception e) {
				LOG.error("Broad Cast Service IOException", e);
			}
		}).start();
	}

	private void unicastServiceStart() {
		new Thread(() -> {
			try (ServerSocket listener = new ServerSocket(GlobalParam.UNI_PORT);) {
				while (true) {
					Socket client = listener.accept();
					new UnicastHandler(client);
				}
			} catch (Exception e) {
				LOG.error("unicast Service Exception", e);
			}
		}).start();
	}

	class UnicastHandler implements Runnable {
		private Socket socket;

		public UnicastHandler(Socket client) {
			socket = client;
			new Thread(this).start();
		}

		public void run() {
			try {
				InputStream is = socket.getInputStream();
				Request rq = (Request) new ObjectInputStream(is).readObject();
				if (rq != null && !discardMessage(rq)) {
					if (rq.getType().getVal() < 200) {
						autoCloud(rq, MESSAGE_SEND_TYPE.UNICAST);
					} else if (rq.getType().getVal() < 300) {
						p2pLockMessage(rq, MESSAGE_SEND_TYPE.UNICAST);
					} else if (rq.getType().getVal() < 400) {
						csLockMessage(rq, MESSAGE_SEND_TYPE.UNICAST);
					} else if (rq.getType().getVal() < 500) {
						leaderMessage(rq, MESSAGE_SEND_TYPE.UNICAST);
					}
				}
				is.close();
			} catch (Exception e) {
				LOG.error("Receive Message Exception,", e);
			}
		}
	}
 
	@SuppressWarnings("unchecked")
	private void autoCloud(Request rq, MESSAGE_SEND_TYPE messageType) { 
		if (rq.getType()==MESSAGE_TYPE.CLOUD_LIVECHECK) { 
			GlobalParam.SendRequestProcessor.put(
					new Request(MESSAGE_TYPE.CLOUD_LIVERESPONSE, messageType == MESSAGE_SEND_TYPE.BROCAST ? GlobalParam.BC_PORT : GlobalParam.UNI_PORT,
							rq.getSourceIP(), rq.getFlag(), node_ips),
					messageType);
			ServerMaintain.serverAdd(rq.getSourceIP());
		} else {
			List<String> tmp = (List<String>) rq.getData();
			node_ips.removeAll(tmp);
			node_ips.addAll(tmp);
			for(String ip:node_ips) {
				ServerMaintain.serverAdd(ip);
			}
		}
	}

	private void leaderMessage(Request rq, MESSAGE_SEND_TYPE messageType) {
		discardMessageId.add(rq.getId());
		switch (rq.getType()) {
		case LEADER_VOTE:
			if(GlobalParam.CLOUD_NODES.isLeader()) {
				GlobalParam.SendRequestProcessor.put(
						new Request(MESSAGE_TYPE.LEADER_DISAGREE, GlobalParam.UNI_PORT,
								rq.getSourceIP(), rq.getFlag(), GlobalParam.CLOUD_NODES.currentServer()),
						MESSAGE_SEND_TYPE.UNICAST);
			}else {
				GlobalParam.SendRequestProcessor.put(
						new Request(MESSAGE_TYPE.LEADER_AGREE, GlobalParam.UNI_PORT,
								rq.getSourceIP(), rq.getFlag(), null),
						MESSAGE_SEND_TYPE.UNICAST);
			}
			break;
			
		case LEADER_AGREE:
			if(ackPassNums.containsKey(rq.getFlag())) {
				if(ackPassNums.get(rq.getFlag()).addAndGet(1)>GlobalParam.CLOUD_HOSTS.size()/2) {
					GlobalParam.CLOUD_NODES.currentServer().isLeader.set(true);
					GlobalParam.SendRequestProcessor.put(
							new Request(MESSAGE_TYPE.LEARDER_BRC_CONFIRM, GlobalParam.BC_PORT, "",rq.getFlag(),null),
							MESSAGE_SEND_TYPE.BROCAST);
				} 	
			}else {
				ackPassNums.put(rq.getFlag(), new AtomicInteger(1));
			}
			break;
			
		case LEADER_DISAGREE:
			ServerMaintain.setLeader(rq.getSourceIP());
			break;
			
		case LEADER_LIVECHECK:
			GlobalParam.SendRequestProcessor.put(
					new Request(MESSAGE_TYPE.LEADER_LIVERESPONSE, GlobalParam.UNI_PORT,
							rq.getSourceIP(), rq.getFlag(), null),
					MESSAGE_SEND_TYPE.UNICAST);
			ServerMaintain.serverAdd(rq.getSourceIP());
			break;
			
		case LEADER_LOCKREQUEST:
			if(GlobalParam.lockerHoldNodes.getAndSet(rq.getFlag()).size()==0) {
				GlobalParam.lockerHoldNodes.addAndSet(rq.getFlag(), new Server(GlobalParam.NODE_IP));
				GlobalParam.SendRequestProcessor.put(
						new Request(MESSAGE_TYPE.LEADER_LOCKAGREE, GlobalParam.UNI_PORT,
								rq.getSourceIP(), rq.getFlag(), null),
						MESSAGE_SEND_TYPE.UNICAST);
			}else {
				GlobalParam.SendRequestProcessor.put(
						new Request(MESSAGE_TYPE.LEADER_DISAGREE, GlobalParam.UNI_PORT,
								rq.getSourceIP(), rq.getFlag(), null),
						MESSAGE_SEND_TYPE.UNICAST);
			}
			break;
			
		case LEADER_LOCKAGREE:
			synchronized (GlobalParam.LOCKERS.getAndSet(rq.getFlag())) { 
				GlobalParam.LOCKERS.get(rq.getFlag()).set(true);
				GlobalParam.LOCKERS.get(rq.getFlag()).notifyAll(); 
			}
			break;
			
		case LEADER_LOCKDISAGREE:
			synchronized (GlobalParam.LOCKERS.getAndSet(rq.getFlag())) { 
				GlobalParam.LOCKERS.get(rq.getFlag()).set(false);
				GlobalParam.LOCKERS.get(rq.getFlag()).notifyAll(); 
			}
			break;
			
		case LEADER_LOCKRELEASE:
			GlobalParam.lockerHoldNodes.removeAndSet(rq.getFlag(),rq.getSourceIP());
			break;
			
		default:
			break;	
		}
	}
 
	private void csLockMessage(Request rq, MESSAGE_SEND_TYPE messageType) {
		
	}
	
	@SuppressWarnings("unchecked")
	private void p2pLockMessage(Request rq, MESSAGE_SEND_TYPE messageType) {
		discardMessageId.add(rq.getId());
		switch (rq.getType()) {
		case P2P_LOCK_VOTE:
			synchronized (GlobalParam.LOCKERS.getAndSet(rq.getFlag())) { 
				if (GlobalParam.LOCKERS.get(rq.getFlag()).get()) { 
					GlobalParam.SendRequestProcessor.put(
							new Request(MESSAGE_TYPE.P2P_LOCK_DISAGREE, GlobalParam.UNI_PORT, rq.getSourceIP(),rq.getFlag(),GlobalParam.lockerHoldNodes.get(rq.getFlag())),
							MESSAGE_SEND_TYPE.UNICAST);
					if(!GlobalParam.waitLockerServers.containsKey(rq.getFlag())) {
						GlobalParam.waitLockerServers.put(rq.getFlag(), new PriorityQueue<>());
					} 
					GlobalParam.waitLockerServers.get(rq.getFlag()).add(GlobalParam.CLOUD_NODES.get(rq.getSourceIP()));
				} else {  
					GlobalParam.SendRequestProcessor.put(
							new Request(MESSAGE_TYPE.P2P_LOCK_AGREE, GlobalParam.UNI_PORT, rq.getSourceIP(),rq.getFlag(),null),
							MESSAGE_SEND_TYPE.UNICAST);
				}
			}
			break;

		case P2P_LOCK_AGREE:
			synchronized (GlobalParam.LOCKERS.getAndSet(rq.getFlag())) {
				if(ackPassNums.containsKey(rq.getFlag())) {
					if(ackPassNums.get(rq.getFlag()).addAndGet(1)>GlobalParam.CLOUD_HOSTS.size()/2) {
						successGetLock(rq);
						GlobalParam.SendRequestProcessor.put(
								new Request(MESSAGE_TYPE.P2P_LOCK_BRC_GETLOCK, GlobalParam.BC_PORT, "",rq.getFlag(),null),
								MESSAGE_SEND_TYPE.BROCAST);
					} 	
				}else {
					ackPassNums.put(rq.getFlag(), new AtomicInteger(1));
				}
			}
			break;
			
		case P2P_LOCK_DISAGREE:
			synchronized (GlobalParam.LOCKERS.getAndSet(rq.getFlag())) { 
				if(rq.getData()!=null) {
					GlobalParam.lockerHoldNodes.addServers(rq.getFlag(), (ArrayList<Server>) rq.getData());
				}  
				GlobalParam.LOCKERS.get(rq.getFlag()).set(false);
				GlobalParam.LOCKERS.get(rq.getFlag()).notifyAll(); 
			}
			break;
			
		case P2P_LOCK_BRC_GETLOCK:
			if(rq.getData()!=null) {
				GlobalParam.lockerHoldNodes.addServers(rq.getFlag(), (ArrayList<Server>) rq.getData());
			}  
			break;
			
		case P2P_LOCK_RECEIVE:
			synchronized (GlobalParam.LOCKERS.getAndSet(rq.getFlag())) { 
				successGetLock(rq);
			}
			break;
		default:
			break;
		}
	}
	
	private void successGetLock(Request rq) {
		GlobalParam.LOCKERS.get(rq.getFlag()).set(true);
		GlobalParam.LOCKERS.get(rq.getFlag()).notifyAll(); 
		GlobalParam.lockerHoldNodes.addAndSet(rq.getFlag(), new Server(GlobalParam.NODE_IP));
	}
	
	/**
	 * message time outoftime 60000 for machine time different error!
	 * @param rq
	 * @return
	 */
	private boolean discardMessage(Request rq) {
		if(discardMessageId.size()>GlobalParam.discardMessageQueueSize) {
			discardMessageId.poll();
		}
		if(discardMessageId.contains(rq.getId())) {
			return true;
		}
		if(rq.getCreateTime()-System.currentTimeMillis()>60000) {
			return true;
		}
		if(rq.getSourceIP().equals(GlobalParam.NODE_IP)) {
			return true;
		}
		return false;
	}

	private void AutoCloudInit() {
		for (String ip : GlobalParam.CLOUD_HOSTS) {
			GlobalParam.SendRequestProcessor.put(
					new Request(MESSAGE_TYPE.CLOUD_LIVECHECK, GlobalParam.UNI_PORT, ip, GlobalParam.CLOUD_NAME, null), MESSAGE_SEND_TYPE.UNICAST);
		} 
		GlobalParam.SendRequestProcessor.put(new Request(MESSAGE_TYPE.CLOUD_LIVECHECK, GlobalParam.UNI_PORT, "", GlobalParam.CLOUD_NAME, null),
				MESSAGE_SEND_TYPE.BROCAST);
	}
}
