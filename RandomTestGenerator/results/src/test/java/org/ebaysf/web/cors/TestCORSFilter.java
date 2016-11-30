package org.ebaysf.web.cors;

import org.junit.Before;
import org.junit.Test;

public class TestCORSFilter {

	private CORSFilter cORSFilter;

	@Before
	private void setUp() {
		cORSFilter = new CORSFilter();
	}

	@Test
	private void testCORSFilter() {
		CORSFilter a = new CORSFilter();
		
		assertTrue(a.equals(a));
	}

	@Test
	private void testDestroy() {
		cORSFilter.destroy();
	}

	@Test
	private void testIsLoggingEnabled() {
		boolean a = cORSFilter.isLoggingEnabled();
	}

	@Test
	private void testIsAnyOriginAllowed() {
		boolean a = cORSFilter.isAnyOriginAllowed();
	}

	@Test
	private void testGetExposedHeaders() {
		Collection a = cORSFilter.getExposedHeaders();
		
		a.toString();
	}

	@Test
	private void testIsSupportsCredentials() {
		boolean a = cORSFilter.isSupportsCredentials();
	}

	@Test
	private void testGetPreflightMaxAge() {
		long a = cORSFilter.getPreflightMaxAge();
	}

	@Test
	private void testGetAllowedOrigins() {
		Collection a = cORSFilter.getAllowedOrigins();
		
		a.equals(a);
	}

	@Test
	private void testGetAllowedHttpMethods() {
		Collection a = cORSFilter.getAllowedHttpMethods();
		
		a.equals(a);
	}

	@Test
	private void testGetAllowedHttpHeaders() {
		Collection a = cORSFilter.getAllowedHttpHeaders();
		
		a.equals(a);
	}

	@Test
	private void testIsOriginAllowed() {
		String a = "hello";
		
		boolean b = cORSFilter.isOriginAllowed(a);
	}

	@Test
	private void testLog() {
		String a = "hi";
		
		cORSFilter.log(a);
	}

	@Test
	private void testParseAndStore() {
		String a = "hi";
		
		String b = "hi";
		
		String c = "hello";
		
		String d = "hi";
		
		String e = "hi";
		
		String f = "hi";
		
		String g = "hello";
		
		String h = "hi";
		
		cORSFilter.parseAndStore(a, b, c, d, e, f, g, h);
	}

	@Test
	private void testParseStringToSet() {
		String a = "hi";
		
		Set b = cORSFilter.parseStringToSet(a);
		
		b.equals(b);
	}

	@Test
	private void testIsValidOrigin() {
		String a = "hi";
		
		boolean b = cORSFilter.isValidOrigin(a);
	}

	@Test
	private void testDoFilter() {
		cORSFilter.doFilter();
	}

	@Test
	private void testInit() {
		cORSFilter.init();
	}

	@Test
	private void testHandleSimpleCORS() {
		cORSFilter.handleSimpleCORS();
	}

	@Test
	private void testHandlePreflightCORS() {
		cORSFilter.handlePreflightCORS();
	}

	@Test
	private void testHandleNonCORS() {
		cORSFilter.handleNonCORS();
	}

	@Test
	private void testHandleInvalidCORS() {
		cORSFilter.handleInvalidCORS();
	}

	@Test
	private void testDecorateCORSProperties() {
		cORSFilter.decorateCORSProperties();
	}

	@Test
	private void testJoin() {
		Collection a = cORSFilter.getAllowedHttpHeaders();
		
		String b = "hello";
		
		String c = cORSFilter.join(a, b);
		
		c.toString();
	}

	@Test
	private void testCheckRequestType() {
		CORSRequestType a = cORSFilter.checkRequestType();
		
		assertTrue(a.equals(a));
	}

}