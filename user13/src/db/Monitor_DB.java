package db;

import helperClasses.Constants;

public class Monitor_DB implements Runnable, Constants {
	private int seconds;
	private boolean run = true;

	public Monitor_DB(int delay) {
		this.seconds = delay;
	}

	public static void main(String[] args) {
		// start the monitoring thread
		Monitor_DB monitor = new Monitor_DB(monitor_delay);
		Thread monitorThread = new Thread(monitor);
		monitorThread.start();
	}

	public void shutdown() {
		this.run = false;
	}

	@Override
	public void run() {
		while (run) {
			// Query to DB for the list of queues and number of messages per queue.
			// Show the results as a table
//			System.out.println(String.format();
			try {
				Thread.sleep(seconds * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
}