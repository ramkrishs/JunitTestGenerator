package model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

public class Method {

	private SimpleName name;
	
	private Map<Integer, Type> argumentTypes;
	
	private Type returnType;
	
	public Method() {
		this(null, new HashMap<Integer, Type>(), null);
	}

	public Method(SimpleName name, Map<Integer, Type> argumentTypes, Type returnType) {
		this.name = name;
		this.argumentTypes = argumentTypes;
		this.returnType = returnType;
	}
	
	public void put(Type parameter) {
		argumentTypes.put(argumentTypes.size(), parameter);
	}
	
	@Override
	public String toString() {
		String methodString = name.toString();
		
		methodString += "(";
		for(int i = 0; i < argumentTypes.keySet().size(); i++) {
			methodString += argumentTypes.get(i).toString();
			
			if(i<argumentTypes.keySet().size()-1) {
				methodString += ", ";
			}
		}
		methodString += ")";
		
		methodString += "/" + returnType.toString();
		
		return methodString;
	}

	public SimpleName getName() {
		return name;
	}

	public void setName(SimpleName name) {
		this.name = name;
	}

	public Map<Integer, Type> getArgumentTypes() {
		return argumentTypes;
	}

	public void setArgumentTypes(Map<Integer, Type> argumentTypes) {
		this.argumentTypes = argumentTypes;
	}

	public Type getReturnType() {
		return returnType;
	}

	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}
	
}