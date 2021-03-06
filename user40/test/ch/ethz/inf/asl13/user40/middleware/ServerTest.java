package ch.ethz.inf.asl13.user40.middleware;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ServerTest {

	private String originalServerUrl;

	@Before
	public void initialize() {
		originalServerUrl = System.getProperty(Server.SERVICE_URL_KEY);
	}

	@After
	public void cleanup() {
		if (originalServerUrl == null) {
			System.clearProperty(Server.SERVICE_URL_KEY);
		} else {
			System.setProperty(Server.SERVICE_URL_KEY, originalServerUrl);
		}
	}

	@Test
	public void getServiceUrlDefault() {
		System.clearProperty(Server.SERVICE_URL_KEY);
		assertEquals("http://localhost:9999/", Server.getServiceUrl());
	}

	@Test
	public void getServiceUrlSystemProperty() {
		System.setProperty(Server.SERVICE_URL_KEY, "http://abcd.org:123/ha");
		assertEquals("http://abcd.org:123/ha", Server.getServiceUrl());
	}
}