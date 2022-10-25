package sso.util.client.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * ����ӿڵ�Ȩ������ ��֤ʧ���Ƿ���ת
**/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SSOAuthorize {
	/**
	 *   Ȩ������
	 */
	String name() default "";
	/**
	 *   ��֤ʧ���Ƿ���Ҫ��ת����¼����
	 */
	boolean unAuthorizedRedirect() default true;
}
