package core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import model.Method;
import model.MethodType;
import model.Sequence;
import util.Util;
import util.ValuePool;

public class TestGenerator {
	
	private static MethodType currentType = MethodType.CONSTRUCTOR;
	private static ArrayList<Method> methods;
	private static int methodNumber = 0;
	private static int invalidCount = 0;
	
	public static void main(String args[]) throws Exception {
		
		Scanner scanner = new Scanner(System.in);
		System.out.print("Please input execution limit (in seconds): ");
		float executionLimit = scanner.nextFloat();
		scanner.close();
		
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
			
			if(!isClass(originalContent))
				continue;
			
			//Get name of the file
			String className = Util.getFileName(file);
			
			//System.out.println(className);
			
			if(executionLimit*1000 > System.currentTimeMillis() - startTime) {
				generateTestCases(className, testTemplate, originalContent);
			} else {
				break;
			}
		}
		
		System.out.println("Invalid: "+invalidCount);
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
		//Generate test class
		String testClass = generateClass(testTemplate, className, getPackage(originalContent));
		
		//Identify methods
		methods = identifyMethods(originalContent);
		
		boolean processed = false;
		
		while(hasNext()) {
			Method method = getNext();
			
			testClass = generateTest(testClass, method, className);
			
//			ValuePool.resetValuePool();
			Util.resetVar();
			
			System.out.println(++methodNumber + ":"+ method.toString());
			currentType = MethodType.CONSTRUCTOR;
			processed = true;
		}
		
		if(processed) {
			//System.out.println(testClass+"\n\n\n\n\n");
			//Generate file
			
			String originalPath = "";
			
			if(getPackage(originalContent) != null)
			for (String pathElement : getPackage(originalContent)) {
				originalPath += "/"+pathElement;
			}
			File directory = new File("results\\src\\test\\java" + originalPath);
			
			File cleanDirectory = new File("results");
			if(cleanDirectory.exists())
				FileUtils.deleteDirectory(cleanDirectory);

			directory.mkdirs();
			
			File file = new File(directory.getAbsolutePath()+"\\Test" + className + ".java");
			FileUtils.writeStringToFile(file, testClass);
			
		}
	}
	
	private static ArrayList<Method> identifyMethods(String content) {
		CompilationUnit unit = parseAST(content);
		
		SimpleVisitor visitor = new SimpleVisitor();
		unit.accept(visitor);
		
		ArrayList<Method> methods = visitor.getMethods();
		
		HashMap<String, Integer> map = new HashMap<>();
		
		for (Method method : methods) {
			
			if(method.getReturnType() != null && !"void".equals(method.getReturnType().toString())) {
				String returnType = method.getReturnType().toString();
				
				if(map.containsKey(returnType)) {
					invalidCount++;
					map.put(returnType, map.get(returnType) + 1);
				} else {
					map.put(returnType, 1);
				}
			}
			
		}
		
		return methods;
	}
	
	private static String generateTest(String content, Method method, String className) throws Exception {
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
		ListRewrite listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);		
		
		//Save statements created in method body (might be saved to ValuePool)
		List<Object> statements = new ArrayList<>();
		
//		2) CREATE METHOD INVOCATION
		MethodInvocation methodInvocation = null;
		ClassInstanceCreation classInvocation = null;
		
		if(method.getReturnType() == null) {
			//constructor
			classInvocation = ast.newClassInstanceCreation();
			classInvocation.setType(ast.newSimpleType(ast.newSimpleName(method.getName().toString())));
		} else {
			if("void".equals(method.getReturnType())) {
				//no return
				methodInvocation = ast.newMethodInvocation();
				methodInvocation.setName(ast.newSimpleName(method.getName().toString()));
				methodInvocation.setExpression(ast.newSimpleName(Util.lowerFirstChar(className)));
			} else {
				//with returns
				methodInvocation = ast.newMethodInvocation();
				methodInvocation.setName(ast.newSimpleName(method.getName().toString()));
				methodInvocation.setExpression(ast.newSimpleName(Util.lowerFirstChar(className)));
			}
		}
		
