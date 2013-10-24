package ch.ethz.inf.asl13.user40.client;

import ch.ethz.inf.asl13.user40.*;

/**
 * A <em>request-response</em> client that generates work requests
 * to be processed and answered by the {@link Worker} client.
 */
public final class Dictator extends MessagingServiceClient {

	private final String commandText;

	private Dictator(String clientID) {
		super(clientID, Worker.RESULT_QUEUE);

		commandText = createMessage().text + " DO SOMETHING NOW!";
	}

	@Override
	protected void beforeSendingMessage(Message message) {
		message.receiver = null; // to all
		message.text = commandText;
		message.queueId = Worker.REQUEST_QUEUE;
	}

	/**
	 * Entry point for the application.
	 * The first argument identifies this <em>Dictator</em>.
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("usage: java " + Dictator.class.getName() + " clientID");
			System.exit(1);
		}

		Dictator app = new Dictator(args[0]);

		Message m = app.createMessage();
		app.sendMessagesIndefinitely(m);
	}
}