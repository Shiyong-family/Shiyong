package srp.bapp.utils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import srp.bapp.pub.runtime.BappDebugException;
import srp.bapp.tools.ConcurrentLRUMap;
import srp.bapp.tools.Entry;
import srp.bapp.tools.ObjectBuilder;

public abstract class CacheUtil {
	// private static final String BAPP_CACHE = "bappCache";
	// private final static CacheManager cacheManager;
	private final static ConcurrentHashMap<String, String> bappCl = new ConcurrentHashMap<String, String>();
	private final static ConcurrentHashMap<String, Integer> bappCl4Size = new ConcurrentHashMap<String, Integer>();
	private final static ConcurrentHashMap<String, HashSet<String>> ssCache = new ConcurrentHashMap<String, HashSet<String>>();
	private final static ConcurrentHashMap<String, ConcurrentLRUMap<String, Object>> bappCache = new ConcurrentHashMap<String, ConcurrentLRUMap<String, Object>>();

	// static {
	// Configuration configuration = new Configuration();
	// configuration.setUpdateCheck(false);
	// configuration.addSizeOfPolicy(new
	// SizeOfPolicyConfiguration().maxDepthExceededBehavior(SizeOfPolicyConfiguration.MaxDepthExceededBehavior.CONTINUE).maxDepth(100));
	// configuration.addDiskStore(new
	// DiskStoreConfiguration().path(FileUtil.TEMP_PATH_NAME + BAPP_CACHE));
	// cacheManager = new CacheManager(configuration);
	// cacheManager.addCache(bappCache);
	// }
	
	/**
	 * 仅用于单元测试
	 */
	public final static void unregisterAll(){
		bappCl.clear();
	}

	/**
	 * 缓存注册
	 * 
	 * @param name
	 *            缓存主键
	 * @param desc
	 *            缓存描述（主要用于页面显示）
	 */
	public final static void registerCache(String name, String desc) {
		if (bappCl.containsKey(name)) {
			throw new BappDebugException("[" + name + "]已经被其他过程注册");
		}
		bappCl.put(name, desc);
	}
	
	/**
	 * 本方法注册可以指定缓存的size
	 * @param name
	 * @param desc
	 * @param size
	 */
	public final static void registerCache(String name, String desc,int size) {
		if (bappCl.containsKey(name)) {
			throw new BappDebugException("[" + name + "]已经被其他过程注册");
		}
		bappCl.put(name, desc);
		bappCl4Size.put(name, size);
	}

	public final static void registerCache(String name, String desc, String ssId) {
		registerCache(name, desc);
		useBy(name, ssId);
	}

	/**
	 * 那个子系统使用了这个缓存
	 * 
	 * @param name
	 * @param ssId
	 */
	public final static void useBy(String name, String ssId) {
		if (ssCache.containsKey(ssId)) {
			ssCache.get(ssId).add(name);
		} else {
			HashSet<String> set = new HashSet<String>();
			set.add(name);
			ssCache.put(ssId, set);
		}
	}

	public final static void clearCache(String name) {
		for (ConcurrentLRUMap<String, Object> lm : bappCache.values()) {
			lm.clear();
		}
		bappCache.clear();
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public final static Object getCacheValue(String name, String key) {
		ConcurrentLRUMap<String, Object> lm = bappCache.get(name);
		if (lm == null) {
			return null;
		} else {
			Object o = lm.get(key);
			return o;
		}
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public final static Object getCacheValue(String name, String key, ObjectBuilder builder) {

		ConcurrentLRUMap<String, Object> lm = bappCache.get(name);
		if (lm == null) {
			int defaultSzie=10000; // 默认可以存储10000个对象
			if( bappCl4Size.get(name)!=null ){
				defaultSzie=bappCl4Size.get(name);
			}
			
			lm = new ConcurrentLRUMap<String, Object>(defaultSzie); 
			bappCache.put(name, lm);
		}
		Object o = lm.get(key);
		if (o == null) {
			o = builder.getObject();
			if (o != null) {
				lm.put(key, o);
			}
		}
		return o;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public final static Object putCache(String name, String key, Serializable value) {
		ConcurrentLRUMap<String, Object> lm = bappCache.get(name);
		if (lm == null) {
			lm = new ConcurrentLRUMap<String, Object>(10*1000); // 默认可以存储10000个对象
			bappCache.put(name, lm);
		}
		if (value != null) {
			lm.put(key, value);
		}
		return value;
	}

	public final static List<Entry<String, String>> getCacheDescBySsId(String ssId) {
		HashSet<String> s = null;
		if (!StringUtil.isBlank(ssId)&&!"BSYS".equals(ssId)) {
			s = ssCache.get(ssId);
		}
		LinkedList<Entry<String, String>> l = new LinkedList<Entry<String, String>>();
		for (Iterator<String> i = s != null ? s.iterator() : bappCl.keySet().iterator(); i.hasNext();) {
			String name = i.next();
			l.add(new Entry<String, String>(name, bappCl.get(name)));
		}
		return l;
	}

	public static void main(String[] args) {
		CacheUtil.registerCache("aaa", "bbb");
		for (int i = 0; i <1000*  1000; i++) {
			CacheUtil.putCache("aaa", "A" + i, "aaaaaaaaaaaaaaabbbbbbbbbbb" + i);
		}

		System.out.println(CacheUtil.getCacheValue("aaa", "A9"));

	}
}
