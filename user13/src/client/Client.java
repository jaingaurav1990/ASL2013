package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import helperClasses.Constants;
import helperClasses.Message;
import helperClasses.Convertor;

public class Client implements Constants, Runnable {

	private Integer ID;
	private Integer clientType;
	private Socket middlewareSocket;
	private PrintWriter out;
	private BufferedReader in;
	private Integer context;
	private long endTime;
	private boolean createQueues;
	Logger lgr = null;

	public Client(int clientType, InetAddress middlewareIP, int middlewarePort,
			boolean createQueues, long timeRunning) {
		try {
			this.clientType = clientType;
			this.middlewareSocket = new Socket(middlewareIP, middlewarePort);
			this.out = new PrintWriter(this.middlewareSocket.getOutputStream(),
					true);
			this.in = new BufferedReader(new InputStreamReader(
					this.middlewareSocket.getInputStream()));
			this.ID = register();
			this.context = this.ID;
			this.createQueues = createQueues;
			this.endTime = System.currentTimeMillis() + timeRunning;
			lgr = Logger.getLogger(Client.class.getName() + this.ID);
		} catch(IOException e) {
			
		}
	}

	public static void main(String args[]) {
		long timeRunning = 1000;
		String middleWareHost = args[0];
		int middleWarePort = Integer.getInteger(args[1]); 
		int numSender = 5, numReceiver = 5;
		if(args.length > 2)
			timeRunning = Long.parseLong(args[2]) * 1000;
		if(args.length > 4) {
			numReceiver = Integer.parseInt(args[3]);
			numSender = Integer.parseInt(args[4]);
		}
		else if(args.length > 3)
			numSender = numReceiver = Integer.parseInt(args[3]);
		ArrayList<Thread> threads = new ArrayList<Thread>();
		try {
			for(int i = 0; i < numSender; i++)
				threads.add(new Thread(new Client(0, InetAddress.getByName(middleWareHost), middleWarePort, true, timeRunning)));
			for(int i = 0; i < numReceiver; i++)
				threads.add(new Thread(new Client(1, InetAddress.getByName(middleWareHost), middleWarePort, false, timeRunning)));
			for (Thread thread : threads) {
				thread.start();
			}
			for (Thread thread : threads) {
				thread.join();
			}
		} catch(IOException e) {
			
		} catch (InterruptedException e) {
			
		}
	}

