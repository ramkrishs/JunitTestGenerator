package model;

import java.util.ArrayList;
import java.util.List;

public class Sequence {
	//types
	private List<Object> types;
	
	//statements
	private List<Object> statements;
	
	public Sequence(Object type, Object value) {
		types = new ArrayList<Object>();
		types.add(type);
		
		statements = new ArrayList<Object>();
		statements.add(value);
	}
	
	public Sequence(Object type, List<Object> value) {
		types = new ArrayList<Object>();
		types.add(type);
		
		statements = new ArrayList<Object>();
		statements.addAll(value);
	}
	
	public Sequence(List<Object> type, Object value) {
		types = new ArrayList<Object>();
		types.addAll(type);
		
		statements = new ArrayList<Object>();
		statements.add(value);
	}
	
	public Sequence(List<Object> type, List<Object> value) {
		types = new ArrayList<Object>();
		types.addAll(type);
		
		statements = new ArrayList<Object>();
		statements.addAll(value);
	}

	public List<Object> getTypes() {
		return types;
	}

	public void setTypes(List<Object> types) {
		this.types = types;
	}

	public List<Object> getStatements() {
		return statements;
	}

	public void setStatements(List<Object> statements) {
		this.statements = statements;
	}
	

}