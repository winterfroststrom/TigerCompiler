package General;

import java.util.LinkedList;
import java.util.List;

public class Type {
	private final boolean array;
	public final String name;
	private final Type type;
	private final List<Integer> dimensions;
	public final static Type INT = new Type("int", null);
	public final static Type CINT = new Type("Constant|int", INT);
	public final static Type STRING = new Type("string", null);
	public final static Type CSTRING = new Type("Constant|string", STRING);
	
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
	
	public boolean isConstant(){
		return this == CINT || this == CSTRING;
	}
	
	public boolean equals(Type other){
		if(this.isConstant() || other.isConstant()){
			return baseType().equals(other.baseType());
		} else {
			return name.equals(other.name);
		}
	}
	
	public Type arrayBaseType(){
		if(array){
			return type.baseType();
		} else {
			return baseType();
		}
	}
	
	public List<Integer> totalDimensions(){
		Type base = baseType();
		List<Integer> dims = new LinkedList<>();
		if(base.array){
			for(Integer i : base.dimensions){
				dims.add(i);
			}
			for(Integer i : base.type.totalDimensions()){
				dims.add(i);
			}
		}
		return dims;
	}
	
	public int length(){
		Type base = baseType();
		if(base.array){
			int size = 1;
			for(int dim : dimensions){
				size *= dim;
			}
			return size * base.type.length();
		} else {
			return 1;
		}
	}
	
	public boolean isArray(){
		Type base = baseType();
		return base.array;
	}
	
	public Type baseType(){
		if(this == Type.INT || this == Type.STRING || array){
			return this;
		} else {
			return type.baseType();
		}
	}
	
	public Type dereference(int amount){
		Type base = baseType();
		if(amount == 0){
			return this;
		} else if(base.array){
			if(amount == base.dimensions.size()){
				return base.type;
			} else if(amount > base.dimensions.size()){
				return base.type.dereference(amount - base.dimensions.size());
			} else {
				return null;
			}
		} else {
			return null;
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
