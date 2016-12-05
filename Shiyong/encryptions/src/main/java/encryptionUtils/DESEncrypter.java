package encryptionUtils;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import utils.*;

import org.apache.log4j.Logger;

/**
 * 使用DES(data encryption Standard)加密字符串操作类
 * 
 * @author oofrank
 * 
 */
public class DESEncrypter {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(DESEncrypter.class);

	public static final String DES_ENCRYPT_KEY = "__BAPP_DES_ENCRYPT_KEY__"; // 长度必须是8的倍数

	private final static String DES = "DES";

	/**
	 * 
	 * 加密
	 * 
	 * @param src
	 *            数据源
	 * 
	 * @param key
	 *            密钥，长度必须是8的倍数
	 * 
	 * @return 返回加密后的数据
	 * 
	 * @throws Exception
	 * 
	 */

	public static byte[] encrypt(byte[] src, byte[] key) throws Exception {

		// DES算法要求有一个可信任的随机数源

		SecureRandom sr = new SecureRandom();

		// 从原始密匙数据创建DESKeySpec对象

		DESKeySpec dks = new DESKeySpec(key);

		// 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象

		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);

		SecretKey securekey = keyFactory.generateSecret(dks);

		// Cipher对象实际完成加密操作

		Cipher cipher = Cipher.getInstance(DES);

		// 用密匙初始化Cipher对象

		cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

		// 获取数据并加密
		// 正式执行加密操作

		return cipher.doFinal(src);

	}

	/**
	 * 
	 * 解密
	 * 
	 * @param src
	 *            数据源
	 * 
	 * @param key
	 *            密钥，长度必须是8的倍数
	 * 
	 * @return 返回解密后的原始数据
	 * 
	 * @throws Exception
	 * 
	 */

	public static byte[] decrypt(byte[] src, byte[] key) throws Exception {

		// DES算法要求有一个可信任的随机数源

		SecureRandom sr = new SecureRandom();

		// 从原始密匙数据创建一个DESKeySpec对象

		DESKeySpec dks = new DESKeySpec(key);

		// 创建一个密匙工厂，然后用它把DESKeySpec对象转换成一个SecretKey对象

		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);

		SecretKey securekey = keyFactory.generateSecret(dks);

		// Cipher对象实际完成解密操作

		Cipher cipher = Cipher.getInstance(DES);

		// 用密匙初始化Cipher对象

		cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

		// 获取数据并解密
		// 正式执行解密操作

		return cipher.doFinal(src);

	}

	/**
	 * 
	 * 密码解密
	 * 
	 * @param encrptString
	 * 
	 * @throws Exception
	 * 
	 */

	public final static String decrypt(String encrptString) {

		return decrypt(encrptString, DES_ENCRYPT_KEY);

	}

	public final static String decrypt(String encrptString, String key) {
		try {

			return new String(decrypt(ByteStringUtil.hex2byte(encrptString.getBytes()),

			fixKey(key).getBytes()), "GBK");

		} catch (Exception e) {
			logger.debug("decrypt(String, String) - encrptString=" + encrptString); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		return null;
	}

	/**
	 * 
	 * 密码加密
	 * 
	 * @param source
	 * 
	 * @throws Exception
	 * 
	 */

	public final static String encrypt(String source) {

		return encrypt(source, DES_ENCRYPT_KEY);

	}

	public final static String encrypt(String source, String key) {

		try {
			return ByteStringUtil.byte2hex(encrypt(source.getBytes("GBK"), fixKey(key).getBytes()));
		} catch (Exception e) {

		}

		return null;

	}

	private static String fixKey(String key) {
		if (null == key) {
			return DES_ENCRYPT_KEY;
		}
		if (key.length() % 8 == 0) {
			return key;
		} else {
			int i = key.length() / 8 + 1;
			return StringUtil.fillString(key, i * 8, '_');
		}
	}

	public static void main(String[] args) {
		System.out.println("DES_ENCRYPT_KEY.length()%8:" + DES_ENCRYPT_KEY.length() % 8);
		System.out.println("encrypt:" + encrypt("aaa"));
		System.out.println("decrypt:" + decrypt("6DE421DDBD629CD28A8D1A319DCE1693EF1B17FD451802736741B7D726A6E887"));
		System.out.println("fixKey(A):" + fixKey("A"));
	}
}
