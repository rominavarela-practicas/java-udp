package chatroom;

import java.util.ArrayList;

import model_impl.Session;
import model_safecollection.SessionCollection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

public class ContactList {
	public static SessionCollection sessionPool;
	public static ObservableList<String> 	nicknamesList;
	
	public static void init(SessionCollection sessionPool, ListView<String> 	ChatList)
	{
		ContactList.sessionPool= sessionPool;
		nicknamesList= FXCollections.observableList(new ArrayList<String>());
		ChatList.setItems(nicknamesList);
		
		listenerThread().run();
	}
	
	private static Thread listenerThread()
	{
		return new Thread(new Runnable()
		{
		     public void run() 
		     {		    	 
		    	 while(true)
		    	 try
		    	 {
		 		    	synchronized(sessionPool)
		 		    	{
		 		    		for(Session s: sessionPool.getSublist(nicknamesList.size()))
		 		    			nicknamesList.add(s.nickname);
		 		    		
		 		    		sessionPool.wait();
		 		    	}
		    	 }
		    	catch(Exception ex){}
		     }
		});
	}
	
}
