package com.bm;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;

public class ClasspathTest {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ClasspathTest.class);

	@Test
	public void testSearchStringString() {
		try {
			URL[] url = Classpath.search("META-INF", "persistence.xml");
			logger
					.info("Found "
							+ url.length
							+ " matching instances of persistence.xml on the classpath");
			assertTrue("Looking for at least one class in the persistence.xml",
					url.length > 0);
			for (URL currentUrl : url) {
				logger.info("Found instances of persistence.xml "
						+ currentUrl.toString());
			}
		} catch (IOException e) {
			fail("Unable to find persistence.xml on the classpath" + e.toString());
		}
	}
}
