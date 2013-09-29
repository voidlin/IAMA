/*
 * ������Ӵ�
 * ���������ַ���s1��s2
 * ��¼�����Ӵ����Ⱥ�ɾȥ�����Ӵ����ٴμ��㹫���Ӵ����ȣ�ֱ��û�й����Ӵ�
 * ���շ��� 
 * (2*��LCSi/len(s1)+len(s2))
 */
package simAlgorithms;

public class Comm {
	
	//���ҹ����Ӵ�
	//lcs��¼�����Ӵ�
	//���ع����Ӵ�����
	
	public static String resLcs = "";
	
	//��str2�ϵ�ƥ����ĩλ��
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
		
		//ѹ�������Ӵ���¼����
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
		
		//�õ������Ӵ�
		lcs = new char[max_len];
		for(int i = 0; i < max_len; i++){
			lcs[i] = str2.charAt(pos - max_len + 1 + i);
		}
		
		resLcs = new String(lcs);
		return max_len;
		
	}
	
	//��¼��Ӵ����Ⱥ�ɾ�����Ӵ����ٴμ�������Ȳ����ӣ�ֱ��û�й����Ӵ�
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
