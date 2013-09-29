/*
 * 最长公共子串
 * 输入两个字符串s1和s2
 * 记录公共子串长度后删去公共子串，再次计算公共子串长度，直至没有公共子串
 * 最终返回 
 * (2*ΣLCSi/len(s1)+len(s2))
 */
package simAlgorithms;

public class Comm {
	
	//查找公共子串
	//lcs记录公共子串
	//返回公共子串长度
	
	public static String resLcs = "";
	
	//在str2上的匹配最末位置
	public static int pos = 0;
	
	public static int LCS(String str1, String str2){
		if(str1 == null || str2 == null){
			return -1;
		}
		char[] lcs = null;
		int len1 = str1.length();
		int len2 = str2.length();
		
		resLcs = "";
		pos = 0;
		
		//压缩后的最长子串记录向量
		int[] c = new int[len2+1];
		for(int i = 0; i < len2; i++){
			c[i] = 0;
		}
		
		int max_len = 0;
		for(int i = 0; i < len1; i++){
			for(int j = len2; j > 0 ; j--){
				if(str1.charAt(i) == str2.charAt(j-1)){
					c[j] = c[j-1] + 1;
					if(c[j] > max_len){
						max_len = c[j];
						pos = j - 1;
					}
				} else{
					c[j] = 0;
				}
			}
		}
		
		if(max_len == 0){
			return 0;
		}
		
		//得到公共子串
		lcs = new char[max_len];
		for(int i = 0; i < max_len; i++){
			lcs[i] = str2.charAt(pos - max_len + 1 + i);
		}
		
		resLcs = new String(lcs);
		return max_len;
		
	}
	
	//记录最长子串长度后删除此子串，再次计算最长长度并叠加，直至没有公共子串
	public static int total(String str1, String str2){
		int len = 0;
		int total = 0;
		while((len = LCS(str1, str2)) > 0){
			total += len;
			str1 = str1.replace(resLcs, "");
//			System.out.println(str1);
			str2 = str2.replace(resLcs, "");
//			System.out.println(str2);
		}
//		System.out.println(total);
		return total;
	}
	
	public static double similarity(String str1, String str2){
		if(str1.equals("")|| str2.equals("")){
			return 0;
		}
		int norm = str1.length() + str2.length();
		int total = total(str1, str2);
//		System.out.println(total+"\t"+norm);
		double x = total;
		double y = norm;
		return 2*x/y;
	}
	
	public static void main(String[] args) {
//		System.out.println(LCS.LCS("abcdefg", "abc"));
//		System.out.println(pos);
		System.out.println(similarity("", "xyz,abc"));
		
	}

}
