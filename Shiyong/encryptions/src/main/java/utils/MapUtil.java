package srp.bapp.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;


public class MapUtil {
	
	private static final Logger logger = Logger.getLogger(MapUtil.class);
	
	/**
	 * 将形如 a=c:/;b=testHello 的字符串转换成map
	 * 
	 * @param String
	 * @return Map map
	 * @throws Exception
	 */
	public static Map<String, Object> arrayToMap(String keyValueStr) {
		Map<String, Object> map = new HashMap<String, Object>();
		String[] paraArr = keyValueStr.split(";");
		for (int i = 0; i < paraArr.length; i++) {
			String[] onePara = paraArr[i].split("=");
			map.put(onePara[0], onePara[1]);
		}
		return map;
	}

	/**
	 * 将map转换成Javabean
	 * 
	 * @param javabean
	 *            javaBean
	 * @param data
	 *            map数据
	 */
	public static Object toJavaBean(Object javabean, Map<String, String> data) {
		Method[] methods = javabean.getClass().getDeclaredMethods();
		for (Method method : methods) {
			try {
				String field = method.getName();
				if (field.startsWith("set")) {
					field = field.substring(3);
					String f1=field.toLowerCase().charAt(0) + field.substring(1);
					String f2 = field.toUpperCase();
					method.invoke(javabean, new Object[] { data.get(f1)==null? data.get(f2): data.get(f1) });
				}
			} catch (Exception e) {
			}
		}

		return javabean;
	}

	/**
	 * 非常好的简化静态map生成的方法 <br>
	 * private static final Map<Short, String> ALIGN = mapAnything(ALIGN_LEFT,
	 * "left", ALIGN_CENTER, "center", ALIGN_RIGHT, "right", ALIGN_FILL, "left",
	 * ALIGN_JUSTIFY, "left", ALIGN_CENTER_SELECTION, "center");
	 * 
	 * @param mapping
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public static <K, V> Map<K, V> mapAnything(Object... mapping) {
		Map<K, V> map = new HashMap<K, V>();
		for (int i = 0; i < mapping.length; i += 2) {
			map.put((K) mapping[i], (V) mapping[i + 1]);
		}
		return map;
	}
	
	public static  Map<String, String> mapAnyString(String... mapping) {
		Map<String, String> map = new HashMap<String,String>();
		for (int i = 0; i < mapping.length; i += 2) {
			map.put(mapping[i], mapping[i + 1]);
		}
		return map;
	}
	
	public static  Map<Object, Object> mapObject(Object... mapping) {
		Map<Object, Object> map = new HashMap<Object,Object>();
		for (int i = 0; i < mapping.length; i += 2) {
			map.put(mapping[i], mapping[i + 1]);
		}
		return map;
	}

	/**
	 * 非常好的简化静态map生成的方法 <br>
	 * private static final Map<Short, String> ALIGN = mapAnything(new
	 * HashMap(),<br>
	 * ALIGN_LEFT, "left", ALIGN_CENTER, "center", ALIGN_RIGHT, "right",
	 * ALIGN_FILL, "left", ALIGN_JUSTIFY, "left", ALIGN_CENTER_SELECTION,
	 * "center");
	 * 
	 * @param mapping
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public static <K, V> Map<K, V> mapAnything(Map<K, V> map, Object... mapping) {
		for (int i = 0; i < mapping.length; i += 2) {
			map.put((K) mapping[i], (V) mapping[i + 1]);
		}
		return map;
	}
	
	/**
	 * 将一个 JavaBean 对象转化为一个  Map 
     * @param bean 要转化的JavaBean 对象 
     * @return 转化出来的  Map 对象 
     * @throws IntrospectionException 如果分析类属性失败 
     * @throws IllegalAccessException 如果实例化 JavaBean 失败 
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
	 * */
	public static Map<String, Object> javaBeanToMap(Object bean) {
        Class<? extends Object> type = bean.getClass();
        Map<String, Object> returnMap = new HashMap<String, Object>();
        try{
	        BeanInfo beanInfo = Introspector.getBeanInfo(type);
	  
	        PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors();
	        for (int i = 0; i< propertyDescriptors.length; i++) {
	            PropertyDescriptor descriptor = propertyDescriptors[i];
	            String propertyName = descriptor.getName();
	            if (!propertyName.equals("class")) {
	                Method readMethod = descriptor.getReadMethod();
	                Object result = readMethod.invoke(bean, new Object[0]);
	                if (result != null) {
	                    returnMap.put(propertyName, result);
	                } else {
	                    returnMap.put(propertyName, "");
	                }
	            }
	        }
        } catch (IntrospectionException e) {
        	logger.warn("分析类属性失败！", e);
		} catch (IllegalArgumentException e) {
			logger.warn("实例化 JavaBean失败！", e);
		} catch (IllegalAccessException e) {
			logger.warn("", e);
		} catch (InvocationTargetException e) {
			logger.warn("调用属性的 setter方法失败", e);
		}
        return returnMap;
    }
	
	/**
	 *比较2个Map<String, String>
	 * @param m1
	 * @param m2
	 * @return
	 */
	public static boolean MapCompare(Map<Integer, String> m1,
			Map<Integer, String> m2) {
		try{
			if(m1.size()!=m2.size()){
				return false;
			}else{
				for (Entry<Integer, String> entry : m1.entrySet()) {
					if (m2.containsKey(entry.getKey())) {
						String v1 = entry.getValue();
						String v2 = m2.get(entry.getKey());
						if (!v1.equals(v2)) {
							return false;
						} else {
							continue;
						}
					}else{
						return false;
					}
				}
			}
		}catch(Exception e){
			logger.error("map比较失败！",e);
		}
		return true;
	}
}
