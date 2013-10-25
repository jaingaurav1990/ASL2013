package middleware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

import org.postgresql.ds.PGPoolingDataSource;

import db.DataBaseEndPoint;
import helperClasses.Constants;
import helperClasses.Convertor;

public class Server implements Runnable, Constants {

	private Socket socket = null;
	private ArrayBlockingQueue<DataBaseEndPoint> queryPool;
	private static volatile boolean main_running = true;
	private boolean running = true;
	private int client_id = -1;
	Logger lgr = null;

	public Server(Socket socket, ArrayBlockingQueue<DataBaseEndPoint> queries) {
		this.socket = socket;
		this.queryPool = queries;
		lgr = Logger.getLogger(Server.class.getName());
	}

	public static void main(String[] args) {
		String dbHost = args[0];
		ServerSocket serverSocket = null;
		PGPoolingDataSource db_pool = null;
		Socket clientSocket = null;
		long connectionsAccepted = 0;
		final Logger mainLogger = Logger.getLogger("MainLogger");
		
		try {
			serverSocket = new ServerSocket(PORT);
			// Create DB connection pool
			db_pool = new PGPoolingDataSource();
			db_pool.setDataSourceName("db_pool");
			db_pool.setServerName(dbHost);
			db_pool.setPortNumber(db_port);
			db_pool.setDatabaseName(db_name);
			db_pool.setUser(db_user);
			db_pool.setPassword(db_password);
			db_pool.setMaxConnections(db_con_pool_size);
			db_pool.setTcpKeepAlive(true);
		} catch (IOException e) {
			System.err.println("Could not open port " + PORT + "\n"
					+ e.getMessage());
			System.exit(-1);
		}
		int capacity = 30;
		ArrayBlockingQueue<DataBaseEndPoint> queryPool = new ArrayBlockingQueue<DataBaseEndPoint>(capacity);
		for (int i = 0; i < capacity; i++) {
			try {
				DataBaseEndPoint dbep = new DataBaseEndPoint(db_pool.getConnection());
				queryPool.offer(dbep);
			} catch (SQLException e) {
				mainLogger.log(ERROR, e.getMessage(), e);
				System.exit(-1);
			}
		}
		
		System.out.println("Did open " + queryPool.size() + " connections!");
		System.out.println("Listening on port: " + serverSocket.getLocalPort());
		ArrayList<Thread> threads = new ArrayList<Thread>();
		while (main_running) {
			try {
				clientSocket = serverSocket.accept();
				connectionsAccepted++;
				mainLogger.log(STAT_MSG, "Accepted connection from IP: "
						+ clientSocket.getInetAddress());
				Thread t = new Thread(new Server(clientSocket, queryPool));
				t.start();
				threads.add(t);
			} catch (IOException e) {
				mainLogger.log(ERROR, e.getMessage(), e);
			}
		}
		mainLogger.log(STAT_MSG, "Accepted a total of " + connectionsAccepted
				+ " connections");
		for (Iterator<DataBaseEndPoint> iterator = queryPool.iterator(); iterator.hasNext();) {
			DataBaseEndPoint dataBaseEndPoint = (DataBaseEndPoint) iterator
					.next();
			try {
				dataBaseEndPoint.close();
			} catch (SQLException e) {
				mainLogger.log(ERROR, e.getMessage(), e);
			}
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			mainLogger.log(WARNING, e.getMessage(), e);
		}
	}

