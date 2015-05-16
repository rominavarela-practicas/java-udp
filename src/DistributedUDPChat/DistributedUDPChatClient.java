package DistributedUDPChat;

import enums.MsgType;
import environment.Console;
import environment.Env;
import listener.InputListener;
import listener.OutputListener;
import model_impl.Msg;
import model_impl.Session;
import model_safecollection.MsgCollection;
import model_safecollection.SessionCollection;

public class DistributedUDPChatClient {
	
	public Connection 				con;
	public SessionCollection		sessionPool;
	public MsgCollection			inbox;
	public MsgCollection			outbox;

	public DistributedUDPChatServer server;
	
	InputListener			in;
	OutputListener			out;
	
	public DistributedUDPChatClient(String nickname) throws Exception
	{
		Env.init();
		con = new Connection(false);
		
		//pools
		sessionPool = new SessionCollection(new Session(-1, nickname, con.address, con.port, System.currentTimeMillis()));
		inbox= new MsgCollection(false);
		outbox= new MsgCollection(true);
		
		//listeners
		in= new InputListener(con,sessionPool,inbox);
		out= new OutputListener(con,sessionPool,outbox);
		
		serverSyncThread().start();
		chatThread().start();
	}
	
	/**
	 * Chat Thread listens to inbox and coordinates respective flux
	 */
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
			    			
		    				while((msgIn=inbox.pop())!=null)
		    				{
		    					if(msgIn.srcNickname.isEmpty())
		    						Env.LAST_SERVER_SYNC = System.currentTimeMillis();
		    					
		    					switch(msgIn.msgType)
			    				{			    					
				    				case SERVER_PING:
			    					{
			    						int id= Integer.parseInt(msgIn.content);
			    						
			    						// update self id
			    						if(id!=sessionPool.self.ID)
			    						{
			    							sessionPool.self.ID= id;
			        						Console.log("New ID: "+id);
			    						}
			    						
			    						Env.SERVER_LAUNCHED=false;
			    						break;
			    					}
			    					
				    				case UPDATE_LIST:
				    				{
				    					for(Session s: sessionPool.deserialize(msgIn.content))
				    						if(!s.nickname.contentEquals(sessionPool.self.nickname))
					    					{
					    						sessionPool.visit(s.nickname, s.address, s.port, s.timestamp, s.ID);
					    						
					    						if(s.isNew)
						    					{
						    						Console.log("Met "+s.nickname);
						    						s.isNew= false;
						    					}
					    					}
				    					
				    					return;
				    				}
				    				
			    					default:
			    						break;
			    				}
		    				}
		    				
		    				
		    		 }
		    	 }
		    	 catch(Exception ex)
		    	 {
		    		 System.out.println("Error @ ChatClientThread >> "+ex.getMessage());
		    	 }
		     }
		});
	}
	
	/**
	 * Server Sync Thread
	 */
	private Thread serverSyncThread()
	{
		return new Thread(new Runnable()
		{
		     public void run()
		     {
		    	 while(true) try
		    	 {
	    			while(Env.IS_SERVER_SYNC())
	    				Thread.sleep(1000);
	    			
	    			 Msg msgOut= new Msg(sessionPool.self.ID, sessionPool.self.nickname, 
			 				 "", MsgType.SERVER_PING, ""+sessionPool.size(), System.currentTimeMillis());
	    			 
	    			 synchronized(msgOut){
			    		 outbox.push(msgOut);
	    				 msgOut.wait();
	    			 }
	    			 
	    			 int chances=20;
	    			 while(!Env.IS_SERVER_SYNC() && chances>=0)
	    			 {
	    				 chances--;
	    				 Thread.sleep(100);
	    			 }
	    			 
	    			 if(Env.IS_SERVER_SYNC())
	    			{
	    				 Console.log("Successfull Server Sync");
	    				 //return;//TODO currency problem patch
	    			}
	    			 else
	    				 if(!Env.SERVER_LAUNCHED)
				    		try
				    		{
				    			Env.SERVER_LAUNCHED=true;
				    			Console.log("Server not found. Starting up own server.");
				    			server= new DistributedUDPChatServer();
				    			Thread.sleep(1000);
				    		}
			    			catch(Exception e)
	    			 		{
				    			Console.log("Server launch failed.");
	    			 		}
		    	 }
		    	 catch(Exception ex)
		    	 {
		    		 System.out.println("Error @ ServerSyncThread >> "+ex.getMessage());
		    		 ex.printStackTrace();
		    	 }
		     }
		});
	}
	
}