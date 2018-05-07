package com.fn.rivers.server;
/**
 * 
 * @author chenwen
 *
 */
public interface Locker {
	 public boolean acquire(String LockName);
	 public boolean release(String LockName);
}
