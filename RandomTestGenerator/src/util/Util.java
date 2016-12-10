package util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {
	
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\customTest";
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\lookup-master\\src\\main";
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\mp3agic-master\\src\\main";
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\GenericTree-master\\src\\main";
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\cors-filter-master\\src\\main";
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\clue-master\\src\\main";
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\encog-java-core-master\\src\\main";
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\floodlight-master\\src\\main";
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\flyingsaucer-master";
//	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\JActor-master\\src\\main";
	public static final String ANALYSIS_DIR = "C:\\Users\\Ronaldo\\Documents\\GitHub\\JunitTestGenerator\\RandomTestGenerator\\spring-data-commons-master\\src\\main";
	
	public static final String TEMPLATE = "TestTemplate.java";
	
	public static int numberOfVariables = 0;
	
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
		StringBuilder result = new StringBuilder();
		
		int currentNumber = ++numberOfVariables;
		
		while(currentNumber > 26) {
			result.append("a");
			currentNumber = currentNumber - 26;
		}
		
		result.append(Character.toString((char)(currentNumber+96)));
		
		return result.toString();
	}
	
	public static void resetVar() {
		numberOfVariables = 0;
	}
	
}
