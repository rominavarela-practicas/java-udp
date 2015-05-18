package DistributedUDPChat;

import java.io.File;

import chatroom.ChatRoom;
import chatroom.Conversation;
import enums.MsgType;
import environment.Console;
import environment.Env;
import environment.IOUtil;
import environment.Routine;
import lib.LZString;
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
	
	InputListener			in;
	OutputListener			out;
	
	public DistributedUDPChatClient(String nickname) throws Exception
	{
		Env.init();
		con = new Connection(false);
		
		//pools
		sessionPool = new SessionCollection(new Session(-1, nickname, con.address, con.port, Env.getTime()));
		inbox= new MsgCollection(false);
		outbox= new MsgCollection(true);
		
		//listeners
		in= new InputListener(con,sessionPool,inbox);
		out= new OutputListener(con,sessionPool,outbox);
		
		serverSyncThread().start();
		chatThread().start();
		
		try
		{
    		//show broadcast pane
    		ChatRoom.push(Env.BROADCAST_NAME);
		}
		catch(Exception e){}
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
		    						Env.LAST_SERVER_SYNC = Env.getTime();
		    					
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
					    						s= sessionPool.visit(s.nickname, s.address, s.port, s.timestamp, s.ID);
					    						checkSession(s);
					    					}
				    					break;
				    				}
				    				
				    				case PRIVATE:
				    				{
				    					if(msgIn.id!=0)
					    					checkSession(sessionPool.find(msgIn.srcNickname));
					    				
				    					ChatRoom.pushMsg(msgIn);
				    					break;
				    				}
				    				
				    				case HELLO:
				    				{
				    					checkSession(sessionPool.find(msgIn.srcNickname));
				    					
				    					Conversation c= ChatRoom.find(msgIn.srcNickname);
				    					c.hello_msg_timestamp= Env.getTime();
				    					break;
				    				}
				    				
				    				case FILE_UP:
				    				{
				    					if(msgIn.id!=0)
					    					checkSession(sessionPool.find(msgIn.srcNickname));
					    				
				    					ChatRoom.pushMsg(msgIn);
				    					break;
				    				}
				    				
				    				case FILE_DOWN:
				    				try
				    				{
				    					System.out.println("Downloading... "+msgIn.content);
				    					
				    					String filename_content[]= msgIn.content.split(";", 2);
				    					File dir= new File("downloads");
				    					if(!dir.exists())
				    						dir.mkdir();
				    					File f= new File(dir+"/"+filename_content[0]);
				    					
				    					String compressed= filename_content[1];
				    					System.out.println("Content... "+compressed);
				    					String decompressed= LZString.decompressFromUTF16(compressed);
				    					System.out.println("Dec... "+decompressed);
				    					@SuppressWarnings("static-access")
										byte[] data= Env.B64.decode(decompressed);
				    					IOUtil.write(f, data);
				    					
				    					System.out.println("File down "+f.getAbsolutePath());
				    				}
				    				catch(Exception e){e.printStackTrace();}
			    					break;
				    				
				    				default:
				    					System.out.println("UNKNOWN "+msgIn.msgType+" >> "+msgIn);
			    				}
		    				}
		    				
		    				
		    		 }
		    	 }
		    	 catch(Exception ex)
		    	 {
		    		 System.out.println("Error @ ChatClientThread >> "+ex.getMessage());
		    		 Routine.exit();
		    	 }
		     }
		});
	}
	
	public void checkSession(Session s)
	{		
		if(s.isNew)
		{
			Console.log("Met "+s.nickname);
			ChatRoom.push(s.nickname);
			s.isNew= false;
		}
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
			 				 "", MsgType.SERVER_PING, ""+sessionPool.size(), Env.getTime());
	    			 
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
				    			Env.server= new DistributedUDPChatServer();
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
	
	public Msg sendPrivateMsg(String dstNickname, String content)
	{
		Msg msgOut= new Msg(sessionPool.self.ID, sessionPool.self.nickname, 
				dstNickname, MsgType.PRIVATE, content, Env.getTime());
		outbox.push(msgOut);
		
		return msgOut;
	}
	
	public Msg sendFileUpMsg(String dstNickname, String content)
	{
		Msg msgOut= new Msg(sessionPool.self.ID, sessionPool.self.nickname, 
				dstNickname, MsgType.FILE_UP, content, Env.getTime());
		outbox.push(msgOut);
		
		return msgOut;
	}
	
	public Msg sendFileDownMsg(String filename)
	{
		Msg msgOut= new Msg(sessionPool.self.ID, sessionPool.self.nickname, 
				"", MsgType.FILE_DOWN, filename, Env.getTime());
		outbox.push(msgOut);
		System.out.println(msgOut.serialize());
		
		return msgOut;
	}
	
	public void sendHelloMsg(String dstNickname)
	{		
		Msg msgOut= new Msg(sessionPool.self.ID, sessionPool.self.nickname, 
				dstNickname, MsgType.HELLO, "", Env.getTime());
		outbox.push(msgOut);
	}
}