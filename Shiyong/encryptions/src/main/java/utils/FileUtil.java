package srp.bapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import srp.bapp.pub.ext.spring.BappContextLoaderListener;

public abstract class FileUtil extends FileUtils {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(FileUtil.class);

	/**
	 * 临时文件存放目录路径 System.getProperty("user.home") + File.separator + "bapp" +
	 * File.separator + "temp" + File.separator;
	 */
	public static final String TEMP_PATH_NAME;
	static {
		String jvmKey =BappContextLoaderListener.getAppStorePath()==null?String.valueOf(System.currentTimeMillis()):String.valueOf(BappContextLoaderListener.getAppStorePath().hashCode()); //如果启动多个jvm，防止互相影响
		TEMP_PATH_NAME = System.getProperty("user.home") + File.separator + "bapp" + File.separator +jvmKey+ "_temp" + File.separator;
		try {
			FileUtils.forceMkdir(new File(TEMP_PATH_NAME));
		} catch (IOException e) {
			logger.error("创建TEMP_PATH_NAME:[" + TEMP_PATH_NAME + "]失败.", e);
		}
		File p = new File(TEMP_PATH_NAME);
		try {
			FileUtil.cleanDirectory(p);
		} catch (IOException e) {
			logger.info("清理TEMP_PATH_NAME:[" + TEMP_PATH_NAME + "]失败.", e);
		}
	}

	private static MessageDigest MD5 = null;

	static {
		try {
			MD5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			logger.error("初始化MD5环境失败.", e);
		}
	}

	public final static String getBappTempDir() {
		return TEMP_PATH_NAME;
	}

	public final static String getBappTempFileName(String ssId, String fileKey, boolean withPath) {
		String f = "tf_" + ssId + "_" + fileKey + ".temp";
		return withPath ? TEMP_PATH_NAME + f : f;
	}

	public static void copy(String source, String dest) {
		try {
			FileUtils.copyFile(new File(source), new File(dest));
		} catch (IOException e) {
			logger.error("copy(String, String) - source=" + source + ", dest=" + dest, e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static void copyToDir(String source, String destDir) {
		try {
			FileUtils.copyFileToDirectory(new File(source), new File(destDir));
		} catch (IOException e) {
			logger.error("copy(String, String) - source=" + source + ", destDir=" + destDir, e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static void copyDir(String sourceDir, String destDir) {
		try {
			FileUtils.copyDirectory(new File(sourceDir), new File(destDir));
		} catch (IOException e) {
			logger.error("copy(String, String) - source=" + sourceDir + ", destDir=" + destDir, e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * 
	 * @param dir
	 * @return 路径全名的list
	 */
	public static List<String> listFiles(String dir) {

		List<String> list = new LinkedList<String>();
		File[] files = new File(dir).listFiles();
		try {
			if (files == null) {
				FileUtils.forceMkdir(new File(dir));
				files = new File(dir).listFiles();
			}
		} catch (IOException e) {
		}
		if (files != null) {
			for (File f : files) {
				list.add(f.getPath());
			}
		}
		return list;
	}

	public static void deleteXMLFiles(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				File file = new File(dir, children[i]);
				if (file.toString().lastIndexOf(".xml") > 0) {
					file.delete();
				}
			}
		}
	}

	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	/**
	 * 使用当前操作系统目录分隔符处理Path字符串
	 * 
	 * @param path
	 * @return
	 */
	public static String formatPathString(String path) {
		if (path == null) {
			return null;
		}
		return path.replace('/', File.separatorChar).replace('\\', File.separatorChar);
	}

	
	public static String getFileMD5(String file) {
		return getMD5(new File(file)); 
	}
	
	/**
	 * 对一个文件获取md5值
	 * 
	 * @return md5串
	 */
	public static String getMD5(File file) {
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int length;
			while ((length = fileInputStream.read(buffer)) != -1) {
				MD5.update(buffer, 0, length);
			}
			return new String(Hex.encodeHex(MD5.digest()));
		} catch (FileNotFoundException e) {
			logger.error("getMD5(File)", e);
			return null;
		} catch (IOException e) {
			logger.error("getMD5(File)", e);
			return null;
		} finally {
			try {
				if (fileInputStream != null){
					fileInputStream.close();
				}
			} catch (IOException e) {
				logger.error("getMD5(File)", e);
			}
		}
	}

}