//		3) GENERATE PARAMETERS
		for (Integer key : method.getArgumentTypes().keySet()) {
			Type type = method.getArgumentTypes().get(key);
			
			//Generate primitive variable and include inside method call as parameter
			if(type.isPrimitiveType()) {
				PrimitiveType primitiveType = (PrimitiveType) type;
				
				Sequence sequence = ValuePool.getType(primitiveType.toString());
				
				
				String value = sequence.getStatements().get(sequence.getStatements().size()-1).toString();
				Object obj = sequence.getStatements().get(sequence.getStatements().size()-1);
				
				
				//list<statmt> --------> obj
				if(obj instanceof VariableDeclarationStatement) {
					VariableDeclarationStatement sequenceDeclarationStmt = (VariableDeclarationStatement) obj;
					VariableDeclarationFragment sequenceVarName = (VariableDeclarationFragment) sequenceDeclarationStmt.fragments().get(0);
					
					VariableDeclarationStatement newVarStmt = ast.newVariableDeclarationStatement(ast.newVariableDeclarationFragment());
					newVarStmt = (VariableDeclarationStatement) ASTNode.copySubtree(newVarStmt.getAST(), sequenceVarName.getRoot());
					
					VariableDeclarationFragment newVarName = (VariableDeclarationFragment) newVarStmt.fragments().get(0);
					newVarName.setName(ast.newSimpleName(Util.getNextVar()));
					
					listRewrite.insertLast(newVarStmt, null);
					statements.add(newVarStmt);
					
					if(methodInvocation != null) {
						methodInvocation.arguments().add(ast.newSimpleName(newVarName.getName().toString()));
					} else {
						classInvocation.arguments().add(ast.newSimpleName(newVarName.getName().toString()));
					}
					//Insert empty line before after parameter variables
					Statement placeHolder = (Statement) rewriter.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
					listRewrite.insertLast(placeHolder, null);
					
				} else {
					//single value ------> object
					VariableDeclarationFragment argumentVarFrag = ast.newVariableDeclarationFragment();
					argumentVarFrag.setName(ast.newSimpleName(Util.getNextVar()));
					
					if(PrimitiveType.BOOLEAN.equals(primitiveType.getPrimitiveTypeCode())) {
						argumentVarFrag.setInitializer(ast.newBooleanLiteral(Boolean.parseBoolean(value)));
					}
					if(PrimitiveType.BYTE.equals(primitiveType.getPrimitiveTypeCode())) {
						argumentVarFrag.setInitializer(ast.newNumberLiteral(value));
					}
					if(PrimitiveType.DOUBLE.equals(primitiveType.getPrimitiveTypeCode())
							|| PrimitiveType.FLOAT.equals(primitiveType.getPrimitiveTypeCode())
							|| PrimitiveType.INT.equals(primitiveType.getPrimitiveTypeCode())
							|| PrimitiveType.LONG.equals(primitiveType.getPrimitiveTypeCode())
							|| PrimitiveType.SHORT.equals(primitiveType.getPrimitiveTypeCode())) {
						argumentVarFrag.setInitializer(ast.newNumberLiteral(value));
					}
					if(PrimitiveType.CHAR.equals(primitiveType.getPrimitiveTypeCode())) {
						CharacterLiteral charLiteral = ast.newCharacterLiteral();
						charLiteral.setCharValue(value.charAt(0));
						argumentVarFrag.setInitializer(charLiteral);
					}
					
					
					VariableDeclarationStatement argumentVarStmt = ast.newVariableDeclarationStatement(argumentVarFrag);
					argumentVarStmt.setType(ast.newPrimitiveType(primitiveType.getPrimitiveTypeCode()));
					
					listRewrite.insertLast(argumentVarStmt, null);
					statements.add(argumentVarStmt);
					
					if(methodInvocation != null) {
						methodInvocation.arguments().add(ast.newSimpleName(argumentVarFrag.getName().toString()));
					} else {
						classInvocation.arguments().add(ast.newSimpleName(argumentVarFrag.getName().toString()));
					}
					
					//Insert empty line before after parameter variables
					Statement placeHolder = (Statement) rewriter.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
					listRewrite.insertLast(placeHolder, null);
				}
				
			} else {
				
				if("String".equals(type.toString())) {
					Sequence sequence = ValuePool.getType(type.toString());
					
					String value = sequence.getStatements().get(sequence.getStatements().size()-1).toString();
					Object obj = sequence.getStatements().get(sequence.getStatements().size()-1);
					
					
					if(obj instanceof VariableDeclarationStatement) {
						
						VariableDeclarationStatement sequenceDeclarationStmt = (VariableDeclarationStatement) obj;
						VariableDeclarationFragment sequenceVarName = (VariableDeclarationFragment) sequenceDeclarationStmt.fragments().get(0);
						
						VariableDeclarationStatement newVarStmt = ast.newVariableDeclarationStatement(ast.newVariableDeclarationFragment());
						newVarStmt = (VariableDeclarationStatement) ASTNode.copySubtree(newVarStmt.getAST(), sequenceVarName.getRoot());
						
						VariableDeclarationFragment newVarName = (VariableDeclarationFragment) newVarStmt.fragments().get(0);
						newVarName.setName(ast.newSimpleName(Util.getNextVar()));
						
						listRewrite.insertLast(newVarStmt, null);
						statements.add(newVarStmt);
						
						if(methodInvocation != null) {
							methodInvocation.arguments().add(ast.newSimpleName(newVarName.getName().toString()));
						} else {
							classInvocation.arguments().add(ast.newSimpleName(newVarName.getName().toString()));
						}
						//Insert empty line before after parameter variables
						Statement placeHolder = (Statement) rewriter.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
						listRewrite.insertLast(placeHolder, null);
						
					} else {
						VariableDeclarationFragment argumentVarFrag = ast.newVariableDeclarationFragment();
						argumentVarFrag.setName(ast.newSimpleName(Util.getNextVar()));
					
						StringLiteral stringLiteral = ast.newStringLiteral();
						stringLiteral.setLiteralValue(value);
						argumentVarFrag.setInitializer(stringLiteral);
						
						VariableDeclarationStatement argumentVarStmt = ast.newVariableDeclarationStatement(argumentVarFrag);
						argumentVarStmt.setType(ast.newSimpleType(ast.newSimpleName(type.toString())));
						
						listRewrite.insertLast(argumentVarStmt, null);
						statements.add(argumentVarStmt);
						
						if(methodInvocation != null) {
							methodInvocation.arguments().add(ast.newSimpleName(argumentVarFrag.getName().toString()));
						} else {
							classInvocation.arguments().add(ast.newSimpleName(argumentVarFrag.getName().toString()));
						}
						
						//Insert empty line before after parameter variables
						Statement placeHolder = (Statement) rewriter.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
						listRewrite.insertLast(placeHolder, null);
					}
					
					
				} else {
					//Generate type variable and include inside method call as parameter
					Sequence sequence = ValuePool.getType(type.toString());
					
					if(sequence == null || sequence.getStatements() == null)
						continue;
					
					for(int i = 0; i < sequence.getStatements().size(); i++) {
						
						//if not the last
						if(i < sequence.getStatements().size() - 1) {
							
							//if var creation?
							if(sequence.getStatements().get(i) instanceof VariableDeclarationStatement) {
								//change var name
								VariableDeclarationStatement sequenceDeclarationStmt = (VariableDeclarationStatement) sequence.getStatements().get(i);
								VariableDeclarationFragment sequenceVarName = (VariableDeclarationFragment) sequenceDeclarationStmt.fragments().get(0);
								
								VariableDeclarationStatement newVarStmt = ast.newVariableDeclarationStatement(ast.newVariableDeclarationFragment());
								newVarStmt = (VariableDeclarationStatement) ASTNode.copySubtree(newVarStmt.getAST(), sequenceVarName.getRoot());
								
								VariableDeclarationFragment newVarName = (VariableDeclarationFragment) newVarStmt.fragments().get(0);
								newVarName.setName(ast.newSimpleName(Util.getNextVar()));
								
								listRewrite.insertLast(newVarStmt, null);
								statements.add(newVarStmt);
							} else {
								listRewrite.insertLast((ASTNode) sequence.getStatements().get(i), null);
								statements.add((ASTNode) sequence.getStatements().get(i));
							}
						} else {
							
							//check if last statement contains a variable declaration
							if(sequence.getStatements().get(i).toString().contains("=")) {
								VariableDeclarationStatement sequenceDeclarationStmt = (VariableDeclarationStatement) sequence.getStatements().get(i);
								VariableDeclarationFragment sequenceVarName = (VariableDeclarationFragment) sequenceDeclarationStmt.fragments().get(0);
								
								VariableDeclarationStatement newVarStmt = ast.newVariableDeclarationStatement(ast.newVariableDeclarationFragment());
								newVarStmt = (VariableDeclarationStatement) ASTNode.copySubtree(newVarStmt.getAST(), sequenceVarName.getRoot());
								
								VariableDeclarationFragment newVarName = (VariableDeclarationFragment) newVarStmt.fragments().get(0);
								newVarName.setName(ast.newSimpleName(Util.getNextVar()));
								
								listRewrite.insertLast(newVarStmt, null);
								statements.add(newVarStmt);
								
								if(methodInvocation != null) {
									methodInvocation.arguments().add(ast.newSimpleName(newVarName.getName().toString()));
								} else {
									classInvocation.arguments().add(ast.newSimpleName(newVarName.getName().toString()));
								}
								//Insert empty line before after parameter variables
								Statement placeHolder = (Statement) rewriter.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
								listRewrite.insertLast(placeHolder, null);
								
							} else {
								VariableDeclarationFragment argumentVarFrag = ast.newVariableDeclarationFragment();
								argumentVarFrag.setName(ast.newSimpleName(Util.getNextVar()));
								argumentVarFrag.setInitializer((Expression) sequence.getStatements().get(i));
								
								VariableDeclarationStatement argumentVarStmt = ast.newVariableDeclarationStatement(argumentVarFrag);
								if(type.isParameterizedType()){
									ParameterizedType argType = (ParameterizedType) type;

									SimpleName nameAux = ast.newSimpleName(argType.getType().toString());
									SimpleType simpleTypeAux = ast.newSimpleType(nameAux);
									
									argumentVarStmt.setType(simpleTypeAux);
								}
								if(type.isSimpleType()) {
									argumentVarStmt.setType(ast.newSimpleType(ast.newSimpleName(type.toString())));
								}
								
								listRewrite.insertLast(argumentVarStmt, null);
								statements.add(argumentVarStmt);
								
								if(methodInvocation != null) {
									methodInvocation.arguments().add(ast.newSimpleName(argumentVarFrag.getName().toString()));
								} else {
									classInvocation.arguments().add(ast.newSimpleName(argumentVarFrag.getName().toString()));
								}
								
								//Insert empty line after generating parameter variables
								Statement placeHolder = (Statement) rewriter.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
								listRewrite.insertLast(placeHolder, null);
							}
						}
					}
				}
			}
		}
		
