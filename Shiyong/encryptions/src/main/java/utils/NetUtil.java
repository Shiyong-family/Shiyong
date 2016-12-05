package srp.bapp.utils;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class NetUtil {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(NetUtil.class);

	public final static String LOCAL_IP=StringUtil.collectionToCommaDelimitedString(getMyIp());
	
	public static List<String> getMyIp() {
		try {
			List<String> ipList = new LinkedList<String>();
			for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
				NetworkInterface item = e.nextElement();
				for (Enumeration<InetAddress> e2 = item.getInetAddresses(); e2.hasMoreElements();) {
					InetAddress address = e2.nextElement();
					if (address instanceof java.net.Inet4Address && !address.isLoopbackAddress()) {
						ipList.add(address.getHostAddress());
					}
				}
			}
			Collections.sort(ipList);
			return ipList;
		} catch (Exception e) { 
			logger.error("getMyIp()", e); //$NON-NLS-1$
			return new LinkedList<String>();
		}
	}

}
