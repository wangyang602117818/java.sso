package sso.util.client;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringExtention {
	public static boolean IsNullOrEmpty(String str) {
		return str == null || str.isBlank();
	}

	public static String Trim(String str, String c) {
		if (IsNullOrEmpty(c))
			c = " ";
		var chars = new String[] { "[", "]", "\\", "^", "$", ".", "|", "?", "*", "+", "(", ")", "{", "}" };
		var transfer = "";
		if (Arrays.asList(chars).contains(c))
			transfer = "\\";
		String regexString = "^" + (transfer + c) + "+|" + (transfer + c) + "+$";
		return str.replaceAll(regexString, "");
	}

	public static String ReplaceHttpPrefix(String str) {
		Pattern pattern = Pattern.compile("https?://|www.", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(str);
		return matcher.replaceAll("");
	}

	public static String StrToBase64(String str) throws UnsupportedEncodingException {
		String base64 = Base64.getEncoder().encodeToString(str.getBytes("UTF8"));
		return Base64SecureURL.Encode(base64);
	}

	public static String GetFileName(String str) {
		var index = str.lastIndexOf("\\");
		return str.substring(index + 1);
	}
	
	public static String GetFileExt(String str) {
		var index = str.lastIndexOf('.');
        if (index == -1) return "";
        return str.substring(index);
	}
}
