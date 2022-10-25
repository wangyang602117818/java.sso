package sso.util.client.interceptor;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import javax.net.ssl.SSLException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.core.type.TypeReference;

import sso.util.client.ApplicationProperties;
import sso.util.client.Base64SecureURL;
import sso.util.client.WebClientRequestHelper;
import sso.util.client.JsonSerializerHelper;
import sso.util.client.JwtManager;
import sso.util.client.StringExtention;
import sso.util.client.models.ErrorCode;
import sso.util.client.models.ResponseModel;
import sso.util.client.models.ServiceModel;
import sso.util.client.models.UserData;

public class SSOAuthorizeInterceptor implements HandlerInterceptor {

	public boolean unAuthorizedRedirect = true;
	public String permissionName = "";
	JwtManager jwtManager = new JwtManager();

	public SSOAuthorizeInterceptor(boolean unAuthorizedRedirect) {
		this.unAuthorizedRedirect = unAuthorizedRedirect;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (!(handler instanceof HandlerMethod))
			return true;
		HandlerMethod handlerMethod = (HandlerMethod) handler;
		var controllerAnnotation = handlerMethod.getBeanType().getAnnotation(AllowAnonymous.class);
		var methodAnnotation = handlerMethod.getMethod().getAnnotation(AllowAnonymous.class);
		var authNameAnnotation = handlerMethod.getMethod().getAnnotation(SSOAuthorize.class);
		if (controllerAnnotation != null || methodAnnotation != null)
			return true;
		if (authNameAnnotation != null) {
			permissionName = authNameAnnotation.name();
			unAuthorizedRedirect = authNameAnnotation.unAuthorizedRedirect();
		}
		// ��֤�����ļ�
		if (!VerifyConfig(response))
			return false;
		var ssourl = request.getParameter("ssourls");
		var absoluteUrl = ApplicationProperties.GetAbsoluteUri(request);
		if (!StringExtention.IsNullOrEmpty(ssourl)) {
			var returnUrl = request.getParameter("returnUrl");
			ArrayList<String> ssoUrls = JsonSerializerHelper.Deserialize(
					new String(Base64.getDecoder().decode(Base64SecureURL.Decode(ssourl))),
					new TypeReference<ArrayList<String>>() {
					});
			DeleteCookies(response);
			for (int i = 0; i < ssoUrls.size(); i++) {
				String url = ssoUrls.get(i);
				if (absoluteUrl.contains(url)) {
					ssoUrls.remove(i);
					break;
				}
			}
			if (ssoUrls.size() > 0) {
				String newSsoUrls = JsonSerializerHelper.Serialize(ssoUrls);
				response.sendRedirect(ssoUrls.get(0) + "?ssourls=" + StringExtention.StrToBase64(newSsoUrls)
						+ "&returnUrl=" + returnUrl);
			} else // ���һ��
			{
				response.sendRedirect(
						StringExtention.Trim(jwtManager.ssoBaseUrl, "/") + "/sso/login?returnUrl=" + returnUrl);
			}
			return false;
		}
		String authorization = jwtManager.GetAuthorization(request);
		String ticket = request.getParameter("ticket");
		if (StringExtention.IsNullOrEmpty(authorization)) {
			if (StringExtention.IsNullOrEmpty(ticket)) {
				return SendResult(response, absoluteUrl);
			} else {
				String from = StringExtention
						.Trim(StringExtention.ReplaceHttpPrefix(ApplicationProperties.GetApplicationUrl(request)), "/");
				String audience = ApplicationProperties.GetRemoteIp(request);
				authorization = GetTokenByTicket(ticket, from, audience);
				if (!StringExtention.IsNullOrEmpty(authorization)) {
					SetCookies(response, authorization);
				} else {
					if (absoluteUrl.contains("ticket")) {
						var index = absoluteUrl.indexOf("ticket");
						absoluteUrl = absoluteUrl.substring(0, index - 1);
					}
					return SendResult(response, absoluteUrl);
				}
			}
		}
		try {
			if (!CheckPermission(permissionName, authorization)) {
				var result = JsonSerializerHelper.Serialize(new ResponseModel<String>(ErrorCode.error_permission, ""));
				response.getWriter().println(result);
				return false;
			}
			UserData userData = jwtManager.ParseToken(authorization);
			request.setAttribute("userData", userData);
			SetCookies(response, authorization);
		} catch (Exception e) {
			DeleteCookies(response);
			return SendResult(response, absoluteUrl);
		}
		return true;
	}

