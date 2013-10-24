package ch.ethz.inf.asl13.user40.middleware;

import java.util.*;
import java.util.concurrent.*;
import ch.ethz.inf.asl13.user40.*;

/**
 * A basic, in-memory implementation of <code>MessagingService</code>.
 * This allows for continuing development even while the data store is
 * being developed; and is useful as a stub when unit testing other
 * components of the system.
 */
public class MemoryMessagingService implements MessagingService {

	private final Object queueLock = new Object();
	private final Queue<Message> messages = new ConcurrentLinkedQueue<Message>();
	private int[] queues = new int[0];

    @Override
    public void createClient(Client client) {}

	@Override
	public void insertMessage(Message message) {
		if (message == null) throw new NullPointerException();
		if (message.isNullMessage()) throw new IllegalArgumentException();

		messages.add(message);
	}

	@Override
	public Message getFirstMessage(int queue, Client sender, boolean remove) {
		for (Message m : messages) {
			if (m.queueId == queue &&
				(m.receiver == null ||
				(sender != null && m.receiver.id == sender.id))) 
			{

				if (remove) {
					messages.remove(m);
				}

				return m;
			}
		}

		return null;
	}

	@Override
	public Message getMessageOfHighestPriority(int queue, Client sender, boolean remove) {
		return getFirstMessage(queue, sender, remove);
	}

	@Override
	public int[] listQueues() {
		return queues;
	}

	@Override
	public int[] listQueuesWithWaitingMessages(Client client) {
		return new int[0];
	}

	@Override
	public void createQueue(int queueId) {
		synchronized(queueLock) {
			int[] q = new int[queues.length + 1];
            for (int i = 0; i < queues.length; i++) {
                q[i] = queues[i];
			}
			q[queues.length] = queueId;
			queues = q; // now swap the queues
		}
	}

	@Override
	public void deleteQueue(int queue) {
		synchronized(queueLock) {
			int[] q = new int[queues.length - 1];
			for (int i = 0, j = 0; i < queues.length; i++) {
				if (queues[i] != queue) {
					if (j < i) {
						q[j++] = queues[i];
					} else {
						// if we get here, the queue to be removed is
						// (no longer?) part of queues; no need to delete.
						// -> return without swapping to the new queue
						return;
					}
				}
			}

			queues = q; // the element was found
		}
	}
}
