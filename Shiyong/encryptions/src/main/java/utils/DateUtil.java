package srp.bapp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import srp.bapp.tools.EasyMap;

public abstract class DateUtil {
	public static final String MM = "MM";
	public static final String FORMAT6 = "yyMMdd";
	public static final String FORMAT8 = "yyyyMMdd";
	public static final String YYYYMMDDHHMM = "yyyyMMddHHmm";
	public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	public static final String FORMAT10 = "yyyy-MM-dd";
	public static final String FORMAT20 = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT20_2 = "yyyy/MM/dd HH:mm:ss";
	public static final String FORMAT30 = "yyyy-MM-dd HH:mm:ss SSS";
	public static final String FORMAT_YMD_CN = "yyyy年MM月dd日";
	public static final String FORMAT_YM_CN = "yyyy年MM月";
	public static final String FORMAT_Y_CN = "yyyy年";
	public static final String YYYYMM = "yyyyMM";
	public static final String YYYY = "yyyy";

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(DateUtil.class);

	/**
	 * 返回当天日期的yyyyMMdd表示
	 * 
	 * @return
	 */
	public static String getToday2() {
		return new DateTime().toString(FORMAT8);
	}
	/**
	 * 返回当天日期的yyyyMMddHHmmss表示
	 * 
	 * @return
	 */
	public static String getToday3() {
		return new DateTime().toString(YYYYMMDDHHMMSS);
	}

	/**
	 * 返回当年yyyy表示
	 * 
	 * @return
	 */
	public static String getYear() {
		return new DateTime().toString(YYYY);
	}

	/**
	 * 返回当年yy表示
	 * 
	 * @return new DateTime().toString(YYYY).substring(2)
	 */
	public static String getYear2() {
		return new DateTime().toString(YYYY).substring(2);
	}

	/**
	 * 返回当月MM表示
	 * 
	 * @return
	 */
	public static String getMonth() {
		return new DateTime().toString(MM);
	}

	/**
	 * 返回当天日期的yyyyMMdd表示
	 * 
	 * @return
	 */
	public static String getTodayYYYYMMDD() {
		return new DateTime().toString(FORMAT8);
	}

	/**
	 * 返回当天日期的yyMMdd表示
	 * 
	 * @return
	 */
	public static String getTodayYYMMDD() {
		return new DateTime().toString(FORMAT6);
	}

	/**
	 * 返回当天日期的yyyy-MM-dd表示
	 * 
	 * @return
	 */
	public static String getToday() {
		return new DateTime().toString(FORMAT10);
	}

	/**
	 * 返回当天日期的yyyy-MM-dd hh:mm:ss表示
	 * 
	 * @return
	 */
	public static String getNowTime() {
		return new DateTime().toString(FORMAT20);
	}

	/**
	 * 返回当天日期的yyyyMMdd hh:mm:ssssss表示
	 * 
	 * @return
	 */
	public static String getNowTimeMillisecond() {
		return new DateTime().toString(FORMAT30);
	}

	/**
	 * 返回 当时时间的自选format显示--yyyyMMddhhmmssssss
	 * 
	 * @return
	 */
	public static String getNow(String format) {
		return new DateTime().toString(format);
	}

	/**
	 * 返回当天日期的yyyy-MM-dd表示
	 * 
	 * @return
	 */
	public static String getDateByTime(String time20) {
		return time20.substring(0, 10);
	}

	/**
	 * 返回明天日期的yyyy-MM-dd表示
	 * 
	 * @return
	 */
	public static String getTomorrow() {
		return new DateTime().plusDays(1).toString(FORMAT10);
	}

	/**
	 * 返回今天的日期（几号）
	 * 
	 * @return
	 */
	public static int getTodayDay() {
		return new DateTime().getDayOfMonth();
	}

	/**
	 * 返回现在的"HHmm"时间字符串
	 * 
	 * @return
	 */
	public static String getNowHHmm() {
		return new DateTime().toString("HHmm");
	}

	/**
	 * 返回现在的"HH:MM"时间字符串
	 * 
	 * @return
	 */
	public static String getNowHHMM() {
		DateTime dt = new DateTime();
		int h = dt.getHourOfDay();
		int m = dt.getMinuteOfHour();
		StringBuffer sb = new StringBuffer();
		if (h < 10) {
			sb.append('0');
		}
		sb.append(h).append(':');
		if (m < 10) {
			sb.append('0');
		}
		sb.append(m);
		return sb.toString();
	}

	/**
	 * 返回前n天的日期字符串
	 * 
	 * @return
	 */
	public static String getOffsetDay(int days) {
		return new DateTime().minusDays(days).toString(FORMAT10);
	}
	
