package ch.ethz.inf.asl13.user40.client;

import ch.ethz.inf.asl13.user40.*;

/**
 * A <em>request-response</em> client that processes and answers
 * work requests sent by the {@link Dictator} client.
 */
public final class Worker extends MessagingServiceClient {

	public static final int REQUEST_QUEUE = 56;
	public static final int RESULT_QUEUE = REQUEST_QUEUE + 1;

	private Worker(String clientID) {
		super(clientID, REQUEST_QUEUE);
	}

	@Override
	protected void beforeSendingMessage(Message message) {

		// Simulate working
		try {
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
		}

		message.queueId = RESULT_QUEUE;
		message.text += "Done by Worker " + getClient();
	}

	/**
	 * Entry point for the application.
	 * The first argument identifies this <em>Worker</em>.
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("usage: java " + Worker.class.getName() + " clientID");
			System.exit(1);
		}

		Worker app = new Worker(args[0]);

		app.sendMessagesIndefinitely(null);
	}
}