package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class DataBaseEndPoint {
	private Connection con = null;
	private PreparedStatement register = null;
	private PreparedStatement createQueue = null;
	private PreparedStatement deleteQueue = null;
	private PreparedStatement findRequest = null;
	private PreparedStatement sendOne = null;
	private PreparedStatement sendMany = null;
	private PreparedStatement readPriority = null;
	private PreparedStatement readPriorityContext = null;
	private PreparedStatement readTime = null;
	private PreparedStatement readTimeContext = null;
	private PreparedStatement getPriority = null;
	private PreparedStatement getPriorityContext = null;
	private PreparedStatement getTime = null;
	private PreparedStatement getTimeContext = null;
	private PreparedStatement listQueues = null;
	private PreparedStatement listQueuesMsgs = null;
	private PreparedStatement listUsers = null;
	
	public DataBaseEndPoint(Connection con) throws SQLException {
		this.con = con;
		this.register = con.prepareStatement("SELECT createClient()");
		this.createQueue = con.prepareStatement("SELECT createQueue()");
		this.deleteQueue = con.prepareStatement("SELECT deleteQueue(?)");
		this.findRequest = con.prepareStatement(
			"SELECT * FROM message WHERE context > 0 LIMIT 1");
		this.sendOne = con.prepareStatement(
			"SElECT insertMessageOne(?, ?, ?, ?, ?, ?)");
		this.sendMany = con.prepareStatement(
			"SElECT insertMessageMany(?, ?, ?, ?, ?, ?)");
		this.readTime = con.prepareStatement(
			"SELECT id,sender_id,receiver_id,queue_id,context,priority,text "
			+ "FROM message WHERE queue_id=? AND (receiver_id IS NULL OR receiver_id=?) "
			+ "AND context is NULL ORDER BY arrive_time DESC LIMIT 1");
		this.readTimeContext = con.prepareStatement(
			"SELECT id,sender_id,receiver_id,queue_id,context,priority,text "
			+ "FROM message WHERE queue_id=? AND (receiver_id IS NULL OR receiver_id=?) "
			+ "AND context=? ORDER BY arrive_time DESC LIMIT 1");
		this.readPriority = con.prepareStatement(
			"SELECT id,sender_id,receiver_id,queue_id,context,priority,text "
			+ "FROM message WHERE queue_id=? AND (receiver_id IS NULL OR receiver_id=?) "
			+ "AND context is NULL ORDER BY priority DESC LIMIT 1");
		this.readPriorityContext = con.prepareStatement(
			"SELECT id,sender_id,receiver_id,queue_id,context,priority,text "
			+ "FROM message WHERE queue_id=? AND (receiver_id IS NULL OR receiver_id=?) "
			+ "AND context=? ORDER BY priority DESC LIMIT 1");
		this.getPriority = con.prepareStatement("SELECT getMessage(?, ?)");
		this.getPriorityContext = con.prepareStatement("SELECT getMessage(?, ?, ?)");
		this.getPriority = con.prepareStatement("SELECT getMessageTime(?, ?)");
		this.getPriorityContext = con.prepareStatement("SELECT getMessageTime(?, ?, ?)");
		this.listQueues = con.prepareStatement("SELECT listQueue()");
		this.listQueuesMsgs = con.prepareStatement("SELECT listQueues(?)");
		this.listUsers = con.prepareStatement("SELECT listUsers()");
	}
	
	public ResultSet registerUser() throws SQLException {
		return register.executeQuery();
	}
	
	public ResultSet createAQueue() throws SQLException {
		return createQueue.executeQuery();
	}
	
	public ResultSet deleteAQueue() throws SQLException {
		return deleteQueue.executeQuery();
	}
	
	public ResultSet sendMessage(int senderId, int receiverId, int queueId,
			int context, int priority, String message) throws SQLException {
		sendOne.setInt(1, senderId);
		if(receiverId > -1)
			sendOne.setInt(2, receiverId);
		else
			sendOne.setNull(2, Types.NULL);
		sendOne.setInt(3, queueId);
		if(context > -1)
			sendOne.setInt(4, context);
		else
			sendOne.setNull(4, Types.NULL);
		sendOne.setInt(5, priority);
		sendOne.setString(6, message);
		return sendOne.executeQuery();
	}
	
	public ResultSet sendMessage(int senderId, int receiverId, List<Integer> queueIds,
			int context, int priority, String message) throws SQLException {
		sendMany.setInt(1, senderId);
		if(receiverId > -1)
			sendMany.setInt(2, receiverId);
		else
			sendMany.setNull(2, Types.NULL);
		sendMany.setArray(3, con.createArrayOf("INT", queueIds.toArray()));
		if(context > -1)
			sendMany.setInt(4, context);
		else
			sendMany.setNull(4, Types.NULL);
		sendMany.setInt(5, priority);
		sendMany.setString(6, message);
		return sendMany.executeQuery();
	}
	
	public ResultSet readMessage(int queueId, int receiverId) throws SQLException {
		readPriority.setInt(1, queueId);
		readPriority.setInt(2, receiverId);
		return readPriority.executeQuery();
	}
	
	public ResultSet readMessage(int queueId, int receiverId, int context) throws SQLException {
		readPriorityContext.setInt(1, queueId);
		readPriorityContext.setInt(2, receiverId);
		readPriorityContext.setInt(3, context);
		return readPriorityContext.executeQuery();
	}
	
	public ResultSet readMessageTime(int queueId, int receiverId) throws SQLException {
		readTime.setInt(1, queueId);
		readTime.setInt(2, receiverId);
		return readTime.executeQuery();
	}
	
	public ResultSet readMessageTime(int queueId, int receiverId, int context) throws SQLException {
		readTimeContext.setInt(1, queueId);
		readTimeContext.setInt(2, receiverId);
		readTimeContext.setInt(3, context);
		return readTimeContext.executeQuery();
	}
	
	public ResultSet getMessage(int queueId, int receiverId) throws SQLException {
		getPriority.setInt(1, queueId);
		getPriority.setInt(2, receiverId);
		return getPriority.executeQuery();
	}
	
	public ResultSet getMessage(int queueId, int receiverId, int context) throws SQLException {
		getPriorityContext.setInt(1, queueId);
		getPriorityContext.setInt(2, receiverId);
		getPriorityContext.setInt(3, context);
		return getPriorityContext.executeQuery();
	}
	
	public ResultSet getMessageTime(int queueId, int receiverId) throws SQLException {
		getTime.setInt(1, queueId);
		getTime.setInt(2, receiverId);
		return getTime.executeQuery();
	}
	
	public ResultSet getMessageTime(int queueId, int receiverId, int context) throws SQLException {
		getTimeContext.setInt(1, queueId);
		getTimeContext.setInt(2, receiverId);
		getTimeContext.setInt(3, context);
		return getTimeContext.executeQuery();
	}
	
	public ResultSet findReqRespMessage() throws SQLException {
		return findRequest.executeQuery();
	}
	
	public ResultSet listAllQueues() throws SQLException {
		return listQueues.executeQuery();
	}
	
	public ResultSet listQueuesWithMsgs(int receiverId) throws SQLException {
		listQueuesMsgs.setInt(1, receiverId);
		return listQueuesMsgs.executeQuery();
	}
	
	public ResultSet listAllUsers() throws SQLException {
		return listUsers.executeQuery();
	}
	
	public final void close() throws SQLException {
		this.createQueue.close();
		this.deleteQueue.close();
		this.findRequest.close();
		this.getPriority.close();
		this.getPriorityContext.close();
		this.getTime.close();
		this.getTimeContext.close();
		this.listQueues.close();
		this.listQueuesMsgs.close();
		this.listUsers.close();
		this.readPriority.close();
		this.readPriorityContext.close();
		this.readTime.close();
		this.readTimeContext.close();
		this.register.close();
		this.sendMany.close();
		this.sendOne.close();
		this.con.close();
	}
}
