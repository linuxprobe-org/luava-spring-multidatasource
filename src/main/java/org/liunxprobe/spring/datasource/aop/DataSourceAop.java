package org.liunxprobe.spring.datasource.aop;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.liunxprobe.spring.datasource.MultiDataSource;
import org.liunxprobe.spring.datasource.annotation.DataSource;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Aspect
public class DataSourceAop {
	private MultiDataSource multiDataSource;

	public DataSourceAop(MultiDataSource multiDataSource) {
		this.setMultiDataSource(multiDataSource);
	}

	@Pointcut("@annotation(org.liunxprobe.spring.datasource.annotation.DataSource)")
	public void pointcut() {
	}

	@Around("pointcut()")
	public Object switchDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
		if (this.multiDataSource == null) {
			throw new IllegalArgumentException("multiDataSource can not be null");
		}
		DataSource dataSource = ((MethodSignature) joinPoint.getSignature()).getMethod()
				.getAnnotation(DataSource.class);
		if (StringUtils.isNoneBlank(dataSource.value())) {
			this.multiDataSource.markByKey(dataSource.value());
		} else if (dataSource.slave()) {
			this.multiDataSource.markSlave();
		} else {
			this.multiDataSource.markMaster();
		}
		Object result = null;
		try {
			result = joinPoint.proceed();
		} catch (Throwable e) {
			throw e;
		} finally {
			this.multiDataSource.clearDataSourceKey();
		}
		return result;
	}

	public MultiDataSource getMultiDataSource() {
		return multiDataSource;
	}

	public void setMultiDataSource(MultiDataSource multiDataSource) {
		if (multiDataSource == null) {
			throw new IllegalArgumentException("multiDataSource can not be null");
		}
		this.multiDataSource = multiDataSource;
	}

	@PostConstruct
	public void init() {
		if (multiDataSource == null) {
			throw new IllegalArgumentException("multiDataSource can not be null");
		}
	}
}
