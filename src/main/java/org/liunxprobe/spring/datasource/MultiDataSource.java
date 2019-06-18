package org.liunxprobe.spring.datasource;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class MultiDataSource extends AbstractRoutingDataSource {
	private final static String masterKey = "master";
	private final ThreadLocal<String> contextHolder = new ThreadLocal<String>();
	/** 从库keys */
	private List<String> slaveDataSourceKeys;

	private int keyIndex = 0;

	public MultiDataSource(HashMap<Object, Object> targetDataSources) {
		if (targetDataSources == null || targetDataSources.isEmpty()) {
			throw new IllegalArgumentException("targetDataSources can not be null");
		}
		super.setTargetDataSources(targetDataSources);
		initSlaveDataSourceKeys(targetDataSources);
	}

	public MultiDataSource() {
	}

	public void initSlaveDataSourceKeys(Map<Object, Object> targetDataSources) {
		slaveDataSourceKeys = new LinkedList<>();
		Set<Object> keys = targetDataSources.keySet();
		for (Object key : keys) {
			if (!masterKey.equals(key.toString())) {
				slaveDataSourceKeys.add(key.toString());
			}
		}
	}

	@Override
	public void setTargetDataSources(Map<Object, Object> targetDataSources) {
		if (targetDataSources == null || targetDataSources.isEmpty()) {
			throw new IllegalArgumentException("targetDataSources can not be null");
		}
		super.setTargetDataSources(targetDataSources);
		initSlaveDataSourceKeys(targetDataSources);
	}

	@Override
	protected Object determineCurrentLookupKey() {
		return this.getDataSourceKey();
	}

	/** 标记主库 */
	public void markMaster() {
		contextHolder.set(masterKey);
	}

	/** 标记从库 */
	public synchronized void markSlave() {
		contextHolder.set(this.slaveDataSourceKeys.get(keyIndex));
		this.keyIndex++;
		if (keyIndex >= this.slaveDataSourceKeys.size()) {
			keyIndex = 0;
		}
	}

	/** 根据key标记库 */
	public void markByKey(String key) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("key can not be null");
		}
		contextHolder.set(key);
	}

	public String getDataSourceKey() {
		String key = contextHolder.get();
		if (key == null) {
			key = masterKey;
			contextHolder.set(masterKey);
		}
		return key;
	}

	public void clearDataSourceKey() {
		contextHolder.remove();
	}
}