//		4) GENERATE TEST STATEMENT (if any)
		if(method.getReturnType() == null) {
			//constructor
			VariableDeclarationFragment testVarFrag = ast.newVariableDeclarationFragment();
			testVarFrag.setName(ast.newSimpleName(Util.getNextVar()));
			
			if(methodInvocation != null) {
				testVarFrag.setInitializer(methodInvocation);
			} else {
				testVarFrag.setInitializer(classInvocation);
			}
			
			VariableDeclarationStatement testVarStmt = ast.newVariableDeclarationStatement(testVarFrag);
			testVarStmt.setType(ast.newSimpleType(ast.newSimpleName(method.getName().toString())));
			
			listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
			listRewrite.insertLast(testVarStmt, null);
			statements.add(testVarStmt);
			
			//Insert empty line before operation contract
			Statement placeHolder = (Statement) rewriter.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
			listRewrite.insertLast(placeHolder, null);
			
			Expression operationContract = generateOperationContract(testVarFrag.getName().toString(), ast);	
			
			ExpressionStatement contractStmt = ast.newExpressionStatement(operationContract);
			listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
			listRewrite.insertLast(contractStmt, null);
			
			
		} else if(!"void".equals(method.getReturnType().toString())) {

			VariableDeclarationFragment testVarFrag = ast.newVariableDeclarationFragment();
			testVarFrag.setName(ast.newSimpleName(Util.getNextVar()));
			
			if(methodInvocation != null) {
				testVarFrag.setInitializer(methodInvocation);
			} else {
				testVarFrag.setInitializer(classInvocation);
			}
			
			VariableDeclarationStatement testVarStmt = ast.newVariableDeclarationStatement(testVarFrag);
			
			if(method.getReturnType().isPrimitiveType()) {
				PrimitiveType.Code codeAux = PrimitiveType.toCode(method.getReturnType().toString());
				testVarStmt.setType(ast.newPrimitiveType(codeAux));
			} 

			if(method.getReturnType().isParameterizedType()){
				ParameterizedType returnType = (ParameterizedType) method.getReturnType();

				if(method.getReturnType().toString().contains(".")) {
					testVarStmt.setType(ast.newSimpleType(ast.newName(returnType.getType().toString())));
				} else {
					testVarStmt.setType(ast.newSimpleType(ast.newSimpleName(returnType.getType().toString())));
				}
			}
			
			if(method.getReturnType().isSimpleType()) {
				if(method.getReturnType().toString().contains(".")) {
					testVarStmt.setType(ast.newSimpleType(ast.newName(method.getReturnType().toString())));
				} else {
					testVarStmt.setType(ast.newSimpleType(ast.newSimpleName(method.getReturnType().toString())));
				}
			}
			
			listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
			listRewrite.insertLast(testVarStmt, null);
			statements.add(testVarStmt);
			
			//add operation contract (primitive type cannot have)
			if(method.getReturnType().isSimpleType() || method.getReturnType().isParameterizedType()) {
				//Insert empty line before operation contract
				Statement placeHolder = (Statement) rewriter.createStringPlaceholder("", ASTNode.EMPTY_STATEMENT);
				listRewrite.insertLast(placeHolder, null);
				
				Expression operationContract = generateOperationContract(testVarFrag.getName().toString(), ast);
				
				ExpressionStatement contractStmt = ast.newExpressionStatement(operationContract);
				listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
				listRewrite.insertLast(contractStmt, null);
			}
		} else {
			//void methods should not contain variable or operation contract, just the call
			ExpressionStatement expressionStatement = null;
			if(methodInvocation != null) {
				expressionStatement = ast.newExpressionStatement(methodInvocation);
			} else {
				expressionStatement = ast.newExpressionStatement(classInvocation);
			}
			listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
			listRewrite.insertLast(expressionStatement, null);
			statements.add(expressionStatement);
		}
		
