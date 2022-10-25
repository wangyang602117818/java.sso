package sso.util.client;

import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class ApplicationProperties {
	public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static ResourceBundle bundle = null;
	static {
		try {
			bundle = ResourceBundle.getBundle("application");
		} catch (Exception e) {
		}
	}

	public static String getValue(String key) {
		if (bundle == null)
			return null;
		return bundle.containsKey(key) ? bundle.getString(key) : null;
	}

	/**
	 * tips: 获取应用程序的根目录 1: 如果在开发环境可能返回 C:\JavaWorkspace\springwebmvc02 2:
	 * 如果是在tomcat可能返回 C:\soft\apache-tomcat-9.0.65\bin
	 */
	public static String GetAppBasePath() {
		return System.getProperty("user.dir");
	}

	/**
	 * tips: 获取应用程序的基本路径
	 */
	public static String GetApplicationUrl(HttpServletRequest request) {
		int port = request.getServerPort();
		var portstr = port == 80 ? "" : (":" + port);
		return request.getScheme() + "://" + request.getServerName() + portstr + request.getContextPath();
	}

	/**
	 * tips: 获取应用程序的完全路径
	 */
	public static String GetAbsoluteUri(HttpServletRequest request) {
		if (request.getQueryString() == null)
			return GetApplicationUrl(request) + request.getServletPath();
		return GetApplicationUrl(request) + request.getServletPath() + "?" + request.getQueryString();
	}

	public static String GetRemoteIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (StringExtention.IsNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip))
			ip = request.getHeader("Proxy-Client-IP");
		if (StringExtention.IsNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip))
			ip = request.getHeader("WL-Proxy-Client-IP");
		if (StringExtention.IsNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip))
			ip = request.getRemoteAddr();
		return ip;
	}

	public static String GetCookieValueByKey(HttpServletRequest request, String cookieKey) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(cookieKey)) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
