package model_safecollection;

import java.net.InetAddress;

import model_impl.Session;

public class SessionCollection extends safecollection<Session>{
	
	public Session self;
	
	public SessionCollection(Session self)
	{
		this.self= self;
	}
	
	/**
	 * If session exists, update timestamp and connection info.<br/>
	 * If not, create new session.<br/>
	 * Calls notifyAll whenever a change is done
	 * @param srcNickname equivalent to Message.src
	 * @param packet
	 */
	public Session visit (String srcNickname, InetAddress address, int port , long timestamp , int ID )
	{
		if(srcNickname.contentEquals(self.nickname) || srcNickname.isEmpty())
			return null;
		
		Session s= super.find(srcNickname);
		if(s!=null)
		{
			if(ID!=-1 && !self.nickname.isEmpty())
				s.ID= ID;
			
			s.address= address;
			s.port= port;
			s.timestamp= timestamp;
			s.notify= true;
			
			return s;
		}
		
		s= new Session(ID, srcNickname, address, port,timestamp);
		int newID = super.push(s);
		if(self.isServer())
			s.ID= newID;
		
		synchronized(this) {
			this.notifyAll();
		}
		
		return s;
	}
}
