package model_impl;

import java.io.StringReader;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import enums.MsgType;
import environment.Env;
import model.MsgModel;

public class Msg extends MsgModel{
	
	public Msg(int id, String srcNickname, String destNickname, MsgType msgType, String content, long timestamp)
	{
		this.id= id;
		this.srcNickname= srcNickname;
		this.destNickname= destNickname;
		this.msgType= msgType;
		this.content= content;
		this.timestamp= timestamp;
	}
	
	public Msg(byte[] rawMsg) throws Exception
	{
		//decipher byte stream
		boolean isFile= false;
		String rawText="";
		for(byte b: rawMsg)
		{
			rawText+= (char) b;
			if(rawText.endsWith("</msg>"))
				break;
			
			if(rawText.endsWith("<msgType>FILE_"))
				isFile=true;
		}
		
		//if is file, extract content separately
		if(isFile)
		{
			String[] A=rawText.split("<content>",2);
			String[] B=A[1].split("</content>",2);
			
			rawText=A[0]+B[1];
			this.content=B[0];
		}
		
		DocumentBuilder db = Env.dbf.newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(rawText));
		Document doc = db.parse(is);
		Element element = (Element) doc.getElementsByTagName("msg").item(0);
		
		this.id= Integer.parseInt(element.getElementsByTagName("id").item(0).getTextContent());
		this.srcNickname= element.getElementsByTagName("srcNickname").item(0).getTextContent();
		this.destNickname= element.getElementsByTagName("destNickname").item(0).getTextContent();
		this.msgType= MsgType.valueOf(element.getElementsByTagName("msgType").item(0).getTextContent());
				
		Element contentElement = (Element) element.getElementsByTagName("content").item(0);
		switch(this.msgType)
		{
			case UPDATE_LIST:
				contentElement = (Element) contentElement.getElementsByTagName("sessions").item(0);
				
				this.content="<sessions>";
				NodeList children = contentElement.getElementsByTagName("session");
				for(int i=0; i<children.getLength(); i++)
					this.content += new Session((Element) children.item(i)).serialize();
				this.content+="</sessions>";
				
				break;
			
			case FILE_UP:
			case FILE_DOWN:
				break;
				
			default:
				this.content= contentElement.getTextContent();
		}
		
		
		this.timestamp= Long.parseLong(element.getElementsByTagName("timestamp").item(0).getTextContent());
	}
	
	@Override
	public String key() {
		return this.timestamp+":"+this.srcNickname;
	}

	@Override
	public String serialize() {
		return "<msg>"
				+ "<id>"+id+"</id>"
				+ "<srcNickname>"+srcNickname+"</srcNickname>"
				+ "<destNickname>"+destNickname+"</destNickname>"
				+ "<msgType>"+msgType+"</msgType>"
				+ "<content>"+content+"</content>"
				+ "<timestamp>"+timestamp+"</timestamp>"
				+ "</msg>";
	}
	
	public String toChatMsg()
	{
		switch(this.msgType)
		{
			case FILE_UP:
				return "\n[FILE : click to download]\n\n"+this.srcNickname+" @ "+Env.dateFormat.format(new Date(this.timestamp))+"\n\n"+this.content;
			default:
				return "\n"+this.srcNickname+" @ "+Env.dateFormat.format(new Date(this.timestamp))+"\n\n"+this.content;
		}
		
	}
}
