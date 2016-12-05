package common;
import java.awt.TextField;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
public class test {
	public static void main(String[] args) {
		//交互式控制台
		TextField password = new TextField(8);
		password.setEchoChar('*');
	  //alt+shift+j
		
		
		
		
//实验
			/*	Console console = System.console();
				BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(System.in));
				System.out.println("请输入密码");
				try {
					int a = bufferedReader.read(console.readPassword());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				char[] passwd=console.readPassword();
				System.out.println("password="+passwd);*/
		
		
//网络公认
			/*	Console console = System.console();

				char [] pass = console.readPassword();

				System.out.println(pass);*/
		
		
		/*Console cons=System.console();  
		System.out.print(" 密码：");  
		char[] passwd=cons.readPassword();  
		System.out.println(passwd);*/
			
		}
	
}
