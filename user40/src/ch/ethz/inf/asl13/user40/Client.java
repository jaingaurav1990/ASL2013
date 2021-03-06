package ch.ethz.inf.asl13.user40;

import javax.xml.bind.annotation.*;

/**
 * Represents a client in the messaging system.
 */
@XmlRootElement
public class Client {

	/** A number that identifies this client in the system. */
	public int id;

	/** Specifies whether this client is allowed to send messages. */
	public boolean canSend;

	/** Specifies whether this client is allowed to receive messages. */
	public boolean canReceive;

	/**
	 * Constructs a new client with ID 0 and not allowed to send or receive.
	 */
	public Client() {}

	/**
	 * Constructs a new client interpreting <code>s</code> as numeric ID
	 * and with permission to send and receive messages.
	 * @param s a string containing the int representation to be parsed.
	 */
	public Client(String s) {
		id = Integer.parseInt(s);
		canSend = true;
		canReceive = true;
	}

    public Client(int id, boolean canSend, boolean canReceive) {
        this.id = id;
        this.canSend = canSend;
        this.canReceive = canReceive;
    }

	@Override
	public String toString() {
		return "Client(" + id + ")";
	}
}
