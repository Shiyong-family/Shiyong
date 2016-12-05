package core;

import java.io.File;




public class Encryption {
	public static void main(String[] args) throws Exception {
		FileOperation file=new FileOperation();
		InputDeal deal = new InputDeal(file.getfile());
		file.zipfile();
	}
}