	@Override
	public void run() {
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			String cmd, message_to_client;
			while (running) {
				cmd = in.readLine();
				if(cmd != null) {
					message_to_client = handleCmd(cmd, in);
	
					if (message_to_client != null) {
						if (message_to_client.contains("!")) {
							String[] mtc = message_to_client.split("!");
							out.println(mtc[0]);
							out.println(mtc[1]);
							out.flush();
						} else {
							out.println(message_to_client);
							out.flush();
						}
					}
				}
			}
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			lgr.log(WARNING, "IO Error when reading from socket.");
		} finally {
			in = null;
			out = null;
			socket = null;
		}
	}

	private String handleCmd(String cmd, BufferedReader in) throws IOException {
		String message_to_client = null, inline;
		if (cmd.equals("register")) {
			int id = register();
			System.out.println("New id: " + id);
			message_to_client = id >= 0 ? OK + "!" + "id:" + id : "Registration failed.";
		} else if (cmd.equals("unregister")) {
			running = false;
			message_to_client = OK;
		} else if(cmd.equals("users")) {
			List<Integer> users = listUsers();
			message_to_client = users == null ? "Failure." : OK + "!users:" + users;
		} else if(cmd.equals("find")) {
			String str = findRequest();
			message_to_client = str != null ? OK + "!" + str : "Failure message";
		} else {
			inline = in.readLine();
			HashMap<String, String> query = Convertor.stringToMap(inline);
			if (cmd.equals("create")) {
				int id = createQueueFromString(query);
				message_to_client = id >= 0 ? OK + "!" + "id:" + id : "Failure";
			} else if (cmd.equals("delete")) {
				boolean success = deleteQueueFromString(query);
				message_to_client = success ? OK : "Faulty message";
			} else if (cmd.equals("send")) {
				int success = sendMessageFromString(query);
				message_to_client = success >= 0 ? OK : "Faulty message";
			} else if (cmd.equals("get") || cmd.equals("read")) {
				String str = messageFromString(cmd, query);
				message_to_client = str != null ? OK + "!" + str : "Failure message";
			} else if (cmd.equals("list")) {
				message_to_client = OK + "!" + listQueueFromString(query);
			} else {
				message_to_client = "Bad Request";
			}
		}
		return message_to_client;
	}

	private int createQueueFromString(HashMap<String, String> query) {
		if (query.containsKey("name")) {
			lgr.log(STAT_MSG, "Created queue:" + query.get("name"));
			return createQueue(query.get("name"));
		}
		return -1;
	}

	private boolean deleteQueueFromString(HashMap<String, String> query) {
		if (query.containsKey("id")) {
			lgr.log(STAT_MSG, "Deleted queue:" + query.get("id"));
			return deleteQueue(Integer.valueOf(query.get("id")));
		}
		return false;
	}

	private int sendMessageFromString(HashMap<String, String> query) {
		lgr.log(STAT_MSG, "Sent a message.");
		
		Integer receiver = -1,sender = -1,queue = 1,context = -1,priority = 5;
		String message = query.get("message");
		if(query.get("receiver") != null)
			receiver = Integer.valueOf(query.get("receiver"));
		if(query.get("sender") != null)
			sender = Integer.valueOf(query.get("sender"));
		if(query.get("queue") != null)
			queue = Integer.valueOf(query.get("queue"));
		if(query.get("context") != null)
			context = Integer.valueOf(query.get("context"));
		return sendMessage(sender, receiver, queue, context, priority, message);
	}

	private String messageFromString(String cmd, HashMap<String, String> query) {
		Integer queue, context;
		boolean arrive_time_order = false;
		
		queue = Integer.valueOf(query.get("queue"));
		if (queue == null)
			return "Queue needs to be specified";
		lgr.log(STAT_MSG, cmd + " a message.");
		
		if (query.containsKey("arrive_time_order"))
			arrive_time_order = Boolean.getBoolean(query
					.get("arrive_time_order"));
		
		if(query.containsKey("context"))
		context = Integer.valueOf(query.get("context"));
		else
			context = null;
		
		if (context != null) {
			return ("read").equals(cmd) ? readMessage(queue.intValue(),
					arrive_time_order, context.intValue()) : getMessage(queue
					.intValue(), arrive_time_order, context.intValue());
		} else {
			return ("read").equals(cmd) ? readMessage(queue.intValue(),
					arrive_time_order) : getMessage(queue.intValue(),
					arrive_time_order);
		}
	}

	private String listQueueFromString(HashMap<String, String> query) {
		Integer receiver = null;
		if( query.containsKey("receiver") && query.get("receiver") != null)
			receiver = Integer.valueOf(query.get("receiver"));
		
		List<Integer> ids;
		if (receiver != -1)
			ids = listQueues(receiver);
		else
			ids = listQueues();

		lgr.log(STAT_MSG, "Returned a list of queue ids.");
		String str = "queues:" + ids;
		return str;
	}

	/*
	 * DB interface
	 */
	private int register() {
		int client_id = -1;
		try {
			DataBaseEndPoint dbep = queryPool.take();
			System.err.println("Have db endpoint");
			ResultSet rs = dbep.registerUser();
			queryPool.put(dbep);
			System.err.println("Gave it back");
			if (rs.next()) {
				client_id = rs.getInt(1);
			}
			rs.close();
		} catch (SQLException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return -1;
		} catch (InterruptedException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return -1;
		}
		this.client_id = client_id;
		lgr.log(STAT_MSG, "client created with id : " + client_id);
		return client_id;
	}

	private int createQueue(String name) {
		int queue_id = -1;
		try {
			DataBaseEndPoint dbep = queryPool.take();
			ResultSet rs = dbep.createAQueue();
			queryPool.put(dbep);
			if (rs.next()) {
				queue_id = rs.getInt(1);
			}
			rs.close();
		} catch (SQLException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return -1;
		} catch (InterruptedException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return -1;
		}
		return queue_id;
	}

	private boolean deleteQueue(int id) {
		boolean success = false;
		try {
			DataBaseEndPoint dbep = queryPool.take();
			ResultSet rs = dbep.deleteAQueue();
			queryPool.put(dbep);
			if (rs.next()) {
				success = true;
			}
			rs.close();
		} catch (SQLException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return false;
		} catch (InterruptedException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return false;
		}
		return success;
	}

	private int sendMessage(int senderId, int receiverId, int queueId,
			int context, int priority, String message) {
		int msg_id = -1;
		ResultSet rs = null;
		try {
			DataBaseEndPoint dbep = queryPool.take();
			rs = dbep.sendMessage(senderId, receiverId, queueId, context, priority, message);
			queryPool.put(dbep);
			if (rs != null && rs.next()) {
				msg_id = rs.getInt(1);
			}
			rs.close();
		} catch (SQLException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return -1;
		} catch (InterruptedException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return -1;
		}
		return msg_id;
	}
	
	private String findRequest() {
		String msg = null;
		ResultSet rs = null;
		try {
			DataBaseEndPoint dbep = queryPool.take();
			rs = dbep.deleteAQueue();
			queryPool.put(dbep);
			if(rs != null && rs.next()) {
				msg = Convertor.ResultMessageToString(rs);
			}
		} catch (SQLException e) {
			lgr.log(ERROR, e.getMessage(), e);
		} catch (InterruptedException e) {
			lgr.log(ERROR, e.getMessage(), e);
		}
		return msg;
	}

	private String getMessage(int queue, boolean arrive_time_order) {
		String msg = null;
		ResultSet rs = null;
		try {
			DataBaseEndPoint dbep = queryPool.take();
			if (arrive_time_order) {
				rs = dbep.getMessageTime(queue, this.client_id);
			} else {
				rs = dbep.getMessage(queue, this.client_id);
			}
			queryPool.put(dbep);
			if (rs != null && rs.next()) {
				msg = Convertor.ResultMessageToString(rs);
				System.err.println(msg);
			}
			rs.close();
		} catch (SQLException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		} catch (InterruptedException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		}
		return msg;
	}

	private String getMessage(int queue, boolean arrive_time_order, int context) {
		String msg = null;
		ResultSet rs = null;
		try {
			DataBaseEndPoint dbep = queryPool.take();
			if (arrive_time_order) {
				rs = dbep.getMessageTime(queue, this.client_id, context);
			} else {
				rs = dbep.getMessage(queue, this.client_id, context);
			}
			queryPool.put(dbep);
			if (rs != null && rs.next()) {
				msg = Convertor.ResultMessageToString(rs);
			}
			rs.close();
		} catch (SQLException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		} catch (InterruptedException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		}
		return msg;
	}

