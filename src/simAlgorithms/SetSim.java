/*
 * 两个集合之间的相似度
 * sim = 2 * comm / (s1 + s2)
 * 
 */
package simAlgorithms;

import java.util.HashSet;
import java.util.Set;

public class SetSim {

	public static double similarity(Set<String> s1, Set<String> s2){
		if(s1.size() == 0 || s2.size() == 0){
			return 0;
		}
		
		double all = s1.size() + s2.size();
		
		int same = 0;
		for(String o1 : s1){
			for(String o2 : s2){
				if( o1.equals(o2)){
					same ++;
				}
			}
		}
		
		double comm = same;
		double res = 2 * comm / all;
		return res;
	}
	
	public static void main(String[] args) {
		Set s1 = new HashSet<String>();
		Set s2 = new HashSet<String>();
		s1.add("1");
		s1.add("2");
		s2.add("1");
		s2.add("0");
		s2.add("3");
		System.out.println(similarity(s1, s2));
	}

}
