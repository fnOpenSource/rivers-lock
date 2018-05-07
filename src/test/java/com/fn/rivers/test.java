package com.fn.rivers;

import java.io.IOException;

import com.fn.rivers.locks.ReentrantLock;

public class test {
	public static void main(String[] args) throws IOException, InterruptedException {
		river.setHosts("10.200.200.100");
		river.setStartMode("P2P");
		river.setMininum_nodes(1);
		river.start(); 
		ReentrantLock lock = new ReentrantLock();
		Thread.sleep(1000);
		System.out.println("try get lock!");
		lock.lock("test");
		while(true) { 
			if(lock.lock("test")) {
				System.out.println("success get lock!");
				break;
			}else {
				System.out.println("lock occupy!");
			}
			Thread.sleep(3000);
		} 
		lock.unLock("test");
	}
}
