package util;

import java.util.ArrayList;
import java.util.List;

import model.Sequence;

public class ValuePool {
	
	public static List<Sequence> pool = new ArrayList<Sequence>();
	
	public static final String ANY_CLASS = "*.class";
	
	static {
		
		//integers
		pool.add(new Sequence(int.class, 0));
		pool.add(new Sequence(int.class, 1));
		pool.add(new Sequence(Integer.class, 0));
		pool.add(new Sequence(Integer.class, 1));
		
		//byte FIXME
		pool.add(new Sequence(byte.class, 0));
		pool.add(new Sequence(byte.class, 1));
		pool.add(new Sequence(Byte.class, 0));
		pool.add(new Sequence(Byte.class, 1));
		
		//short
		pool.add(new Sequence(short.class, 0));
		pool.add(new Sequence(short.class, 1));
		pool.add(new Sequence(Short.class, 0));
		pool.add(new Sequence(Short.class, 1));
		
		//long
		pool.add(new Sequence(long.class, 0));
		pool.add(new Sequence(long.class, 1));
		pool.add(new Sequence(Long.class, 0));
		pool.add(new Sequence(Long.class, 1));
		
		//float
		pool.add(new Sequence(float.class, 0));
		pool.add(new Sequence(float.class, 1));
		pool.add(new Sequence(Float.class, 0));
		pool.add(new Sequence(Float.class, 1));
		
		//double
		pool.add(new Sequence(double.class, 0));
		pool.add(new Sequence(double.class, 1));
		pool.add(new Sequence(Double.class, 0));
		pool.add(new Sequence(Double.class, 1));
		
		//boolean
		pool.add(new Sequence(boolean.class, false));
		pool.add(new Sequence(boolean.class, true));
		pool.add(new Sequence(Boolean.class, false));
		pool.add(new Sequence(Boolean.class, true));
		
		//char
		pool.add(new Sequence(char.class, 'a'));
		pool.add(new Sequence(char.class, 'b'));
		pool.add(new Sequence(Character.class, 'a'));
		pool.add(new Sequence(Character.class, 'b'));
		
		//String
		pool.add(new Sequence("String", "hi"));
		pool.add(new Sequence("String", "hello"));	
	}
	
	//TODO IMPLEMENT RANDOM?
	public static Sequence getType(String type) {
		Sequence possibleSequence = null;
		
		for (Sequence sequence : pool) {
			for (Object typeGenerated : sequence.getTypes()) {
				if (typeGenerated.toString().equals(type)) {
					possibleSequence = sequence;
					break;
				}
			}
			if(possibleSequence!=null) break;
		}
		
		return possibleSequence;
	}
	
}
