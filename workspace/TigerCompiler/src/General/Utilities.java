package General;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Utilities {
	public static <T> LinkedList<T> toLL(@SuppressWarnings("unchecked") T... inputs){
		LinkedList<T> outputs = new LinkedList<>();
		for(int i = 0; i < inputs.length;i++){
			outputs.add(inputs[i]);
		}
		return outputs;
	}
	
	public static <T> ArrayList<T> toAL(@SuppressWarnings("unchecked") T... inputs){
		ArrayList<T> outputs = new ArrayList<>();
		for(int i = 0; i < inputs.length;i++){
			outputs.add(inputs[i]);
		}
		return outputs;
	}
	
	public static <T extends Comparable<? super T>> T min(Collection<T> items){
		T min = null;
		for(T current : items){
			if(min.compareTo(current) > 0){
				min = current;
			}
		}
		return min;
	}
	
	public static <T extends Comparable<? super T>> T max(Collection<T> items){
		T max = null;
		for(T current : items){
			if(max.compareTo(current) < 0){
				max = current;
			}
		}
		return max;
	}

	
}
