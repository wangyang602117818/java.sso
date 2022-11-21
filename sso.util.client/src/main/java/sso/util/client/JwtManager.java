package sso.util.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import sso.util.client.models.UserData;

public class JwtManager {
	public String ssoBaseUrl = ApplicationProperties.getValue("ssoUrl");
	public String ssoSecretKey = ApplicationProperties.getValue("ssoSecret");
	public String ssoCookieKey = ApplicationProperties.getValue("ssoCookieKey");
	public String ssoCookieTime = ApplicationProperties.getValue("ssoCookieTime");

	public String messageUrl = ApplicationProperties.getValue("messageUrl");

	public String fileUrl = ApplicationProperties.getValue("fileUrl");

	public UserData ParseToken(String authorization) throws JWTVerificationException {
		Algorithm algorithm = Algorithm.HMAC256(Base64.getDecoder().decode(ssoSecretKey));
		JWTVerifier verifier = JWT.require(algorithm).acceptExpiresAt(5).build();
		DecodedJWT jwt = verifier.verify(authorization);
		Map<String, Claim> maps = jwt.getClaims();
		UserData userData = new UserData();
		userData.setExtra(new HashMap<String, String>());
		for (Entry<String, Claim> entry : maps.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().asString();
			ArrayList<String> list = new ArrayList<String>(Arrays.asList("aud", "nbf", "iss", "exp", "iat"));
			if (list.contains(key))
				continue;
			if (key.equals("unique_name")) {
				userData.setUserId(value);
			} else if (key.equals("lang")) {
				userData.setLang(value);
			} else if (key.equals("name")) {
				userData.setUserName(value);
			} else if (key.equals("from")) {
				userData.setFrom(value);
			} else {
				userData.getExtra().put(key, value);
			}
		}
		return userData;
	}

	public UserData ParseToken(HttpServletRequest request) {
		String authorization = GetAuthorization(request);
		return ParseToken(authorization);
	}

	public String ModifyTokenLang(String token, String lang) {
		DecodedJWT decodedJWT = JWT.decode(token);
		Map<String, Claim> claims = decodedJWT.getClaims();
		JWTCreator.Builder builder = JWT.create();
		for (String key : claims.keySet()) {
			if (key.equals("lang")) {
				builder.withClaim("lang", lang);
			} else {
				builder.withClaim(key, claims.get(key).asString());
			}
		}
		builder.withIssuedAt(decodedJWT.getIssuedAt());
		builder.withNotBefore(decodedJWT.getNotBefore());
		builder.withExpiresAt(decodedJWT.getExpiresAt());
		builder.withIssuer(decodedJWT.getIssuer());
		builder.withAudience(decodedJWT.getAudience().get(0));
		return builder.sign(Algorithm.HMAC256(ssoSecretKey));
	}

	public String GetAuthorization(HttpServletRequest request) {
		return GetAuthorization(request, ssoCookieKey);
	}

	public String GetAuthorization(HttpServletRequest request, String cookieKey) {
		String authorization = ApplicationProperties.GetCookieValueByKey(request, cookieKey);
		if (authorization == null)
			authorization = request.getHeader("Authorization");
		if (authorization == null)
			authorization = request.getParameter("Authorization");
		return authorization;
	}

}
