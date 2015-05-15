package model_impl;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import environment.Env;
import model.PingModel;

public class Ping extends PingModel{
	
	public Ping(int clientId, int serverListSize){
		this.clientId= clientId;
		this.serverListSize= serverListSize;
	}
	
	public Ping(String s) throws Exception
	{
		DocumentBuilder db = Env.dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(s));
		Document doc = db.parse(is);
		
		Element element = (Element) doc.getElementsByTagName("ping").item(0);
		
		this.clientId = Integer.parseInt(element.getElementsByTagName("clientId").item(0).getTextContent());
		this.serverListSize = Integer.parseInt(element.getElementsByTagName("serverListSize").item(0).getTextContent());
	}

	@Override
	public String key() {
		return null;
	}

	@Override
	public String serialize() {
		return "<ping>"
				+"<clientId>"+clientId+"</clientId>"
				+"<serverListSize>"+serverListSize+"</serverListSize>"
			+"</ping>";
	}

}
