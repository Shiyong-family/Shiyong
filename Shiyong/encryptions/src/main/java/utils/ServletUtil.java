package srp.bapp.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;

public class ServletUtil {
	public static void setExportName(HttpServletRequest request, HttpServletResponse response, String name) {
		//if (-1 == request.getHeader("USER-AGENT").toUpperCase().indexOf("CHROME")) {
			try {
				name = URLEncoder.encode(name, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		//}
		response.addHeader("Content-Disposition", "attachment;filename=" + name);
	}
	public static String result4Download(ModelMap model, String msg) {
		model.put(
				"content",
				"<script>" + "Ext.require('Ext.ux.window.Notification');" + "Ext.onReady(function() {window.parent.bappAlert('"
						+ msg.replace('\'', '"') + "');});</script>");
		return "nojsp";
	}
}
