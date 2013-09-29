package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaxValue {
	
	public static double getMaxValue(List<Double> values){
		Collections.sort(values);
		return values.get(values.size() - 1);
	}
	
	public static void main(String[] args) {
		List<Double> values = new ArrayList<Double>();
		values.add(1.0);
		values.add(0.5);
		values.add(0.1);
		values.add(2.3);
		
		System.out.println(MaxValue.getMaxValue(values));
	}

}
