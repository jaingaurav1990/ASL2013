package ch.ethz.inf.asl13.user40.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Measures execution times of functions.
 */
public class Stopwatch {

	private static final Logger logger = Logger.getLogger(Stopwatch.class.getName());

	/**
	 * Executes the specified function and measures its execution time.
	 * @param function the function to execute; must not be <code>null</code>.
	 */
	public static <T> T runAndMeasure(Function<T> function) {
		if (function == null) throw new NullPointerException();

		Exception exception = null;
		long time = System.nanoTime();
		try {
			return function.execute();
		}
		catch (RuntimeException e) {
			exception = e;
			throw e;
		}
		catch (Exception e) {
			// If we catch a (supposedly) checked exception here, something
			// went very wrong, as the interface (unless it was changed,
			// which should also result in a change to this logic) doesn't
			// declare any exceptions to be thrown.
			// -> Log the exceptional condition as usual, and wrap it in a
			//    new runtime exception that is then thrown instead.
			exception = e;
			throw new RuntimeException(e);
		}
		finally {
			time = (System.nanoTime() - time) / 1000 + 1; // convert to millis rounding up (-ish)

			String name = function.getClass().getName();
			if (exception == null) {
				logger.log(Level.FINE, String.format("%d ms for %s", time, name));
			} else {
				logger.log(Level.FINE, String.format("%d ms for %s with error. %s", time, name, exception));
			}
		}
	}
}