package srp.bapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.ParseException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import httl.Engine;
import httl.Template;
import srp.bapp.pub.runtime.BappConfig;
import srp.bapp.tools.EasyMap;

public abstract class TextTemplateUtil {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(TextTemplateUtil.class);
	private static ConcurrentHashMap<String, Template> templateCache = new ConcurrentHashMap<String, Template>(); // 使用成功解析的模板，替换原模板
	private static Engine engine;
	private static Properties config = new Properties();

	static {
		config.put("compiler", "httl.spi.compilers.JavassistCompiler");
		config.put("json.codec", "srp.bapp.pub.ext.spring.BappJsonCodec");
		config.put("cache.capacity", "1000");
		config.put("output.encoding", "UTF-8");
		config.put("input.encoding", "UTF-8");
		config.put("dump.once", "true");
		config.put("dump.override", "true");


		config.put("code.directory", FileUtil.TEMP_PATH_NAME + "httl" +  File.separator + "java");
		config.put("compile.directory", FileUtil.TEMP_PATH_NAME + "httl" +  File.separator + "classes");
		try {
			FileUtil.forceMkdir(new File(FileUtil.TEMP_PATH_NAME + "httl" +  File.separator + "classes"
					+ "/httl/spi/translators/templates".replace('/', File.separatorChar)));
		} catch (IOException e) {
			logger.error("不能正常创建HTTL编译临时目录", e);
		}
		config.put("dump.directory", FileUtil.TEMP_PATH_NAME + "httl" +  File.separator + "dumps");
		try {
			FileUtil.forceMkdir(new File(FileUtil.TEMP_PATH_NAME + "httl" +  File.separator + "dumps".replace('/', File.separatorChar)));
		} catch (IOException e) {
			config.put("dump.directory", "");
			logger.error("不能正常创建HTTL-DUMP目录", e);
		}
		try {
			config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("httl.bapp.properties"));
		} catch (Exception e) {
			logger.error("不能正常装载  httl.bapp.properties", e);
		}
		engine = Engine.getEngine(config);
	}

	/**
	 * 根据classpath模板获取其内部包含的变量信息
	 * 
	 * @param vmPath
	 * @return
	 */
	public static Map<String, Class<?>> getClassPathVars(String vmPath) {
		Template t = null;
		try {
			t = getTemplate(vmPath);
			return t.getVariables();
		} catch (ParseException e) {
			logger.error("不能正确解析模板:" + vmPath, e);
		} catch (Exception e) {
			logger.error("IO异常:" + vmPath, e);
		}
		return null;
	}

	/**
	 * 根据string模板获取其内部包含的变量信息
	 * 
	 * @param vmPath
	 * @return
	 */
	public static Map<String, Class<?>> getStringVars(String source) {
		Template t = null;
		try {
			t = engine.parseTemplate(source, "UTF-8");
			return t.getVariables();
		} catch (ParseException e) {
			logger.error("模板语法异常" + source, e);
		} catch (Exception e) {
			logger.error("IO异常:" + source, e);
		}
		return null;
	}

	/**
	 * 根据vm模板的classpath进行freemarker模板合并返回
	 * 
	 * @param vmPath
	 *            freemarker模板的类路径
	 * @return 模板处理后的数据字符串
	 */
	public static String mergingByClassPath(String vmPath) {
		return mergingByClassPath(vmPath, EasyMap.EMPTY_MAP);
	}

	/**
	 * 根据vm模板的classpath进行freemarker模板合并返回
	 * 
	 * @param vmPath
	 *            freemarker模板的类路径
	 * @param model
	 *            数据模型
	 * @return 模板处理后的数据字符串
	 */
	public static String mergingByClassPath(String vmPath, Object model) {
		if (vmPath.charAt(0) == '/') {//去除/前导符：即    /vm/dis/template/column.js  ====>  vm/dis/template/column.js
			vmPath = vmPath.substring(1);
		}

		// 新增不使用httl引擎，直接返回字符串的能力
		if (model == null || model == EasyMap.EMPTY_MAP || (model instanceof Map && ((Map<?, ?>) (model)).size() == 0)) {
			return fromClassFile(vmPath);
		}

		Template t = null;
		try {
			if (!BappConfig.isDevEnv() && templateCache.containsKey(vmPath)) {
				t = templateCache.get(vmPath);
			} else {
				t = getTemplate(vmPath);
			}
		} catch (ParseException e) {
			logger.error("不能正确解析模板:" + vmPath, e);
			return "";
		} catch (Exception e) {
			logger.error("模板获取异常:" + vmPath, e);
			return "";
		}
		java.io.CharArrayWriter cw = new java.io.CharArrayWriter();
		java.io.PrintWriter pw = new java.io.PrintWriter(cw, true);
		try {
			t.render(model, pw);
		} catch (ParseException e) {
			logger.error("模板语法异常:" + vmPath, e);
		} catch (NullPointerException e) {
			// 有可能是浦发的特殊情况
			logger.error(vmPath + "模板渲染失败,尝试使用字符串解析。", e);
			int retry = 0;
			String s = null;
			try {
				s = InputStreamUtil.inputStreamTOString(Thread.currentThread().getContextClassLoader().getResourceAsStream(vmPath), "UTF-8");
			} catch (Exception ex) {
				logger.error("模板获取异常:" + vmPath, e);
				return "";
			}
			if (StringUtil.isBlank(s)) {
				logger.error("模板内容为空:" + vmPath);
				return "";
			}
			while (++retry < 6) {
				String r = mergingByString4null(s, model, vmPath);
				if (StringUtil.isBlank(r)) {
					s += " "; // 加个空格防止缓存
				} else {
					return r;
				}
			}
			logger.error("重试[" + retry + "]次后模板渲染失败:" + vmPath, e);
			return "";

		} catch (Exception e) {
			logger.error("模板渲染异常:" + vmPath, e);
		}
		pw.close();
		return cw.toString();
	}

	private static final ConcurrentHashMap<String /* path */, String /* content */> classFileString = new ConcurrentHashMap<String, String>();

	private static String fromClassFile(String vmPath) {
		if (BappConfig.isDevEnv()) {
			classFileString.clear();
		}
		if (classFileString.containsKey(vmPath)) {
			return classFileString.get(vmPath);
		} else {
			synchronized (classFileString) {
				String s = null;
				InputStream in = null;
				try {
					URL theUrl = Thread.currentThread().getContextClassLoader().getResource(vmPath);
					if (null == theUrl) {
						theUrl = TextTemplateUtil.class.getResource(vmPath);
					}
					File theFile = new File(theUrl.getFile());
					if (StringUtil.isNotBlank(theUrl.getFile()) && theFile.exists()) {
						in = new FileInputStream(theFile);
					} else {
						String p;
						if (vmPath.startsWith("/")) {
							p = vmPath.substring(1);
						} else {
							p = vmPath;
						}
						in = Thread.currentThread().getContextClassLoader().getResourceAsStream(p);
					}
					s = InputStreamUtil.inputStreamTOString(in, "UTF-8");

				} catch (Exception e) {
					logger.error("模板获取异常:" + vmPath, e);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
						}
					}
				}
				if (s == null) {
					logger.error("模板获取内容为空:" + vmPath);
					s = "";
				}
				classFileString.put(vmPath, s);
				return s;
			}
		}
	}

	private static Template getTemplate(String vmPath) throws IOException, ParseException {
		Template t;
		if (BappConfig.isDevEnv() && !config.containsKey("reloadable")) {
			config.put("cache.capacity", "0");
			config.put("reloadable", "true");
			engine = Engine.getEngine("dev", config);
		}
		t = engine.getTemplate(vmPath, "UTF-8");
		return t;
	}

	public static Template getTemplateByString(String source) {
		try {
			return engine.parseTemplate(source, "UTF-8");
		} catch (ParseException e) {
			logger.error("模板语法异常", e);
		}
		return null;
	}

	public static String render(Template t, Object model) {
		java.io.CharArrayWriter cw = new java.io.CharArrayWriter();
		java.io.PrintWriter pw = new java.io.PrintWriter(cw, true);
		try {
			t.render(model, pw);
		} catch (ParseException e) {
			logger.error("模板语法异常", e);
		} catch (IOException e) {
			logger.error("IO异常", e);
		}
		pw.close();
		return cw.toString();
	}

	public static String mergingByString4null(String source, Object model, String vmPath) {
		Template t = null;
		try {
			t = engine.parseTemplate(source, "UTF-8");
		} catch (ParseException e) {
			logger.error("模板语法异常", e);
		}
		java.io.CharArrayWriter cw = new java.io.CharArrayWriter();
		java.io.PrintWriter pw = new java.io.PrintWriter(cw, true);
		try {
			t.render(model, pw);
			templateCache.put(vmPath, t);
		} catch (Exception e) {
			logger.error("模板处理异常", e);
		}
		pw.close();
		return cw.toString();
	}

	public static String mergingByString(String source, Object model) {
		Template t = null;
		try {
			t = engine.parseTemplate(source, "UTF-8");
		} catch (ParseException e) {
			logger.error("模板语法异常", e);
		}
		java.io.CharArrayWriter cw = new java.io.CharArrayWriter();
		java.io.PrintWriter pw = new java.io.PrintWriter(cw, true);
		try {
			t.render(model, pw);
		} catch (ParseException e) {
			logger.error("模板语法异常", e);
		} catch (IOException e) {
			logger.error("IO异常", e);
		}
		pw.close();
		return cw.toString();
	}

	public static void mergingByClassPath(String vmPath, Object model, String fileName, String encoding) throws Exception {
		Template t = null;
		try {
			t = getTemplate(vmPath);
		} catch (ParseException e) {
			logger.error("模板语法异常:" + vmPath, e);
			throw e;
		} catch (Exception e) {
			logger.error("IO异常:" + vmPath, e);
			throw e;
		}
		outputToFile(model, fileName, t, encoding);
	}

	public static void mergingByString(String source, Object model, String fileName) throws Exception {
		mergingByString(source, model, fileName, "UTF-8");
	}

	public static void mergingByString(String source, Object model, String fileName, String encoding) throws Exception {
		Template t = null;
		try {
			t = engine.parseTemplate(source, encoding);
		} catch (ParseException e) {
			logger.error("模板语法异常", e);
			throw e;
		}
		outputToFile(model, fileName, t, encoding);
	}

	private static void outputToFile(Object model, String fileName, Template t, String encoding)
			throws ParseException, IOException, FileNotFoundException {
		OutputStreamWriter os = null;
		try {
			os = new OutputStreamWriter(new FileOutputStream(fileName), encoding);
			try {
				t.render(model, os);
			} catch (ParseException e) {
				logger.error("模板语法异常", e);
				throw e;
			} catch (IOException e) {
				logger.error("IO异常", e);
				throw e;
			}
		} catch (FileNotFoundException e) {
			logger.error("IO异常 toFile:[" + fileName + "]", e);
			throw e;
		} finally {
			if (null != os) {
				os.close();
			}
		}
	}

	public static void main(String[] args) {
		try {
			mergingByString("hello ${user}!", new EasyMap<String, String>().putValue("user", "oofrank"), "C:\\aaa\\test.txt", "GBK");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			logger.error("", e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("", e);
		}
	}
}
