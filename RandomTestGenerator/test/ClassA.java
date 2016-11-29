package test;

public class ClassA {
	
	public ClassA() {}
	
	public void method1() {
	}
	
	public ClassA method2(ClassA a) {
		return new ClassA();
	}
	
	
	public String liveDemo(int a, int b, double c, ClassA classA, String test) {
		return test;
	}
}