//	private String getMessage(int queue, boolean arrive_time_order,
//			int context, int sender) {
//		String msg = null;
//		try {
//			Query qu = new Query(db_con_pool.getConnection());
//			ResultSet rs = qu
//					.execute("SELECT id,sender_id,receiver_id,queue_id,context,priority,text FROM message WHERE queue_id="
//							+ queue
//							+ " AND (receiver_id IS NULL OR receiver_id="
//							+ this.client_id
//							+ ")"
//							+ " AND context="
//							+ context
//							+ " AND sender_id="
//							+ sender
//							+ " ORDER BY "
//							+ (arrive_time_order ? "arrive_time DESC"
//									: " priority DESC") + " LIMIT 1");
//			if (rs.next()) {
//				ResultSet del_msg = qu.execute("SELECT Delete_Message("
//						+ rs.getInt(1) + ")");
//				if (del_msg.next()) {
//					msg = Convertor.ResultMessageToString(rs);
//				}
//			}
//			rs.close();
//		} catch (SQLException e) {
//			lgr.log(ERROR, e.getMessage(), e);
//			return null;
//		}
//		return msg;
//	}

	private String readMessage(int queue, boolean arrive_time_order) {
		String msg = null;
		ResultSet rs = null;
		try {
			DataBaseEndPoint dbep = queryPool.take();
			if (arrive_time_order) {
				rs = dbep.readMessageTime(queue, this.client_id);
			} else {
				rs = dbep.readMessage(queue, this.client_id);
			}
			queryPool.put(dbep);
			if (rs != null && rs.next()) {
				msg = Convertor.ResultMessageToString(rs);
			}
			rs.close();
		} catch (SQLException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		} catch (InterruptedException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		}
		return msg;
	}

	private String readMessage(int queue, boolean arrive_time_order, int context) {
		String msg = null;
		ResultSet rs = null;
		try {
			DataBaseEndPoint dbep = queryPool.take();
			if (arrive_time_order) {
				rs = dbep.readMessageTime(queue, this.client_id, context);
			} else {
				rs = dbep.readMessage(queue, this.client_id, context);
			}
			queryPool.put(dbep);
			if (rs != null && rs.next()) {
				msg = Convertor.ResultMessageToString(rs);
			}
			rs.close();
		} catch (SQLException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		} catch (InterruptedException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		}
		return msg;
	}

