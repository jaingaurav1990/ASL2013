package ch.ethz.inf.asl13.user40.middleware;

import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/*
 * Only one instance of the connection pool should  
 * be present at all times. Using the Lazy Creation
 * Thread Safe approach (Singleton design)
 */
class ConnectionPool {
    private static ConnectionPool instance = null;

    private ConnectionPool() throws SQLException{
        initializeConnectionPool();
    }

    public static ConnectionPool getInstance() throws SQLException{
        synchronized (ConnectionPool.class) {
            if (instance != null) {
                return instance;
            }
            else {
                return new ConnectionPool();
            }
        }
    }

    /**
     * Meant to be used only by Junit testing code.
     */
    public static ConnectionPool getTestInstance() throws SQLException {
        return new ConnectionPool();
    }

    private static String url = "jdbc:postgresql://127.0.0.1:5432/db";
    private static String user = System.getProperty("user.name");
    private static String pass = "";

    private static final int INIT_POOL_SIZE = 5;
    private static ArrayList<Connection> poolConnections = new ArrayList<Connection>();

    private static int allOpenConnectionsCount = 0;

    public int getInitPoolSize(){
        return INIT_POOL_SIZE;
    }

    public int getConnectionsInPool() {
        return poolConnections.size();
    }
    /**
     * Returns a connection object to be used by the application to connect
     * to the database. synchronized to maintain safe access to ArrayList and 
     * allOpenConnectionsCount variable. 
     * <p>
     * May throw an SQL exception while connecting to the database.
     */
    public int getAllOpenConnectionsCount() {
        return allOpenConnectionsCount;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (!poolConnections.isEmpty()){
            return poolConnections.remove(0);
        }
        else {
            Connection conn = DriverManager.getConnection(url, user, pass);
            allOpenConnectionsCount += 1;
            return conn;
        }
    }

    /**
     * @param 
     * conn Connection to be returned to the pool of connections. The connection
     * to the database is not actually closed. purgeConnection(Connection conn)
     * does that.
     */
    public synchronized void returnConnection(Connection conn) {
        poolConnections.add(conn);
    }

    /**
     * @param 
     * conn Connection to the database to be closed. May throw an SQL Exception
     * if the connection has been already be closed.
     */
    private void purgeConnection(Connection conn) throws SQLException {
        conn.close();
        allOpenConnectionsCount -= 1;
    }

    /**
     * Called by the constructor of the class to initialize the connection pool
     * with INIT_POOL_SIZE number of connections. May throw an SQL 
     * exception while connecting to the database.
     */
    private void initializeConnectionPool() throws SQLException {
        for (int i = 0; i < INIT_POOL_SIZE; i++) {
            poolConnections.add(DriverManager.getConnection(url, user, pass));
            allOpenConnectionsCount += 1;
        }
    }

    /*
     * This function should be called only after each user of
     * the Connection pool has returned the connection back
     * to the pool (after use). If that is not the case, function 
     * returns -1 without closing any of the underlying connections.
     * Else, 0 is returned on success.
     */
    public int destroyConnectionPool() throws SQLException {
        if (allOpenConnectionsCount != poolConnections.size()) {
            return -1;
        }
        while (!poolConnections.isEmpty()) {
            purgeConnection(poolConnections.remove(0));
        }
        return 0;
    }
}

