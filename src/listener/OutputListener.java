package listener;

import java.net.DatagramPacket;

import environment.Console;
import environment.Env;
import DistributedUDPChat.Connection;
import model_impl.Msg;
import model_impl.Session;
import model_safecollection.MsgCollection;
import model_safecollection.SessionCollection;

/**
 * Take Msg objects from outbox and send them according to the MsgType
 * <ul>
 * <li>SERVER_PING client sends message to server, server replies message to client
 * <li>LIST_UPDATE server-only; every user receives updated list
 * </ul>
 * @author romina
 */
public class OutputListener {
	
	Connection 					con;
	SessionCollection			sessionPool;
	MsgCollection				outbox;
	
	public OutputListener(Connection con, SessionCollection sessionPool, MsgCollection outbox)
	{
		this.con=con;
		this.sessionPool=sessionPool;
		this.outbox= outbox;
		
		listenerThread().start();
	}
	
	private Thread listenerThread()
	{
		return new Thread(new Runnable()
		{
		     public void run() 
		     {
		    	Console.log("Output listener up");
		    	Msg msg=null;
		 		
		 		while(true) try {
		 			synchronized (outbox){
			 			if(outbox.isEmpty()) outbox.wait();
		 			}
	 				
	 				while((msg=outbox.pop())!=null)
	 				{
			 			byte[] 			data 	= msg.toByteArray();
						int 			len 	= data.length;
						DatagramPacket 	reply;
						Session 		dest;
						
		 				System.out.println("out "+msg.serialize());
						
						switch(msg.msgType)
	 					{
							//SERVER_PING client sends message to server, server replies message to client
		 					case SERVER_PING:
		 						if(con.isServer())
		 						{
		 							dest = sessionPool.find(msg.destNickname);
		 							reply= new DatagramPacket(data.clone(), len, dest.address, dest.port);
		 							con.socket.send(reply);
		 						}
		 						else synchronized(msg)
		 						{
		 							reply= new DatagramPacket(data.clone(), len, Env.SERVER_ADDRESS, Env.SERVER_PORT);
		 							con.socket.send(reply);
		 							msg.notifyAll();
		 						}
	 							break;
		 					
		 					case UPDATE_LIST:
	 							dest = sessionPool.find(msg.destNickname);
	 							reply= new DatagramPacket(data.clone(), len, dest.address, dest.port);
	 							con.socket.send(reply);
	 							break;
		 					
							default:
								break;
		 					
	 					}
	 					
	 				}
	 			}
	 			catch(Exception ex)
	 			{
	 				System.out.println("Error @ OutputListener >> "+ex.getMessage());
	 			}
		     }
		});
	}
}
