/*
 * 字符串的清理，去除前后空格，去除标点符号，英文全部变为小写
 */
package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CleanUp {

	// 去除标点符号，可以继续添加
	private static Pattern p = Pattern
			.compile("[(.|,|\"|\\?|!|:|;|\\-|_|'|<|>|\\[|\\]|《|》|，|。|！|？|（|）)]+");

	public static String clean(String str) {
		String res = "";
		if (str == null)
			return res;

		Matcher m = p.matcher(str);
		// System.out.println(m.replaceAll(""));
		res = m.replaceAll("");
		// 去除前后空格，英文全部变为小写
		res = res.trim().toLowerCase();
		return res;
	}

	// 把字符串中的标点转换成空格
	public static String splitToSpace(String str) {
		String res = "";
		if (str == null)
			return res;
		Matcher m = p.matcher(str);
		// System.out.println(m.replaceAll(""));
		res = m.replaceAll(" ");
		// 去除前后空格，英文全部变为小写
		res = res.trim().toLowerCase();
		return res;

	}

	public static void main(String[] args) {
		System.out.println(CleanUp.clean("jin今天,.<>!...看来-_一本书《java什么的》"));
	}

}
