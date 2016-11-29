package util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;

public class Util {
	
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ramakrishnan_sathyav\\Documents\\Coll work\\STVV\\JunitTestGenerator\\RandomTestGenerator\\cors-filter-master\\src\\main";
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\GenericTree-master\\src\\main";
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\cors-filter-master\\src\\main";
	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\lookup-master\\src\\main";
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\mp3agic-master\\src\\main";
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\test";

	public static final String TEMPLATE = "TestTemplate.java";
	
	public static int numOfVars = 0;
	
	public static char nextAvailable = 96;
	
	public static String readFile(String path) throws IOException {
		
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		
		return new String(encoded, Charset.defaultCharset());
	}
	
	/**
	 * Change the first letter to lowerCase
	 * @param className
	 * @return
	 */
	public static String lowerFirstChar(String className) {
		char c[] = className.toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		return new String(c);
	}
	
	/**
	 * Change the first letter to UpperCase
	 * @param className
	 * @return
	 */
	public static String upperFirstChar(String className) {
		char c[] = className.toCharArray();
		c[0] = Character.toUpperCase(c[0]);
		return new String(c);
	}
	
	
	/**
	 * Returns the name of a java file (without the extension)
	 * 
	 * @param javaFile
	 * @return file name
	 */
	public static String getFileName(File javaFile) {
		return javaFile.getName().substring(0, javaFile.getName().length()-5);
	}

	public static boolean isPrimitive(String typeString) {
		boolean isPrimitive;
		switch (typeString) {
		case "int":
		case "byte":
		case "short":
		case "long":
		case "float":
		case "double":
		case "boolean":
		case "char":
		case "String": isPrimitive = true; break;
		default: isPrimitive = false; break;
		}
		return isPrimitive;
	}
	
	public static String getNextVar() {
		return Character.toString(++nextAvailable);
	}
	
	public static void resetVar() {
		nextAvailable = 96;
	}
	
	/*
	public static String getNextVarName() {
		String varName = "";
		
		int vars = ++numOfVars;
		
		int varsAux = vars;
		while(varsAux > 26)
			varsAux -= 26;
		
		
		int numberOfChars = 1;
		while(vars > 26) {
			numberOfChars++;
			
			vars = (int)(vars/26);
		}
		
		
		vars = numOfVars;
		
		int j = 0;
		for (int i = numberOfChars; i > 0; i--) {
			
			int code = 96 + (int)(((vars/((int)Math.pow(26, j)))-1)/(int)Math.pow(26, i-1));
			
			if(i == 1) {
				code = 96 + varsAux;
			}
			
			j++;
			
			varName += (char) code;
//			System.out.print((char) code);
		}
//		System.out.println();
		
		
		
//		((int)((vars-1)/(26*26*26)))+1			((int)((vars-1)/(26*26)))+1			((int)((vars-1)/26))+1			((int)(vars-1))+1
//		
//		(int)((vars-1)/26)
//		(int)((vars-1)/26)
//		while(vars > 27) {
//			
//		}
//		
//		if(vars > 26) {
//			
//			//49
//			char thirdChar = (char) (96 + ((int) (vars-1)/26*26));
//			
//			char secondChar = (char) (96 + ((int) (vars-1)/26));
//			
//			char firstChar = (char) (97 + ((vars-1) % 26));
//			
//			varName = thirdChar + "" + secondChar + "" +firstChar;
//			
//		} else {
//			char firstChar = (char) (96 + vars);
//			
//			varName += firstChar;
//		}
		
		return varName;
	}
	*/
	
	private static String calculate(int number) {
		return calculate(number, number);
	}
	
	private static String calculate(int number, int value) {
		if(number > 26) {
			value = number % 26;
			number = number / 26;
			
			String returnString = calculate(number, value==0?1:value);
			returnString = (char) (96 + number) + returnString;
			return returnString;
		} else {
			return Character.toString((char) (96 + value));
		}
	}
	
	public static void main(String[] args) {
		
		/*		
		97 122
		25
		
		1a
		2b
		...
		25z
		26aa
		27ab
		...
		50az (50/25 = 2) (50-25=25)
		51ba
		52bb (52/25 = 2) mod 2(52-25=27)
		...
		74by
		75bz (75/25 = 3) (75-50=25)
		76ca
	*/	
		for (int i = 1; i <= 150; i++) {
//			System.out.println(calculate(i));
//			getNextVarName();
			System.out.println(ThreadLocalRandom.current().nextInt(0, 4));
		}
	}

}
