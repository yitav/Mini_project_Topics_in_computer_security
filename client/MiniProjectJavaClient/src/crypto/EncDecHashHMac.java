package crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
/**
 * 
 * This Class is a a helper class used for cryptography functionality needed in this program
 * Functionality includes Encryption and Decryption with AES-256 CBC , 
 * Authentication with HMac-SHA-256 , and Hashing with SHA-256  
 * Initialization should be done with the user's password as parameter to getInstance(String password) ,
 * and afterwards a call to get the singleton instance should be done by getInstancce() with no parameters
 * 
 */
public class EncDecHashHMac {

	private String password;

	private static final int IV_LENGTH=16;

	private static EncDecHashHMac self = null;

	public EncDecHashHMac(String password) {
		this.password = password;
	}

	public static EncDecHashHMac getInstance(String password) {
		if(self == null) {
			self = new EncDecHashHMac(password);
		}
		return self;
	}
	public static EncDecHashHMac getInstance() {
		if(self==null) throw new IllegalStateException("HttpConnection instance was not initialized");
		return self;
	}

	/**
	 * This function computes the SHA-256 hash of the @param toHash 
	 * 
	 * @param toHash the String to be hashed
	 * @return the hashed String
	 */
	public String hashSha256(String toHash) {
		StringBuffer hexString;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest((toHash).getBytes(StandardCharsets.UTF_8));
			hexString = new StringBuffer();
			//converting from bytes to hex string 
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}

		}catch(Exception e) {
			System.out.println( e.getMessage());
			e.printStackTrace();
			return null;
		}
		return hexString.toString();
	}
