package core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import model.Method;
import model.MethodType;
import util.Util;

public class TestGenerator {
	
	private static MethodType currentType = MethodType.CONSTRUCTOR;
	private static ArrayList<Method> methods;
	
	public static void main(String args[]) throws Exception {
		
		//Template to populate
		String testTemplate = Util.readFile(Util.TEMPLATE);
		
		//Directory to analyze
		File dir = new File(Util.ANALYSIS_DIR);

		//Read all java files
		String[] extension = { "java" };
		Collection<File> javaFiles = FileUtils.listFiles(dir, extension, true);
		
		long startTime = System.currentTimeMillis();
		for (File file : javaFiles) {
			//Get original file contents
			String originalContent = Util.readFile(file.getAbsolutePath());
			
			//Get name of the file
			String className = Util.getFileName(file);
			
			generateTestCases(className, testTemplate, originalContent);
		}
		
		System.out.println("Executed in "+ (System.currentTimeMillis()-startTime) + "(ms)");
	}
	
	/**
	 * Generate the test cases for a given class (named className). A template provided is used (testTemplate) which might include fixture, etc.
	 * The test cases are generated with respect to a file in String representation (originalContent)
	 * @param className
	 * @param testTemplate
	 * @param originalContent
	 * @throws Exception
	 */
	private static void generateTestCases(String className, String testTemplate, String originalContent) throws Exception {
		/**
		Do until time limit expires:
			• Create a new sequence
				• Randomly pick a method call m(T1...Tk)/Tret
				• For each input parameter of type Ti, randomly pick a sequence Si from the value pool that constructs an object vi of type Ti
				• Create new sequence Snew = S1; ... ; Sk ; Tret vnew = m(v1...vk);
				• if Snew was previously created (lexically), go to first step
			• Classify the new sequence Snew
				• May discard, output as test case, or add to pool
		*/
		
		//Generate test class
		String testClass = generateClass(testTemplate, className, getPackage(originalContent));
		
		//Identify methods
		methods = identifyMethods(originalContent);
		
		//TODO Change to random method instead of all (for statement from 1 to size)
		boolean processed = false;
		
		while(hasNext()) {
			processed = true;
			
			Method method = getNext();
			
			//TODO create a Sequence object (start with constructors? after that, primitive parameters? after that the rest?)
			//TODO check sequence against contracts
			//FIXME remove (POSSIBLE CONTRACTS)
//					• o.equals(o)==true
//					• o.equals(o) throws no exception
//					• o.hashCode() throws no exception
//					• o.toString() throw no exception
//					• No null inputs and:
//						• Java: No NPEs
//						• .NET: No NPEs, out-of-bounds, of illegal state exceptions
			//if violated, create test case and add "contract-violating test case"
			//else, check if the sequence is redundant
			//if redundant, discard
			//else, create test case normally and pool
			
			testClass = generateTest(testClass, method);

//			System.out.println(method.toString());
			currentType = MethodType.CONSTRUCTOR;
		}
		
		if(processed) {
			System.out.println(testClass+"\n\n\n\n\n");
		}
	}
	
	private static ArrayList<Method> identifyMethods(String content) {
		CompilationUnit unit = parseAST(content);
		
		SimpleVisitor visitor = new SimpleVisitor();
		unit.accept(visitor);
		
		return visitor.getMethods();
	}
	
	private static String generateTest(String content, Method method) throws Exception {
		CompilationUnit unit = parseAST(content);
		
		AST ast = unit.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);

		Document document = new Document(content);
		
		//Create the Test Case
		MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
		//Create annotation @Test
		MarkerAnnotation annotation= ast.newMarkerAnnotation();
		annotation.setTypeName(ast.newName("Test"));
		methodDeclaration.modifiers().add(annotation);
		//Private
		methodDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		//Void
		methodDeclaration.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		//Method name
		methodDeclaration.setName(ast.newSimpleName("test"+Util.upperFirstChar(method.getName().toString())));
		
		//Create the test case body
		Block block = ast.newBlock();
		
		//Create assertEquals(true, true)
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setName(ast.newSimpleName("assertEquals"));
		methodInvocation.arguments().add(ast.newBooleanLiteral(true));
		methodInvocation.arguments().add(ast.newBooleanLiteral(true));
		
		ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);

		//Insert assert into block
		ListRewrite listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		listRewrite.insertFirst(expressionStatement, null);
		
		methodDeclaration.setBody(block);
		
		//Insert method into the types of the compilation unit
		TypeDeclaration typeDeclaration = (TypeDeclaration) unit.types().get(0);
		listRewrite = rewriter.getListRewrite(typeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertAt(methodDeclaration, 2, null);
		
//		1. generate random method invocations
		
		
//		2. generate random parameters
		
		TextEdit edits = rewriter.rewriteAST(document, null);
		edits.apply(document);

		return document.get();
	}
	
	private static String generateClass(String content, String className, String[] packageName) throws Exception {
		
		CompilationUnit unit = parseAST(content);

		AST ast = unit.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);

		Document document = new Document(content);
		
		// PACKAGE
		PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
		Name name = ast.newName(packageName);
		packageDeclaration.setName(name);
		
		rewriter.set(unit, CompilationUnit.PACKAGE_PROPERTY, packageDeclaration, null);
		
		// IMPORT - (NOT NEEDED FOR NOW)
