package common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSearch {


	static List<File> list = new ArrayList<File>();// 存储目标文件

	//遍历得到
	/**
	 * 
	 * @param root
	 * @param name
	 * @return
	 */
	public static List findFile(File root, String name) {
		if (root.exists() && root.isDirectory()) {
			for (File file : root.listFiles()) {
				if (file.isFile() && file.getName().equals(name)) {// 如果是文件，而且同名
					list.add(file);
					System.out.println(file.getName());//这里输出文件名！
				} else if (file.isDirectory()) {// 如果是目录，则继续递归遍历
					findFile(file, name);
					System.out.println("没有找到所需要的文件");
				}
			}
		}
		return list;
	}
}
