package ch.ethz.inf.asl13.user40.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import ch.ethz.inf.asl13.user40.*;
import ch.ethz.inf.asl13.user40.middleware.Server;

/**
 * Provides access to the messaging service.
 */
public class MessagingServiceClient {

	private static final Logger logger = Logger.getLogger(MessagingServiceClient.class.getName());

	private final MessagingService messagingService = getPort();
	private final Client client;
	private final int queueId;

	public MessagingServiceClient(String clientID, int queueId) {
		this.client = new Client(clientID);
		this.queueId = queueId;
	}

	public Client getClient() {
		return client;
	}

    public int getQueueId() {
        return queueId;
    }

	private void sendIfNotNull(Message message) {
		if (message != null && !message.isNullMessage()) {
			message.sender = client;
			messagingService.insertMessage(message);
            //System.out.println(client.id + ": " + "Sending message " + message);
		}
	}

	/**
	 * Creates a new message containing the class name client ID.
	 */
	public Message createMessage() {
		return new Message(toString(), client.id % 9, queueId);
	}

	/**
	 * Waits for a message on the queue and sends it back to the origin.
	 * The {@link #beforeSendingMessage(Message)} method is called before
	 * sending to allow for modifying the message content.
	 *
	 * @param initialMessage If set, this message is sent before waiting
	 *        for messages to start the ball rolling.
	 */
	public void sendMessagesIndefinitely(Message initialMessage) {
		logger.log(Level.INFO, "Start {0}…", this);

		sendIfNotNull(initialMessage);

		while (true) {
			Message message = messagingService.getFirstMessage(queueId, client, true);
			if (!message.isNullMessage()) {
				message.receiver = message.sender; // send message back to sender
				message.text += "," + client.id;	
                System.out.println(client.id + ": " + message.text);

				beforeSendingMessage(message);
				sendIfNotNull(message);
			} else {
				try {
                    System.out.println(client.id + ": " + "Going to sleep as it received null message");
					Thread.sleep(50);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

    /*
     * This method is overriden by specific type of
     * clients. Default implementation does nothing.
     */ 
    public void sendMessagesIndefinitelyWithoutReceiving(Message initialMessage) {
        logger.log(Level.INFO, "Start {1}...", this);
        Message message = initialMessage;
        while (true) {
            if (!message.isNullMessage()) {
                int counter = Integer.parseInt(message.text);
                counter += 1;
                message.text = Integer.toString(counter);

                beforeSendingMessage(message);
                sendIfNotNull(message);
            }
            try {
                Thread.sleep(5000);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	/**
	 * Called before a message is being sent.
	 * The default implementation is empty.
	 *
	 * @param message The message that is subject to being sent.
	 */
	protected void beforeSendingMessage(Message message) {}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + client.id;
	}

	/**
	 * Returns a proxy the messaging service, located at the configured address.
	 */
	public static MessagingService getPort() {
		String address = Server.getServiceUrl();
		try {
			URL url = new URL(address);
			QName qname = new QName(MessagingService.NAMESPACE, MessagingService.SERVICE_NAME);
			Service service = Service.create(url, qname);
			return service.getPort(MessagingService.class);
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		}
	}
}
