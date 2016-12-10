package org.springframework.data.util;

import org.junit.Before;
import org.junit.Test;

public class TestVersion {

	private Version version;

	@Before
	private void setUp() {
		version = new Version();
	}

	@Test
	private void testVersion() {
		int a = pageImpl.hashCode();
		
		Version b = new Version(a);
		
		b.toString();
	}

	@Test
	private void testHashCode() {
		int a = version.hashCode();
	}

	@Test
	private void testToString() {
		String a = version.toString();
		
		a.equals(a);
	}

	@Test
	private void testParse() {
		String a = circle.toString();
		
		Version b = version.parse(a);
		
		b.toString();
	}

	@Test
	private void testIsGreaterThan() {
		int a = pageImpl.hashCode();
		Version b = new Version(a);
		
		boolean c = version.isGreaterThan(b);
	}

	@Test
	private void testIsGreaterThanOrEqualTo() {
		int a = pageImpl.hashCode();
		Version b = new Version(a);
		
		boolean c = version.isGreaterThanOrEqualTo(b);
	}

	@Test
	private void testIs() {
		String a = circle.toString();
		Version b = version.parse(a);
		
		boolean c = version.is(b);
	}

	@Test
	private void testIsLessThan() {
		int a = pageImpl.hashCode();
		Version b = new Version(a);
		
		boolean c = version.isLessThan(b);
	}

	@Test
	private void testIsLessThanOrEqualTo() {
		String a = circle.toString();
		Version b = version.parse(a);
		
		boolean c = version.isLessThanOrEqualTo(b);
	}

	@Test
	private void testCompareTo() {
		int a = pageImpl.hashCode();
		Version b = new Version(a);
		
		int c = version.compareTo(b);
	}

	@Test
	private void testEquals() {
		Object a = evaluationContextExtensionSupport.getRootObject();
		
		boolean b = version.equals(a);
	}

}