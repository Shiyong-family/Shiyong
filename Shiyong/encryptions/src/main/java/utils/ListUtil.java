package srp.bapp.utils;

import java.util.LinkedList;
import java.util.List;

public class ListUtil {
	public static List<String> makeList( String... ms ){
		List<String> l = new LinkedList<String>();
		for (int i = 0; i < ms.length; i++) {
			l.add(  ms[i]);
		}
		return l;
	}
	
	public static List<Object> makeList( Object... ms ){
		List<Object> l = new LinkedList<Object>();
		for (int i = 0; i < ms.length; i++) {
			l.add(  ms[i]);
		}
		return l;
	}
}
