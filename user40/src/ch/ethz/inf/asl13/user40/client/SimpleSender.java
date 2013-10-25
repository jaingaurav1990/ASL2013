package ch.ethz.inf.asl13.user40.client;

import java.lang.Integer;
import ch.ethz.inf.asl13.user40.*;
/**
 * It is assumed that this client can only send messages
 * and not receive messages. The main method must be 
 * provided with the queueId to which this client
 * infinitely sends messages
 */
class SimpleSender extends MessagingServiceClient {

    public SimpleSender(String clientID, String queueID) {
        super(clientID, Integer.parseInt(queueID));
        System.out.println("SimpleSender client starting with clientID: " + clientID);
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("usage: java " + SimpleSender.class.getName() + " clientID queueID");
            System.exit(1);
        }
        else {
            String clientID = args[0];
            String queueID = args[1];
            SimpleSender app = new SimpleSender(clientID, queueID);
            Client sender = new Client(Integer.parseInt(clientID), true, false);
            String initialText = "1";
            int priority = 5;
            Message message = new Message(initialText, priority, Integer.parseInt(queueID), sender, null);
            app.sendMessagesIndefinitelyWithoutReceiving(message);
        }


    }
}
