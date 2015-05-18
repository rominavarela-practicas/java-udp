package chatroom;

import java.util.ArrayList;

import model_impl.Msg;
import environment.Console;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

public class ChatRoom {

	static ArrayList<Conversation> 	conversations;
	static ObservableList<String> 	nicknamesList;
	
	public static void init(ListView<String> ChatList)
	{
		conversations= new ArrayList<Conversation>();
		nicknamesList= FXCollections.observableList(new ArrayList<String>());
		ChatList.setItems(nicknamesList);
	}
	
	static String newDstNickname="";
	public static synchronized void push(String dstNickname)
	{
		newDstNickname= dstNickname;
		
		new Thread(new Runnable()
		{
		     public void run() 
		     {
		    	 try
		    	 {		    			
		    		conversations.add(new Conversation(newDstNickname));
    				nicknamesList.add(newDstNickname);
		    	 }
		    	 catch(Exception ex)
		    	 {
		    		 System.out.println("Error @ ChatRoom.updateListListenerThread >> "+ex.getMessage());
		    		 Console.log("Error @ ChatRoom.updateListListenerThread >> "+ex.getMessage());
		    	 }
		     }
		}).run();
	}
	
	public static Conversation find(String dstNickname)
	{
		for(Conversation c: conversations)
			if(c.dstNickname.contentEquals(dstNickname))
				return c;
		
		return null;
	}
	
	public static void pushMsg(Msg msg)
	{
		try
		{
			if(msg.id==0)
			{
				Conversation c= conversations.get(0);
				if(c.enabled)
					c.msgStack.push(msg.toChatMsg());
			}
			else
			for(Conversation c: conversations)
				if(c.dstNickname.contentEquals(msg.srcNickname))
				{
					if(c.enabled)
						c.msgStack.push(msg.toChatMsg());
					break;
				}
		}
		catch(Exception ex){}
		
	}
}