//	private String readMessage(int queue, boolean arrive_time_order,
//			int context, int sender) {
//		
//		String msg = null;
//		try {
//			Query qu = new Query(db_con_pool.getConnection());
//			ResultSet rs = qu
//					.execute("SELECT id,sender_id,receiver_id,queue_id,context,priority,text FROM message WHERE queue_id="
//							+ queue
//							+ " AND (receiver_id IS NULL OR receiver_id="
//							+ this.client_id
//							+ ")"
//							+ " AND context="
//							+ context
//							+ " AND sender_id="
//							+ sender
//							+ " ORDER BY "
//							+ (arrive_time_order ? "arrive_time DESC"
//									: " priority DESC") + " LIMIT 1");
//			if (rs.next()) {
//				msg = Convertor.ResultMessageToString(rs);
//			}
//			rs.close();
//		} catch (SQLException e) {
//			lgr.log(ERROR, e.getMessage(), e);
//			return null;
//		}
//		return msg;
//	}

	private List<Integer> listQueues() {
		try {
			DataBaseEndPoint dbep = queryPool.take();
			ResultSet rs = dbep.listAllQueues();
			queryPool.put(dbep);
			List<Integer> q_list = new ArrayList<Integer>();
			while (rs.next()) {
				q_list.add(rs.getInt(1));
			}
			rs.close();
			return q_list;
		} catch (SQLException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		} catch (InterruptedException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		}
	}

	private List<Integer> listQueues(int receiver) {
		try {
			DataBaseEndPoint dbep = queryPool.take();
			ResultSet rs = dbep.listQueuesWithMsgs(receiver);
			queryPool.put(dbep);
			List<Integer> q_list = new ArrayList<Integer>();
			while (rs.next()) {
				q_list.add(rs.getInt(1));
			}
			rs.close();
			return q_list;
		} catch (SQLException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		} catch (InterruptedException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		}
	}
	
	private List<Integer> listUsers() {
		try {
			DataBaseEndPoint dbep = queryPool.take();
			ResultSet rs = dbep.listAllUsers();
			queryPool.put(dbep);
			List<Integer> q_list = new ArrayList<Integer>();
			while (rs.next()) {
				q_list.add(rs.getInt(1));
			}
			rs.close();
			return q_list;
		} catch (SQLException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		} catch (InterruptedException e) {
			lgr.log(ERROR, e.getMessage(), e);
			return null;
		}
	}
}
