package srp.bapp.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import srp.bapp.jackson.JsonParseException;
import srp.bapp.jackson.map.JsonMappingException;
import srp.bapp.jackson.map.ObjectMapper;
import srp.bapp.pub.runtime.LoginManager;
import srp.bapp.tools.EasyMap;

public abstract class StringUtil extends StringUtils {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(StringUtil.class);
	private static final String[] EMPTY_STRING_ARRAY = {};
	private static char[] lowercases = { '\000', '\001', '\002', '\003', '\004', '\005', '\006', '\007', '\010', '\011', '\012', '\013', '\014',
			'\015', '\016', '\017', '\020', '\021', '\022', '\023', '\024', '\025', '\026', '\027', '\030', '\031', '\032', '\033', '\034', '\035',
			'\036', '\037', '\040', '\041', '\042', '\043', '\044', '\045', '\046', '\047', '\050', '\051', '\052', '\053', '\054', '\055', '\056',
			'\057', '\060', '\061', '\062', '\063', '\064', '\065', '\066', '\067', '\070', '\071', '\072', '\073', '\074', '\075', '\076', '\077',
			'\100', '\141', '\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157', '\160',
			'\161', '\162', '\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\133', '\134', '\135', '\136', '\137', '\140', '\141',
			'\142', '\143', '\144', '\145', '\146', '\147', '\150', '\151', '\152', '\153', '\154', '\155', '\156', '\157', '\160', '\161', '\162',
			'\163', '\164', '\165', '\166', '\167', '\170', '\171', '\172', '\173', '\174', '\175', '\176', '\177' };

	/**
	 * 填充字符串
	 * 
	 * @param source
	 * @param length
	 * @param c
	 * @return
	 */
	public final static String fillString(String source, int length, char c) {
		if (null == source) {
			source = "";
		}
		if (source.length() > length) {
			return source;
		}
		StringBuffer sb = new StringBuffer(source);
		for (int i = source.length(); i < length; i++) {
			sb.append(c);
		}

		return sb.toString();
	}

	/**
	 * 向前填充字符串
	 * 
	 * @param source
	 * @param length
	 * @param c
	 * @return
	 */
	// 12 00012 0 5
	public final static String fillStringBefore(String source, int length, char c) {
		if (null == source) {
			source = "";
		}
		if (source.length() > length) {
			return source;
		}
		StringBuffer sb = new StringBuffer(source);
		for (int i = source.length(); i < length; i++) {
			sb.insert(0, c);
		}

		return sb.toString();
	}

	/**
	 * 填充GBK字符串
	 * 
	 * @param source
	 * @param length
	 *            ---bytes字节宽度
	 * @param c
	 * @return
	 */
	public final static String fillStringGBK(String source, int length, char c) {
		try {
			if (null == source) {
				source = "";
			}
			int sl = source.getBytes("GBK").length;
			if (sl > length) {
				return source;
			}
			StringBuffer sb = new StringBuffer(source);
			for (int i = sl; i < length; i++) {
				sb.append(c);
			}

			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
			return null;
		}
	}

	/**
	 * 按字节数截取GBK字符串
	 * 
	 * @param beginIndex
	 *            the beginning index, inclusive.
	 * @param endIndex
	 *            the ending index, exclusive.
	 * @return
	 */
	public final static String subStringByByte(String str, int beginIndex, int endIndex) {
		if (beginIndex < 0) {
			throw new StringIndexOutOfBoundsException(beginIndex);
		}
		if (beginIndex > endIndex) {
			throw new StringIndexOutOfBoundsException(endIndex - beginIndex);
		}
		String result = null;
		if (str != null) {
			byte[] a = null;
			try {
				a = str.getBytes("GBK");
			} catch (UnsupportedEncodingException e) {
				result = null;
				logger.error("", e);
			}
			if (endIndex > a.length) {
				throw new StringIndexOutOfBoundsException(endIndex);
			}
			if (endIndex > 0) {
				try {
					result = new String(a, beginIndex, endIndex - beginIndex, "GBK");
				} catch (UnsupportedEncodingException e) {
					result = null;
					logger.error("", e);
				}
			}
		}
		return result;
	}

	/**
	 * 按字节数截取GBK字符串
	 * 
	 * @param str
	 * @param beginIndex
	 * @return
	 */
	public final static String subStringByByte(String str, int beginIndex) {
		byte[] a = null;
		String result = null;
		try {
			a = str.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
			return null;
		}
		result = subStringByByte(str, beginIndex, a.length);
		return result;
	}

