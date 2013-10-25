package ch.ethz.inf.asl13.user40.util;

/**
 * A method that returns a value.
 * Incorporates some aspected of the command pattern
 * and  of .NET's <code>Func of T</code> delegate.
 */
public interface Function<T> {

	/**
	 * Executes this function.
	 * @return The wrapped method's return value.
	 */
	T execute();
}