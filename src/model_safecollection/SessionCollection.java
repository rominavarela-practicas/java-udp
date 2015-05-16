package model_safecollection;

import java.io.StringReader;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import environment.Env;
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
	
	public String serialize()
	{
		String s="<sessions>";
		
		for(Session session: super.getList())
			s+= session.serialize();
		
		return s+"</sessions>";
	}
	
	public ArrayList<Session> deserialize(String s) throws Exception
	{
		ArrayList<Session> list = new ArrayList<Session>();
		
		DocumentBuilder db = Env.dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(s));
		Document doc = db.parse(is);
		Element element = (Element) doc.getElementsByTagName("sessions").item(0);
		
		NodeList children = element.getElementsByTagName("session");
		for(int i=0; i<children.getLength(); i++)
			list.add(new Session((Element) children.item(i)));
		
		return list;
	}
}
