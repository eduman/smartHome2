package it.eduman.smartHome.deprecated.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class MyCipher {

	public static String encrypt (String strPassword, String plainText) throws SecurityException {		
		try {
			AlgorithmParameterSpec paramSpec = new IvParameterSpec(strPassword.getBytes());
			SecretKeySpec key = new SecretKeySpec(strPassword.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec); 
			String encrypted = Base64.encodeBytes(cipher.doFinal(plainText.getBytes()));
					//encodeBase64(cipher.doFinal(plainText.getBytes()));
			String output = encrypted; //new String(encrypted.ge, "ISO-8859-1");
			return output;
		} catch (NoSuchAlgorithmException e) {
			throw new SecurityException(e);
		} catch (NoSuchPaddingException e) {
			throw new SecurityException(e);
		} catch (InvalidKeyException e) {
			throw new SecurityException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new SecurityException(e);
		} catch (IllegalBlockSizeException e) {
			throw new SecurityException(e);
		} catch (BadPaddingException e) {
			throw new SecurityException(e);
//		} catch (UnsupportedEncodingException e) {
//			throw new SecurityException(e);
		}

	}

	public static String decrypt(String strPassword, String cipherText) throws SecurityException {
		try {
			AlgorithmParameterSpec paramSpec = new IvParameterSpec(strPassword.getBytes());
			SecretKeySpec key = new SecretKeySpec(strPassword.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, paramSpec); 
			byte[] decrypted = cipher.doFinal(Base64.decode(new String(cipherText.getBytes("ISO-8859-1"), "ISO-8859-1")));
//					.decodeBase64(cipherText.getBytes("ISO-8859-1"));
			String output =  new String(decrypted, "ISO-8859-1");
			return output;
		} catch (NoSuchAlgorithmException e) {
			throw new SecurityException(e);
		} catch (NoSuchPaddingException e) {
			throw new SecurityException(e);
		} catch (InvalidKeyException e) {
			throw new SecurityException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new SecurityException(e);
		} catch (IllegalBlockSizeException e) {
			throw new SecurityException(e);
		} catch (BadPaddingException e) {
			throw new SecurityException(e);
		} catch (UnsupportedEncodingException e) {
			throw new SecurityException(e);
		}
	}
}
