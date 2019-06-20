package org.liunxprobe.spring.datasource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class MultiDataSource extends AbstractRoutingDataSource {
	private final static Logger logger = LoggerFactory.getLogger(MultiDataSource.class);
	private final static String masterKey = "master";
	private final ThreadLocal<String> contextHolder = new ThreadLocal<String>();
	private Map<Object, Object> targetDataSources;
	/** 从库keys */
	private List<String> slaveDataSourceKeys;

	private int keyIndex = 0;

	public MultiDataSource(Map<Object, Object> targetDataSources) {
		if (targetDataSources == null || targetDataSources.isEmpty()) {
			throw new IllegalArgumentException("targetDataSources can not be null");
		}
		super.setTargetDataSources(targetDataSources);
		initSlaveDataSourceKeys(targetDataSources);
	}

	public MultiDataSource() {
	}

	@SuppressWarnings("static-access")
	public void initSlaveDataSourceKeys(Map<Object, Object> targetDataSources) {
		if (targetDataSources.get(this.masterKey) == null) {
			throw new IllegalArgumentException("master datasorce can not be null");
		}
		this.targetDataSources = targetDataSources;
		this.slaveDataSourceKeys = new LinkedList<>();
		Set<Object> keys = targetDataSources.keySet();
		for (Object key : keys) {
			if (!masterKey.equals(key.toString())) {
				this.slaveDataSourceKeys.add(key.toString());
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
		if (logger.isDebugEnabled()) {
			logger.debug("switch master datasource");
		}
	}

	/** 标记从库 */
	public void markSlave() {
		if (this.slaveDataSourceKeys == null || this.slaveDataSourceKeys.isEmpty()) {
			this.markMaster();
		} else {
			synchronized (this) {
				contextHolder.set(this.slaveDataSourceKeys.get(keyIndex));
				this.keyIndex++;
				if (keyIndex >= this.slaveDataSourceKeys.size()) {
					keyIndex = 0;
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("switch {} datasource", contextHolder.get());
			}
		}
	}

	/** 根据key标记库 */
	public void markByKey(String key) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("key can not be null");
		}
		contextHolder.set(key);
		if (logger.isDebugEnabled()) {
			logger.debug("switch {} datasource", key);
		}
	}

	public String getDataSourceKey() {
		String key = contextHolder.get();
		if (key == null) {
			key = masterKey;
			this.markMaster();
		}
		return key;
	}

	public void clearDataSourceKey() {
		contextHolder.remove();
	}

	public String getDriverClassName() {
		Object dataSource = this.targetDataSources.get(this.getDataSourceKey());
		String driverClassName = null;
		Method method;
		try {
			method = dataSource.getClass().getMethod("getDriverClassName");
			driverClassName = (String) method.invoke(dataSource);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return driverClassName;
	}
}
