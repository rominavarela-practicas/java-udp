package DistributedUDPChat;

import java.io.File;

import enums.MsgType;
import environment.Console;
import environment.Env;
import environment.IOUtil;
import environment.Routine;
import listener.InputListener;
import listener.OutputListener;
import model_impl.Msg;
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
		sessionPool = new SessionCollection(new Session(0, "", con.address, con.port, Env.getTime()));
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
		    						//send id
		    						Session client= sessionPool.find(msgIn.srcNickname);
		    						Msg outMsg= new Msg(0, "", 
		    								msgIn.srcNickname, MsgType.SERVER_PING, 
		    								""+client.ID,
		    								Env.getTime());
		    						
//		    						System.out.println("server hello "+msgIn.srcNickname);
			    					outbox.push(outMsg);
			    					
			    					//update list
			    					if(sessionPool.size()-1 > Integer.parseInt(msgIn.content))
			    					{
			    						System.out.println("list-update for "+msgIn.srcNickname);
			    						outMsg= new Msg(0, "", 
			    								msgIn.srcNickname, MsgType.UPDATE_LIST, 
			    								sessionPool.serialize(),
			    								Env.getTime());
				    					
		    							outbox.push(outMsg);
			    					}
			    						
		    						break;
		    					}
		    					
			    				case PRIVATE:
			    				{
			    					for(Session s: sessionPool.getList())
			    						if(!s.nickname.contentEquals(msgIn.srcNickname))
			    						{
			    							Msg msgOut= new Msg(0, msgIn.srcNickname, 
			    									s.nickname, MsgType.PRIVATE, msgIn.content, Env.getTime());
			    							outbox.push(msgOut);
			    						}
			    					
			    					break;
			    				}
			    				
			    				case FILE_UP:
			    				{
			    					try
			    					{
			    						//upload
			    						String filename= msgIn.content.substring(0, msgIn.content.indexOf(';'));
				    					File dir= new File(".up");
				    					if(!dir.exists())
				    						dir.mkdir();
				    					
				    					File f= new File(dir+"/"+filename);
				    					IOUtil.write(f, msgIn.content);
				    					System.out.println("File up "+f.getAbsolutePath());
				    					
				    					//notify
				    					if(msgIn.destNickname.contentEquals(Env.BROADCAST_NAME))
				    					{
				    						for(Session s: sessionPool.getList())
				    						{
				    							Msg msgOut= new Msg(0, msgIn.srcNickname, 
				    									s.nickname, MsgType.FILE_UP, f.getName(), Env.getTime());
				    							outbox.push(msgOut);
				    						}
				    					}
				    					else
				    					{
				    						Msg msgOut= new Msg(msgIn.id, msgIn.srcNickname, 
			    									msgIn.destNickname, MsgType.FILE_UP, f.getName(), Env.getTime());
			    							outbox.push(msgOut);
				    					}
			    					}
			    					catch(Exception ex)
			    					{
			    						System.out.println("Error @ ChatServerThread.upload >> "+ex.getMessage());
			    			    		Console.log("Error @ ChatServerThread.upload >> "+ex.getMessage());
			    					}
			    					
			    					break;
			    				}
			    				
			    				case FILE_DOWN:
			    				{
			    					System.out.println("Downloading "+msgIn.content);
			    					File dir= new File(".up");
			    					File f= new File(dir+"/"+msgIn.content);
			    					
			    					String content= IOUtil.read(f);
			    					System.out.println("SERVER CONTENT... "+content);
			    					Msg msgOut= new Msg(0, "", 
			    							msgIn.srcNickname, MsgType.FILE_DOWN, content, Env.getTime());
	    							outbox.push(msgOut);
	    							break;
			    				}
		    					
		    					default:
			    					System.out.println("UNKNOWN "+msgIn.msgType+" >> "+msgIn);
		    				}
		    		 }
		    	 }
		    	 catch(Exception ex)
		    	 {
		    		 System.out.println("Error @ ChatServerThread >> "+ex.getMessage());
		    		 Routine.exit();
		    	 }
		     }
		});
	}
	
}