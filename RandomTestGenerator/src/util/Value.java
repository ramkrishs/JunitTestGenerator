package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.Sequence;

public class Value {
	
	public static List<Sequence> pool = new ArrayList<Sequence>();
	
	public static final String ANY_CLASS = "*.class";
	
	static {
		
		//integers
		pool.add(new Sequence(int.class, 0));
		pool.add(new Sequence(int.class, 1));
		pool.add(new Sequence(Integer.class, 0));
		pool.add(new Sequence(Integer.class, 1));
		
		//TODO byte
		
		//short
		
		//long
		
		//float
		
		//double
		
		//boolean
		
		//char
		
		//String
		pool.add(new Sequence(String.class, Arrays.asList("String s;", "s = \"hi\";")));	
		
		//null
		pool.add(new Sequence(Value.ANY_CLASS, null));	
	}
	
	public static void main(String[] args) {
		
		for (Sequence sequence : pool) {
			System.out.println("");
			for (Object types : sequence.getTypes()) {
				System.out.println(types);
			}
			for (Object types : sequence.getStatements()) {
				System.out.println(types);
			}
		}
	}
}
