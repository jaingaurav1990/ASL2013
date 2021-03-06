package ch.ethz.inf.asl13.user40.util;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import ch.ethz.inf.asl13.user40.*;

/**
 * A decorator for the messaging service that adds tracing and logging capabilities.
 */
@WebService(endpointInterface = "ch.ethz.inf.asl13.user40.MessagingService")
public final class RequestTracer implements MessagingService {

	/** Specifies the number of messages to log before entering silent mode. */
	private static final int MESSAGES_TO_LOG = 100;

	private static final Logger logger = Logger.getLogger(RequestTracer.class.getName());

	private final MessagingService service;
	private final AtomicLong insertCount = new AtomicLong();

	/**
	 * Initializes a new tracer with the specified back-end service.
	 */
	public RequestTracer(MessagingService service) {
		if (service == null) throw new NullPointerException();

		this.service = service;
	}

    @Override
    public void createClient(final Client client) {
        Stopwatch.runAndMeasure(new Function<Void>() {
            @Override
            public Void execute() {
                service.createClient(client);
                return null;
            }
        });


    }
	@Override
	public void insertMessage(final Message message) {
		Stopwatch.runAndMeasure(new Function<Void>() {
			@Override
			public Void execute() {
				service.insertMessage(message);
				return null;
			}
		});

		log(message);
	}

	@Override
	public Message getFirstMessage(final int queue, final Client sender, final boolean remove) {
		Message message = Stopwatch.runAndMeasure(new Function<Message>() {
			@Override
			public Message execute() {
				return service.getFirstMessage(queue, sender, remove);
			}
		});

		log(message);
		return message;
	}

	@Override
	public Message getMessageOfHighestPriority(final int queue, final Client sender, final boolean remove) {
		Message message =  Stopwatch.runAndMeasure(new Function<Message>() {
			@Override
			public Message execute() {
				return service.getMessageOfHighestPriority(queue, sender, remove);
			}
		});

		log(message);
		return message;
	}

	@Override
	public int[] listQueues() {
		return Stopwatch.runAndMeasure(new Function<int[]>() {
			@Override
			public int[] execute() {
				return service.listQueues();
			}
		});
	}

	@Override
	public int[] listQueuesWithWaitingMessages(final Client client) {
		return Stopwatch.runAndMeasure(new Function<int[]>() {
			@Override
			public int[] execute() {
				return service.listQueuesWithWaitingMessages(client);
			}
		});
	}

	@Override
	public void createQueue(final int queueId) {
		Stopwatch.runAndMeasure(new Function<Void>() {
			@Override
			public Void execute() {
				service.createQueue(queueId);
                return null;
			}
		});
	}

	@Override
	public void deleteQueue(final int queue) {
		Stopwatch.runAndMeasure(new Function<Void>() {
			@Override
			public Void execute() {
				service.deleteQueue(queue);
				return null;
			}
		});
	}

	private void log(Message message)
	{
		long n = insertCount.incrementAndGet();
		if (n < MESSAGES_TO_LOG && message != null) {
			logger.log(Level.FINE, message.toString());
		}
		if (n % 1000 == 0) {
			logger.log(Level.INFO, "{0} messages processed", n);
		}
	}
}
