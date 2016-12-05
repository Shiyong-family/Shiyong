package srp.bapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class FtpMtsUtil {
	/** 程序运行日志 */
	private static Logger logger = Logger.getLogger(FtpMtsUtil.class.getName());

	private String host;
	private int port;
	private String username;
	private String password;
	private String connectMode;
	private FTPClient ftpClient = new FTPClient();
	private byte[] lock = new byte[0];
	/** 文件缓冲区的长度 */
	private int buffersize = 1024;

	private char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" + "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();

	public static final String tokenFileName = "Token.lock";

	/**
	 * 
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @param connectMode
	 *            0 1
	 */
	public FtpMtsUtil(String host, int port, String username, String password, String connectMode) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.connectMode = connectMode;
	}

	/**
	 * @param ftpClient
	 * @param serverIp
	 * @param userName
	 * @param passWord
	 * @throws IOException
	 * @throws SocketException
	 */
	public boolean connect() throws Exception {
		boolean result = false;
		/** 设置FTP链接地址 */
		ftpClient.connect(host, port);
		if ("0".equals(connectMode)) {
			ftpClient.enterLocalActiveMode();
		} else {
			ftpClient.enterLocalPassiveMode();
		}
		/** 设置FTP链接用户名和密码 */
		result = ftpClient.login(username, password);
		return result;
	}

	/**
	 * Disconnect with server
	 * 
	 * @throws Exception
	 */
	public void disconnect() throws Exception {
		if (null != ftpClient) {
			if (ftpClient.isConnected()) {
				ftpClient.disconnect();
			} else {
				logger.info("FTP-Server is closed already");
			}

		}

	}

	/**
	 * @author lkl
	 * @return true:表示有令牌Token.lock, false没有
	 * @throws Exception
	 */
	public boolean isHaveTokenlock(String directory) throws Exception {
		Map<String, String> fileMap = getFtpFile(directory);
		if (fileMap.containsKey(tokenFileName)) {
			return isExpire(directory + "/" + tokenFileName);
		}
		return false;
	}

	/**
	 * 上传令牌Token.lock
	 * 
	 * @author lkl
	 * @throws Exception
	 */
	public void uploadTokenlock(String targetPath, String tokenpath) throws Exception {
		List<String> listFile = new ArrayList<String>();
		listFile.add(tokenpath);
		uploadFtpFile(listFile, targetPath);
	}

	/**
	 * 上传文件到FTP服务器
	 * 
	 * @author lkl
	 * @param listFile
	 *            上传文件本地路径:
	 *            D:/ACCData/cHistorySend/20121023/ACCTT00000000001212101001
	 *            /ACCCA00000000001212101001.XML
	 * @param uploadDir
	 *            ftp上传目录: /home/ACCData/Send/ACCTT00000000001212101001
	 * @throws Exception
	 */
	public void uploadFtpFile(List<String> listFile, String uploadDir) throws Exception {
		boolean mkdFlag = false;
		boolean changeFlag = false;
		boolean storeFlag = false;
		String fileName = "";

		FileInputStream fis = null;

		try {
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				throw new Exception("未连接到FTP，用户名或密码错误!");
			}
			/** 设置编码 */
			ftpClient.setControlEncoding("GBK");
			/** 设置文件类型（二进制） */
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

			synchronized (lock) {
				/** 切换到上传目录 */
				changeFlag = ftpClient.changeWorkingDirectory(uploadDir);
				if (changeFlag) {
					logger.info("切换目录成功，当前路径：" + ftpClient.printWorkingDirectory());
				} else {
					logger.warn("切换目录失败，创建目录，当前路径：" + ftpClient.printWorkingDirectory());

					/** 创建FTP服务器目录 */
					mkdFlag = ftpClient.makeDirectory(uploadDir);
					if (mkdFlag) {
						logger.info("FTP目录创建成功！");
						/** 切换到上传目录 */
						ftpClient.changeWorkingDirectory(uploadDir);
					} else {
						throw new Exception("FTP目录创建失败！" + "IP: " + host + "!目录：" + ftpClient.printWorkingDirectory());
					}
				}
				for (String filePath : listFile) {
					File srcFile = new File(filePath);
					if (!srcFile.exists()) {
						logger.error("文件上传失败！在本地服务器上不存在文件：" + filePath);
						throw new Exception("文件上传失败！在本地服务器上不存在文件：" + filePath);
					}
					fis = new FileInputStream(srcFile);
					fileName = srcFile.getName();
					/** 上传文件 */
					try {
						storeFlag = ftpClient.storeFile(fileName, fis);// 文件名
					} finally {
						try {
							fis.close();
						} catch (Exception e) {
							// do nothing
						}
					}
					if (storeFlag) {
						logger.info("    文件上传成功: ip: " + host + " 路径： 【" + uploadDir + "/" + fileName + "】");
					} else {
						logger.error("    文件上传失败ip: " + host + " :  路径：【" + uploadDir + "/" + fileName + "】");
						throw new Exception("ip: " + host + "  文件上传失败！路径：【" + uploadDir + "/" + fileName + "】");
					}
				}
			}
		} catch (Exception e) {
			logger.error("文件上传发生异常：", e);
			throw new Exception("文件上传发生异常：" + e);
		}

	}

	/**
	 * 删除FTP服务器文件。
	 * 
	 * @author lkl
	 * @param listFile
	 *            删除ftp全路径:
	 *            D:/ACCData/cHistorySend/20121023/ACCTT00000000001212101001
	 *            /ACCCA00000000001212101001.XML
	 * @throws Exception
	 */
	public void deleteFtpFile(List<String> listFile) throws Exception {
		boolean storeFlag = false;
		try {
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				throw new Exception("未连接到FTP，用户名或密码错误!");

			}
			synchronized (lock) {
				for (String filePath : listFile) {
					/** 删除文件 */
					storeFlag = ftpClient.deleteFile(filePath);
					if (storeFlag) {
						logger.debug("ftp文件删除成功！filePath:" + filePath);
					} else {
						throw new Exception("ftp文件删除失败！filePath:" + filePath);
					}
				}

			}
		} catch (Exception e) {
			logger.error("ftp文件删除发生异常：", e);
			throw new Exception("ftp文件删除发生异常.", e);
		}
	}

	/**
	 * 下载FTP服务器文件: 把ftp上某个目录下所有xml文件复制到本地服务器目录
	 * 
	 * @author lkl
	 * @param fileFtpDir
	 *            下载FTP服务器文件目录: D:/ACCData/Feedback/ACCTT00000000001212101001ERR
	 * @param localDir
	 *            本地目录：从ftp下载下来的文件存放目录：
	 *            D:/ACCData/Temp/ACCTT00000000001212101001ERR
	 * @return 返回所下载的文件全路径
	 * @throws Exception
	 */
	public List<String> downloadFtpFile(String fileFtpDir, String localDir) throws Exception {
		InputStream inputStream = null;
		List<String> listLocalFile = new ArrayList<String>();
		try {
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				throw new Exception("未连接到FTP，用户名或密码错误!");
			}
			synchronized (lock) {
				FTPFile[] ftpFile = ftpClient.listFiles(fileFtpDir);
				if (ftpFile != null && ftpFile.length > 0) {
					int length = ftpFile.length;
					for (int i = 0; i < length; i++) {
						FTPFile file = ftpFile[i];
						String filename = file.getName();
						if (filename.endsWith(".xml") || filename.toUpperCase().endsWith(".XML")) {
							// 从ftp上读取文件: 一次连接ftp只能读取一个文件
							// if(i>0){
							// ftpClient.connect(serverIp);
							// ftpClient.login(userName, passWord);
							// ftpLogin(ftpClient, serverIp, userName,
							// passWord);
							// }
							inputStream = ftpClient.retrieveFileStream(fileFtpDir + "/" + filename);
							String localpath = localDir + "/" + filename;
							if (writeFile(localpath, inputStream)) {
								logger.info("从ftp下载文件到本地服务器成功，本地服务器路径为：【" + localpath + "】ip:" + host);
								listLocalFile.add(localpath);
							} else {
								logger.info("ftp文件写入到本地服务器失败：ip:" + host + "ftp文件路径：" + fileFtpDir + "/" + filename);
							}
							inputStream.close();
							ftpClient.completePendingCommand();
						}

					}
				}

			}
		} catch (Exception e) {
			logger.error("下载FTP服务器文件发生异常：", e);
			throw new Exception("下载FTP服务器文件发生异常：" + e);
		}
		return listLocalFile;

	}

	/**
	 * 下载指定后缀名的文件
	 * 
	 * @param fileFtpDir
	 * @param localDir
	 * @return
	 * @throws Exception
	 */
	public List<String> downloadFtpFileSuffix(String fileFtpDir, String localDir, String suffix) throws Exception {
		InputStream inputStream = null;
		List<String> listLocalFile = new ArrayList<String>();
		try {
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				throw new Exception("未连接到FTP，用户名或密码错误!");
			}
			synchronized (lock) {
				FTPFile[] ftpFile = ftpClient.listFiles(fileFtpDir);
				if (ftpFile != null && ftpFile.length > 0) {
					int length = ftpFile.length;
					for (int i = 0; i < length; i++) {
						FTPFile file = ftpFile[i];
						String filename = file.getName();
						if (filename.toUpperCase().endsWith(suffix.toUpperCase())) {
							inputStream = ftpClient.retrieveFileStream(fileFtpDir + "/" + filename);
							String localpath = localDir + "/" + new String(filename.getBytes("iso-8859-1"), "GBK");
							if (writeFile(localpath, inputStream)) {
								logger.info("从ftp下载文件到本地服务器成功，本地服务器路径为：【" + localpath + "】ip:" + host);
								listLocalFile.add(localpath);
							} else {
								logger.info("ftp文件写入到本地服务器失败：ip:" + host + "ftp文件路径：" + fileFtpDir + "/" + filename);
							}
							inputStream.close();
							ftpClient.completePendingCommand();
						}

					}
				}

			}
		} catch (Exception e) {
			logger.error("下载FTP服务器文件发生异常：", e);
			throw new Exception("下载FTP服务器文件发生异常：" + e);
		}
		return listLocalFile;

	}

	/**
	 * 取ftp上目录下所有目录和令牌文件
	 * 
	 * @author lkl
	 * @param fileFtpDir
	 *            下载FTP服务器文件目录: D:/ACCData/Feedback/ACCTT00000000001212101001ERR
	 * @return 返回ftp上目录下所有文件
	 * @throws Exception
	 */
	public Map<String, String> getFtpFile(String fileFtpDir) throws Exception {
		Map<String, String> fileMap = new LinkedHashMap<String, String>();
		try {
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				throw new Exception("未连接到FTP，用户名或密码错误!");
			}
			synchronized (lock) {
				FTPFile[] ftpFile = ftpClient.listFiles(fileFtpDir);
				if (ftpFile != null && ftpFile.length > 0) {
					int length = ftpFile.length;
					for (int i = 0; i < length; i++) {
						FTPFile file = ftpFile[i];
						String filename = file.getName();
						if (file.isDirectory() || filename.equals(tokenFileName)) {
							fileMap.put(filename, filename);
						}

					}
				}

			}
		} catch (Exception e) {
			logger.error("", e);
			logger.error("取ftp上目录下所有文件发生异常：" + e);
			throw new Exception("取ftp上目录下所有文件发生异常：" + e);
		}
		return fileMap;

	}

	public Set<String> getFtpFileSet(String fileFtpDir) throws Exception {
		Set<String> fileSet = new HashSet<String>();
		try {
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				throw new Exception("未连接到FTP，用户名或密码错误!");
			}
			synchronized (lock) {
				FTPFile[] ftpFile = ftpClient.listFiles(fileFtpDir);
				if (ftpFile != null && ftpFile.length > 0) {
					int length = ftpFile.length;
					for (int i = 0; i < length; i++) {
						FTPFile file = ftpFile[i];
						String filename = file.getName();
						// if (file.isDirectory() ||
						// filename.equals(tokenFileName)) {
						fileSet.add(filename);
						// }

					}
				}

			}
		} catch (Exception e) {
			logger.error("", e);
			logger.error("取ftp上目录下所有文件发生异常：" + e);
			throw new Exception("取ftp上目录下所有文件发生异常：" + e);
		}
		return fileSet;

	}

	/**
	 * 写文件
	 * 
	 * @author
	 * @param filename
	 *            ： D:/ACCTT00000000001212101001ERR.XML
	 * @param in
	 * @return true成功
	 */
	public boolean writeFile(String filename, InputStream in) {
		FileOutputStream output;
		try {
			output = new FileOutputStream(filename);
		} catch (Exception e) {
			logger.error("", e);
			logger.error("", e);
			;
			return false;
		}
		byte[] buffer = new byte[buffersize];
		int len;
		try {
			while ((len = in.read(buffer, 0, buffersize)) > 0) {
				output.write(buffer, 0, len);
			}
			return true;
		} catch (Exception e) {
			logger.error("", e);
			return false;
		} finally {
			try {
				if (output != null)
					output.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

	/**
	 * @author
	 * @return 取得一个唯一的文件名
	 */
	public String getRandonFileName() {
		SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmss");
		String returnValue = "";
		returnValue = df.format(new Date());

		return returnValue + getRandomString(5);
	}

	/**
	 * 取得一个随机字符串,包含数字和字符
	 * 
	 * @author
	 * @param length
	 *            字符串长度
	 * @return 随机字符串
	 */
	public String getRandomString(int length) {
		if (length < 1) {
			return null;
		}
		char[] randBuffer = new char[length];
		try {
			java.security.SecureRandom s = java.security.SecureRandom.getInstance("SHA1PRNG");
			for (int i = 0; i < randBuffer.length; i++) {
				randBuffer[i] = numbersAndLetters[s.nextInt(71)];
			}

		} catch (NoSuchAlgorithmException e) {
			logger.error("", e);
		}
		return new String(randBuffer);
	}

	/**
	 * 创建目录
	 * 
	 * @author
	 * @param buildFilePath
	 *            : D:/temp 已存在目录
	 * @param newpath
	 *            : /fp/WEB-INF/classes/com/ist/bmp/platform/controller.class
	 *            可以创建的目录 两组合成一个完整文件路径.
	 */
	public void buildDir(String buildFilePath, String newpath) {
		String[] array = newpath.split("//");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			if (i != (array.length - 1)) {
				sb.append("/").append(array[i]);
				File file = new File(buildFilePath + sb.toString());
				if (!file.exists()) {
					file.mkdir();
				}
			}

		}

	}

	/**
	 * 取本地目录下所有目录和令牌文件
	 * 
	 * @author lkl
	 * @param fileFtpDir
	 * @return 返回本地上目录下所有文件
	 * @throws Exception
	 */
	public List<String> getFile(String fileFtpDir) throws Exception {
		List<String> fileList = new ArrayList<String>();
		try {
			File files = new File(fileFtpDir);
			File[] ftpFile = files.listFiles();
			if (ftpFile != null && ftpFile.length > 0) {
				int length = ftpFile.length;
				for (int i = 0; i < length; i++) {
					File file = ftpFile[i];
					String filename = file.getName();
					if (file.isDirectory() || filename.equals(tokenFileName)) {
						fileList.add(filename);
					}

				}
			}

		} catch (Exception e) {
			logger.error("", e);
			logger.error("取本地上目录下所有文件发生异常：" + e);
			throw new Exception("取本地目录下所有文件发生异常：" + e);
		}
		return fileList;

	}

	/**
	 * 如果服务器上存在令牌则需要下载到本地判断此令牌是否是程序问题导致没有删除
	 * 
	 * @param args
	 */
	private boolean isExpire(String ftpFilePath) {
		String localPath = System.getProperty("user.home") + "/Token.lock";
		try {
			downloadFile(ftpFilePath, localPath);
			return hasTokenFile(localPath);
		} catch (Exception e) {
			logger.error("", e);
		}
		return true;

	}

	/**
	 * 单独从FTP上下载一个文件
	 * 
	 * @param fileFtpPath
	 * @param localpath
	 * @return
	 * @throws Exception
	 */
	public String downloadFile(String fileFtpPath, String localpath) throws Exception {
		InputStream inputStream = null;
		String fileName = null;
		try {
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				throw new Exception("未连接到FTP，用户名或密码错误!");
			}
			synchronized (lock) {
				inputStream = ftpClient.retrieveFileStream(fileFtpPath);
				if (writeFile(localpath, inputStream)) {
					logger.info("从ftp下载文件到本地服务器成功，本地服务器路径为：【" + localpath + "】ip:" + host);
					fileName = localpath;
				} else {
					logger.info("ftp文件写入到本地服务器失败：ip:" + host + "ftp文件路径：" + fileFtpPath);
				}

				// FTPFile[] ftpFile = ftpClient.listFiles(fileFtpDir);
				// if(ftpFile != null && ftpFile.length >0){
				// int length = ftpFile.length;
				//
				// for (int i = 0; i < length; i++) {
				// FTPFile file = ftpFile[i];
				// String filename = file.getName();
				// if (filename.endsWith(".xml")||
				// filename.toUpperCase().endsWith(".XML")) {
				// //从ftp上读取文件: 一次连接ftp只能读取一个文件
				// // if(i>0){
				// // ftpClient.connect(serverIp);
				// // ftpClient.login(userName, passWord);
				// //ftpLogin(ftpClient, serverIp, userName, passWord);
				// // }
				// inputStream = ftpClient.retrieveFileStream(fileFtpDir + "/" +
				// filename);
				// String localpath = fileName;
				// if(writeFile(localpath, inputStream)){
				// logger.info("从ftp下载文件到本地服务器成功，本地服务器路径为：【" + localpath +
				// "】ip:"+host);
				// listLocalFile.add(localpath);
				// }else{
				// logger.info("ftp文件写入到本地服务器失败：ip:"+host+"ftp文件路径：" +
				// fileFtpDir + "/" + filename);
				// }
				// inputStream.close();
				// ftpClient.completePendingCommand();
				// }
				//
				// }
				// }

			}
		} catch (Exception e) {
			logger.error("下载FTP服务器文件发生异常：", e);
			throw new Exception("下载FTP服务器文件发生异常：" + e);
		}
		return fileName;

	}

	public static void main(String[] args) {

	}

	public boolean hasTokenFile(String absolutePath) {
		boolean tokenFileFlag = true;

		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(absolutePath, "r");
			if (randomAccessFile.length() > 0L) {
				String tokenStr = randomAccessFile.readLine();
				if ((tokenStr != null) && (tokenStr.startsWith("yinfeng"))) {
					String time = tokenStr.substring(tokenStr.indexOf("_") + 1);
					if ((System.currentTimeMillis() - Long.parseLong(time)) > 1000 * 60 * 60) {
						tokenFileFlag = false;
					}
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("", e);
			try {
				randomAccessFile.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			logger.error("", e);
			try {
				randomAccessFile.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				randomAccessFile.close();
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		try {
			randomAccessFile.close();
		} catch (IOException e) {
			logger.error("", e);
		}
		return tokenFileFlag;
	}

}
