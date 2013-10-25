package helperClasses;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Convertor {
	
	
	public Convertor(){
		
	}
	
	public static HashMap<String, String> stringToMap(String str) {
			
		String[] args = str.split(";");
		HashMap<String, String> query = new HashMap<String, String>();
		for(String arg: args) {
			String[] a = arg.split(":");
			
            
			query.put(a[0], a[1]);
		}
		return query;
	}
	
	public static List<Integer> stringToList(String str) {
	   List<Integer> outlist = new ArrayList<Integer>();
	   if(str.equals("[]"))
		   return outlist;
	   str = str.substring(1,str.length() - 1);  
	   
	   String[] strs = str.split(",");
	 
	   for(String s:strs)
	   {
		   if(s != strs[0])
			   s = s.substring(1);
		   outlist.add(Integer.valueOf(s));
	   } 
	   return outlist;
   }
	
	public static Message mapToMessage(HashMap<String, String> query){
		Message outmsg = null;
		int messageType = 1;
		
		Integer receiverID = null, queueID, priority = 5, senderID;
		
		senderID = Integer.valueOf(query.get("sender"));
		queueID = Integer.valueOf(query.get("queue"));
		String message = query.get("message");
		
		if(query.containsKey("receiver"))
			receiverID = Integer.valueOf(query.get("receiver"));
		
		if(query.containsKey("priority"))
			priority = Integer.valueOf(query.get("priority"));
		
		if(!query.containsKey("context") ) {
			messageType = 1;
		}
		
		if(messageType == 1) {
			outmsg = new Message(senderID, receiverID, queueID, priority, message);
		}
		
		if(messageType == 2) {
			int context = Integer.valueOf(query.get("context"));
			outmsg = new Message(senderID, receiverID, queueID, context, priority, message);
		}
		
		return outmsg;	
	
	}
	
	
	public static String messageToString(Message msg){
		String outstr = "sender:" + msg.getSenderID()
				+ ";priority:" +msg.getPriority() + ";message:" + msg.getMessage();
		if(msg.getQueueID() != null)
			outstr += ";queue:" + msg.getQueueID();
		else
			outstr += ";queue:" + msg.getQueueIDs();
		if(msg.getReceiverID() != null)
			outstr += ";receiver:" + msg.getReceiverID();
		if(msg.getContext() != null)
			outstr += ";context:" + msg.getContext();
		
		return outstr;		
	}
	
	public static String ResultMessageToString(ResultSet rs) throws SQLException{
		String outstr = "sender:" + rs.getInt("sender_id")
				+ ";queue:" + rs.getInt("queue_id") + ";priority:" + rs.getInt("priority")
				+ ";message:" + rs.getString("text");
		if(rs.getInt("context") != 0)
			outstr += ";context:" + rs.getInt("context");
		if(rs.getInt("receiver_id") != 0)
			outstr += ";receiver:" + rs.getInt("receiver_id");
		return outstr;
	}
}
