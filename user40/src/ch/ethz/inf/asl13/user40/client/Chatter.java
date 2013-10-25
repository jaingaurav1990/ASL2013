package ch.ethz.inf.asl13.user40.client;

import ch.ethz.inf.asl13.user40.*;

/**
 * A <em>request-response</em> client that sends chat messages
 * and responds to them.
 */
public final class Chatter extends MessagingServiceClient {

	private Chatter(String clientID) {
		super(clientID, 4);
	}

	/**
	 * Entry point for the application.
	 * The first argument identifies this <em>Chatter</em>.
	 * The second argument identifies the chat partner to send an initial message to.
	 * If unspecified, this chatter will wait for such a message.
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("usage: java " + Chatter.class.getName() + " clientID [partnerID]");
			System.exit(1);
		}

		Chatter app = new Chatter(args[0]);

		Message m;
		if (args.length > 1) {
			m = app.createMessage();
			m.receiver = new Client(args[1]);
		} else {
			m = null;
		}

		app.sendMessagesIndefinitely(m);
	}
}