/*
	public String checkIfFileNameLegal(String fileName,String tempFileName,String resultFileName) {
		File file = new File(fileName);
		if(!file.exists()){
			System.out.println("No file "+fileName);
			return null;
		}
		File file2 = new File(tempFileName);
		File file3 = new File(resultFileName);
		if(file2.exists() || file3.exists()){
			System.out.println("File for encrypted temp file or for the result decrypted file already exists. Please remove it or use a different file name");
			return null;
		}
		return "success";
	}
*/
	
	/**
	 * This function encrypts the in stream to the out stream with AES-256 encryption
	 * using this function helps in dealing with big files so ram memory wont 
	 * be a factor in the encryption
	 * 
	 * @param in Stream of message to be encrypted
	 * @param out Stream to be written the encrypted message i.e. ciphertext
	 * @param password Encryption key
	 * @throws Exception IOException  NoSuchPaddingException  InvalidAlgorithmParameterException
	 */
	private void encrypt(InputStream in, OutputStream out, String password) throws Exception{

		SecureRandom r = new SecureRandom();
		byte[] iv = new byte[IV_LENGTH];
		r.nextBytes(iv);
		out.write(iv); //write IV as a prefix
		out.flush();

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //"DES/ECB/PKCS5Padding";"AES/CFB8/NoPadding"
		SecretKeySpec keySpec = new SecretKeySpec(hexStringToByteArray(password), "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);    	

		out = new CipherOutputStream(out, cipher);
		byte[] buf = new byte[1024];
		int numRead = 0;
		while ((numRead = in.read(buf)) >= 0) {
			out.write(buf, 0, numRead);
		}
		out.close();
	}

	/**
	 * This function decrypts the in stream to the out stream with AES-256 decryption
	 * using this function helps in dealing with big files so ram memory wont 
	 * be a factor in the decryption
	 * 
	 * @param in Stream of message to be decrypted
	 * @param out Stream to be written the decrypted message i.e. plaintext
	 * @param password Encryption key
	 * @throws Exception IOException  NoSuchPaddingException  InvalidAlgorithmParameterException
	 */
	private void decrypt(InputStream in, OutputStream out, String password) throws Exception{

		byte[] iv = new byte[IV_LENGTH];
		in.read(iv);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //"DES/ECB/PKCS5Padding";"AES/CFB8/NoPadding"
		SecretKeySpec keySpec = new SecretKeySpec(hexStringToByteArray(password), "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

		in = new CipherInputStream(in, cipher);
		byte[] buf = new byte[1024];
		int numRead = 0;
		while ((numRead = in.read(buf)) >= 0) {
			out.write(buf, 0, numRead);
		}
		out.close();
	}

	/**
	 * This function is a wrapper for the encrypt() or decrypt functions in this class that use AES-256
	 * 
	 * @param mode can be Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
	 * @param inputFile File to be encrypted or decrypted according to @param mode
	 * @param outputFile File result - ciphertext or plaintext according to @param mode
	 * @throws Exception IOException  NoSuchPaddingException  InvalidAlgorithmParameterException
	 */
	public void copy(int mode, String inputFile, String outputFile) throws Exception {

		String passwordKeyHash = hashSha256(this.password+"1");
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(inputFile));
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
		if(mode==Cipher.ENCRYPT_MODE){
			encrypt(is, os, passwordKeyHash);
		}
		else if(mode==Cipher.DECRYPT_MODE){
			decrypt(is, os, passwordKeyHash);
		}
		else {
			is.close();
			os.close();
			throw new Exception("unknown mode");
		}
		is.close();
		os.close();
	}
	/**
	 * This function encrypts a String with AES-256 encryption
	 * 
	 * @param plainText the message String to be encrypted
	 * @return ciphertext the result of the encryption
	 * @throws Exception  NoSuchPaddingException InvalidAlgorithmParameterException BadPaddingException
	 */
	public String encryptString(String plainText) throws Exception {
		String key = hashSha256(this.password+"1");
		byte[] clean = plainText.getBytes("UTF-8");
		//System.out.println("plaintext in hex:");
		//System.out.println(bytesToHexStr(clean));
		//System.out.println("");

		int ivSize = 16;
		byte[] iv = new byte[ivSize];
		SecureRandom random = new SecureRandom();
		random.nextBytes(iv);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

		byte[] keyBytes = hexStringToByteArray(key);
		//		System.out.println("key in hex:");
		//		System.out.println(bytesToHexStr(keyBytes));
		//		System.out.println("key in base64:");
		//		System.out.println(DatatypeConverter.printBase64Binary(hexStringToByteArray(bytesToHexStr(keyBytes))));
		//		System.out.println("");

		SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes , "AES");

		// Encrypt.
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] encrypted = cipher.doFinal(clean);

		//Combine IV and encrypted part.
		byte[] encryptedIVAndText = new byte[ivSize + encrypted.length];
		System.arraycopy(iv, 0, encryptedIVAndText, 0, ivSize);
		System.arraycopy(encrypted, 0, encryptedIVAndText, ivSize, encrypted.length);


		return bytesToHexStr(encryptedIVAndText);
	}
	/**
	 * This function decrypts a String with AES-256 encryption
	 * 
	 * @param encryptedIvTextStr ciphertext to be decrypted
	 * @return plaintext the result of the decryption
	 * @throws Exception  NoSuchPaddingException InvalidAlgorithmParameterException BadPaddingException 
	 */
	public String decryptString(String encryptedIvTextStr) throws Exception {
		String key = hashSha256(this.password+"1");
		int ivSize = 16;
		//int keySize = 32;

		// Extract IV.
		byte[] encryptedIvTextBytes = hexStringToByteArray(encryptedIvTextStr);
		byte[] iv = new byte[ivSize];
		System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

		// Extract encrypted part.
		int encryptedSize = encryptedIvTextBytes.length - ivSize;
		byte[] encryptedBytes = new byte[encryptedSize];
		System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize);

		byte[] keyBytes = hexStringToByteArray(key);
		SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");

		// Decrypt.
		Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");//
		cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);

		return new String(decrypted);
	}
	/**
	 * This function computes the HMac-SHA-256 of the @param inputFile
	 * 
	 * @param inputFile the file to be authenticated
	 * @return the result of the HMac-SHA-256 computation
	 */
	public String createHMac(String inputFile) {
		String key = hashSha256(this.password+"2");
		try (FileInputStream in = new FileInputStream(inputFile)) {
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec skey = new javax.crypto.spec.SecretKeySpec(hexStringToByteArray(key), "HmacSHA256");
			mac.init(skey);


			byte[] macb = processFile(mac, in);
			System.out.println("createHMac()");
			System.out.println(bytesToHexStr(macb));
			return bytesToHexStr(macb);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	private final byte[] processFile(Mac mac,InputStream in)
			throws java.io.IOException
	{
		byte[] ibuf = new byte[1024];
		int len;
		while ((len = in.read(ibuf)) != -1) {
			mac.update(ibuf, 0, len);
		}
		return mac.doFinal();
	}
	
	private String bytesToHexStr(byte[] bytes) {
		StringBuffer hexString;
		hexString = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xff & bytes[i]);
			if(hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}
	
	private byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}
	
}
