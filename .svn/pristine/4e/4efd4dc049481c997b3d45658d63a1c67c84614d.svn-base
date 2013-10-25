package ch.ethz.inf.asl13.user40;

import javax.xml.bind.annotation.*;

/**
 * Represents a message in the system.
 */
@XmlRootElement
public class Message {

	public String text;

	public int priority;

	public int queueId;

	public Client sender;

	public Client receiver;

	public Message() {
        this(null, 0, 0, null, null);
    }

    public Message(String text) {
        this(text, 0, 0, null, null);
    }

	public Message(String text, int priority, int queueId) {
		this(text, priority, queueId, null, null);
	}

    public Message(String text, int priority, int queueId, Client sender, Client receiver) {
        this.text = text;
        this.priority = priority;
        this.queueId = queueId;
        this.sender = sender;
        this.receiver = receiver;
    }

	/**
	 * Determines whether this message is a <em>null message</em>.
	 * @return <code>true</code> if this message is the null message;
	 *         <code>false</code> otherwise.
	 */
	public boolean isNullMessage() {
		return text == null && sender == null && receiver == null;
	}

	@Override
	public String toString() {
		if (isNullMessage()) {
			return "NULL_MESSAGE";
		} else {
			return String.format(
				"Message on queue %d, priority %d:, from %s, for %s: \"%s\"",
				queueId,
				priority,
				sender,
				receiver == null ? "all" : receiver,
				text);
		}
	}
}
