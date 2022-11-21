package sso.util.client.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 *  SSO Login
**/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SSOAuthorize {
	/**
	 *   permission name
	 */
	String name() default "";
	/**
	 *  If Redirected when Verify fail
	 */
	boolean unAuthorizedRedirect() default true;
}
