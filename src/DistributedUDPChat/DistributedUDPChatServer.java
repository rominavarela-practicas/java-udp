package DistributedUDPChat;

import enums.MsgType;
import listener.InputListener;
import listener.OutputListener;
import model_impl.Msg;
import model_impl.Ping;
import model_impl.Session;
import model_safecollection.MsgCollection;
import model_safecollection.SessionCollection;

public class DistributedUDPChatServer {
	
	public Connection 				con;
	public SessionCollection		sessionPool;
	public MsgCollection			inbox;
	public MsgCollection			outbox;
	
	InputListener			in;
	OutputListener			out;
	
	public DistributedUDPChatServer() throws Exception
	{
		con = new Connection(true);
		
		//pools
		sessionPool = new SessionCollection(new Session(0, "", con.address, con.port, System.currentTimeMillis()));
		inbox= new MsgCollection(false);
		outbox= new MsgCollection(true);
		
		//listeners
		in= new InputListener(con,sessionPool,inbox);
		out= new OutputListener(con,sessionPool,outbox);
		
		chatThread().start();
	}
	
	private Thread chatThread()
	{
		return new Thread(new Runnable()
		{
		     public void run() 
		     {
		    	 Msg msgIn;
		    	 
		    	 while(true) try
		    	 {
		    		 synchronized(inbox)
		    		 {
		    			 if(inbox.isEmpty())
			    				inbox.wait();
			    			
		    				while((msgIn=inbox.pop())!=null) switch(msgIn.msgType)
		    				{
			    				case SERVER_PING:
		    					{
		    						Ping ping = new Ping(
		    								sessionPool.find(msgIn.srcNickname).ID, 
		    								sessionPool.size());
		    						
		    						//answer ping
		    						Msg outMsg= new Msg(0, "", 
		    								msgIn.srcNickname, MsgType.SERVER_PING, 
		    								ping.serialize(),
		    								System.currentTimeMillis());
		    						
			    					outbox.push(outMsg);
			    						
		    						break;
		    					}
		    					
			    				case UPDATE_LIST_REQ:
			    				{
			    					System.out.println(msgIn);
			    					for(Session s: sessionPool.getList())
			    						if(s.ID!= msgIn.id)
			    						{
					    					System.out.println("notify "+s.ID+" to "+msgIn.srcNickname);
					    					
			    							Msg outMsg= new Msg(0, "", 
				    								msgIn.srcNickname, MsgType.UPDATE_LIST_RES, 
				    								s.serialize(),
				    								System.currentTimeMillis());
			    							
					    					System.out.println(outMsg.serialize());
			    							
			    							outbox.push(outMsg);
			    						}
			    					break;
			    				}
		    					
		    					default:
			    					System.out.println("UNKNOWN "+msgIn);
		    				}
		    		 }
		    	 }
		    	 catch(Exception ex)
		    	 {
		    		 System.out.println("Error @ ChatServerThread >> "+ex.getMessage());
		    		 ex.printStackTrace();
		    	 }
		     }
		});
	}
}