package com.fn.rivers.locks;

import java.util.concurrent.TimeUnit;
/**
 * 
 * @author chenwen
 *
 */
public class ReentrantLock extends LockerContainer implements Lock {
	 
	public boolean lock(String LockName) {
		return getLocker().acquire(LockName); 
	}

	public boolean tryLock(String LockName) {
		return getLocker().acquire(LockName); 
	}

	public boolean tryLock(String LockName, long timeout, TimeUnit unit) {
		return getLocker().acquire(LockName); 
	}

	public boolean unLock(String LockName) {
		return getLocker().release(LockName); 
	}  
}
