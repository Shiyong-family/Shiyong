package core;

import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


import common.FileSearch;
import encryptionUtils.ZipUtil;


/**
 * 
 * @author Sy
 *
 */
public  class FileOperation {
	
	//获取到所需要的文件
	
	/**
	 * @return
	 * @throws Exception
	 */
	public File getfile() throws Exception{
//			ZipUtils utils = new ZipUtils();//解压文件使用的工具类，ziputils和ziputil有所区别，暂时使用ziputils	
			ZipUtil utils=new ZipUtil();
			String startaddress = "e:/bapp.war";
			File targetaddress = new File("e:/456");
			
//			utils.zip(startaddress.toString(), targetaddress);
			utils.unzip(startaddress, targetaddress.toString());
			
			java.util.List list=FileSearch.findFile(targetaddress, "jdbc.properties");
			File file = (File) list.get(0); 

		return file;
		
	}
/*		public static void main(String[] args) {
					
//			ZipUtil.unzip("e:/456", "e:/bapp.war");		
//			ZipUtil.unzip("e:/bapp.war", "e:/123");
		
		}*/
	
	//处理完成后压缩
	public void zipfile() throws FileNotFoundException, IOException{
		ZipUtil util = new ZipUtil();
		util.zip("e:/456/bapppppp.war", null, "e:/456");
	}
	
}
	