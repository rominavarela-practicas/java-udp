package controller;

import java.net.URL;
import java.util.ArrayList;

import main.FXMain;
import chatroom.ContactList;
import chatroom.Room;
import environment.Console;
import environment.Env;
import environment.IOUtil;
import DistributedUDPChat.DistributedUDPChatClient;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.WindowEvent;

public class ChatController {
	
	DistributedUDPChatClient chat;
	ArrayList<Room> rooms;
	
	@FXML private Label 		NicknameLabel;
	@FXML private TextField		NicknameTextField;
	@FXML private Button 		NicknameButton;

	@FXML private TabPane		ChatTabPane;
	@FXML private Tab			ChatRoomTab;
	@FXML private AnchorPane	ChatRoomAnchorPane;
	@FXML private TextArea		ConsoleTextArea;
	
	@FXML private ListView<String> 	ChatList;
	
	/**
	 * When nickname is entered, DistributedUDPChat object is created and chat begins
	 * @throws Exception
	 */
	public void NicknameEntered() throws Exception
	{
		//get nickname
		String nickname= NicknameTextField.getText().toUpperCase();
		rooms = new ArrayList<Room>();
		Console.init(ConsoleTextArea);
		
		//create chat
		chat = new DistributedUDPChatClient(nickname);
		FXMain.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	          public void handle(WindowEvent we) {
	        	  System.out.println("Closing "+chat.con.socket.getPort());
	              chat.con.socket.close();
	              if(chat.server!=null)
	              {
		        	  System.out.println("Closing "+chat.server.con.socket.getPort());
	            	  chat.server.con.socket.close();
	              }
	              System.out.println("Connection closed");
	              System.exit(0);
	          }
		});
		
		//update view
		NicknameLabel.setText(nickname);
		NicknameTextField.setVisible(false);
		NicknameButton.setVisible(false);
		Console.log("WELLCOME "+nickname+"!!!");
		
		
		
		//wait for server sync and enable the rest
		new Thread(new Runnable()
		{
		     public void run() 
		     {
		    	 try
		    	 {
		    		 Console.log("Synchronizing with server...");
		    		 while(!Env.IS_SERVER_SYNC())
		    			 Thread.sleep(1000);
		    		 
		    		//enable and focus chat-room tab
		    		ChatRoomTab.setDisable(false);
		    		ChatTabPane.getSelectionModel().select(ChatRoomTab);
		    		
		    		//ChatList.setItems(chat.sessionPool.keyList);TODO
		    		ContactList.init(chat.sessionPool,ChatList);
		    		
				} catch (Exception e) {
					e.printStackTrace();
				}
		     }
		}).start();/**/
	}
	
	/**
	 * When a nickname is selected in the chat list<br/>
	 * corresponding conversation is focused
	 */
	@SuppressWarnings("static-access")
	public void ChatListClicket()
	{
		String nickname= ChatList.getSelectionModel().getSelectedItem();
		if(nickname.isEmpty())
			return;
		
		Room 		r = null;
		AnchorPane 	room = null;
		
		for(Room aux: rooms)
			if(aux.nickname.contentEquals(nickname))
			{
				r= aux;
				break;
			}
		
		if(r==null || !r.nickname.contentEquals(nickname))
			try
			{
				@SuppressWarnings("deprecation")
				URL loadUrl = IOUtil.getFile("view/room.fxml").toURL();
				FXMLLoader fxmlLoader = new FXMLLoader(loadUrl);
				room = (AnchorPane) fxmlLoader.load();
				
				r= new Room(nickname, room);
			}
			catch(Exception ex)
			{
				System.out.println("Error @ ChatRoom >> "+ex.getMessage());
				return;
			}
		else
			room= r.room;
		

		ChatRoomAnchorPane.getChildren().clear();
		ChatRoomAnchorPane.getChildren().add(room);
		
		ChatRoomAnchorPane.setLeftAnchor(room, 0.0);
		ChatRoomAnchorPane.setRightAnchor(room, 0.0);
		ChatRoomAnchorPane.setTopAnchor(room, 0.0);
		ChatRoomAnchorPane.setBottomAnchor(room, 0.0);
		
	}
}
