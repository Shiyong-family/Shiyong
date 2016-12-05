package encryptionUtils;

/**
 * 
 * 可逆的加密解密工具类,现在的版本基于DES
 * 
 * @author oofrank
 * 
 */

public abstract class EncryptUtil {

	/**
	 * 解密BAPP加密的字符串
	 * 
	 * @param source
	 * @return
	 */
	public final static String decrypt(String encrypted) {
		return DESEncrypter.decrypt(encrypted);
	}

	public final static String decrypt(String encrypted, String encryptKey) {
		return DESEncrypter.decrypt(encrypted, encryptKey);
	}

	/**
	 * 加密字符串
	 * 
	 * @param source
	 * @return
	 */
	public final static String encrypt(String source) {
		return DESEncrypter.encrypt(source);
	}

	/**
	 * 使用指定Key加密字符串
	 * 
	 * @param source
	 * @return
	 */
	public final static String encrypt(String source, String encryptKey) {
		return DESEncrypter.encrypt(source, encryptKey);
	}

	public static void main(String[] args) {
		//System.out.println("encrypt:" + encrypt("bsys"));
		//System.out.println("encrypt:" + encrypt("jdbc:oracle:thin:@192.168.1.216:1521:BAPP"));
		//System.out.println("decrypt:" + decrypt("46B901E251E3BB88"));

		System.out.println(decrypt("1A6F02FA4CFB8B435027F155F5731E7AE301708ABBD77D07EA99869007E3DBDC"));
		System.out.println("lisence:" + "==" + encrypt("**银行|正式版|2015-07-31") + "==");

	}

}
