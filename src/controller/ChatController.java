package controller;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import main.FXMain;
import model_impl.Session;
import chatroom.ChatRoom;
import chatroom.Conversation;
import chatroom.Room;
import environment.Console;
import environment.Env;
import environment.Routine;
import DistributedUDPChat.DistributedUDPChatClient;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
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
	
	ArrayList<Room> rooms;
	
	@FXML private Label 		NicknameLabel;
	@FXML private TextField		NicknameTextField;
	@FXML private Button 		NicknameButton;

	@FXML private TabPane		ChatTabPane;
	@FXML private Tab			ChatRoomTab;
	@FXML private AnchorPane	ChatRoomAnchorPane;
	@FXML private TextArea		ConsoleTextArea;
	

	@FXML private Tab			RandomTab;
	@FXML private Button 		ClimateButton;
	@FXML private TextArea		ClimateTextArea;
	
	@FXML private ListView<String> 	ChatList;
	
	/**
	 * When nickname is entered, DistributedUDPChat object is created and chat begins
	 */
	public void NicknameEntered()
	{
		try
		{
			String nickname= NicknameTextField.getText().toUpperCase();
			
			// init static controllers
			Console.init(ConsoleTextArea);
    		ChatRoom.init(ChatList);
			
			// init chat
			Env.client = new DistributedUDPChatClient(nickname);
			FXMain.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		          public void handle(WindowEvent we) {
		        	  Routine.exit();
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
			    		RandomTab.setDisable(false);
			    		ChatTabPane.getSelectionModel().select(ChatRoomTab);
			    					    		
					} catch (Exception e) 
			    	{
						System.out.println("Error @ ChatController Initialization >> "+e.getMessage());
						Console.log("Error @ ChatController Initialization >> "+e.getMessage());
						Routine.exit();
					}
			     }
			}).start();
		}
		catch(Exception ex)
		{
			System.out.println("Error @ ChatController Initialization >> "+ex.getMessage());
			Console.log("Error @ ChatController Initialization >> "+ex.getMessage());
			Routine.exit();
		}
	}
	
	
	//////////////////////////////////////////////////////
	
	/**
	 * When a nickname is selected in the chat list<br/>
	 * corresponding conversation is focused
	 */
	@SuppressWarnings("static-access")
	public void ChatListClicket()
	{
		String conversationName= ChatList.getSelectionModel().getSelectedItem();
		if(conversationName.isEmpty())
			return;
		
		Conversation c = ChatRoom.find(conversationName);
		if(c!=null)
		{
			ChatRoomAnchorPane.getChildren().clear();
			ChatRoomAnchorPane.getChildren().add(c.room.pane);
			
			ChatRoomAnchorPane.setLeftAnchor(c.room.pane, 0.0);
			ChatRoomAnchorPane.setRightAnchor(c.room.pane, 0.0);
			ChatRoomAnchorPane.setTopAnchor(c.room.pane, 0.0);
			ChatRoomAnchorPane.setBottomAnchor(c.room.pane, 0.0);
		}
	}
	
	public void ClimateService()
	{
		ClimateTextArea.setText("climate service");
	}
	
	public void AddExternalNet()
	{
		try
		{
			System.out.println("Add ext net");
			String name= JOptionPane.showInputDialog("Target name");
			String addr= JOptionPane.showInputDialog("Target address");
			InetAddress address= Inet4Address.getByName(addr);
			int port= Integer.parseInt(JOptionPane.showInputDialog("Target port"));
			
			Session extS= new Session(-2,name,address,port,Env.getTime());
			extS.isExt=true;
			
			Env.client.sessionPool.visit(name, address, port, Env.getTime(), -2);
			Env.client.sendHelloMsg(name);
			ChatRoom.push(name);
			
		}
		catch(Exception ex)
		{
			System.out.println("Error @ ChatController.AddExternalNet >> "+ex.getMessage());
    		Console.log("Error @ ChatController.AddExternalNet >> "+ex.getMessage());
		}
	}
}