	/**
	 * 获取Object的Json表达式
	 * 
	 * @param object
	 * @return
	 */
	public static String jsonFromObject(Object object) {

		StringWriter writer = new StringWriter();

		try {
			objectMapper.writeValue(writer, object);

		} catch (RuntimeException e) {
			logger.error("jsonFromObject failure", e);
			throw e;

		} catch (Exception e) {
			logger.error("jsonFromObject failure", e);
			return null;
		}

		return writer.toString();

	}

	/**
	 * 将json串转换为list+linkedhashMap对象
	 * 
	 * @param json
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List listFromjson(String json) {
		try {
			List l = objectMapper.readValue(json, List.class);
			return l;
		} catch (JsonParseException e) {
			logger.error("objectFromJson failure", e);
		} catch (JsonMappingException e) {
			logger.error("objectFromJson failure", e);
		} catch (IOException e) {
			logger.error("objectFromJson failure", e);
		}
		return null;
	}

	/**
	 * 将json串转换为linkedhashMap+list对象
	 * 
	 * @param json
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Map mapFromjson(String json) {
		try {
			Map m = objectMapper.readValue(json, Map.class);
			return m;
		} catch (JsonParseException e) {
			logger.error("objectFromJson failure", e);
		} catch (JsonMappingException e) {
			logger.error("objectFromJson failure", e);
		} catch (IOException e) {
			logger.error("objectFromJson failure", e);
		}
		return null;
	}

	/**
	 * 获取错误堆栈的字符串
	 * 
	 * @param e
	 * @return
	 */
	public static String getErrorStackString(Throwable e) {
		java.io.CharArrayWriter cw = new java.io.CharArrayWriter();
		java.io.PrintWriter pw = new java.io.PrintWriter(cw, true);
		e.printStackTrace(pw);
		pw.close();
		return cw.toString();
	}

	/**
	 * 在错误堆栈的字符串字符串前 加一句简单的 "错误信息：" + e.getMessage() + "\n 错误堆栈："
	 * 
	 * @param e
	 * @return
	 */
	public static String getErrorDetailMessage(Throwable e) {
		return "错误信息：" + e.getMessage() + "\n 错误堆栈：" + getErrorStackString(e);
	}

	/**
	 * 将不一定规范的WebAppName格式化成 /bsys 类似的以 /开头，且结尾无/ 的格式
	 * 
	 * @param name
	 * @return
	 */
	public static String makeWebAppName(String name) {
		if ('/' != name.charAt(0)) {
			name = "/" + name;
		}

		if (name.endsWith("/")) {
			name = name.substring(0, name.length() - 1);
		}

		return name;
	}

	public static Pattern pMakeTruthString = Pattern.compile("\\$\\{([\\u4e00-\\u9fa5\\w\\.#]+)\\}");
	private static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 将形如xxx${a}xxx${b}的字符串，使用values-map进行填充，得到真实的字符串结果。
	 * 例如：makeTrustString("http://${ip}:${port}/bpm",new
	 * EasyMap().putValue("ip","192.168.1.211").putValue("port","8080"));
	 * return: http://192.168.1.211:8080/bpm
	 * 
	 * @param source
	 * @param values
	 * @return
	 */
	public static String makeTruthString(String source, Map<String, ?> values) {
		if (StringUtil.isEmpty(source)) {
			return "";
		}

		Matcher m = pMakeTruthString.matcher(source);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String name = m.group(1);// 键名
			String value = (String) values.get(name);// 键值
			if (value == null) {
				value = "";
			}
			m.appendReplacement(sb, "");
			sb.append(value);
		}
		m.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 将形如xxx${a}xxx${b}的字符串， 返回 [a,b]
	 * 
	 * @param source
	 * @return
	 */
	public static Set<String> getVars(String source) {
		Set<String> l = new HashSet<String>();

		if (StringUtil.isEmpty(source)) {
			return l;
		}

		Matcher m = pMakeTruthString.matcher(source);
		while (m.find()) {
			String name = m.group(1);// 键名
			l.add(name);
		}
		return l;
	}

