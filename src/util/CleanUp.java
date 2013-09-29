/*
 * �ַ���������ȥ��ǰ��ո�ȥ�������ţ�Ӣ��ȫ����ΪСд
 */
package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CleanUp {

	// ȥ�������ţ����Լ������
	private static Pattern p = Pattern
			.compile("[(.|,|\"|\\?|!|:|;|\\-|_|'|<|>|\\[|\\]|��|��|��|��|��|��|��|��)]+");

	public static String clean(String str) {
		String res = "";
		if (str == null)
			return res;

		Matcher m = p.matcher(str);
		// System.out.println(m.replaceAll(""));
		res = m.replaceAll("");
		// ȥ��ǰ��ո�Ӣ��ȫ����ΪСд
		res = res.trim().toLowerCase();
		return res;
	}

	// ���ַ����еı��ת���ɿո�
	public static String splitToSpace(String str) {
		String res = "";
		if (str == null)
			return res;
		Matcher m = p.matcher(str);
		// System.out.println(m.replaceAll(""));
		res = m.replaceAll(" ");
		// ȥ��ǰ��ո�Ӣ��ȫ����ΪСд
		res = res.trim().toLowerCase();
		return res;

	}

	public static void main(String[] args) {
		System.out.println(CleanUp.clean("jin����,.<>!...����-_һ���顶javaʲô�ġ�"));
	}

}
