package SemanticChecking;

import General.Configuration;

public class ScopedName {
	public final boolean function;
	public final Type type;
	public final String name;
	
	public ScopedName(boolean function, String name, Type type){
		this.name = name;
		this.type = type;
		this.function = function;
	}
	
	@Override
	public String toString(){
		if(function){
			if(type == null){
				return name + ":|void|";
			} else {
				return name + ":" + type;				
			}
		} else {
			return name + ":" + type;				
		}
	}
	
	public static String createScopedName(String scope, String name){
		return scope + Configuration.SCOPE_DELIMITER + name;
	}
	
}