	public static boolean isNumeric(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static void main(String[] args) throws UnsupportedEncodingException {

		System.out.println(endOf("aaabcd", 3).equals("bcd"));

		System.out.println(getVars("http${中文}://${中文}:${英文}/bpm$${aaa}"));

		System.out.println("A啊 getGbkLength:" + getGbkLength("A啊"));
		System.out.println("A:" + "A".getBytes("GBK").length);
		System.out.println("┐:" + "┐".getBytes("GBK").length);
		System.out.println("犇:" + "犇".getBytes("GBK").length);
		System.out.println("┐ isHanZi:" + isHanZi('┐'));
		System.out.println("★ isHanZi:" + isHanZi('★'));
		System.out.println("嶹 isHanZi:" + isHanZi('嶹'));
		System.out.println("犇 isHanZi:" + isHanZi('犇'));
		System.out.println("X isHanZi:" + isHanZi('X'));

		System.out.println("你好A的GBK长度:" + "你好A".getBytes("GBK").length);

		System.out.println(makeTruthString("http://${i.p}:${port}/bpm",
				new EasyMap<String, String>().putValue("i.p", "ttt${aaa}tt").putValue("port", "8080${bbb}")));
		// System.out.println(makeSqlCondition(new EasyMap<String,
		// String>().putValue("f.f1:eq", "1").putValue("f.f2:like", "8"),
		// "A${condition}B"));

		System.out.println(">=1.2 2".replaceAll(" ", ""));
		System.out.println(">=1.2 ".replaceAll("[\\d\\.]", ""));
		System.out.println(">=1.2 ".replaceAll("[\\>\\<\\=]", ""));

		System.out.println(getErrorStackString(new Throwable("Test")));

		try {
			Object o = new ObjectMapper().readValue(
					"{\"X\":{\"a\":\"a\",\"b\":\"b\"},\"Y\":{\"a\":\"a1\",\"b\":\"b\"},\"Z\":{\"a\":\"a2\",\"b\":\"b\"}}", Map.class);

			System.out.println(o);
		} catch (JsonParseException e) {

			logger.error("", e);
		} catch (JsonMappingException e) {

			logger.error("", e);
		} catch (IOException e) {

			logger.error("", e);
		}
	}

	/**
	 * null+null=false;any string contains ""
	 * 
	 * @param source
	 * @param part
	 * @return
	 */
	public static boolean contains(String source, String part) {
		if (source == null || part == null) {
			return false;
		}
		if (isBlank(part) || source.indexOf(part) != -1) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isNotBlank(String s) {
		return !isBlank(s);
	}

	/**
	 * return (s == null) || "".equals(s.trim())
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isBlank(String s) {
		return (s == null) || "".equals(s.trim());
	}

	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}

	/**
	 * return (s == null) || "".equals(s);
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String s) {
		return (s == null) || "".equals(s);
	}

	/**
	 * return (s == null) || "".equals(s) || "null".equals(s) ||
	 * "".equals(s.trim());
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isNullValue(String s) {
		return (s == null) || "".equals(s) || "null".equals(s) || "".equals(s.trim());
	}

	public static String trim(Object obj) {
		return obj == null || obj.toString() == null ? "" : obj.toString().trim();
	}

	/**
	 * 对需要查询的参数，生成模糊查询，即在两侧增加%。例：pan -> %pan%
	 * 
	 * @param str
	 * @return
	 */
	public static String makeSqlLike(String str) {
		return StringUtils.isEmpty(str) ? null : "%" + str + "%";
	}

	/**
	 * 
	 * @param ip
	 * @return
	 */
	public static String makeIpv6toIpv4ForLocalhost(String ip) {
		if (ip.equals("0:0:0:0:0:0:0:1")) {
			return "127.0.0.1"; // 暂时处理一下IPV6的问题
		} else {
			return ip;
		}
	}

	/**
	 * 使用java正则表达式去掉多余的.与0
	 * 
	 * @param s
	 * @return
	 */
	public static String subZeroAndDot(String s) {
		if (s.indexOf(".") > 0) {
			s = s.replaceAll("0+?$", "");// 去掉多余的0
			s = s.replaceAll("[.]$", "");// 如最后一位是.则去掉
		}
		return s;
	}

	/**
	 * 描述：双精度浮点数转换成指定标度的字符串，其采用舍入模式为向上舍入模式
	 * 
	 * @param d
	 *            双精度浮点数
	 * @param i
	 *            标度
	 * @return String
	 */
	public static String doubleToString(double d, int i) {
		BigDecimal bd = new BigDecimal(Double.toString(d));
		return bd.setScale(i, BigDecimal.ROUND_HALF_UP).toString();
	}

	/**
	 * 函数功能说明： 金额格式化(如果小数不部分都为0，那就值返回整数部分) 使用案例：
	 * StringUtil.doubleToStringNotEndWithZero(33.0001,1) 返回结果：33 注释编写人： 袁金锋
	 * 
	 * @param d
	 *            金额
	 * @param i
	 *            保留几位小数
	 * @return
	 */
	public static String doubleToStringNotEndWithZero(double d, int i) {
		String s = doubleToString(d, i);
		if (i > 0) {
			s = s.replaceAll("0+$", "");
			if (s.charAt(s.length() - 1) == '.') {
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}

	public static String doubleToStringWithThNotZeroEnd(double d, int i) {
		String s = doubleToStringWithTh(d, i);
		if (i > 0) {
			s = s.replaceAll("0+$", "");
			if (s.charAt(s.length() - 1) == '.') {
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}

	public static String doubleToStringWithTh(double d, int i) {
		String ss = doubleToString(d, i);
		int indexOfPoint = ss.indexOf(".");
		String s = "#,##0";
		DecimalFormat decimalformat = (DecimalFormat) NumberFormat.getInstance();
		decimalformat.applyPattern(s);
		if (indexOfPoint == -1) { // 是整数
			return decimalformat.format(d);
		}
		String intString = ss.substring(0, indexOfPoint); // 整数部分
		String smallString = ss.substring(indexOfPoint + 1); // 小数部分
		return (d < 0 ? "-" : "") + decimalformat.format(Math.abs(Long.parseLong(intString))) + "." + smallString;
	}

	/**
	 * 函数功能说明：字符串source中包含多少个某字符(ch)
	 * 
	 * @param source
	 *            源字符串
	 * @param ch
	 *            查找的字符
	 * @return count 个数
	 */
	public static int containCharCount(String source, char ch) {
		int index = 0;
		int count = 0;
		while (true) {
			index = source.indexOf(ch);
			if (index == -1)
				break;
			source = source.substring(index + 1);
			count++;
		}
		return count;
	}

	/**
	 * like Oracle--nvl <br>
	 * return testValue==null?testValue:defaultValue;
	 * 
	 * @param testValue
	 * @param defaultValue
	 * @return
	 */
	public static String nvl(String testValue, String defaultValue) {
		return StringUtil.isEmpty(testValue) ? defaultValue : testValue;
	}

	/**
	 * like Oracle--nvl <br>
	 * return testValue==null?testValue:defaultValue;
	 * 
	 * @param testValue
	 * @param defaultValue
	 * @return
	 */
	public static String nvlFilterNUll(String testValue, String defaultValue) {
		if (StringUtil.isEmpty(testValue) || "null".equals(testValue) || "NULL".equals(testValue)) {
			return defaultValue;
		} else {
			return testValue;
		}
	}

	/**
	 * return "["+id+"]"+name;
	 * 
	 * @param id
	 * @param name
	 * @return
	 */
	public static String idNameText(String id, String name) {
		if (StringUtil.isEmpty(id)) {
			return name;
		}
		if (StringUtil.isEmpty(name)) {
			return id;
		}
		return "[" + id + "]" + name;
	}

	/**
	 * return k1+'\t'+k2;
	 * 
	 * @param k1
	 * @param k2
	 * @return
	 */
	public static String safeJoin(String k1, String k2) {
		return k1 + '\t' + k2;
	}

	public static String safeJoin(String... k) {
		StringBuilder sb = new StringBuilder();
		for (String s : k) {
			sb.append(s).append('\t');
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public static String[] safeSplit(String s) {
		return s.split("\t");
	}

	public static <T> T objectFormJson(String source, Class<T> clazz) {
		T o = null;
		try {
			o = objectMapper.readValue(source, clazz);
		} catch (JsonParseException e) {
			logger.error("objectFormJson(String, Class<T>) " + source, e);
		} catch (JsonMappingException e) {
			logger.error("objectFormJson(String, Class<T>) " + source, e);
		} catch (IOException e) {
			logger.error("objectFormJson(String, Class<T>) " + source, e);
		}
		return o;
	}

	/**
	 * 如果字符串较短，substring会抛异常。这个安全
	 * 
	 * @param source
	 * @param limit
	 * @return
	 */
	public static String safeCut(String source, int limit) {
		if (source.length() < limit) {
			// limit = source.length(); //有点笨
			return source;
		}
		return source.substring(0, limit);
	}

	/**
	 * 不四舍五入格式化数字
	 */
	public static String formatNumberNoRound(double d, int i) {
		BigDecimal bd = new BigDecimal(Double.toString(d));
		return bd.setScale(i, BigDecimal.ROUND_FLOOR).toString();
	}

	public static String[] splitPreserveAllTokens(String str, char separatorChar) {
		return splitWorker(str, separatorChar, true);
	}

	private static String[] splitWorker(String str, char separatorChar, boolean preserveAllTokens) {
		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len == 0) {
			return EMPTY_STRING_ARRAY;
		}
		List<String> list = new ArrayList<String>();
		int i = 0;
		int start = 0;
		boolean match = false;
		boolean lastMatch = false;
		while (i < len)
			if (str.charAt(i) == separatorChar) {
				if ((match) || (preserveAllTokens)) {
					list.add(str.substring(start, i));
					match = false;
					lastMatch = true;
				}
				i++;
				start = i;
			} else {
				lastMatch = false;

				match = true;
				i++;
			}
		if ((match) || ((preserveAllTokens) && (lastMatch))) {
			list.add(str.substring(start, i));
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	public static String getUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static String getUtf8FromGbk(String gbk) {
		try {
			return new String(gbk.getBytes("GBK"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("getUtf8FromGbk(String) - gbk=" + gbk, e); //$NON-NLS-1$ 
			return "";
		}
	}

	/**
	 * return "Y".equals(flag);
	 * 
	 * @param flag
	 * @return
	 */
	public static boolean isY(String flag) {
		return "Y".equals(flag);
	}

	public static String newString(byte[] bytes, String encoding) {
		try {
			return new String(bytes, encoding);
		} catch (UnsupportedEncodingException e) {
			logger.error("newString(byte[], String) - encoding=" + encoding, e); //$NON-NLS-1$
			return null;
		}
	}

	public static Object asciiToLowerCase(String s) {

		char[] c = null;
		int i = s.length();

		// look for first conversion
		while (i-- > 0) {
			char c1 = s.charAt(i);
			if (c1 <= 127) {
				char c2 = lowercases[c1];
				if (c1 != c2) {
					c = s.toCharArray();
					c[i] = c2;
					break;
				}
			}
		}

		while (i-- > 0) {
			if (c[i] <= 127)
				c[i] = lowercases[c[i]];
		}

		return c == null ? s : new String(c);
	}

	/**
	 * 删除尾部0字符
	 * 
	 * @param s
	 * @param c
	 * @return
	 */
	public static String delete0FromTail(String s, char c) {
		return s.replaceAll("0+$", "");
	}

	/**
	 * 针对sql语句进行 ${user},${bank}的替换，通常用于数据权限控制<BR>
	 * EasyMap<String, String> userMap = new EasyMap<String, String>()<BR>
	 * .putValue("user", LoginManager.getUserId())<BR>
	 * .putValue("bank", LoginManager.getLogin().getUser().getInstId());<BR>
	 * <BR>
	 * return StringUtil.makeTruthString(sql, userMap);
	 * 
	 * @param sql
	 * @return
	 */
	public String makeUserSql(String sql) {
		EasyMap<String, String> userMap = new EasyMap<String, String>().putValue("user", LoginManager.getUserId()).putValue("bank",
				LoginManager.getLogin().getUser().getInstId());

		return StringUtil.makeTruthString(sql, userMap);
	}

	final static long YI = (long) '一';
	final static long ZOU = (long) '龥';

	public static boolean isHanZi(char c) {
		if (c >= YI && c <= ZOU) {
			return true;
		} else {
			return false;
		}
	}

	public static int getGbkLength(String s) {
		if (s == null || s.equals("")) {
			return 0;
		} else {
			try {
				return s.getBytes("GBK").length;
			} catch (UnsupportedEncodingException e) {
				return 0; // 不该如此
			}
		}
	}

	/**
	 * 32位小写 MD5 编码
	 * 
	 * @param s
	 * @param encoding
	 * @return
	 */
	public static String md5(String s, String encoding, boolean lowercase) {

		String result = null;

		if (encoding == null) {
			encoding = "GBK";
		}

		// 用来将字节转换成 16 进制表示的字符
		char[] hexDigits;
		if (lowercase) {
			hexDigits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		} else {
			hexDigits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		}

		if (s != null) {

			try {

				// 返回实现指定摘要算法的 MessageDigest 对象

				MessageDigest md = MessageDigest.getInstance("MD5");

				// 使用utf-8编码将originstr字符串编码并保存到source字节数组

				byte[] source = s.getBytes(encoding);

				// 使用指定的 byte 数组更新摘要

				md.update(source);

				// 通过执行诸如填充之类的最终操作完成哈希计算，结果是一个128位的长整数

				byte[] tmp = md.digest();

				// 用16进制数表示需要32位

				char[] ss = new char[32];

				for (int i = 0, j = 0; i < 16; i++) {

					// j表示转换结果中对应的字符位置

					// 从第一个字节开始，对 MD5 的每一个字节

					// 转换成 16 进制字符

					byte b = tmp[i];

					// 取字节中高 4 位的数字转换

					// 无符号右移运算符>>> ，它总是在左边补0

					// 0x代表它后面的是十六进制的数字. f转换成十进制就是15

					ss[j++] = hexDigits[b >>> 4 & 0xf];

					// 取字节中低 4 位的数字转换

					ss[j++] = hexDigits[b & 0xf];

				}

				result = new String(ss);// 结果转换成字符串用于返回

			} catch (NoSuchAlgorithmException e) {

				// 当请求特定的加密算法而它在该环境中不可用时抛出此异常

				logger.warn("md5(String) - s=" + s, e); //$NON-NLS-1$

			} catch (UnsupportedEncodingException e) {

				// 不支持字符编码异常

				logger.warn("md5(String) - s=" + s, e); //$NON-NLS-1$

			}

		}

		return result;

	}

	/**
	 * 全角空格为12288，半角空格为32 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
	 * 
	 * 将字符串中的全角字符转为半角
	 * 
	 * @param src
	 *            要转换的包含全角的任意字符串
	 * @return 转换之后的字符串
	 */
	public static String toSemiangle(String src) {
		char[] c = src.toCharArray();
		for (int index = 0; index < c.length; index++) {
			if (c[index] == 12288) {// 全角空格
				c[index] = (char) 32;
			} else if (c[index] > 65280 && c[index] < 65375) {// 其他全角字符
				c[index] = (char) (c[index] - 65248);
			}
		}
		return String.valueOf(c);
	}

	/**
	 * 转换object对象为字符串,不论是否数值
	 * 
	 * @param resultObject
	 * @return
	 */
	public static String convertToStr(Object resultObject) {
		if (null == resultObject) {
			return "";
		} else if (resultObject instanceof BigDecimal) {
			return ((BigDecimal) resultObject).toPlainString();
		} else if (resultObject instanceof Number) {
			return ((Number) resultObject).toString();
		} else if (resultObject instanceof String) {
			return (String) resultObject;
		} else {
			return resultObject.toString();
		}
	}

	public static CharSequence endOf(CharSequence cs, int length) {
		if (cs.length() < length) {
			return "";
		} else {
			return cs.subSequence(cs.length() - length, cs.length());
		}
	}
	/**
	 * XML 特殊字符处理
	 * <转成&lt;
	 * >转成&gt;
	 * &转成&amp;
	 * @param originStr
	 * @return
	 */
	public static String escapeXml(String originStr) {
		if (StringUtil.isBlank(originStr)) {
			return originStr;
		}
		originStr = StringUtil.replace(originStr, "&", "&amp;");
		originStr = StringUtil.replace(originStr, "<", "&lt;");
		originStr = StringUtil.replace(originStr, ">", "&gt;");
		return originStr;
	}
	public static String makeMoreThan1000InSql(String field, List<String> inList, int inCount) {

		int listSize = inList.size();
		if (listSize == 0) {
			return "";
		}
		int count = listSize / inCount;
		String orSql = "";
		for (int i = 1; i <= count; i++) {
			orSql += " or " + field + " in('" + StringUtil.collectionToDelimitedString(inList.subList(inCount * (i - 1), inCount * i), "','") + "') ";
		}
		if (listSize % inCount != 0) {
			orSql += " or " + field + " in('" + StringUtil.collectionToDelimitedString(inList.subList(inCount * count, listSize), "','") + "') ";
		}
		orSql = orSql.substring(3, orSql.length());
		return orSql;
	}
	
	public static int subStrCount(String str,String sub) {
		int count = 0;
		int start = 0;
		while (str.indexOf(sub, start) >= 0 && start < str.length()) {
			count++;
			start = str.indexOf(sub, start) + sub.length();
		}
		return count;
	}
	
	public static String makeSqlWildcard(int count){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<count;i++){
			sb.append("?,");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	
	public static String makeNumberOfChar(int number,char str){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<number;i++){
			sb.append(str);
		}
		return sb.toString();
	}
	
	

}
