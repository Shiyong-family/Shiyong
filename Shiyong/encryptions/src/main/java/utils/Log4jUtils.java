package srp.bapp.utils;

import java.io.File;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.LogManager;

public class Log4jUtils {
	public static  String getFirstDailyRollingAppenderDir() {
		DailyRollingFileAppender fileAppender =null;
		@SuppressWarnings("unchecked")
		Enumeration<Appender> e =(Enumeration<Appender>) LogManager.getRootLogger().getAllAppenders();
		while (e.hasMoreElements()) {
			Appender a = (Appender) e.nextElement();
			if (a instanceof DailyRollingFileAppender) {
				fileAppender = (DailyRollingFileAppender) a;
				break;
			}
		}
		String dir = "";
		if(null ==fileAppender){
			return dir;
		}
		String filename = fileAppender.getFile();
		File file = new File(filename);
		dir = file.getAbsolutePath();
		return dir;
	}
}
