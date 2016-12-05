package srp.bapp.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import srp.bapp.tools.Entry;
import srp.bapp.tools.zip.ZipInputStream;

public class ZipUtil {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(ZipUtil.class);
	static final int BUFFER = 8192;

	private ZipUtil() {

	}

	public static List<String> unzip(InputStream zipFileIn, String dir) {
		List<String> r = null;
		ZipInputStream zip = new ZipInputStream(zipFileIn);
		new File(dir).mkdir();
		try {
			r = new LinkedList<String>();
			while (true) {

				srp.bapp.tools.zip.ZipEntry nextEntry = zip.getNextEntry();

				if (nextEntry == null) {
					break;
				}
				String name = nextEntry.getName();
				String fullName = dir + File.separator + name;
				r.add(fullName);
				FileOutputStream fileOut = new FileOutputStream(new File(fullName));
				IOUtil.copy(zip, fileOut);
				fileOut.flush();
				fileOut.close();
			}
			return r;
		} catch (IOException e) {
			logger.error("unzip(String, String) - , dir=" + dir, e); //$NON-NLS-1$ //$NON-NLS-2$
		} finally {
			try {
				zipFileIn.close();
			} catch (IOException e) {
			}

			try {
				zip.close();
			} catch (IOException e) {
			}
		}
		return r;

	}

	public static List<String> unzip(String zipfile, String dir) {
		FileInputStream zipFileIn = null;

		try {
			zipFileIn = new FileInputStream(zipfile);
		} catch (FileNotFoundException e) {
			logger.error("unzip(String, String) - zipfile=" + zipfile, e); //$NON-NLS-1$
		}
		return unzip(zipFileIn, dir);
	}

	/**
	 * 
	 * @param zipFileName
	 *            压缩产生的zip包文件名--带路径,如果为null或空则默认按文件名生产压缩文件名
	 * @param directory
	 *            被文件或目录的绝对路径
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void zip(String zipFileName, String directory) throws FileNotFoundException, IOException {
		zip(zipFileName, "", directory);
	}

	/***
	 * 压缩
	 * 
	 * @param zipFileName
	 *            压缩产生的zip包文件名--带路径,如果为null或空则默认按文件名生产压缩文件名
	 * @param relativePath
	 *            相对路径，默认为空
	 * @param directory
	 *            文件或目录的绝对路径
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void zip(String zipFileName, String relativePath, String directory) throws FileNotFoundException, IOException {
		if (relativePath == null) {
			relativePath = "";
		}

		String fileName = zipFileName;
		if (fileName == null || fileName.trim().equals("")) {
			File temp = new File(directory);
			if (temp.isDirectory()) {
				fileName = directory + ".zip";
			} else {
				if (directory.indexOf(".") > 0) {
					fileName = directory.substring(0, directory.lastIndexOf(".") + 1) + "zip";
				} else {
					fileName = directory + ".zip";
				}
			}
		}
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(fileName));
		zos.setEncoding("GBK");
		try {
			zip(zos, relativePath, directory);
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (null != zos) {
				zos.close();
			}
		}
	}

	/**
	 * 压缩
	 * 
	 * @param zos
	 *            压缩输出流
	 * @param relativePath
	 *            相对路径
	 * @param absolutPath
	 *            文件或文件夹绝对路径
	 * @throws IOException
	 */
	private static void zip(ZipOutputStream zos, String relativePath, String absolutPath) throws IOException {
		File file = new File(absolutPath);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				File tempFile = files[i];
				if (tempFile.isDirectory()) {
					String newRelativePath = relativePath + tempFile.getName() + File.separator;
					// createZipNode(zos, newRelativePath);
					zip(zos, newRelativePath, tempFile.getPath());
				} else {
					zipFile(zos, tempFile, relativePath);
				}
			}
		} else {
			zipFile(zos, file, relativePath);
		}
	}

	/**
	 * 压缩文件目录
	 * 
	 * @param zos
	 *            压缩输出流
	 * @param file
	 *            文件对象
	 * @param relativePath
	 *            相对路径
	 * @throws IOException
	 */
	private static void zipFile(ZipOutputStream zos, File file, String relativePath) throws IOException {
		ZipEntry entry = new ZipEntry(relativePath + file.getName());
		zos.putNextEntry(entry);
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			int BUFFERSIZE = 2 << 10;
			int length = 0;
			byte[] buffer = new byte[BUFFERSIZE];
			while ((length = is.read(buffer, 0, BUFFERSIZE)) >= 0) {
				zos.write(buffer, 0, length);
			}
			zos.flush();
			zos.closeEntry();
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (null != is) {
				is.close();
			}
		}
	}

	/**
	 * * 创建目录
	 * 
	 * @param zos
	 *            zip输出流
	 * @param relativePath
	 *            相对路径
	 * @throws IOException
	 */
	private static void createZipNode(ZipOutputStream zos, String relativePath) throws IOException {
		ZipEntry zipEntry = new ZipEntry(relativePath);
		zos.putNextEntry(zipEntry);
		zos.closeEntry();
	}

	public static void zip(List<Entry<String, ByteArrayOutputStream>> datas, OutputStream os) {
		ZipOutputStream zos = null;
		InputStream is = null;
		try {
			zos = new ZipOutputStream(os);
			zos.setEncoding("GBK");
			for (Entry<String, ByteArrayOutputStream> entry : datas) {
				ZipEntry zipEntry = new ZipEntry(entry.getKey());
				zos.putNextEntry(zipEntry);
				is = new BufferedInputStream(new ByteArrayInputStream(entry.getValue().toByteArray()));
				IOUtils.copy(is, zos);
				zos.flush();
				zos.closeEntry();
			}
		} catch (Exception e) {
			logger.error("zip(List<Entry<String,ByteArrayOutputStream>>, OutputStream) - exception ignored", e); //$NON-NLS-1$
		} finally {
			try {
				zos.close();
				zos = null;
			} catch (Exception e) {
				logger.error("ZipOutputStream:", e);
			}
			try {
				if (null != is) {
					is.close();
					is = null;
				}
			} catch (Exception e) {
				logger.error("InputStream:", e);
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		zip(null, "D:/abc");
	}
}
