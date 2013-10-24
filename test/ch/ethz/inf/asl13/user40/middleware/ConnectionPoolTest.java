package ch.ethz.inf.asl13.user40.middleware;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class ConnectionPoolTest {

    private ConnectionPool pool;
    private List <Connection> openConnectionsList; // Used for proper cleanup

	@Before
	public void initialize() throws SQLException{
        pool = ConnectionPool.getTestInstance();
        openConnectionsList = new ArrayList<Connection>();
        assertEquals(pool.getInitPoolSize(), pool.getAllOpenConnectionsCount());
	}


	@After
	public void cleanup() throws SQLException {
        while (!openConnectionsList.isEmpty()) {
            pool.returnConnection(openConnectionsList.remove(0));
        }

        int ret = pool.destroyConnectionPool();
        assertEquals(ret, 0);
	}

    @Test
    public void getAndReturnConnection() throws SQLException {
        openConnectionsList.add(pool.getConnection());
        assertEquals(pool.getConnectionsInPool(), pool.getInitPoolSize() - 1);
        pool.returnConnection(openConnectionsList.remove(0));
        assertEquals(pool.getConnectionsInPool(), pool.getInitPoolSize());
        assertEquals(pool.getAllOpenConnectionsCount(), pool.getConnectionsInPool());
    }

    @Test
    public void getAndReturnMoreThanInitConnections() throws SQLException {
        assertEquals(pool.getInitPoolSize(), pool.getConnectionsInPool());
        for (int i = 0; i < pool.getInitPoolSize() + 1; i++) {
            openConnectionsList.add(pool.getConnection()); 
        }

        assertEquals(pool.getConnectionsInPool(), 0);
        assertEquals(pool.getInitPoolSize() + 1, pool.getAllOpenConnectionsCount());
        for (int i = 0; i < pool.getInitPoolSize() + 1; i++) {
            pool.returnConnection(openConnectionsList.remove(0));
        }

        assertEquals(pool.getConnectionsInPool(), pool.getInitPoolSize() + 1);
    }

    @Test 
    public void destroyBeforeReturningAllConnections() throws SQLException {
        openConnectionsList.add(pool.getConnection());
        assertEquals(pool.getConnectionsInPool(), pool.getInitPoolSize() - 1);
        int ret = pool.destroyConnectionPool();
        assertEquals(ret, -1);

    }

}