	/**
	 * 返回前n天的日期字符串
	 * 
	 * @return FORMAT8
	 */
	public static String getOffsetDay8(int days) {
		return new DateTime().minusDays(days).toString(FORMAT8);
	}

	/**
	 * 返回上月的最后一天
	 * 
	 * @return
	 */
	public static String getLastMonthDay() {
		return getOffsetDay(getTodayDay());
	}

	/**
	 * 根据传入日期 MM 返回上月的yyyy-MM 其他 返回上月的yyyy-MM-dd
	 * 
	 * @return
	 */
	public static String getLastMonthTime(String dataDate, String type) {
		int day = formatDateTime(dataDate, FORMAT10).getDayOfMonth();
		String s = formatDateTime(dataDate, FORMAT10).minusDays(day).toString(FORMAT10);
		if (MM.equals(type)) {
			s = s.substring(0, 7);
		}
		return s;
	}

	/**
	 * 返回上个月yyyy-MM
	 * 
	 * @return
	 */
	public static String getLastMonth() {
		String s = getLastMonthDay();
		s = s.substring(0, 7);
		return s;
	}

	/**
	 * 返回前n天的日期字符串
	 * 
	 * @return
	 */
	public static String getOffsetDay(int days, String date) {
		return formatDateTime(date, FORMAT10).minusDays(days).toString(FORMAT10);
	}
 
	/**
	 * 返回后n天的日期字符串
	 * 
	 * @return
	 */
	public static String getNextOffsetDay(int days, String date) {
		return formatDateTime(date, FORMAT10).plusDays(days).toString(FORMAT10);
	}

	public static String getNextOffsetDay8(int days, String date) {
		return formatDateTime(date, FORMAT8).plusDays(days).toString(FORMAT8);
	}

	/**
	 * 返回与今天的日期差值(早于今天为正，晚于今天为负)
	 * 
	 * @return
	 */
	public static int getOffsetDays(String date) {
		return getOffsetDays(date, getToday());
	}

	/**
	 * 返回两个日期差值
	 * 
	 * @return
	 */
	public static int getOffsetDays(String ldate, String udate) {
		long upperTime, lowerTime;
		upperTime = formatDateTime(udate, FORMAT10).getMillis();
		lowerTime = formatDateTime(ldate, FORMAT10).getMillis();
		Long result = new Long((upperTime - lowerTime) / (1000 * 60 * 60 * 24));
		return result.intValue();
	}

