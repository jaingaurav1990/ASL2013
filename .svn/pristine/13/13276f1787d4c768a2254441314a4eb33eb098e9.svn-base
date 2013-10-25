package ch.ethz.inf.asl13.user40.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class StopwatchTest {

	@Test(expected = NullPointerException.class)
	public void runAndMeasureThrowsOnNull() {
		Stopwatch.runAndMeasure(null);
	}

	@Test
	public void runAndMeasureReturnsTheFunctionsReturnValue() {
		assertSame(o, Stopwatch.runAndMeasure(new F()));
	}

	@Test
	public void runAndMeasureExecutesFunctionExactlyOncePerCall() {
		F f = new F();

		Stopwatch.runAndMeasure(f);
		assertEquals(1, f.count);

		Stopwatch.runAndMeasure(f);
		assertEquals(2, f.count);
	}

	@Test(expected = RuntimeException.class)
	public void runAndMeasureRethrowsException() {
		Stopwatch.runAndMeasure(new Function<Void>() {
			@Override
			public Void execute() {
				throw new RuntimeException();
			}
		});
	}

	private static final Object o = new Object();

	private static class F implements Function<Object> {
		int count = 0;

		@Override
		public Object execute() {
			count++;
			return o;
		}
	}
}