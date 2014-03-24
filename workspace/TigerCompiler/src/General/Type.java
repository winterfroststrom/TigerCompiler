package General;

import java.util.LinkedList;
import java.util.List;

public class Type {
	public final boolean array;
	public final String name;
	public final Type type;
	public final List<Integer> dimensions;
	public final static Type INT = new Type("int", null);
	public final static Type STRING = new Type("string", null);
	
	public Type(String name, Type type){
		this(name, false, type, new LinkedList<Integer>());
	}
	
	public Type(String name, Type type, List<Integer> dimensions){
		this(name, true, type, dimensions);
	}
	
	private Type(String name, boolean array, Type type, List<Integer> dimensions){
		this.array = array;
		this.name = name;
		this.dimensions = dimensions;
		this.type = type;
	}
	
	public boolean equals(Type other){
		return name.equals(other.name);
	}
	
	public Type baseType(){
		if(equals(Type.INT) || equals(Type.STRING)|| array){
			return this;
		} else {
			return type.baseType();
		}
	}
	
	@Override
	public String toString(){
		if(array){
			return name + dimensions + " of "+ type;
		} else if(type == null) {
			return name;
		} else {
			return name + " of "+ type.name;
		} 
	}
}
