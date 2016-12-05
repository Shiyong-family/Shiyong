package srp.bapp.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import srp.bapp.subsystem.web.f.BaseSSOFilter;

/**
 * <p>
 * </p>
 * 
 * @作者: oofrank
 * @日期: 2010-8-31 下午02:05:07
 * @描述: cookie读写
 * 
 * @TestCase：
 */
public class SSOHelper {

	/**
	 * 函数功能说明：设置cookie 参数说明： response,cookie 返回值说明: 注释编写人： 注释审核人: 使用案例测试人：
	 */
	public static void setCookie(HttpServletResponse response, Cookie cookie) {
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	/**
	 * 函数功能说明：设置cookie,路径为当前域;没有指定失效时间，会随浏览器进程结束而失效 参数说明： response,key,value
	 * 返回值说明: 注释编写人： 注释审核人: 使用案例测试人：
	 */
	public static void setCookie(HttpServletResponse response, String key, String value) {
		 	Cookie cookie = new Cookie(key, value);
			cookie.setPath("/");
			response.addCookie(cookie); 
	}

	/**
	 * 函数功能说明： 读cookie 参数说明： request,key 返回值说明: 注释编写人： 注释审核人: 使用案例测试人：
	 */
	public static String getCookieValue(HttpServletRequest request, String key) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
			for (int i = 0, j = cookies.length; i < j; i++) {
				if (cookies[i].getName().equals(key))
					return cookies[i].getValue();
			}
		return null;
	}

	/**
	 * 函数功能说明： 获取真实IP 参数说明： request 返回值说明: 注释编写人： 注释审核人: 使用案例测试人：
	 */
	public static String getRemoteAddr(HttpServletRequest request) {

		String ip = request.getHeader("x-forwarded-for");

		if (ip != null && ip.indexOf(",") != -1) {
			ip = ip.substring(0, ip.indexOf(","));
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}

		return ip;
	}
	
	
	/**
	 * 尝试从requset和cookie中获取关键值
	 * @param request
	 * @param key
	 * @return
	 */
	public static String getRequestValue(HttpServletRequest request, String key){
		String v = request.getParameter(key); 
		if (v == null) {
			v = SSOHelper.getCookieValue(request, key);
		}
		return v;
	}

}
