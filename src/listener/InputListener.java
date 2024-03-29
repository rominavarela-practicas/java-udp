package listener;

import environment.Console;
import environment.Env;
import DistributedUDPChat.Connection;
import model_impl.Msg;
import model_safecollection.MsgCollection;
import model_safecollection.SessionCollection;

public class InputListener {
	
	Connection 					con;
	SessionCollection			sessionPool;
	MsgCollection				inbox;
	InputListener				self;
	
	public InputListener(Connection con, SessionCollection sessionPool, MsgCollection inbox)
	{
		this.con=con;
		this.sessionPool=sessionPool;
		this.inbox= inbox;
		self= this;
		
		listenerThread().start();
	}
	
	private Thread listenerThread()
	{
		return new Thread(new Runnable()
		{
		     public void run() 
		     {
		    	 Console.log("Input listener up");
		 		
		 		while(true)
		 			try
		 			{
	 					con.socket.receive(con.packet);
		 				Msg msg = new Msg(con.packet.getData());
		 				inbox.push(msg);
		 				
		 				//lamport sync
		 				long curr= Env.getTime();
		 				if(msg.timestamp > curr)
		 					Env.CLOCK_DIFFERENCE+= msg.timestamp - curr;
		 					
		 				sessionPool.visit(msg.srcNickname, con.packet.getAddress(), con.packet.getPort(), msg.timestamp, msg.id);
			 			
		 				synchronized(inbox){
			 				inbox.notifyAll();
		 				 }
		 			}
		 			catch(Exception ex)
		 			{
		 				System.out.println("Error @ InputListener >> "+ex.getMessage());
			    		Console.log("Error @ InputListener >> "+ex.getMessage());
		 			}
		     }
		});
	}
}