	private void SetCookies(HttpServletResponse response, String authorization) {
		Cookie cookie = new Cookie(jwtManager.ssoCookieKey, authorization);
		cookie.setPath("/");
		if (!jwtManager.ssoCookieTime.equals("session")) {
			cookie.setMaxAge(Integer.parseInt(jwtManager.ssoCookieTime) * 60);
		}
		response.addCookie(cookie);
	}

	private void DeleteCookies(HttpServletResponse response) {
		Cookie newCookie = new Cookie(jwtManager.ssoCookieKey, null);
		newCookie.setMaxAge(0);
		newCookie.setPath("/");
		response.addCookie(newCookie);
	}

	private boolean SendResult(HttpServletResponse response, String returnUrl) throws Exception {
		if (unAuthorizedRedirect) {
			String url = StringExtention.Trim(jwtManager.ssoBaseUrl, "/") + "/sso/login?returnUrl=" + returnUrl;
			response.sendRedirect(url);
		} else {
			var result = JsonSerializerHelper.Serialize(new ResponseModel<String>(ErrorCode.authorize_fault, ""));
			response.getWriter().println(result);
		}
		return false;
	}

	private boolean CheckPermission(String permission, String authorization) throws SSLException {
		if (StringExtention.IsNullOrEmpty(permission))
			return true;
		var url = StringExtention.Trim(jwtManager.ssoBaseUrl, "/") + "/permission/checkPermission?permissionName="
				+ permission;
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Authorization", authorization);
		var result = new WebClientRequestHelper().Get(url, header, new ParameterizedTypeReference<ServiceModel<String>>() {
		}).block();
		if (result.code == 0)
			return true;
		return false;
	}

	private String GetTokenByTicket(String ticket, String from, String audience) throws SSLException {
		var url = StringExtention.Trim(jwtManager.ssoBaseUrl, "/") + "/sso/gettoken?ticket=" + ticket + "&from=" + from
				+ "&audience=" + audience;
		var result = new WebClientRequestHelper().Get(url, null, new ParameterizedTypeReference<ServiceModel<String>>() {
		}).block();
		if (result.code == 0)
			return result.result.toString();
		return "";
	}

	private boolean VerifyConfig(HttpServletResponse response) throws Exception {
		response.setContentType("application/json");
		if (StringExtention.IsNullOrEmpty(jwtManager.ssoBaseUrl) && unAuthorizedRedirect) {
			String respString = JsonSerializerHelper
					.Serialize(new ResponseModel<String>(ErrorCode.baseUrl_not_config, ""));
			response.getWriter().println(respString);
			return false;
		}
		if (StringExtention.IsNullOrEmpty(jwtManager.ssoSecretKey)) {
			String respString = JsonSerializerHelper
					.Serialize(new ResponseModel<String>(ErrorCode.secretKey_not_config, ""));
			response.getWriter().println(respString);
			return false;
		}
		if (StringExtention.IsNullOrEmpty(jwtManager.ssoCookieKey)) {
			String respString = JsonSerializerHelper
					.Serialize(new ResponseModel<String>(ErrorCode.cookieKey_not_config, ""));
			response.getWriter().println(respString);
			return false;
		}
		if (StringExtention.IsNullOrEmpty(jwtManager.ssoCookieTime)) {
			String respString = JsonSerializerHelper
					.Serialize(new ResponseModel<String>(ErrorCode.cookieTime_not_config, ""));
			response.getWriter().println(respString);
			return false;
		}
		return true;
	}
}
