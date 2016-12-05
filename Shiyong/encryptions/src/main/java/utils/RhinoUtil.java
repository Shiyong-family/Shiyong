package srp.bapp.utils;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class RhinoUtil {

	/**
	 * 对一个js函数字符串进行检测，确认其符合js语法
	 * 
	 * @param functionDefineString
	 * @param functionName
	 * @param returnValue
	 * @param args
	 *            function的参数
	 * @return
	 */
	public static boolean testJsFunctionSyntax(String functionDefineString) {
		// 没有找到合适的方法
		return true;
	}

	public static void main(String[] args) {
		Context cx = Context.enter();
		Scriptable scope = cx.initStandardObjects();

		ScriptableObject.putProperty(scope, "x", 10);

		Object result = cx.evaluateString(scope, "function add(a,b){return a*x+b;} \n add(3,5);", "<cmd>", 1, null);

		System.out.println(result);

		Context.exit();

		testJsFunctionSyntax("turn(1);");

	}
}
