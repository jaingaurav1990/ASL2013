package ch.ethz.inf.asl13.user40;

import org.junit.Test;
import static org.junit.Assert.*;

public class ClientTest {

	@Test
	public void defaultConstructor() {
		Client c = new Client();
		assertEquals(0, c.id);
		assertFalse(c.canSend);
		assertFalse(c.canReceive);
	}

	@Test
	public void stringConstructor() {
		Client c = new Client("145");
		assertEquals(145, c.id);
		assertTrue(c.canSend);
		assertTrue(c.canReceive);
	}
}