package General;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Utilities {
	public static <T> List<T> toLL(@SuppressWarnings("unchecked") T... inputs){
		List<T> outputs = new LinkedList<>();
		for(int i = 0; i < inputs.length;i++){
			outputs.add(inputs[i]);
		}
		return outputs;
	}
	
	public static <T> List<T> toAL(@SuppressWarnings("unchecked") T... inputs){
		List<T> outputs = new ArrayList<>();
		for(int i = 0; i < inputs.length;i++){
			outputs.add(inputs[i]);
		}
		return outputs;
	}
}
