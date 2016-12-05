package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Properties;

import encryptionUtils.EncryptUtil;

public class InputDeal {
	public InputDeal(File file) {
		// TODO Auto-generated constructor stub
		try {
			
			//密码的密文输入
			
			
			//接收信息
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("请输入用户名");
			String name = bufferedReader.readLine();
			System.out.println("请输入密码");
			String password = bufferedReader.readLine(); 
			
			//装载加密存储
			Properties  props = new Properties();
			FileOutputStream oFile = new FileOutputStream("file", true);//true表示追加打开
			/*InputStream in = encryptionUtils.class.getResourceAsStream("/jdbc.properties");*/
			props.load(new FileReader(InputDeal.class.getClassLoader().getResource("file").getPath()));
			
			
			props.setProperty("jdbc.username", EncryptUtil.encrypt(name));
			props.setProperty("jdbc.password", EncryptUtil.encrypt(password));
			props.store(oFile, "file");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	


}
