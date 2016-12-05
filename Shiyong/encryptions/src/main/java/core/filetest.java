package core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import common.FileSearch;
import encryptionUtils.ZipUtil;

public class filetest {

	//寻找文件
	/*static List<File> list = new ArrayList<File>();// 存储目标文件

	public static void main(String[] args) {
		File root = new File("e:/123");
		findFile(root, "jdbc.properties");
	}

	public static void findFile(File root, String name) {
		if (root.exists() && root.isDirectory()) {
			for (File file : root.listFiles()) {
				if (file.isFile() && file.getName().equals(name)) {// 如果是文件，而且同名
					list.add(file);
					System.out.println(file.getName());//这里输出文件名！
				} else if (file.isDirectory()) {// 如果是目录，则继续递归遍历
					findFile(file, name);
				}
			}
		}
	}*/
	
	//文件的解压出现问题
	public static void main(String[] args) throws Exception {
		ZipUtil utils = new ZipUtil();//解压文件使用的工具类，ziputils和ziputil有所区别，暂时使用ziputils		
		String startaddress = "e:/bapp.war";
		File targetaddress = new File("e:/456");
		
		ZipUtil util=new ZipUtil();
		util.unzip(startaddress, targetaddress.toString());
//		utils.zip(startaddress.toString(), targetaddress);
//		utils.unzip(startaddress, targetaddress.toString());
	
		
		
		
		
		
		
		
		
		
		
		
		
		
/*		ZipUtils utils = new ZipUtils();//解压文件使用的工具类，ziputils和ziputil有所区别，暂时使用ziputils		
		String startaddress = "e:/bapp.war";
		File targetaddress = new File("e:/456");
		
//		utils.zip(startaddress.toString(), targetaddress);
		try {
			utils.unzip(startaddress, targetaddress.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		java.util.List list=FileSearch.findFile(targetaddress, "jdbc.properties");
		System.out.println(list.get(0));
	}*/
	
	}	
}