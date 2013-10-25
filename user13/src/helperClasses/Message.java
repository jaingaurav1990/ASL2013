package helperClasses;
import java.util.*;

public class Message {
	
	private Integer messageID;
	private Integer senderID;
	private Integer receiverID;
	private Integer queueID;
	private List<Integer> queueIDs;
	private Integer priority;
	private long timestamp;
	private Integer context;
	private String message;
	private Integer counter;
	private List<Integer> track;
		
	//One way Message
	public Message(Integer senderID, Integer receiverID, Integer queueID,
			Integer priority, String message) {
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.queueID = queueID;
		this.queueIDs = null;
		this.priority = priority;
		this.message = message;
		this.context = null;
		this.counter = 0;
		track = new ArrayList<Integer>();
	}
	
	public Message(Integer senderID, Integer receiverID, List<Integer> queueIDs,
			Integer priority, String message) {
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.queueID = null;
		this.queueIDs = queueIDs;
		this.priority = priority;
		this.message = message;
		this.context = null;
	}
	
	//request-response Message
	public Message(Integer senderID, Integer receiverID, Integer queueID,
			Integer context, Integer priority, String message){
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.queueID = queueID;
		this.context = context;
		this.priority = priority;
		this.message = message;
		this.counter = 0;
		track = new ArrayList<Integer>();
	}
	
	
	public void setsenderID(Integer senderID){
		this.senderID = senderID;
	}
	
	public void setReceiverID(Integer receiverID){
		this.receiverID = receiverID;
	}
	
	public void setQueueID(Integer queueID){
		
		this.queueID = queueID;
		
	}
	
	public void updateTrack(Integer clientID){
		track.add(clientID);
	}
	
	public void updateCounter(){
		this.counter++;
	}
	
	public long getMessageID(){
		return this.messageID;
	}
	
	public Integer getSenderID(){
		return this.senderID;
	}
	
	public Integer getReceiverID(){
		return this.receiverID;
	}
	
	public Integer getQueueID(){
		return this.queueID;
	}
	
	public List<Integer> getQueueIDs() {
		return this.queueIDs;
	}
	
	public void setQueueIDs(List<Integer> queueIDs) {
		this.queueIDs = queueIDs;
	}

	public Integer getPriority(){
		return this.priority;
	}
	
	public void setTimestamp(long ts) {
		this.timestamp = ts;
	}
	
	public long getTimestamp(){
		return this.timestamp;
	}
	
	public Integer getContext(){
		return this.context;
	}
	
	public String getMessage(){
		return this.message;
	}

}