//		ImportDeclaration importDeclaration = ast.newImportDeclaration();
//		QualifiedName importName = ast.newQualifiedName(ast.newSimpleName(testClassPackage), ast.newSimpleName(testClassName));
//		importDeclaration.setName(importName);
//		unit.imports().add(importDeclaration);
		ListRewrite listRewrite = rewriter.getListRewrite(unit, CompilationUnit.IMPORTS_PROPERTY);
//		listRewrite.insertFirst(importDeclaration, null);
		
		//Insert empty line between package and import
		Statement placeHolder = (Statement) rewriter.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
		listRewrite.insertFirst(placeHolder, null);
		
		// FIELD - create the field of the class to be tested
		VariableDeclarationFragment newDeclFrag= ast.newVariableDeclarationFragment();
		newDeclFrag.setName(ast.newSimpleName(Util.lowerFirstChar(className)));

		FieldDeclaration fieldDeclaration= ast.newFieldDeclaration(newDeclFrag);
		fieldDeclaration.setType(ast.newSimpleType(ast.newSimpleName(className)));
		fieldDeclaration.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		
		TypeDeclaration typeDeclaration = (TypeDeclaration) unit.types().get(0);
		typeDeclaration.bodyDeclarations().add(fieldDeclaration);
		rewriter.set(typeDeclaration, TypeDeclaration.NAME_PROPERTY, ast.newSimpleName("Test"+className), null);
		
		listRewrite = rewriter.getListRewrite(typeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(fieldDeclaration, null);
		
		// FIXTURE - instantiate within test fixture
		for (MethodDeclaration methodDecl : typeDeclaration.getMethods()) {
			Block block = methodDecl.getBody();
			
			ClassInstanceCreation instance = ast.newClassInstanceCreation();
			instance.setType(ast.newSimpleType(ast.newSimpleName(className)));
			
			Assignment assignment = ast.newAssignment();
			assignment.setLeftHandSide(ast.newSimpleName(Util.lowerFirstChar(className)));
			assignment.setOperator(Assignment.Operator.ASSIGN);
			assignment.setRightHandSide(instance);
			
			ExpressionStatement expressionStatement = ast.newExpressionStatement(assignment);
			
			//Add the expression to the first line of the method block
			listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
			listRewrite.insertFirst(expressionStatement, null);
		}
		
		// apply all the edits to the compilation unit
		TextEdit edits = rewriter.rewriteAST(document, null);
		edits.apply(document);

		return document.get();
	}
	
	private static String[] getPackage(String content) {
		CompilationUnit unit = parseAST(content);
		
		String packageString = unit.getPackage().toString();
		String packageName = packageString.substring(8, packageString.length()-2);
		
		String[] packageElements = packageName.split("\\.");
		
		return packageElements;
	}
	
	private static CompilationUnit parseAST(String content) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(content.toCharArray());

		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		return (CompilationUnit) parser.createAST(null);
	}
	
	private static boolean hasNext() {
		boolean hasNext = false;
		
		if(currentType == MethodType.CONSTRUCTOR) {
			for (Method method : methods) {
				if(method.getArgumentTypes() == null || method.getArgumentTypes().size() <= 0) {
					hasNext = true;
					break;
				}
			}
			if(!hasNext) {
				currentType = MethodType.PRIMITIVE;
			}
		}
		
		if(currentType == MethodType.PRIMITIVE) {
			for (Method method : methods) {
				boolean isPrimitive = true;
				for (Integer key : method.getArgumentTypes().keySet()) {
					Type type = method.getArgumentTypes().get(key);
					
					if(!Util.isPrimitive(type.toString())){
						isPrimitive = false;
						break;
					}
				}
				if(isPrimitive) {
					hasNext = true;
					break;
				}
			}
			if(!hasNext) {
				currentType = MethodType.COMPLEX;
			}
		}
		
		if(currentType == MethodType.COMPLEX) {
			if(methods.size() > 0) {
				hasNext = true;
			}
		}
		
		return hasNext;
	}
	
	//TODO IMPLEMENT	
	private static Method getNext() {
		Method nextMethod = null;
		
		if(currentType == MethodType.CONSTRUCTOR) {
			for (Method method : methods) {
				if(method.getArgumentTypes() == null || method.getArgumentTypes().size() <= 0) {
					nextMethod = method;
					break;
				}
			}
			if(nextMethod == null) {
				currentType = MethodType.PRIMITIVE;
			}
		}
		
		if(currentType == MethodType.PRIMITIVE) {
			for (Method method : methods) {
				boolean isPrimitive = true;
				for (Integer key : method.getArgumentTypes().keySet()) {
					Type type = method.getArgumentTypes().get(key);
					
					if(!Util.isPrimitive(type.toString())){
						isPrimitive = false;
						break;
					}
				}
				if(isPrimitive) {
					nextMethod = method;
					break;
				}
			}
			if(nextMethod == null) {
				currentType = MethodType.COMPLEX;
			}
		}
		
		if(currentType == MethodType.COMPLEX) {
			if(methods.size() > 0) {
				nextMethod = methods.get(0);
			}
		}
		
		if(nextMethod != null) {
			methods.remove(nextMethod);
		}
		return nextMethod;
	}
}