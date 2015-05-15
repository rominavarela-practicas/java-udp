package DistributedUDPChat;

import enums.MsgType;
import environment.Console;
import environment.Env;
import listener.InputListener;
import listener.OutputListener;
import model_impl.Msg;
import model_impl.Ping;
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
			    			
		    				while((msgIn=inbox.pop())!=null) switch(msgIn.msgType)
		    				{
			    				case SERVER_PING:
		    					{
		    						Ping ping = new Ping(msgIn.content);
		    						
		    						// update self id
		    						if(ping.clientId!=sessionPool.self.ID)
		    						{
		    							sessionPool.self.ID= ping.clientId;
		        						Console.log("New ID: "+sessionPool.self.ID);
		    						}
		    						
		    						// update session pool
		    						if(sessionPool.size()<ping.serverListSize-1)
		    						{
			    						Console.log("updating contact list...");
			    						Msg msgOut=
			    								new Msg(sessionPool.self.ID, sessionPool.self.nickname,
			    								"", MsgType.UPDATE_LIST_REQ, 
				    							"", System.currentTimeMillis());
			    						Console.log(msgOut.serialize());
			    						outbox.push(msgOut);
		    							
		    						}
		    						
		    						Env.IS_SERVER_SYNC=true;
		    						Env.SERVER_LAUNCHED=false;
		    						break;
		    					}
		    					
			    				case UPDATE_LIST_RES:
			    				{
			    					Session s= new Session(msgIn.content);
			    					s= sessionPool.visit(s.nickname, s.address, s.port, s.timestamp, s.ID);
			    					
			    					if(s.isNew)
			    					{
			    						Console.log("Met "+s.nickname);
			    						s.isNew= false;
			    						
			    						if(msgIn.srcNickname.isEmpty())
			    						{
				    						Msg outMsg= new Msg(sessionPool.self.ID, sessionPool.self.nickname,
				    								s.nickname, MsgType.UPDATE_LIST_RES, 
				    								sessionPool.self.serialize(),
				    								System.currentTimeMillis());
				    						
				    						outbox.push(outMsg);
			    							
			    						}
			    					}
			    					
			    					return;
			    				}
			    				
		    					default:
		    						break;
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
		    		 Ping ping = new Ping(sessionPool.self.ID, -1);
			    	 Msg msgOut= new Msg(sessionPool.self.ID, sessionPool.self.nickname, 
			 				 "", MsgType.SERVER_PING, ping.serialize(), System.currentTimeMillis());
			    	 
		    		 synchronized(msgOut)
		    		 {
		    			// 1 - IF SERVER IS NOT IN SYNC
			    		while(!Env.IS_SERVER_SYNC)
			    		{
			    			outbox.push(msgOut);
			    			msgOut.wait();
			    			Thread.sleep(1000);
			    			
			    			// try to connect with server
				    		for(int chances=20; chances>=0; chances--)
				   			{
				   				Thread.sleep(100);
				   				if(Env.IS_SERVER_SYNC)
				   				{
				   					Console.log("Successfull Server Sync");
				   					break;
				   				}
				   			}
				    		 
				    		// if server wasn't found, launch own server
				    		//TODO HACER VOTACION SI EL ID != -1
				    		if(!Env.IS_SERVER_SYNC && !Env.SERVER_LAUNCHED)
					    		try
					    		{
					    			Console.log("Server not found. Starting up own server.");
					    			server= new DistributedUDPChatServer();
					    			Env.SERVER_LAUNCHED=true;
					    			Thread.sleep(1000);
					    		}
				    			catch(Exception e){}
			    		}
			    		// 2 - PING TO SERVER EVERY SECOND TO PROOVE IT IS ALIVE
			    		while(Env.IS_SERVER_SYNC)
			    		{
			    			Thread.sleep(Env.SERVER_TIMEOUT);
			    			outbox.push(msgOut);
			    			Env.IS_SERVER_SYNC=false;
				    		
				    		// try to connect with server
				    		for(int chances=30; chances>=0; chances--)
				   			{
				   				Thread.sleep(100);
				   				if(Env.IS_SERVER_SYNC)
				   					break;
				   			}
			    		}
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