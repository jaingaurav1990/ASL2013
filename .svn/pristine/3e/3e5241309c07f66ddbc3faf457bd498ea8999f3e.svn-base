package ch.ethz.inf.asl13.user40;

import org.junit.Test;
import static org.junit.Assert.*;

public class MessageTest {

	@Test
	public void isNullMessage() {
		Message m = new Message();
		assertTrue(m.isNullMessage());

		m.text = "";
		assertFalse(m.isNullMessage());
	}

	@Test
	public void toStringWithContent() {
		Message m = new Message();
		m.text = "Yo";
		m.priority = 3;
		assertEquals("Message on queue 0, priority 3:, from null, for all: \"Yo\"", m.toString());
	}

	@Test
	public void toStringNullMessage() {
		assertTrue(new Message().toString().length() > 10);
	}
}