	/**
	 * 格式化日期
	 * 
	 * @param date
	 * @param pattern
	 * @return 日期类型
	 * @throws Exception
	 */
	public static DateTime formatDateTime(String date, String pattern) {
		try {
			return new DateTime(new SimpleDateFormat(pattern).parse(date));
		} catch (Exception e) {
			logger.error("formatDateTime(String, String) - date=" + date + ", pattern=" + pattern, e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	/**
	 * 格式化日期
	 * 
	 * @param date
	 * @param pattern
	 * @return 日期类型
	 * @throws Exception
	 */
	public static java.util.Date formatJavaDateTime(String date, String pattern) {
		try {
			return new SimpleDateFormat(pattern).parse(date);
		} catch (Exception e) {
			logger.error("formatDateTime(String, String) - date=" + date + ", pattern=" + pattern, e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	/**
	 * 格式化日期
	 * 
	 * @param date
	 * @param pattern
	 * @return 日期类型
	 * @throws Exception
	 */
	public static String formatDateTime(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}

	public static Map<String, String> getDataDateVars() {
		return getDatadateVars(getToday());
	}

	public static String formatP1ToP2DateString(String date, String p1, String p2) {
		return new SimpleDateFormat(p2).format(formatJavaDateTime(date, p1));
	}

	public static Map<String, String> getDatadateVars(String dataDate) {
		String date = dataDate;
		String date1 = DateUtil.convertDate(dataDate, FORMAT10, FORMAT8);
		String date2 = dataDate;
		String dateY = DateUtil.convertDate(dataDate, FORMAT10, "yyyy");
		String dateM = DateUtil.convertDate(dataDate, FORMAT10, "MM");
		String dateD = DateUtil.convertDate(dataDate, FORMAT10, "dd");
		EasyMap<String, String> easyMap = new EasyMap<String, String>();
		easyMap.putValue("date", date).putValue("date1", date1).putValue("date2", date2).putValue("dateY", dateY).putValue("dateM", dateM)
				.putValue("dateD", dateD);
		return easyMap;
	}

	/**
	 * 转换输入字符串的格式
	 * 
	 * @param date
	 * @param inPattern
	 * @param outPattern
	 * @return
	 * @throws Exception
	 */
	public static String convertDate(String date, String inPattern, String outPattern) {
		try {
			return new DateTime(new SimpleDateFormat(inPattern).parse(date)).toString(outPattern);
		} catch (Exception e) {
			logger.error("convertDateStr(String, String, String) - 日期格式不正确：" + date, e); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * 返回传入日期月的最后一天
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String getLastDay(String date, String pattern) {
		DateTime dateTime = formatDateTime(date, pattern);
		dateTime = dateTime.dayOfMonth().withMaximumValue();
		return dateTime.toString(pattern);
	}

	/**
	 * HHmmssSSS
	 */
	public static String getShortTimeStamp() {
		return formatDateTime(new Date(), "HHmmssSSS");
	}

	/**
	 * yyyyMMddHHmmssSSS
	 */
	public static String getFullTimeStamp() {
		return formatDateTime(new Date(), "yyyyMMddHHmmssSSS");
	}

	/**
	 * 返回前n年的日期字符串FORMAT10
	 * 
	 * @return
	 */
	public static String getOffsetYear10(String dataDate10, int years) {
		return new DateTime(dataDate10).minusYears(years).toString(FORMAT10);
	}

	/**
	 * 返回前n天的日期字符串FORMAT20
	 * 
	 * @return
	 */
	public static String getOffsetDay20(int days) {
		return new DateTime().minusDays(days).toString(FORMAT20);
	}

	/**
	 * 返回前n天的日期字符串FORMAT20
	 * 
	 * @return
	 */
	public static String getOffsetDay30(int days) {
		return new DateTime().minusDays(days).toString(FORMAT30);
	}

	/**
	 * 格式化日期
	 * 
	 * @param date
	 * @param pattern
	 * @return 日期类型
	 */
	public static String formatDateTime(DateTime now, String format) {
		return now.toString(format);
	}

	/**
	 * FORMAT20
	 * 
	 * @param minutes
	 * @return
	 */
	public static String getOffsetMins(int minutes) {
		return new DateTime().plusMinutes(minutes).toString(FORMAT20);
	}
	
	/**
	 * YYYYMMDDHHMM,FORMAT12
	 * 
	 * @param minutes
	 * @return
	 */
	public static String getOffsetMins12(String yyyyMMddhhmm,int minutes) {
		int yyyy=Integer.parseInt(yyyyMMddhhmm.substring(0, 4));
		int month=Integer.parseInt(yyyyMMddhhmm.substring(4, 6));
		int date=Integer.parseInt(yyyyMMddhhmm.substring(6, 8));
		int hour=Integer.parseInt(yyyyMMddhhmm.substring(8, 10));
		int min=Integer.parseInt(yyyyMMddhhmm.substring(10, 12));
		return new DateTime(yyyy,month,date,hour,min).plusMinutes(minutes).toString(YYYYMMDDHHMM);
	}

	/**
	 * 返回两个日期之间的差多少秒
	 * 
	 * @return
	 */
	public static int getDiffSeconds(String startDate, String endDate) {
		long upperTime, lowerTime;
		upperTime = formatDateTime(endDate, FORMAT20).getMillis();
		lowerTime = formatDateTime(startDate, FORMAT20).getMillis();
		Long result = new Long((upperTime - lowerTime) / (1000));
		return result.intValue();
	}

	/**
	 * 取季初
	 * 
	 * @param date
	 * @return
	 */
	public static String getSeasonBegin(Date date) {
		int month = Integer.parseInt(formatDateTime(date, "MM"));
		String newDateStr = formatDateTime(date, "yyyy") + "-";
		if (month >= 1 && month <= 3) {
			newDateStr += "01-01";
		} else if (month >= 4 && month <= 6) {
			newDateStr += "04-01";
		} else if (month >= 7 && month <= 9) {
			newDateStr += "07-01";
		} else if (month >= 10 && month <= 12) {
			newDateStr += "10-01";
		}
		return newDateStr;
	}

	/**
	 * 今天周几 返回1-7
	 * 
	 * @return
	 */
	public static int whatDayIsToday() {
		return new DateTime().getDayOfWeek();
	}

	/**
	 * yyyyMMdd周几 返回1-7
	 * 
	 * @return
	 */
	public static int whatDayIs(String yyyyMMdd) {
		return new DateTime(Integer.parseInt(yyyyMMdd.substring(0, 4)), Integer.parseInt(yyyyMMdd.substring(4, 6)), Integer.parseInt(yyyyMMdd
				.substring(6, 8)), 1, 0).getDayOfWeek();
	}
	
	public static void main(String[] args) {
		System.out.println(getOffsetMins12("201509301530",-5555));
	}
	
}