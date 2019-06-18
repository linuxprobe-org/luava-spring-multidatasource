package org.liunxprobe.spring.datasource.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.liunxprobe.spring.datasource.MultiDataSource;

import lombok.NoArgsConstructor;

@NoArgsConstructor
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
		this.multiDataSource.markMaster();
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
}
