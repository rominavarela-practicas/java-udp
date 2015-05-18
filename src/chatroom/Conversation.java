package chatroom;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Stack;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import lib.LZString;
import model_impl.Msg;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import environment.Console;
import environment.Env;
import environment.IOUtil;

public class Conversation {
	
	public String dstNickname;
	public boolean enabled;
	public long hello_msg_timestamp;
	
	public Stack<String>	msgStack;
	public File 			fileToShare;
	public Room 			room;
	
	public Thread listener;
	
	public Conversation(String dstNickname)
	{
		this.dstNickname= dstNickname;
		this.enabled= true;
		this.hello_msg_timestamp= Env.getTime();
		this.msgStack= new Stack<String>();
		
		try
	   	 {
	   		@SuppressWarnings("deprecation")
			URL loadUrl = IOUtil.getFile("view/room.fxml").toURL();
			FXMLLoader fxmlLoader = new FXMLLoader(loadUrl);
			AnchorPane roomPane = (AnchorPane) fxmlLoader.load();
	   		
			room = new Room(dstNickname, roomPane);
			setEventHandlers();
	   	 }
	   	 catch(Exception ex)
	   	 {
	   		 System.out.println("Error @ Conversation.constructor >> "+ex.getMessage());
	   		 Console.log("Error @ Conversation.constructor >> "+ex.getMessage());
	   	 }
		
		listener=conversationListener();
		listener.start();
	}
	
	void setEventHandlers()
	{
		//Send
		room.MessageSendButton.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				Msg msg= Env.client.sendPrivateMsg(dstNickname, room.MessageTextField.getText());
				room.MessageTextField.setText("");
				room.ChatHistoryContent.add(msg.toChatMsg());
			}
		});
		
		//ATTACH
		//Attach
		room.MessageAttachButton.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				room.MessagePane.setVisible(false);
				room.AttachPane.setVisible(true);
			}
		});
		
		//AttachSelect
		room.AttachOkButton.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				
				JFileChooser chooser = new JFileChooser(); 
			    chooser.setDialogTitle("Select a file to share.");
			    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);
			    
			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
			    {
			    	fileToShare= chooser.getSelectedFile();
			    	room.AttachTextField.setText(fileToShare.getAbsolutePath().toString());
			    }
			    
			}
		});
		
		//AttachOk
		room.AttachSelectButton.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				
				try
				{
					//get data
					byte[] data = Files.readAllBytes(fileToShare.toPath());
					
					//compress
					@SuppressWarnings("static-access")
					String uncompressed=Env.B64.encode(data);

					System.out.println("UNCOMPRESSED "+uncompressed.length());
					String compressed= LZString.compressToUTF16(uncompressed);
					System.out.println("COMPRESSED "+compressed.length());
					compressed=fileToShare.getName()+";"+compressed;
					System.out.println("Compressed... "+compressed);
					//upload
					if(compressed.length() > Env.BUFFER_SIZE -100 )
						JOptionPane.showMessageDialog(null, "Error. File exceeds buffer size.");
					else
						Env.client.sendFileUpMsg(dstNickname, compressed);
				}
				catch(Exception ex)
				{
	 				System.out.println("Error @ Conversation.AttachFile >> "+ex.getMessage());
					JOptionPane.showMessageDialog(null,"Error @ Conversation.AttachFile >> "+ex.getMessage());
		    		Console.log("Error @ Conversation.AttachFile >> "+ex.getMessage());
				}
				
				room.MessagePane.setVisible(true);
				room.AttachPane.setVisible(false);
			}
		});
		
		//AttachCancel
		room.AttachCancelButton.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				room.MessagePane.setVisible(true);
				room.AttachPane.setVisible(false);
			}
		});
		
		room.ChatHistory.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				String selected= room.ChatHistory.getSelectionModel().getSelectedItem();
				if(selected.startsWith("\n[FILE :"))
				{
					String filename= selected.substring(selected.lastIndexOf('\n')+1);
					System.out.println("DOWNLOAD "+filename);
					Env.client.sendFileDownMsg(filename);
				}
			}
		});
		
		//MENU
		//ClearHistory
		room.OptionClearHistory.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				room.ChatHistoryContent.clear();
			}
		});
		
		//Block
		room.OptionBlock.setOnAction(new EventHandler<ActionEvent>() {
			@SuppressWarnings("deprecation")
			@Override
			public void handle(ActionEvent arg0) {
				if(enabled)
				{
					room.OptionBlock.setText("UnBlock");
					room.MessagePane.setDisable(true);
					room.AttachPane.setDisable(true);
					room.ChatHistory.setDisable(true);
					enabled=false;
					listener.suspend();
				}
				else
				{
					room.OptionBlock.setText("Block");
					room.MessagePane.setDisable(false);
					room.AttachPane.setDisable(false);
					room.ChatHistory.setDisable(false);
					enabled=true;
					listener.resume();
				}
			}
		});
		
	}
	
	Thread conversationListener()
	{
		return new Thread(new Runnable()
		{
		     public void run() 
		     {
		    	int count=6;
		    	
		 		while(true) try
		 		{
		 			while(!msgStack.isEmpty())
		 			{
		 				hello_msg_timestamp= Env.getTime();
		 				room.ChatHistoryContent.add(msgStack.remove(0));
		 			}
		 			
		 			// if is not broadcast
		 			if(!dstNickname.contentEquals(Env.BROADCAST_NAME))
		 			{
		 				// send hello every minute
			 			if(count<=0)
			 			{
			 				Env.client.sendHelloMsg(dstNickname);
			 				count=60;
			 			}
			 			
			 			// update labels
			 			long timeout= Env.getTime()-hello_msg_timestamp;
			 			if(timeout<Env.ONLINE_TIMEOUT)
			 				room.StatusLabel1.setText("Online");
			 			else
			 				room.StatusLabel1.setText("Online "+(timeout/6000)+" minutes ago");
		 			}
		 			
		 			count --;
		 			Thread.sleep(1000);
		 		}
		 		catch(Exception ex)
		 		{}
		     }
		});
	}
}
