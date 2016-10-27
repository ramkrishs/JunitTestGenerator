package core;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import model.Method;

public class SimpleVisitor extends ASTVisitor {
	
	private ArrayList<Method> methods = new ArrayList<Method>();

	public boolean visit(MethodDeclaration node) {
		Method method = new Method();
		
		method.setName(node.getName());

		for (Object object : node.parameters()) {
			method.put(((SingleVariableDeclaration) object).getType());
		}
		
		if(node.getReturnType2() != null) {
			method.setReturnType(node.getReturnType2());
		}
		
		methods.add(method);
		return super.visit(node);
	}

	public ArrayList<Method> getMethods() {
		return methods;
	}

	public void setMethods(ArrayList<Method> methods) {
		this.methods = methods;
	}
}