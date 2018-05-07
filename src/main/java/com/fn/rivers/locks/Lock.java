package com.fn.rivers.locks;

import java.util.concurrent.TimeUnit;
/**
 * 
 * @author chenwen
 *
 */
public interface Lock {
	public boolean lock(String LockName);

	public boolean tryLock(String LockName);

	public boolean tryLock(String LockName,long timeout, TimeUnit unit);
	
	public boolean unLock(String LockName);
	 
}
