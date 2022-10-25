package sso.util.client.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import sso.util.client.ApplicationProperties;
import sso.util.client.WebClientRequestHelper;
import sso.util.client.JsonSerializerHelper;
import sso.util.client.JwtManager;
import sso.util.client.StringExtention;
import sso.util.client.models.ErrorCode;
import sso.util.client.models.LogModel;
import sso.util.client.models.ResponseModel;
import sso.util.client.models.ServiceModel;
import sso.util.client.models.UserData;

public class LogInterceptor implements HandlerInterceptor {
	JwtManager jwtManager = new JwtManager();
	Logger logger = LoggerFactory.getLogger(LogInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		request.setAttribute("log_time_start", System.currentTimeMillis());
		request.setAttribute("step", 1);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		int step = (int) request.getAttribute("step");
		request.setAttribute("step", step + 1);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception exception) throws Exception {
		int step = (int) request.getAttribute("step");
		request.removeAttribute("step");
		if (!(handler instanceof HandlerMethod))
			return;
		HandlerMethod handlerMethod = (HandlerMethod) handler;
		var controllerAnnotation = handlerMethod.getBeanType().getAnnotation(NoneLogRecord.class);
		var methodAnnotation = handlerMethod.getMethod().getAnnotation(NoneLogRecord.class);
		boolean logRecord = true;
		if (controllerAnnotation != null || methodAnnotation != null)
			logRecord = false;
		if (!logRecord)
			return;
		if (!VerifyConfig(response))
			return;
		String to = StringExtention
				.Trim(StringExtention.ReplaceHttpPrefix(ApplicationProperties.GetApplicationUrl(request)), "/")
				.toLowerCase();
		String controller = handlerMethod.getBeanType().getSimpleName();
		String action = handlerMethod.getMethod().getName();
		String route = request.getRequestURI();
		String queryString = request.getQueryString();
		String host = request.getRemoteHost().toString();
		String agent = request.getHeader("USER-AGENT");
		long time = System.currentTimeMillis() - (long) request.getAttribute("log_time_start");
		String userId = "", userName = "", from = "";
		String authorization = jwtManager.GetAuthorization(request);
		if (!StringExtention.IsNullOrEmpty(authorization)) {
			UserData userData = jwtManager.ParseToken(authorization);
			userId = userData.getUserId();
			userName = userData.getUserName();
			from = userData.getFrom();
		}
		boolean exp = false;
		if (exception != null) {
			exp = true;
		} else {
			exp = step == 1 ? true : false;
		}
		this.InsertLog(from, to, controller, action, route, queryString, "", "", userId, userName, host, agent, time,
				exp);
	}

	private boolean VerifyConfig(HttpServletResponse response) throws Exception {
		response.setContentType("application/json");
		if (StringExtention.IsNullOrEmpty(jwtManager.messageUrl)) {
			String respString = JsonSerializerHelper
					.Serialize(new ResponseModel<String>(ErrorCode.messageBaseUrl_not_config, ""));
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
		return true;
	}

	private void InsertLog(String from, String to, String controller, String action, String route, String querystring,
			String requestContent, String responseContent, String userId, String userName, String userHost,
			String userAgent, long time, boolean exception) throws Exception {
		LogModel logModel = new LogModel();
		logModel.From = from;
		logModel.To = to;
		logModel.Controller = controller;
		logModel.Action = action;
		logModel.Route = route;
		logModel.QueryString = querystring;
		logModel.Content = requestContent;
		logModel.Response = responseContent;
		logModel.UserId = userId;
		logModel.UserName = userName;
		logModel.UserHost = userHost;
		logModel.UserAgent = userAgent;
		logModel.Time = time;
		logModel.Exception = exception;
		try {
			new WebClientRequestHelper().Post(StringExtention.Trim(jwtManager.messageUrl, "/") + "/log/insert", logModel,
					null, new ParameterizedTypeReference<ServiceModel<String>>() {
					}).block();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
