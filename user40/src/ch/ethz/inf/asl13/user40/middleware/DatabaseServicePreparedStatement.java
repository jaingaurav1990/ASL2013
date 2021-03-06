package ch.ethz.inf.asl13.user40.middleware;

import java.sql.*;
import java.util.*;
import ch.ethz.inf.asl13.user40.*;
import ch.ethz.inf.asl13.user40.util.*;

/**
 * Abstracts away the Postgresql database.
 */
public class DatabaseServicePreparedStatement implements MessagingService {

    public static final String DB_SERVICE_URL_KEY = "dbservice.url";

    public static String getDbServiceUrl() {
        return System.getProperty(DB_SERVICE_URL_KEY, "127.0.0.1:5432");
    }

    /**
     * Used only by the Management Console
     */
    public void createClient(final Client client) {
        execute(new StatementHolder("INSERT INTO clients values(?, ?, ?)") {
            @Override
            public void fillParameters(PreparedStatement pst) throws SQLException {
                pst.setInt(1, client.id);
                pst.setBoolean(2, client.canSend);
                pst.setBoolean(3, client.canReceive);
            }
        });
    }
    
    @Override
    public void insertMessage(final Message message) {
        execute(new StatementHolder("INSERT INTO messages values(DEFAULT, ?, ?, ?, ?, ?, ?, now())") {
            @Override
            public void fillParameters(PreparedStatement pst) throws SQLException {
                pst.setInt(1, message.queueId);
                pst.setString(2, message.text);
                pst.setInt(3, message.priority);
                pst.setInt(4, 0); // Should be actual message type
                setIdOrNull(pst, 5, message.sender);
                setIdOrNull(pst, 6, message.receiver);
            }
        });
    }

    @Override
    public Message getFirstMessage(int queue, Client sender, boolean remove) {
        if (remove == false) {
        return execute(new MessageQuery(queue, sender, "SELECT msg, pri, qid, sender, receiver FROM messages WHERE (qid = ? AND (receiver IS NULL OR receiver = ?)) ORDER BY tstamp LIMIT 1"));
        }
        else {
            return execute (new MessageQuery(queue, sender, "DELETE FROM messages WHERE mid IN (SELECT mid FROM messages WHERE (qid = ? AND (receiver IS NULL OR receiver = ?)) ORDER BY tstamp LIMIT 1) RETURNING msg, pri, qid, sender, receiver"));
        }
    }

    @Override
    public Message getMessageOfHighestPriority(int queue, Client sender, boolean remove) {
        if (remove == false) {
            return execute(new MessageQuery(queue, sender, "SELECT msg, pri, qid, sender, receiver FROM messages WHERE (qid = ? AND (receiver IS NULL OR receiver = ?)) ORDER BY pri DESC, tstamp ASC"));
        }
        else {
            return execute(new MessageQuery(queue, sender, "DELETE FROM messages WHERE mid IN (SELECT mid FROM messages WHERE (qid = ? AND (receiver IS NULL OR receiver = ?)) ORDER BY pri DESC, tstamp ASC) RETURNING msg, pri, qid, sender, receiver"));
        }
    }

    public int[] listQueues() {
        return execute(new IntArrayQuery("SELECT id FROM queues"));
    }

    /**
     * Used only by the Management Console
     */
    public int[] listClients() {
        return execute(new IntArrayQuery("SELECT id FROM clients"));
    }


    @Override
    public int[] listQueuesWithWaitingMessages(final Client client) {
        return execute(new IntArrayQuery("SELECT id FROM messages WHERE sender = ?") {
            @Override
            public void fillParameters(PreparedStatement pst) throws SQLException {
                pst.setInt(1, client.id);
            }
        });
    }

    @Override
    public void createQueue(final int queueId) {
        execute(new StatementHolder("INSERT INTO queues VALUES(?)") {
            @Override
            public void fillParameters(PreparedStatement pst) throws SQLException {
                pst.setInt(1, queueId);
            }
        });
    }

    @Override
    public void deleteQueue(final int queue) {
        execute(new StatementHolder("DELETE FROM queues WHERE id = ?") {
            @Override
            public void fillParameters(PreparedStatement pst) throws SQLException {
                pst.setInt(1, queue);
            }
        });
    }