//		5) SAVE BLOCK TO VALUE POOL
		if(method.getReturnType() == null) {
			if(statements.size() > 0) {
				
				List<Object> types = new ArrayList<>();
				types.add(method.getName().toString());
				Sequence newSequence = new Sequence(types, statements);
				
				ValuePool.pool.add(newSequence);
			}
		} else {
			if(!"void".equals(method.getReturnType().toString()) && statements.size() > 0) {
				
				List<Object> types = new ArrayList<>();
				types.add(method.getReturnType().toString());
				Sequence newSequence = new Sequence(types, statements);
				
				ValuePool.pool.add(newSequence);
			}
		}
		
		methodDeclaration.setBody(block);
		
		//Insert method into the types of the compilation unit
		TypeDeclaration typeDeclaration = (TypeDeclaration) unit.types().get(0);
		listRewrite = rewriter.getListRewrite(typeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertLast(methodDeclaration, null);
		
		TextEdit edits = rewriter.rewriteAST(document, null);
		edits.apply(document);
		
		return document.get();
	}
	
	private static MethodInvocation generateOperationContract(String varName, AST ast) {
		MethodInvocation operationContract = null;
		
		int randomOC = ThreadLocalRandom.current().nextInt(0, 4);
		
		switch(randomOC) {
//		• o.equals(o)==true / assertTrue(o.equals(o))
			case 0: {
				MethodInvocation equals = ast.newMethodInvocation();
				equals.setExpression(ast.newSimpleName(varName));
				equals.setName(ast.newSimpleName("equals"));
				equals.arguments().add(ast.newSimpleName(varName));
				
				operationContract = ast.newMethodInvocation();
				operationContract.setName(ast.newSimpleName("assertTrue"));
				operationContract.arguments().add(equals);
				
				break;
			}
//		• o.equals(o) throws no exception
			case 1: {
				operationContract = ast.newMethodInvocation();
				operationContract.setExpression(ast.newSimpleName(varName));
				operationContract.setName(ast.newSimpleName("equals"));
				operationContract.arguments().add(ast.newSimpleName(varName));
				
				break;
			}
//		• o.hashCode() throws no exception
			case 2: {
				operationContract = ast.newMethodInvocation();
				operationContract.setExpression(ast.newSimpleName(varName));
				operationContract.setName(ast.newSimpleName("hashCode"));
				
				break;
			}
//		• o.toString() throw no exception
			case 3: {
				operationContract = ast.newMethodInvocation();
				operationContract.setExpression(ast.newSimpleName(varName));
				operationContract.setName(ast.newSimpleName("toString"));
				
				break;
			}
		}
		
		return operationContract;
	}
	
	private static String generateClass(String content, String className, String[] packageName) throws Exception {
		
		CompilationUnit unit = parseAST(content);

		AST ast = unit.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);

		Document document = new Document(content);
		
		// PACKAGE
		if(packageName != null) {
			PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
			Name name = ast.newName(packageName);
			packageDeclaration.setName(name);
			
			rewriter.set(unit, CompilationUnit.PACKAGE_PROPERTY, packageDeclaration, null);
		}
		
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
		
		if(unit.getPackage() == null)
			return null;
		
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
	
	/**
	 * Checks if the final represented by originalContent contains at least 1 class
	 * @param originalContent
	 * @return
	 */
	private static boolean isClass(String originalContent) {
		
		CompilationUnit unit = parseAST(originalContent);
		
		return ((unit.types() == null || unit.types().size() <= 0))?false:true;
	}
}