	@Override
	public void run() {
		Random ran = new Random();
		try {
			if(createQueues) {
				for(int j = 2; j > 0; j--) {
					createQueue("new queue");
				}
			}
			if (this.clientType == 3) {
				Message msg = createMessage(1);
				List<Integer> receivers = queryUsers();
				receivers.remove(this.ID);
				Integer receiverID = this.ID;
				sendMessageToReceiver(msg, receiverID);
				msg = null;
				List<Integer> queuesIDs = queryQueues(this.ID);
				msg = readMessage(queuesIDs.get(0), null, null, true);
				System.out.println("I received a message:" + msg.getMessage());
			} else if(this.clientType == 0) {
				while(!runLongEnough()) {
					List<Integer> queues = queryQueues();
					Integer queueID = queues.get(ran.nextInt(queues.size()));
					sendMessage(null, null, ran.nextInt(10) + 1, queueID);
				}
			} else if(this.clientType == 1) {
				while(!runLongEnough()) {
					List<Integer> queues;
					queues = queryQueues(this.ID);
					if(queues.size() == 0) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) { }
						continue;
					}
					Message msg = readMessage(
						queues.get(ran.nextInt(queues.size())), null, this.ID, true
					);
					System.out.println(msg != null ? msg.getMessage() : "no message");
				}
			} else { // clientType == 2
				
			}
			deregister();
		} catch (IOException e) {
			
		}
	}
	
	private boolean runLongEnough() {
		return System.currentTimeMillis() > endTime;
	}


	public Integer register() throws IOException {
		
		out.println("register");
		String status = in.readLine();
		if (!OK.equals(status)) {
			return null;
		} 
		else {
			HashMap<String, String> query = Convertor
					.stringToMap(in.readLine());
			return Integer.valueOf(query.get("id"));
		}
	}

	public void deregister() throws IOException {
		do {
			out.println("unregister");
		}while(!(in.readLine()).equals(OK));
		
		this.out.close();
		this.in.close();
		this.middlewareSocket.close();
	}

	public Integer createQueue(String queueName) throws IOException {
		
		out.println("create");
		out.println("name:" + queueName);
		String status = in.readLine();
		
		if (!OK.equals(status)) {
			return null;
			
		} else {
			HashMap<String, String> query = Convertor
					.stringToMap(in.readLine());
			int queueID = Integer.valueOf(query.get("id"));
			return queueID;
			
		}
	}

	public void deleteQueue(int queueID) throws IOException {
		
		out.println("delete");
		out.println("id:" + queueID);
		String status = in.readLine();
		if (!OK.equals(status)) {
			lgr.log(WARNING, "Deleting queue failed");
		}
	}

	public Message createMessage(int messageType) {
		Random ran = new Random();
		Message msg = null;
		int priority = ran.nextInt(10);
		String message = "Hello,are you at home now ?";
		if (messageType == 1) {
			msg = new Message(this.ID, null, 1, priority, message);
		}
		if (messageType == 2) {
			msg = new Message(this.ID, null, null, context, priority, message);
			context++;
		}

		return msg;
	}
	
	public void sendMessage(Integer receiver, Integer context, Integer priority,
			List<Integer> queueIDs) throws IOException {
		Message msg = new Message(this.ID, receiver, queueIDs, priority, "This is a multi message.");
		do {
			out.println("send");
			out.println(Convertor.messageToString(msg));
		}while(!in.readLine().equals(OK));
	}
	
	public void sendMessage(Integer receiver, Integer context, Integer priority,
			Integer queueID) throws IOException {
		Message msg = new Message(this.ID, receiver, queueID, priority, "This is a message.");
		do {
			out.println("send");
			out.println(Convertor.messageToString(msg));
		}while(!in.readLine().equals(OK));
	}

	public void sendMessagesToQueues(Message msg, List<Integer> QueuesIDs)
			throws IOException {
		List<Integer> queueIDs = queryQueues();
		Random ran = new Random();
		
		
		Integer queueID = ran.nextInt(queueIDs.size());
		msg.setQueueID(queueID);
		msg.setsenderID(ID);
		msg.updateCounter();
		msg.updateTrack(ID);
		out.println("send");
		out.println(Convertor.messageToString(msg));
		String status = in.readLine();
		if (!OK.equals(status)) {
			System.out.println("mission failed");
		} else {
			System.out.println("sending mission done");
		}
	}

	public void sendMessageToReceiver(Message msg, int receiverID)
			throws IOException {
		List<Integer> queueIDs = queryQueues();
		
		Random ran = new Random();
		int index = ran.nextInt(queueIDs.size());
		msg.setQueueID(queueIDs.get(index));
		msg.setsenderID(ID);
		msg.setReceiverID(receiverID);
		msg.updateCounter();
		msg.updateTrack(ID);
		out.println("send");
		out.println(Convertor.messageToString(msg));
		String status = in.readLine();
		if (!OK.equals(status)) {
			System.out.println("mission failed");
		} else {
			System.out.println("sending mission done");
		}
		
		
	}

	public Message getMessage(Integer queueID, Integer context,
			Integer receiverID, boolean by_priority) throws IOException {
		
		String queryString = "queue:" + queueID + ";arrive_time_order:" + by_priority;
		if(context != null)
			queryString += ";context:" + context;
		if(receiverID != null)
			queryString += ";receiver:" + receiverID;
		out.println("get");
		out.println(queryString);
		String status = in.readLine();
		if (OK.equals(status)) {
			HashMap<String, String> query = Convertor
					.stringToMap(in.readLine());
			lgr.log(STAT_MSG, query.get("message"));
			return Convertor.mapToMessage(query);
		}
		return null;
	}

	public Message readMessage(Integer queueID, Integer context,
			Integer receiverID, boolean by_priority) throws IOException {
		String queryString = "queue:" + queueID + ";arrive_time_order:" + by_priority;
		if(context != null)
			queryString += ";context:" + context;
		if(receiverID != null)
			queryString += ";receiver:" + receiverID;
		out.println("read");
		out.println(queryString);
		String status = in.readLine();
		if (!OK.equals(status)) {
			return null;
		} else {
			HashMap<String, String> query = Convertor
					.stringToMap(in.readLine());
			return Convertor.mapToMessage(query);
			
		}
	}
	
	public List<Integer> queryUsers() throws IOException {
		out.println("users");
		String status = in.readLine();
		if(OK.equals(status)) {
			HashMap<String, String> query = Convertor.stringToMap(in.readLine());
			return Convertor.stringToList(query.get("users"));
		}
		return null;
	}

	public List<Integer> queryQueues(int receiverID) throws IOException {
		
		out.println("list"); 
		out.println("receiver:" + receiverID);
		String status = in.readLine();
		if (!OK.equals(status)) {
			return null;
		} else {
			HashMap<String, String> query = Convertor
					.stringToMap(in.readLine());
			return Convertor.stringToList(query.get("queues"));
		}
	}

	public List<Integer> queryQueues() throws IOException {
		
		out.println("list");
		out.println("receiver:-1");
		String status = in.readLine();
		if (!OK.equals(status)) {
			return null;
			
		} else {
			HashMap<String, String> query = Convertor
					.stringToMap(in.readLine());
			List<Integer> list = Convertor.stringToList(query.get("queues"));
			return list;
		}
	}

	public int getClientID() {
		return this.ID;
	}

	public int getClientType() {
		return this.clientType;
	}
}
