package srp.bapp.utils;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

public abstract class IdUtil {

	private static String lastId = "";// 记录最近一次id变化
	private static int step = 0;// 附加当前数值

	/**
	 * 根据时间和随机数生成一个Id主键
	 * 
	 * @return
	 */
	public synchronized static String getIdByTime() {
		String id = new DateTime().toString("yyyyMMddHHmmssSSS");
		if (id.equals(lastId)) {
			id = id + step++;
		} else {
			lastId = id;
			step = 0;
		}
		return id;
	}
	/*
	 * 测试重复id public static void main(String[] args) { DateTime dt1 = new
	 * DateTime(); for (int i = 0; i < 30000; i++) { String id =
	 * IdUtil.getIdByTime2(); List<String> ids = new LinkedList<String>(); if
	 * (ids.contains(id)) { System.out.println("重复id：" + id); } else {
	 * ids.add(id); } } DateTime dt2 = new DateTime();
	 * System.out.println("over1:" + (dt2.getSecondOfMinute() -
	 * dt1.getSecondOfMinute())); }
	 */
	/**
	 * 获得UUID
	 * @return
	 */
	public synchronized static String getUUID() {
		UUID id = UUID.randomUUID();
		return StringUtil.replace(id.toString(), "-", "");
	}
}
