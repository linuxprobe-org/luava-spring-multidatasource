package org.liunxprobe.spring.datasource.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** 标记使用数据源 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface DataSource {
	/** 是否标记主库 */
	public boolean master() default true;

	/** 是否标记从库, 当为true时, master失效 */
	public boolean slave() default false;

	/** 指定数据源名称, 当不为空时, slave和master失效 */
	public String value() default "";
}