    /**
     * Sets the specified field to the client's ID,
     * or to null if there was no client.
     */
    protected static void setIdOrNull(PreparedStatement pst, int parameterIndex, Client client) throws SQLException {
        if (client == null) {
            pst.setNull(parameterIndex, Types.INTEGER);
        } else {
            pst.setInt(parameterIndex, client.id);
        }
    }

   
    /**
     * Executes a database query.
     * @param query The query to process, with parameters and result processing rules.
     * @return the query result processed by <var>query</var>.
     */
    private <T> T execute(QueryHolder<T> query) {

        Connection connection = null;
        try {
            String user = System.getProperty("user.name");
            String pass = "";
            String dbServiceUrl = "jdbc:postgresql://" + getDbServiceUrl() + "/db";
            connection = DriverManager.getConnection(dbServiceUrl, user, pass);
            PreparedStatement pst = connection.prepareStatement(query.sql);

            query.fillParameters(pst);
            T result = query.execute(pst);
            connection.close();
            return result;
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to process statement \"" + query.sql + "\": " + e.getMessage(), e);
        }
    }

    /**
     * Represents a database query and the methods to set its
     * parameters and to read the resulting data set.
     */
    protected abstract static class QueryHolder<T> {
        private final String sql;

        public QueryHolder(String sql) {
            this.sql = sql;
        }

        /**
         * Sets the statements parameters before calling into the database.
         * @param statement the statement with parameters to set.
         */
        public void fillParameters(PreparedStatement statement) throws SQLException {}

        /**
         * Processes the {@link ResultSet} returned when executing the
         * query and converts it to the expected result type.
         */
        public abstract T processResult(ResultSet rs) throws SQLException;

        /**
         * Executes the SQL query and calls {@link #processResult(ResultSet)}
         * with the query result.
         */
        T execute(PreparedStatement statement) throws SQLException {
            ResultSet rs = statement.executeQuery();
            return processResult(rs);
        }
    }

    /**
     * Represents a database statement that returns no result;
     * along with its parameters.
     */
    protected static class StatementHolder extends QueryHolder<Void> {

        public StatementHolder(String sql) {
            super(sql);
        }

        /** Executes the SQL query. */
        @Override
        Void execute(PreparedStatement statement) throws SQLException {
            statement.executeUpdate();
            return null;
        }

        /** Not supported for SQL statements without result. */
        @Override
        public final Void processResult(ResultSet rs) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * A database query that returns a {@link Message}.
     * Requires that query returns the required fields in that exact same order.
     */
    protected static class MessageQuery extends QueryHolder<Message> {
        private final Client sender;
        private final int queue;

        public MessageQuery(int queue, Client sender, String sql) {
            super(sql);
            this.queue = queue;
            this.sender = sender;
        }

        @Override
        public void fillParameters(PreparedStatement pst) throws SQLException {
            pst.setInt(1, queue);
            setIdOrNull(pst, 2, sender);
        }

        @Override
        public Message processResult(ResultSet rs) throws SQLException {
            if (rs.next()) {
                /**
                 * According to JDBC API, getInt() returns 0 for SQL NULL.
                 */
                return new Message(rs.getString(1), rs.getInt(2), rs.getInt(3), setClient(rs.getInt(4)), setClient(rs.getInt(5)));
            } else {
                return null;
            }
        }
    }

    protected static Client setClient(int id) {
        if (id == 0) {
            return null;
        }
        else {
            return new Client(id, true, true); // We don't really know the information about canSend and canReceive flags
        }
    }
    /**
     * A database query that interprets the result as <code>int</code>
     * array by reading the first field of each row in the results.
     */
    protected static class IntArrayQuery extends QueryHolder<int[]> {
        public IntArrayQuery(String sql) {
            super(sql);
        }

        @Override
        public final int[] processResult(ResultSet rs) throws SQLException {
            List<Integer> list = new LinkedList<Integer>();
            while (rs.next()) {
                list.add(rs.getInt(1));
            }

            int[] result = new int[list.size()];
            Iterator<Integer> it = list.iterator();
            for (int i = 0; i < result.length; i++) {
                result[i] = it.next();
            }

            return result;
        }
    }
}
