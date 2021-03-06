package ch.ethz.inf.asl13.user40;

import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import ch.ethz.inf.asl13.user40.middleware.*;
import ch.ethz.inf.asl13.user40.util.RequestTracer;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class MessagingServiceTest {

	public static final int ALL_RECVEIVERS = -1;

	@Parameters(name = "{index}: service={0}")
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ new MemoryMessagingService() },
			{ new RequestTracer(new MemoryMessagingService()) },
            { new DatabaseServicePreparedStatement() },
		});
	}

	/** Initializes the test with the specified service under test. */
	public MessagingServiceTest(MessagingService service) {
		this.service = service;
	}

	/** The service under test. */
	private static MessagingService service;

	private static final int q1 = 1;
	private static final int q2 = 2;
	private static final int q3 = 3;

    private Message msg1;
    private Message msg2;
    private Message msg3;
    private Message msg4;
    private Message msg5;
    private Message msg6;

	public void cleanup() {
		for (int q : service.listQueues()) {
			service.deleteQueue(q);
		}

	}

    @BeforeClass
    public static void initializeClients() {

        if (service instanceof DatabaseServicePreparedStatement) {
            System.out.println("Executing DatabaseService");
            Client client1 = new Client(7, true, true);
            Client client2 = new Client(3, true, true);
            service.createClient(client1);
            service.createClient(client2);
        }
    }

	@Before
	public void initialize() {
		cleanup();

		// create users

		// create queues
		service.createQueue(q1);
		service.createQueue(q2);
		service.createQueue(q3);

		// create messages
		msg1 = insertTestMessage("TEST_MESSAGE1", q1, 2, 7, ALL_RECVEIVERS);
		msg2 = insertTestMessage("TEST_MESSAGE2", q1, 3, 7, 3);
		msg3 = insertTestMessage("TEST_MESSAGE3", q1, 1, 7, 3);

		msg4 = insertTestMessage("TEST_MESSAGE4", q2, 2, 7, ALL_RECVEIVERS);
		msg5 = insertTestMessage("TEST_MESSAGE5", q2, 3, 7, ALL_RECVEIVERS);
		msg6 = insertTestMessage("TEST_MESSAGE6", q2, 1, 7, ALL_RECVEIVERS);
	}

    
	@Test
	public void getFirstMessageFromEmptyQueue() {
		assertNull(service.getFirstMessage(q3, null, false));
		assertNull(service.getFirstMessage(q3, null, true));
	}

    @Test
    public void getMessageOfHighestPriorityFromEmptyQueue() {
        assertNull(service.getMessageOfHighestPriority(q3, null, false));
        assertNull(service.getMessageOfHighestPriority(q3, null, true));
    }
    
    /*
    @Test
    public void getFirstMessageFromQueueWithItems() {
        Client from = new Client(7, true, true);
        Message message = service.getFirstMessage(q1, from, false);  
        assertMessageEquals(msg1, message);
        //assertEquals(2, getNumberOfMessages(q1));
        message = service.getFirstMessage(q2, from, false);
        assertMessageEquals(msg4, message);
        //assertEquals(3, getNumberOfMessagesInQueue(q2));

    }

    @Test
    public void getMessageOfHighestPriorityFromQueueWithItems() {
        Client from = new Client(7, true, true);
        Message message = service.getMessageOfHighestPriority(q1, from, false);
        assertMessageEquals(msg2, message);
        message = service.getMessageOfHighestPriority(q2, from, false);
        assertMessageEquals(msg5, message);
    }
    */

	@Test
	public void listQueues() {
		assertQueue(q1,q2,q3);
	}

	@Test
	public void deleteQueue() {
		service.deleteQueue(q1);
		assertQueue(q2, q3);
	}

	@Test
	public void deleteNonExistingQueue() {
		service.deleteQueue(-1);
		assertQueue(q1, q2, q3);
	}

	@Test
	public void createQueue() {
        int q4 = 4;
        service.createQueue(q4);
		assertQueue(q1, q2, q3, q4);
	}

	private Message insertTestMessage(String text, int queueId, int priority, int senderId, int receiverId) {
		Message m = new Message();
		m.text = text;
		m.queueId = queueId;
		m.priority = priority;
		m.sender = createClient(senderId);
		if (receiverId != ALL_RECVEIVERS) {
			m.receiver = createClient(senderId);
		}

		service.insertMessage(m);
        return m;
	}

	/*
	 * Factory method that creates a new client instance using
	 * the ID's low bits to infer send and receive permissions.
	 */
	public static Client createClient(int clientId) {
		Client c = new Client();
		c.id = clientId;
		c.canSend = (clientId & 1) == 1; // infer read permission from lowest bit
		c.canReceive = (clientId & 2) == 2; // infer write permission
		return c;
	}

	/**
	 * Asserts that the message is considered the <em>null message</em>.
	 */
	public static void assertNullMessage(Message message) {
		assertNotNull(message);
		assertTrue(message.isNullMessage());
	}

	/**
	 * Asserts that the service contains the expected set of queues.
	 */
	private void assertQueue(int... expected) {
		assertArrayElements(expected, service.listQueues());
	}

	/**
	 * Asserts that an array is of the expected length and contains all
	 * expected elements.
	 * If the <var>expecteds</var> contains no duplicates, this also means
	 * that the <var>actuals</var> array contains no duplicates, and that
	 * it contains no other elements that those in <var>expecteds</var>.
	 */
	public static void assertArrayElements(int[] expecteds, int[] actuals) {
		assertNotNull(actuals);
		assertEquals(expecteds.length, actuals.length);
		for (int e : expecteds) {
			assertArrayContains(e, actuals);
		}
	}

    public static void assertMessageEquals(Message expected, Message actual) {
        assertEquals(expected.text, actual.text);
        assertEquals(expected.priority, actual.priority);
        assertEquals(expected.queueId, actual.queueId);
    }
	/**
	 * Asserts that an array contains the specified element at least once.
	 */
	public static void assertArrayContains(int expected, int... actuals) {
		for (int x : actuals) {
			if (x == expected)
				return;
		}

		fail("Could not find element " + expected + " in the array.");
	}
}
