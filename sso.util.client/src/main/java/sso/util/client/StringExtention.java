package sso.util.client;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringExtention {
	public static boolean IsNullOrEmpty(String str) {
		return str == null || str.isBlank();
	}

	public static String Trim(String str, String c) {
		if (IsNullOrEmpty(c))
			c = " ";
		String[] chars = new String[] { "[", "]", "\\", "^", "$", ".", "|", "?", "*", "+", "(", ")", "{", "}" };
		String transfer = "";
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
		int index = str.lastIndexOf("\\");
		return str.substring(index + 1);
	}

	public static String GetFileExt(String str) {
		int index = str.lastIndexOf('.');
		if (index == -1)
			return "";
		return str.substring(index);
	}

	public static int NumberInRange(int min, int max) {
		return new Random().nextInt((max - min) + 1) + min;
	}

	public static String RandomCode(int numb) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numb; i++) {
			int res = NumberInRange(0, 2);
			switch (res) {
			case 0:
				sb.append(NumberInRange(0, 9)); // 鏁板瓧
				break;
			case 1:
				sb.append((char) NumberInRange(97, 122)); // 灏忓啓瀛楁瘝
				break;
			case 2:
				sb.append((char) NumberInRange(65, 90)); // 澶у啓瀛楁瘝
				break;
			}
		}
		return sb.toString();
	}
}
