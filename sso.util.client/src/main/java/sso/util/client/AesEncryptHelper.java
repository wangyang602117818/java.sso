package sso.util.client;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesEncryptHelper {
	private static byte[] IV = { (byte) 137, 73, (byte) 153, 109, 30, (byte) 161, 2, (byte) 250, 37, 120, (byte) 158,
			116, 24, (byte) 217, (byte) 227, (byte) 250 };

	public static String Encode(String sourceString, String key) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(key), "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(IV);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
		byte[] cipherText = cipher.doFinal(sourceString.getBytes("UTF8"));
		return Base64.getEncoder().encodeToString(cipherText);
	}

	public static String Decode(String secretString, String key) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(key), "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(IV);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		byte[] decryptedText = cipher.doFinal(Base64.getDecoder().decode(secretString));
		return new String(decryptedText);
	}

	public static String GenerateAESKey() throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(256);
		byte[] key = kgen.generateKey().getEncoded();
		return Base64.getEncoder().encodeToString(key);
	}
}
