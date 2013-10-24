package ch.ethz.inf.asl13.user40;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

/**
 * Defines the messaging service.
 */
@WebService
@SOAPBinding(style = Style.RPC)
public interface MessagingService {

	/** The port and service namespace. */
	public final static String NAMESPACE = "http://service.user40.asl13.inf.ethz.ch/";

	/** Identifies the port. */
	public final static String PORT_NAME = "MessagingServicePort";

	/** Identifies the service. */
	public final static String SERVICE_NAME = "MessagingService";

	/**
	 * Sends a message to the messaging system.
	 * @param message the message to be written to the queue.
	 */
	@WebMethod
	void insertMessage(Message message);

	/**
	 * Gets the oldest message waiting in the queue regardless of its
	 * priority.
	 *
	 * @param queue the queue to look at.
	 * @param sender the client to fetch messages for.
	 * @param remove if <code>true</code>, the message will be removed after
	 *        being read; it will be left on the queue otherwise.
	 *
	 * @return The message; or <code>null</code> if there are no messages
	 *         waiting for this client.
	 */
	@WebMethod
	Message getFirstMessage(int queue, Client sender, boolean remove);

	/**
	 * Gets the message of highest priority in the queue.
	 * If there are multiple messages of highest priority waiting,
	 * the oldest such message is returned.
	 *
	 * @param queue the queue to look at.
	 * @param sender the client to fetch messages for.
	 * @param remove if <code>true</code>, the message will be removed after
	 *        being read; it will be left on the queue otherwise.
	 *
	 * @return The message; or <code>null</code> if there are no messages
	 *         waiting for this client.
	 */
	@WebMethod
	Message getMessageOfHighestPriority(int queue, Client sender, boolean remove);

	/**
	 * Lists all queues in the system.
	 * @return a list of all queues (in no particular order).
	 */
	@WebMethod
	int[] listQueues();

	/**
	 * Lists all queues with waiting messages for the specified client.
	 * @return a list of all queues that have one or more messages waiting
	 *         for this client.
	 */
	@WebMethod
	int[] listQueuesWithWaitingMessages(Client client);

	/**
	 * Creates a new queue in the system.
	 * @param queueId Identifies the queue.
	 */
	@WebMethod
	void createQueue(int queueId);

	/**
	 * Deletes the specified queue from the system.
	 * @param queue the queue to delete.
	 */
	@WebMethod
	void deleteQueue(int queue);

    void createClient(Client client);
}
