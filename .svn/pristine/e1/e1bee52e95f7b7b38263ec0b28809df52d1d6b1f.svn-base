package ch.ethz.inf.asl13.user40.util;

import ch.ethz.inf.asl13.user40.middleware.DatabaseServicePreparedStatement;
import ch.ethz.inf.asl13.user40.*;
import java.util.Arrays;

class SetupScenario {
    public static void main(String[] args) {

        DatabaseServicePreparedStatement db = new DatabaseServicePreparedStatement();

        /**
         * Create clients corresponding to the 3 message passers
         * with clientID = 1, 2, and 3 respectively
         */
        Client messagePasser1 = new Client(1, true, true);
        Client messagePasser2 = new Client(2, true, true);
        Client messagePasser3 = new Client(3, true, true);
        db.createClient(messagePasser1); 
        db.createClient(messagePasser2);
        db.createClient(messagePasser3);
        db.createQueue(2);  // 2 is the hard-coded queue for message passers

        /**
         * Create clients corresponding to 2 chatter-bot pairs and the queue.
         */
        Client chatter1 = new Client(201, true, true);
        Client chatter2 = new Client(211, true, true);
        Client bot1 = new Client(202, true, true);
        Client bot2 = new Client(212, true, true);
        db.createClient(chatter1);
        db.createClient(chatter2);
        db.createClient(bot1);
        db.createClient(bot2);
        db.createQueue(4); // 4 is the queueId for chat-bot pair

        /**
         * Create clients and queues corresponding to 2 dictator-worker pair.
         */
        Client dictator1 = new Client(301, true, true);
        Client worker1 = new Client(311, true, true);
        db.createClient(dictator1);
        db.createClient(worker1);
        db.createQueue(56);
        db.createQueue(57);

    }
}
