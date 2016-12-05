package encryptionUtils;

public class ByteStringUtil {

	/**
	 * 
	 * 二行制转字符串
	 * 
	 * @param b
	 * 
	 * @return
	 * 
	 */
	
	public static String byte2hex(byte[] b) {
	
		String hs = "";
	
		String stmp = "";
	
		for (int n = 0; n < b.length; n++) {
	
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
	
			if (stmp.length() == 1)
	
				hs = hs + "0" + stmp;
	
			else
	
				hs = hs + stmp;
	
		}
	
		return hs.toUpperCase();
	
	}

	public static byte[] hex2byte(byte[] b) {
	
		if ((b.length % 2) != 0){
			throw new IllegalArgumentException("长度不是偶数");
		}			
	
		byte[] b2 = new byte[b.length / 2];
	
		for (int n = 0; n < b.length; n += 2) {
	
			String item = new String(b, n, 2);
	
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
	
		}
	
		return b2;
	}

}
