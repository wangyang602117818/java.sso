package sso.util.client.interceptor;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.core.type.TypeReference;

import sso.util.client.ApplicationProperties;
import sso.util.client.Base64SecureURL;
import sso.util.client.HttpClientRequestHelper;
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
		AllowAnonymous controllerAnnotation = handlerMethod.getBeanType().getAnnotation(AllowAnonymous.class);
		AllowAnonymous methodAnnotation = handlerMethod.getMethod().getAnnotation(AllowAnonymous.class);
		SSOAuthorize authNameAnnotation = handlerMethod.getMethod().getAnnotation(SSOAuthorize.class);
		if (controllerAnnotation != null || methodAnnotation != null)
			return true;
		if (authNameAnnotation != null) {
			permissionName = authNameAnnotation.name();
			unAuthorizedRedirect = authNameAnnotation.unAuthorizedRedirect();
		}
		if (!VerifyConfig(response))
			return false;
		String ssourl = request.getParameter("ssourls");
		String absoluteUrl = ApplicationProperties.GetAbsoluteUri(request);
		if (!StringExtention.IsNullOrEmpty(ssourl)) {
			String returnUrl = request.getParameter("returnUrl");
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
			} else
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
						int index = absoluteUrl.indexOf("ticket");
						absoluteUrl = absoluteUrl.substring(0, index - 1);
					}
					return SendResult(response, absoluteUrl);
				}
			}
		}
		try {
			if (!CheckPermission(permissionName, authorization)) {
				String result = JsonSerializerHelper.Serialize(new ResponseModel<String>(ErrorCode.error_permission, ""));
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
			String result = JsonSerializerHelper.Serialize(new ResponseModel<String>(ErrorCode.authorize_fault, ""));
			response.getWriter().println(result);
		}
		return false;
	}

	private boolean CheckPermission(String permission, String authorization) throws Exception {
		if (StringExtention.IsNullOrEmpty(permission))
			return true;
		String url = StringExtention.Trim(jwtManager.ssoBaseUrl, "/") + "/permission/checkPermission?permissionName="
				+ permission;
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Authorization", authorization);
		String resp = new HttpClientRequestHelper().Get(url, header);
		ServiceModel<String> result = JsonSerializerHelper.Deserialize(resp, new TypeReference<ServiceModel<String>>() {
		});
		if (result.code == 0)
			return true;
		return false;
	}

	private String GetTokenByTicket(String ticket, String from, String audience) throws Exception {
		String url = StringExtention.Trim(jwtManager.ssoBaseUrl, "/") + "/sso/gettoken?ticket=" + ticket + "&from=" + from
				+ "&audience=" + audience;
		String resp = new HttpClientRequestHelper().Get(url, null);
		ServiceModel<String> result = JsonSerializerHelper.Deserialize(resp, new TypeReference<ServiceModel<String>>() {
		});
		if (result.code == 0)
			return result.result.toString();
		return "";
	}

	private boolean VerifyConfig(HttpServletResponse response) throws Exception {
		response.setContentType("application/json");
		if (StringExtention.IsNullOrEmpty(jwtManager.ssoBaseUrl) && unAuthorizedRedirect) {
			String respString = JsonSerializerHelper
					.Serialize(new ResponseModel<String>(ErrorCode.ssoUrl_not_config, ""));
			response.getWriter().println(respString);
			return false;
		}
		if (StringExtention.IsNullOrEmpty(jwtManager.ssoSecretKey)) {
			String respString = JsonSerializerHelper
					.Serialize(new ResponseModel<String>(ErrorCode.ssoSecret_not_config, ""));
			response.getWriter().println(respString);
			return false;
		}
		if (StringExtention.IsNullOrEmpty(jwtManager.ssoCookieKey)) {
			String respString = JsonSerializerHelper
					.Serialize(new ResponseModel<String>(ErrorCode.ssoCookieKey_not_config, ""));
			response.getWriter().println(respString);
			return false;
		}
		if (StringExtention.IsNullOrEmpty(jwtManager.ssoCookieTime)) {
			String respString = JsonSerializerHelper
					.Serialize(new ResponseModel<String>(ErrorCode.ssoCookieTime_not_config, ""));
			response.getWriter().println(respString);
			return false;
		}
		return true;
	}
}
