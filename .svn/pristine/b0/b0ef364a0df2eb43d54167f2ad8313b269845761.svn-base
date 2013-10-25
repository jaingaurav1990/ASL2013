package ch.ethz.inf.asl13.user40.util;

import java.io.*;
import java.text.*;
import java.util.Date;
import java.util.logging.*;

/**
 * A {@link Formatter} that writes each entry onto a single line.
 * No longer be necessary in Java 7, which supports setting a log
 * format on the built-in <code>SimpleFormatter</code>.
 */
public class SingleLineFormatter extends Formatter {

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z ");
	private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

	@Override
	public synchronized String format(LogRecord record) {
		StringBuilder sb = new StringBuilder();

		sb.append(dateFormat.format(new Date(record.getMillis())));
		sb.append(' ');
		sb.append(record.getLevel().getLocalizedName());
		sb.append(": ");
		sb.append(formatMessage(record));
		sb.append(LINE_SEPARATOR);

		if (record.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			}
			catch (Exception e) {
				// ignore
			}
		}

		return sb.toString();
	}
}