package sso.util.client;

public class Base64SecureURL {
	public static String Encode(String text) {
		return text.replace('+', '-').replace('/', '_').replace("=", "");
	}

	public static String Decode(String secureUrlBase64) {
		secureUrlBase64 = secureUrlBase64.replace('-', '+').replace('_', '/');
		switch (secureUrlBase64.length() % 4) {
		case 2:
			secureUrlBase64 += "==";
			break;
		case 3:
			secureUrlBase64 += "=";
			break;
		}
		return secureUrlBase64;
	}
}
