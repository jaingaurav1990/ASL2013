package ch.ethz.inf.asl13.user40.client;

import java.util.*;
import ch.ethz.inf.asl13.user40.*;

/**
 * A <em>one way</em> client that sends a message and passes it
 * on to a randomly selected other client.
 */
public final class MessagePasser extends MessagingServiceClient {

	private static final int MESSAGE_PASSER_QUEUE = 2;

	private static Random random = new Random();
	private final List<Client> clients = new ArrayList<Client>();
	private Message message = new Message();

	private MessagePasser(String clientID) {
		super(clientID, MESSAGE_PASSER_QUEUE);
        System.out.println("Message Passer starting with clientID:" + clientID);
	}

	/**
	 * Selects a random client to send the message to.
	 */
	private Client getNextRandomClient() {
		return clients.get(random.nextInt(clients.size()));
	}

	/**
	 * Changes the receiver to a random one of the list of chat partners.
	 */
	@Override
	protected void beforeSendingMessage(Message message) {
		message.receiver = getNextRandomClient();
	}

	/**
	 * Entry point for the application.
	 * The first argument identifies this <em>MessagePasser</em>.
	 * All further arguments, there musst be at least one, identify the chat partners
	 * this <em>MessagePasser</em> sends messages to. If the list of chat partners
	 * contains this <em>MessagePasser</em>'s ID, it will be silently ignored.
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("usage: java " + MessagePasser.class.getName() + " clientID receiver [receiver...]");
			System.exit(1);
		}

		MessagePasser app = new MessagePasser(args[0]);
		for (int i = 1; i < args.length; i++) {
			Client c = new Client(args[i]);
			if (c.id != app.getClient().id) {
				app.clients.add(c);
			}
		}

		Message m = app.createMessage();
		m.receiver = app.getNextRandomClient();
		m.priority = 4;

		app.sendMessagesIndefinitely(m);
	}
}
