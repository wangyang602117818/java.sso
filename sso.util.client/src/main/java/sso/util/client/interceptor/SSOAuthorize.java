package sso.util.client.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 定义接口的权限名称 验证失败是否跳转
**/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SSOAuthorize {
	/**
	 *   权限名称
	 */
	String name() default "";
	/**
	 *   验证失败是否需要跳转到登录界面
	 */
	boolean unAuthorizedRedirect() default true;
}
