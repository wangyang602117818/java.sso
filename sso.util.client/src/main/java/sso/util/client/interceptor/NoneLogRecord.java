package sso.util.client.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ����ӿڲ���¼��־
**/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoneLogRecord {
	
}
