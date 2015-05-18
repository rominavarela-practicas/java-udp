package model_impl;

import java.io.StringReader;
import java.net.Inet4Address;
import java.net.InetAddress;

import javax.xml.parsers.DocumentBuilder;

import model.SessionModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import environment.Env;

public class Session extends SessionModel{
	
	public boolean		isNew;
	public boolean		isExt;

	public Session(int ID, String nickname, InetAddress address, int port, long timestamp)
	{
		this.ID = ID;
		this.nickname= nickname;
		this.address= address;
		this.port= port;
		this.timestamp= timestamp;
		
		this.isNew= true;
	}
	
	public Session(String s) throws Exception
	{
		DocumentBuilder db = Env.dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(s));
		Document doc = db.parse(is);
		
		Element rootElement = (Element) doc.getElementsByTagName("session").item(0);
		
		this.ID = Integer.parseInt(rootElement.getElementsByTagName("id").item(0).getTextContent());
		this.nickname= rootElement.getElementsByTagName("nickname").item(0).getTextContent();
		this.address= Inet4Address.getByName(rootElement.getElementsByTagName("address").item(0).getTextContent());
		this.port= Integer.parseInt(rootElement.getElementsByTagName("port").item(0).getTextContent());
		this.timestamp= System.currentTimeMillis();
	}
	
	public Session(Element rootElement) throws Exception
	{
		this.ID = Integer.parseInt(rootElement.getElementsByTagName("id").item(0).getTextContent());
		this.nickname= rootElement.getElementsByTagName("nickname").item(0).getTextContent();
		this.address= Inet4Address.getByName(rootElement.getElementsByTagName("address").item(0).getTextContent());
		this.port= Integer.parseInt(rootElement.getElementsByTagName("port").item(0).getTextContent());
		this.timestamp= System.currentTimeMillis();
	}
	
	public boolean isServer()
	{
		return this.nickname.isEmpty();
	}

	@Override
	public String key() {
		return this.nickname;
	}
	
	@Override
	public String serialize()
	{
		return "<session>"
				+"<id>"+ID+"</id>"
				+"<nickname>"+nickname+"</nickname>"
				+"<address>"+address.getHostAddress()+"</address>"
				+"<port>"+port+"</port>"
			+"</session>";
	}
	
}
