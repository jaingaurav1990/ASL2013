package ch.ethz.inf.asl13.user40.middleware;

import javax.jws.WebService;
import ch.ethz.inf.asl13.user40.*;

/**
 * The main endpoint is basically a decorator for the true service.
 * It does some basic parameter wrangling, for example by encoding
 * a <code>null</code> as null message, for JAX-WS does not allow
 * returning nil.
 */
@WebService(endpointInterface = "ch.ethz.inf.asl13.user40.MessagingService")
public class MessagingServiceImpl implements MessagingService {

	private final MessagingService service;

	/**
	 * Initializes a new tracer with the specified back-end service.
	 */
    
	public MessagingServiceImpl(MessagingService service) {
		if (service == null) throw new NullPointerException();

		this.service = service;
	}

    @Override
    public void createClient(Client client) {
        service.createClient(client);
    }

	@Override
	public void insertMessage(Message message) {
		service.insertMessage(message);
	}

	@Override
	public Message getFirstMessage(int queue, Client sender, boolean remove) {
		return nullAsNullMessage(service.getFirstMessage(queue, sender, remove));
	}

	@Override
	public Message getMessageOfHighestPriority(int queue, Client sender, boolean remove) {
		return nullAsNullMessage(service.getFirstMessage(queue, sender, remove));
	}

	@Override
	public int[] listQueues() {
		return service.listQueues();
	}

	@Override
	public int[] listQueuesWithWaitingMessages(Client client) {
		return service.listQueuesWithWaitingMessages(client);
	}

	@Override
	public void createQueue(int queueId) {
		service.createQueue(queueId);
	}

	@Override
	public void deleteQueue(int queue) {
		service.deleteQueue(queue);
	}

	private static Message nullAsNullMessage(Message message) {
		return (message != null) ? message : new Message();
	}
}
