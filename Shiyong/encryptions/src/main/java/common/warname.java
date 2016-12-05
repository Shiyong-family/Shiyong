/*package encryption;    

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

//修改名字，并且重新命名

public   class   warname 
{   
        public   static   void   main(String[]   args)   {   
          
          Date now = new Date(); 
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
          SimpleDateFormat dateFormat2 = new SimpleDateFormat();

          String time = dateFormat.format( now ); 
          
          System.out.println(time);
        	
          File   file=new   File("e:/bapp.war");   //指定文件名及路径
          
          String name="bapp_"+time;   
          
         String   filename=file.getAbsolutePath();   
         System.out.println(filename);
          if(filename.indexOf(".")>=0)   
          {   
              filename   =   filename.substring(0,filename.lastIndexOf("."));   
          }  
          file.renameTo(new   File(name+".war"));   //改名   
      }   
    
}*/



package common;

import java.io.File;
import java.io.IOException;

public class warname {

 public static void main(String[] args) throws IOException 
 {
 
  //获取文件
  File oldFile = new File("e:/bapp.war");
  
  if(!oldFile.exists())
  {
   oldFile.createNewFile();
  }
  
  String rootPath = oldFile.getParent();

  File newFile = new File(rootPath + File.separator +oldFile.getName());
  System.out.println("修改后文件名称是："+newFile.getName());
  if (oldFile.renameTo(newFile)) 
  {
   System.out.println("修改成功!");
  } 
  else 
  {
   System.out.println("修改失败");
  }
 }
}