package com.fn.rivers.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @author chengwen
 *
 */
public class LockerStoreModel extends ConcurrentHashMap<String, AtomicBoolean>{
 
	private static final long serialVersionUID = -3118060274722341621L;
	 
	public AtomicBoolean getAndSet(String key) {
		if(!this.containsKey(key)) {
			this.put(key, new AtomicBoolean(false));
		} 
		return this.get(key);
	} 
}
